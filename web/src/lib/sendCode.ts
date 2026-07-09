import { Resend } from "resend";
import twilio from "twilio";
import Lob from "lob";
import { db } from "./firebaseAdmin";

const resend = new Resend(process.env.RESEND_API_KEY);
const twilioClient = twilio(process.env.TWILIO_ACCOUNT_SID, process.env.TWILIO_AUTH_TOKEN);
const lob = new Lob({ apiKey: process.env.LOB_API_KEY ?? "" });

export type DeliveryMethod = "email" | "sms" | "mail" | "pending";

export async function sendCodeByEmail(to: string, courseName: string, code: string) {
  await resend.emails.send({
    from: "Mini Mate <noreply@minimategolf.com>",
    to,
    subject: `Your Mini Mate course code for ${courseName}`,
    html: `
      <div style="font-family:sans-serif;max-width:480px;margin:0 auto;padding:32px">
        <h2 style="color:#AF52DE">Welcome to Mini Mate</h2>
        <p>Here is your course code for <strong>${courseName}</strong>:</p>
        <div style="background:#F4F4F4;border-radius:12px;padding:20px 24px;margin:24px 0;text-align:center">
          <span style="font-family:monospace;font-size:24px;font-weight:bold;letter-spacing:2px">${code}</span>
        </div>
        <p style="color:#555">To activate your listing, open the <strong>Mini Mate Manager</strong> app → tap <strong>Add a Course</strong> → enter this code.</p>
        <p style="color:#555">Need to manage your subscription? Visit <a href="${process.env.NEXT_PUBLIC_BASE_URL}/manage" style="color:#AF52DE">minimategolf.com/manage</a>.</p>
        <hr style="border:none;border-top:1px solid #eee;margin:32px 0"/>
        <p style="font-size:12px;color:#999">Mini Mate · minimategolf.com</p>
      </div>
    `,
  });
}

export async function sendCodeBySMS(phone: string, courseName: string, code: string) {
  await twilioClient.messages.create({
    from: process.env.TWILIO_PHONE_NUMBER,
    to: phone,
    body: `Mini Mate: Your course code for ${courseName} is ${code}. Open Mini Mate Manager → Add a Course → enter this code. Manage: ${process.env.NEXT_PUBLIC_BASE_URL}/manage`,
  });
}

export async function sendCodeByMail(fullAddress: string, courseName: string, code: string) {
  const [line1, ...rest] = fullAddress.split(",").map((s) => s.trim());
  const city = rest[0] ?? "";
  const stateZip = rest[1] ?? "";
  const [state, zip] = stateZip.trim().split(" ");

  await lob.letters.create({
    description: `Course code for ${courseName}`,
    to: { name: courseName, address_line1: line1, address_city: city, address_state: state ?? "", address_zip: zip ?? "", address_country: "US" },
    from: { name: "Mini Mate", address_line1: "123 Mini Mate Lane", address_city: "Your City", address_state: "NY", address_zip: "10001", address_country: "US" },
    file: `<html><body style="font-family:sans-serif;padding:40px"><h2 style="color:#AF52DE">Mini Mate Course Code</h2><p>Your course code for <strong>${courseName}</strong> is:</p><p style="font-family:monospace;font-size:28px;font-weight:bold;margin:24px 0">${code}</p><p>Open the <strong>Mini Mate Manager</strong> app, tap <strong>Add a Course</strong>, and enter this code to activate your listing.</p><p>Questions? Visit minimategolf.com</p></body></html>`,
    color: false,
    double_sided: false,
    mail_type: "usps_first_class",
  } as Parameters<typeof lob.letters.create>[0]);
}

export async function deliverCode(
  contact: { email: string | null; phoneNumber: string | null; url: string | null; address: { fullAddress: string } },
  courseName: string,
  code: string
): Promise<DeliveryMethod> {
  if (contact.email) {
    await sendCodeByEmail(contact.email, courseName, code);
    return "email";
  }
  if (contact.phoneNumber) {
    await sendCodeBySMS(contact.phoneNumber, courseName, code);
    return "sms";
  }
  if (contact.address.fullAddress) {
    await sendCodeByMail(contact.address.fullAddress, courseName, code);
    return "mail";
  }
  // Website-only or no contact — save for manual follow-up
  await db.collection("pendingOutreach").add({ courseName, contact, code, createdAt: new Date() });
  return "pending";
}

// Contact priority: email → phone → website → mail (last resort)
export type ContactMethod = "email" | "phone" | "website" | "mail";

export interface BusinessContact {
  email?: string | null;
  phone?: string | null;
  website?: string | null;
  address?: string | null;
}

export interface ContactRoute {
  method: ContactMethod;
  value: string;
  label: string;
}

export function resolveContactRoute(contact: BusinessContact): ContactRoute | null {
  if (contact.email) {
    return { method: "email", value: contact.email, label: `Email ${contact.email}` };
  }
  if (contact.phone) {
    return { method: "phone", value: contact.phone, label: `Call ${contact.phone}` };
  }
  if (contact.website) {
    return { method: "website", value: contact.website, label: "Contact via website" };
  }
  if (contact.address) {
    return { method: "mail", value: contact.address, label: `Send mail to ${contact.address}` };
  }
  return null;
}

export function getAllContactRoutes(contact: BusinessContact): ContactRoute[] {
  const routes: ContactRoute[] = [];
  if (contact.email) routes.push({ method: "email", value: contact.email, label: contact.email });
  if (contact.phone) routes.push({ method: "phone", value: contact.phone, label: contact.phone });
  if (contact.website) routes.push({ method: "website", value: contact.website, label: contact.website });
  if (contact.address) routes.push({ method: "mail", value: contact.address, label: contact.address });
  return routes;
}

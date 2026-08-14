"use client";
import { useRouter } from "next/navigation";
import { useAuth } from "@/contexts/AuthContext";

export default function GetStartedButton({
  label = "Get Started",
  className,
  style,
}: {
  label?: string;
  className?: string;
  style?: React.CSSProperties;
}) {
  const { user, firestoreUser, loading } = useAuth();
  const router = useRouter();

  function handleClick() {
    if (loading) return;
    if (!user) {
      // Get Started always leads into the course flow after sign-in
      // (find-course for a first course, manage-courses if they already have
      // courses) — NOT back to the page they were on.
      router.push("/signin?next=course");
      return;
    }
    if ((firestoreUser?.adminCourses?.length ?? 0) > 0) {
      router.push("/manage-courses");
    } else {
      router.push("/find-course");
    }
  }

  return (
    <button onClick={handleClick} disabled={loading} className={className} style={style}>
      {label}
    </button>
  );
}

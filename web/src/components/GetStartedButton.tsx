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
    if (!user) { router.push("/signin"); return; }
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

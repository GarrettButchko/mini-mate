"use client";
import { useEffect, useState } from "react";
import { doc, onSnapshot } from "firebase/firestore";
import { db } from "@/lib/firebaseClient";
import type { CourseDoc } from "@/lib/course";

export function useAdminCourses(courseIDs: string[]): { courses: CourseDoc[]; loading: boolean } {
  const [courses, setCourses] = useState<CourseDoc[]>([]);
  const [loading, setLoading] = useState(false);

  const key = [...courseIDs].sort().join(",");

  useEffect(() => {
    if (!courseIDs.length) {
      setCourses([]);
      setLoading(false);
      return;
    }

    setLoading(true);
    const resolved = new Set<string>();

    const unsubs = courseIDs.map((id) =>
      onSnapshot(doc(db, "courses", id), (snap) => {
        resolved.add(id);
        if (resolved.size === courseIDs.length) setLoading(false);

        setCourses((prev) => {
          const rest = prev.filter((c) => c.id !== id);
          if (!snap.exists()) return rest;
          return [...rest, { id, ...snap.data() } as CourseDoc];
        });
      })
    );

    return () => unsubs.forEach((u) => u());
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key]);

  return { courses, loading };
}

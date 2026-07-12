"use client";
import { createContext, useContext, useEffect, useRef, useState } from "react";
import {
  onAuthStateChanged,
  signInWithPopup,
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  GoogleAuthProvider,
  OAuthProvider,
  updateProfile,
  updateCurrentUser,
  sendPasswordResetEmail,
  deleteUser,
  signOut as firebaseSignOut,
  type User,
} from "firebase/auth";
import {
  doc,
  onSnapshot,
  setDoc,
  updateDoc,
  arrayUnion,
  serverTimestamp,
} from "firebase/firestore";
import { auth, db } from "@/lib/firebaseClient";

export interface FirestoreUser {
  googleId: string;
  appleId: string | null;
  name: string | null;
  email: string | null;
  photoURL: string | null;
  isPro: boolean;
  gameIDs: string[];
  adminCourses: string[];
  accountType: string[];
  ballColorDT: string | null;
}

interface AuthContextValue {
  user: User | null;
  firestoreUser: FirestoreUser | null;
  loading: boolean;
  signInWithGoogle: () => Promise<void>;
  signInWithApple: () => Promise<void>;
  signInWithEmail: (email: string, password: string) => Promise<void>;
  createAccountWithEmail: (email: string, password: string) => Promise<void>;
  updateName: (name: string) => Promise<void>;
  sendPasswordReset: () => Promise<void>;
  deleteAccount: () => Promise<void>;
  signOut: () => Promise<void>;
}

function providerIdToAccountType(providerId: string | undefined): string | null {
  if (providerId === "google.com") return "google";
  if (providerId === "apple.com") return "apple";
  if (providerId === "password") return "email";
  return null;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [firestoreUser, setFirestoreUser] = useState<FirestoreUser | null>(null);
  const [loading, setLoading] = useState(true);
  const firestoreUnsubRef = useRef<(() => void) | null>(null);

  useEffect(() => {
    const unsub = onAuthStateChanged(auth, (u) => {
      setUser(u);
      setLoading(false);

      // Tear down previous Firestore listener
      firestoreUnsubRef.current?.();
      firestoreUnsubRef.current = null;

      if (!u) {
        setFirestoreUser(null);
        return;
      }

      const userRef = doc(db, "users", u.uid);

      firestoreUnsubRef.current = onSnapshot(userRef, (snap) => {
        if (snap.exists()) {
          const data = snap.data() as FirestoreUser;
          setFirestoreUser(data);

          // Sync accountType if user signed in with a new provider
          const providerType = providerIdToAccountType(u.providerData[0]?.providerId);
          if (providerType && !data.accountType?.includes(providerType)) {
            updateDoc(userRef, { accountType: arrayUnion(providerType) });
          }

          // Sync photoURL from auth if Firestore doc is missing it
          if (u.photoURL && !data.photoURL) {
            updateDoc(userRef, { photoURL: u.photoURL });
          }

          // Sync email from auth if Firestore doc is missing it
          const authEmail = u.email ?? u.providerData[0]?.email ?? null;
          if (authEmail && !data.email) {
            updateDoc(userRef, { email: authEmail });
          }
        } else {
          // Backfill: user exists in Auth but has no Firestore doc
          const providerType = providerIdToAccountType(u.providerData[0]?.providerId);
          setDoc(userRef, {
            googleId: u.uid,
            appleId: null,
            name: u.displayName ?? `User#${u.uid.slice(0, 5)}`,
            email: u.email ?? u.providerData[0]?.email ?? null,
            photoURL: u.photoURL ?? null,
            isPro: false,
            gameIDs: [],
            adminCourses: [],
            accountType: providerType ? [providerType] : [],
            ballColorDT: null,
            lastUpdated: serverTimestamp(),
          });
        }
      });
    });

    return () => {
      unsub();
      firestoreUnsubRef.current?.();
    };
  }, []);

  async function signInWithGoogle() {
    const provider = new GoogleAuthProvider();
    provider.addScope("email");
    provider.addScope("profile");
    await signInWithPopup(auth, provider);
    if (auth.currentUser) {
      await auth.currentUser.reload();
      await updateCurrentUser(auth, auth.currentUser);
    }
  }

  async function signInWithApple() {
    const provider = new OAuthProvider("apple.com");
    provider.addScope("email");
    provider.addScope("name");
    await signInWithPopup(auth, provider);
    if (auth.currentUser) {
      await auth.currentUser.reload();
      await updateCurrentUser(auth, auth.currentUser);
    }
  }

  async function signInWithEmail(email: string, password: string) {
    await signInWithEmailAndPassword(auth, email, password);
  }

  async function createAccountWithEmail(email: string, password: string) {
    await createUserWithEmailAndPassword(auth, email, password);
  }

  async function updateName(name: string) {
    if (!auth.currentUser) return;
    await updateProfile(auth.currentUser, { displayName: name });
    // updateCurrentUser fires onAuthStateChanged with a fresh User object —
    // spreading auth.currentUser would lose prototype getters like email/displayName
    await updateCurrentUser(auth, auth.currentUser);
    // Also keep Firestore doc in sync
    await updateDoc(doc(db, "users", auth.currentUser.uid), { name });
  }

  async function sendPasswordReset() {
    if (!auth.currentUser?.email) return;
    await sendPasswordResetEmail(auth, auth.currentUser.email);
  }

  async function deleteAccount() {
    if (!auth.currentUser) return;
    await deleteUser(auth.currentUser);
  }

  async function signOut() {
    await firebaseSignOut(auth);
  }

  return (
    <AuthContext.Provider value={{ user, firestoreUser, loading, signInWithGoogle, signInWithApple, signInWithEmail, createAccountWithEmail, updateName, sendPasswordReset, deleteAccount, signOut }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
}

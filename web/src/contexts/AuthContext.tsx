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
  sendEmailVerification as firebaseSendEmailVerification,
  deleteUser,
  reauthenticateWithCredential,
  reauthenticateWithPopup,
  EmailAuthProvider,
  signOut as firebaseSignOut,
  type User,
} from "firebase/auth";
import {
  doc,
  onSnapshot,
  setDoc,
  updateDoc,
  deleteDoc,
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
  sendVerificationEmail: () => Promise<void>;
  refreshEmailVerified: () => Promise<boolean>;
  signOutQuietly: () => Promise<void>;
  updateName: (name: string) => Promise<void>;
  sendPasswordReset: () => Promise<void>;
  reauthenticateWithEmail: (email: string, password: string) => Promise<void>;
  reauthenticateWithGoogle: () => Promise<void>;
  reauthenticateWithApple: () => Promise<void>;
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
      // Tracks whether this listener has ever seen an existing Firestore doc for
      // the user. Used to detect a mid-session deletion (doc existed, then gone).
      let hasSeenExistingDoc = false;

      firestoreUnsubRef.current = onSnapshot(userRef, (snap) => {
        if (snap.exists()) {
          hasSeenExistingDoc = true;
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
          // The Firestore doc for this user no longer exists. This means one of:
          //   1. A brand-new account that just signed in and whose doc hasn't been
          //      created yet → backfill it (below).
          //   2. An existing account whose doc was deleted — normally because the
          //      account was deleted on another device. Recreating the doc here
          //      would resurrect a deleted account's Firestore data, so instead we
          //      sign out locally so this web session follows the account deletion.
          const accountAgeMs = Date.now() - new Date(u.metadata.creationTime ?? 0).getTime();
          const isFreshAccount = accountAgeMs >= 0 && accountAgeMs < 60_000;

          if (hasSeenExistingDoc || !isFreshAccount) {
            // Doc disappeared mid-session, or this account predates the doc (i.e. it
            // was deleted on another device). Tear down the listener and sign out.
            firestoreUnsubRef.current?.();
            firestoreUnsubRef.current = null;
            void firebaseSignOut(auth);
            return;
          }

          // Brand-new account → backfill the user doc.
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
  }

  async function signInWithApple() {
    const provider = new OAuthProvider("apple.com");
    provider.addScope("email");
    provider.addScope("name");
    await signInWithPopup(auth, provider);
  }

  async function signInWithEmail(email: string, password: string) {
    await signInWithEmailAndPassword(auth, email, password);
  }

  async function createAccountWithEmail(email: string, password: string) {
    await createUserWithEmailAndPassword(auth, email, password);
  }

  async function sendVerificationEmail() {
    const u = auth.currentUser;
    if (!u) throw new Error("Not signed in");
    await firebaseSendEmailVerification(u);
  }

  async function refreshEmailVerified(): Promise<boolean> {
    const u = auth.currentUser;
    if (!u) return false;
    try {
      await u.reload();
      return u.emailVerified;
    } catch {
      return u.emailVerified;
    }
  }

  async function signOutQuietly() {
    await firebaseSignOut(auth);
  }

  async function updateName(name: string) {
    if (!auth.currentUser) return;
    await updateProfile(auth.currentUser, { displayName: name });
    // updateCurrentUser replaces auth.currentUser with a fresh clone. Note it does
    // NOT fire onAuthStateChanged for the same uid (only onIdTokenChanged), so the
    // listener above never refreshes React state — the name only seemed to update
    // the first time because the in-place mutation was visible via the stale ref.
    // Push the fresh clone into state explicitly so the UI always reflects it.
    await updateCurrentUser(auth, auth.currentUser);
    await updateDoc(doc(db, "users", auth.currentUser.uid), { name });
    setUser(auth.currentUser);
  }

  async function sendPasswordReset() {
    if (!auth.currentUser?.email) return;
    await sendPasswordResetEmail(auth, auth.currentUser.email);
  }

  async function reauthenticateWithEmail(email: string, password: string) {
    const u = auth.currentUser;
    if (!u) throw new Error("Not signed in");
    await reauthenticateWithCredential(u, EmailAuthProvider.credential(email, password));
  }

  async function reauthenticateWithGoogle() {
    const u = auth.currentUser;
    if (!u) throw new Error("Not signed in");
    await reauthenticateWithPopup(u, new GoogleAuthProvider());
  }

  async function reauthenticateWithApple() {
    const u = auth.currentUser;
    if (!u) throw new Error("Not signed in");
    await reauthenticateWithPopup(u, new OAuthProvider("apple.com"));
  }

  async function deleteAccount() {
    if (!auth.currentUser) return;
    const uid = auth.currentUser.uid;
    await deleteUser(auth.currentUser);
    await deleteDoc(doc(db, "users", uid));
  }

  async function signOut() {
    await firebaseSignOut(auth);
    window.location.href = "/";
  }

  return (
    <AuthContext.Provider value={{ user, firestoreUser, loading, signInWithGoogle, signInWithApple, signInWithEmail, createAccountWithEmail, sendVerificationEmail, refreshEmailVerified, signOutQuietly, updateName, sendPasswordReset, reauthenticateWithEmail, reauthenticateWithGoogle, reauthenticateWithApple, deleteAccount, signOut }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
}

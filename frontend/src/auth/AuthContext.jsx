import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { apiClient } from "../api/client";

const AUTH_STORAGE_KEY = "marketplace-auth";

const AuthContext = createContext(null);

function readStoredSession() {
  const raw = window.localStorage.getItem(AUTH_STORAGE_KEY);

  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw);
  } catch {
    window.localStorage.removeItem(AUTH_STORAGE_KEY);
    return null;
  }
}

export function AuthProvider({ children }) {
  const [session, setSession] = useState(() => readStoredSession());
  const [status, setStatus] = useState("idle");

  useEffect(() => {
    if (session) {
      window.localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session));
      return;
    }

    window.localStorage.removeItem(AUTH_STORAGE_KEY);
  }, [session]);

  async function login(payload) {
    setStatus("loading");
    try {
      const response = await apiClient.login(payload);
      setSession({
        accessToken: response.accessToken,
        refreshToken: response.refreshToken,
        user: {
          id: response.userId,
          email: response.email,
          role: response.role,
          status: response.status,
          sellerApprovalStatus: response.sellerApprovalStatus,
          redirectTo: response.redirectTo
        }
      });
      setStatus("authenticated");
      return response;
    } catch (error) {
      setStatus("error");
      throw error;
    }
  }

  async function registerBuyer(payload) {
    setStatus("loading");
    try {
      const response = await apiClient.registerBuyer(payload);
      setStatus("idle");
      return response;
    } catch (error) {
      setStatus("error");
      throw error;
    }
  }

  async function registerSeller(payload) {
    setStatus("loading");
    try {
      const response = await apiClient.registerSeller(payload);
      setStatus("idle");
      return response;
    } catch (error) {
      setStatus("error");
      throw error;
    }
  }

  async function hydrateUser() {
    if (!session?.accessToken) {
      return null;
    }

    try {
      const currentUser = await apiClient.me(session.accessToken);
      setSession((previous) => ({
        ...previous,
        user: {
          ...previous.user,
          ...currentUser
        }
      }));
      setStatus("authenticated");
      return currentUser;
    } catch (error) {
      setStatus("error");
      throw error;
    }
  }

  async function logout() {
    const refreshToken = session?.refreshToken;

    try {
      if (refreshToken) {
        await apiClient.logout(refreshToken);
      }
    } finally {
      setSession(null);
      setStatus("idle");
    }
  }

  const value = useMemo(() => ({
    session,
    user: session?.user ?? null,
    accessToken: session?.accessToken ?? null,
    refreshToken: session?.refreshToken ?? null,
    status,
    isAuthenticated: Boolean(session?.accessToken),
    login,
    registerBuyer,
    registerSeller,
    hydrateUser,
    logout
  }), [session, status]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }

  return context;
}

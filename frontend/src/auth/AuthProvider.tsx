import { createContext, useContext, useMemo, useState } from 'react';
import type { PropsWithChildren } from 'react';
import type { AuthResponse, AuthUser } from '../api/types';
import { clearAuthSnapshot, readAuthSnapshot, writeAuthSnapshot } from './authStore';
import { useQueryClient } from '@tanstack/react-query';

type AuthContextValue = {
  accessToken: string | null;
  user: AuthUser | null;
  isAuthenticated: boolean;
  setSession: (response: AuthResponse) => void;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const [snapshot, setSnapshot] = useState(readAuthSnapshot);
  const queryClient = useQueryClient();

  const value = useMemo<AuthContextValue>(
    () => ({
      accessToken: snapshot.accessToken,
      user: snapshot.user,
      isAuthenticated: Boolean(snapshot.accessToken),
      setSession: (response) => {
        const next = { accessToken: response.accessToken, user: response.user };
        writeAuthSnapshot(next);
        setSnapshot(next);
      },
      logout: () => {
        clearAuthSnapshot();
        setSnapshot({ accessToken: null, user: null });
        queryClient.clear();
      },
    }),
    [snapshot, queryClient],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }

  return context;
}

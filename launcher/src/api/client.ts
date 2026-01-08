import { useAuthStore } from '../store/authStore';

const API_URL = 'http://api.hegemonia.net/api';

// Simple fetch wrapper
export const api = {
  async get(endpoint: string) {
    const token = useAuthStore.getState().token;
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_URL}${endpoint}`, {
      method: 'GET',
      headers,
    });

    if (!response.ok) {
      if (response.status === 401) {
        useAuthStore.getState().logout();
      }
      const error = await response.json().catch(() => ({ error: 'Erreur de connexion' }));
      throw new Error(error.error || 'Erreur serveur');
    }

    return { data: await response.json() };
  },

  async post(endpoint: string, data?: any) {
    const token = useAuthStore.getState().token;
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_URL}${endpoint}`, {
      method: 'POST',
      headers,
      body: data ? JSON.stringify(data) : undefined,
    });

    if (!response.ok) {
      if (response.status === 401) {
        useAuthStore.getState().logout();
      }
      const error = await response.json().catch(() => ({ error: 'Erreur de connexion' }));
      throw new Error(error.error || 'Erreur serveur');
    }

    return { data: await response.json() };
  },
};

export default api;

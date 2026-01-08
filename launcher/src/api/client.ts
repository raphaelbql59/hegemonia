import { fetch } from '@tauri-apps/api/http';
import { useAuthStore } from '../store/authStore';

const API_URL = 'http://api.hegemonia.net/api';

// Tauri HTTP API wrapper
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
      const error = response.data as any;
      throw new Error(error?.error || 'Erreur serveur');
    }

    return { data: response.data };
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
      body: data ? { type: 'Json', payload: data } : undefined,
    });

    if (!response.ok) {
      if (response.status === 401) {
        useAuthStore.getState().logout();
      }
      const error = response.data as any;
      throw new Error(error?.error || 'Erreur serveur');
    }

    return { data: response.data };
  },
};

export default api;

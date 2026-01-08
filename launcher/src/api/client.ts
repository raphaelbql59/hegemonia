import { fetch, ResponseType } from '@tauri-apps/api/http';
import { useAuthStore } from '../store/authStore';

const API_URL = 'http://api.hegemonia.net/api';

// Tauri HTTP API wrapper
export const api = {
  async get<T = any>(endpoint: string): Promise<{ data: T }> {
    const token = useAuthStore.getState().token;
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch<T>(`${API_URL}${endpoint}`, {
      method: 'GET',
      headers,
      responseType: ResponseType.JSON,
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

  async post<T = any>(endpoint: string, data?: any): Promise<{ data: T }> {
    const token = useAuthStore.getState().token;
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch<T>(`${API_URL}${endpoint}`, {
      method: 'POST',
      headers,
      responseType: ResponseType.JSON,
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

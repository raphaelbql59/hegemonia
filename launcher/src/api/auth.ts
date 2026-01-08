import api from './client';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: {
    id: number;
    uuid: string;
    username: string;
    email: string;
    role: string;
    minecraftUuid?: string;
  };
}

export const authAPI = {
  login: async (data: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/auth/login', data);
    return response.data;
  },

  logout: async (): Promise<void> => {
    await api.post('/auth/logout');
  },

  me: async () => {
    const response = await api.get('/auth/me');
    return response.data;
  },
};

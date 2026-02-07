import api from './axios';

// Interfaces match Backend DTOs
export interface Todo {
    id: number;
    text: string;
    completed: boolean;
    displayOrder: number;
    createdAt: string;
}

export type TodoStats = {
    total: number;
    active: number;
    completed: number;
};

export interface AuthResponse {
    grantType: string;
    accessToken: string;
    accessTokenExpiresIn: number;
    nickname: string;
    email: string;
    role: string;
}

export interface SignupRequest {
    email: string;
    password: string;
    nickname: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

// Auth API
export const login = async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/auth/login', data);
    return response.data;
};

export const signup = async (data: SignupRequest): Promise<void> => {
    await api.post('/auth/signup', data);
};

// Todo API
export const fetchTodos = async (filter: string = 'all'): Promise<Todo[]> => {
    const response = await api.get<Todo[]>('/todos', { params: { filter } });
    return response.data;
};

export const createTodo = async (text: string): Promise<Todo> => {
    const response = await api.post<Todo>('/todos', { text });
    return response.data;
};

export const updateTodo = async (id: number, updates: Partial<Todo>): Promise<Todo> => {
    const response = await api.put<Todo>(`/todos/${id}`, updates);
    return response.data;
};

export const toggleTodo = async (id: number): Promise<Todo> => {
    const response = await api.patch<Todo>(`/todos/${id}/toggle`);
    return response.data;
};

export const deleteTodo = async (id: number): Promise<void> => {
    await api.delete(`/todos/${id}`);
};

export const clearCompletedTodos = async (): Promise<void> => {
    await api.delete('/todos/completed');
};

export const fetchStats = async (): Promise<TodoStats> => {
    const response = await api.get<TodoStats>('/todos/stats');
    return response.data;
};

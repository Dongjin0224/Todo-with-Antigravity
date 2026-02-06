const API_BASE_URL = 'http://localhost:8080/api/todos';

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

export async function fetchTodos(filter: string = 'all'): Promise<Todo[]> {
    const res = await fetch(`${API_BASE_URL}?filter=${filter}`, { cache: 'no-store' });
    if (!res.ok) throw new Error('Failed to fetch todos');
    return res.json();
}

export async function createTodo(text: string): Promise<Todo> {
    const res = await fetch(API_BASE_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text }),
    });
    if (!res.ok) throw new Error('Failed to create todo');
    return res.json();
}

export async function updateTodo(id: number, updates: Partial<Todo>): Promise<Todo> {
    const res = await fetch(`${API_BASE_URL}/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updates),
    });
    if (!res.ok) throw new Error('Failed to update todo');
    return res.json();
}

export async function toggleTodo(id: number): Promise<Todo> {
    const res = await fetch(`${API_BASE_URL}/${id}/toggle`, {
        method: 'PATCH',
    });
    if (!res.ok) throw new Error('Failed to toggle todo');
    return res.json();
}

export async function deleteTodo(id: number): Promise<void> {
    const res = await fetch(`${API_BASE_URL}/${id}`, {
        method: 'DELETE',
    });
    if (!res.ok) throw new Error('Failed to delete todo');
}

export async function clearCompletedTodos(): Promise<void> {
    const res = await fetch(`${API_BASE_URL}/completed`, {
        method: 'DELETE',
    });
    if (!res.ok) throw new Error('Failed to clear completed todos');
}

export async function fetchStats(): Promise<TodoStats> {
    const res = await fetch(`${API_BASE_URL}/stats`, { cache: 'no-store' });
    if (!res.ok) throw new Error('Failed to fetch stats');
    return res.json();
}

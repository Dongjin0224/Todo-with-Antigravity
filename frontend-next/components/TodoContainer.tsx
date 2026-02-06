"use client";

import { useEffect, useState } from "react";
import { Todo, TodoStats, fetchTodos, createTodo, toggleTodo, deleteTodo, updateTodo, clearCompletedTodos, fetchStats } from "@/lib/api";
import Header from "./Header";
import AddTodo from "./AddTodo";
import FilterTabs from "./FilterTabs";
import TodoList from "./TodoList";

export default function TodoContainer() {
    const [todos, setTodos] = useState<Todo[]>([]);
    const [filter, setFilter] = useState("all");
    const [stats, setStats] = useState<TodoStats>({ total: 0, active: 0, completed: 0 });
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const loadData = async () => {
        try {
            // setIsLoading(true); // Initial load only, handled by parent or suspense usually, but here simple
            const [fetchedTodos, fetchedStats] = await Promise.all([
                fetchTodos(filter),
                fetchStats()
            ]);
            setTodos(fetchedTodos);
            setStats(fetchedStats);
        } catch (err) {
            setError("데이터를 불러오는데 실패했습니다.");
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        loadData();
    }, [filter]);

    const handleAdd = async (text: string) => {
        setIsLoading(true);
        try {
            await createTodo(text);
            await loadData();
        } catch {
            setError("추가 실패");
        } finally {
            setIsLoading(false);
        }
    };

    const handleToggle = async (id: number) => {
        // Optimistic Update
        setTodos(prev => prev.map(t => t.id === id ? { ...t, completed: !t.completed } : t));
        try {
            await toggleTodo(id);
            await loadData(); // Re-sync stats
        } catch {
            setError("상태 변경 실패");
            loadData(); // Revert
        }
    };

    const handleDelete = async (id: number) => {
        setTodos(prev => prev.filter(t => t.id !== id));
        try {
            await deleteTodo(id);
            await loadData();
        } catch {
            setError("삭제 실패");
            loadData();
        }
    };

    const handleUpdate = async (id: number, text: string) => {
        try {
            await updateTodo(id, { text });
            await loadData();
        } catch {
            setError("수정 실패");
        }
    };

    const handleClearCompleted = async () => {
        if (!confirm("완료된 항목을 모두 삭제하시겠습니까?")) return;
        setIsLoading(true);
        try {
            await clearCompletedTodos();
            await loadData();
        } catch {
            setError("일괄 삭제 실패");
        } finally {
            setIsLoading(false);
        }
    };

    const handleReorder = async (reorderedTodos: Todo[]) => {
        setTodos(reorderedTodos); // Optimistic

        // Sync changes to server
        try {
            // Find items that changed position and update them
            // For simplicity/robustness, we can update ones where displayOrder differs
            const updates = reorderedTodos
                .filter((t, index) => t.displayOrder !== index)
                .map((t, index) => updateTodo(t.id, { displayOrder: index }));

            await Promise.all(updates);
            // No need to reload data if successful, as local state is correct
        } catch {
            setError("순서 저장 실패");
            loadData(); // Revert
        }
    };

    return (
        <div className="w-full max-w-lg mx-auto">
            {/* Background Orbs */}
            <div className="fixed inset-0 overflow-hidden pointer-events-none -z-10">
                <div className="absolute top-0 right-0 w-[500px] h-[500px] bg-indigo-500/30 rounded-full blur-[100px] animate-pulse" style={{ animationDuration: '4s' }} />
                <div className="absolute bottom-0 left-0 w-[400px] h-[400px] bg-pink-500/20 rounded-full blur-[100px] animate-pulse" style={{ animationDuration: '6s' }} />
            </div>

            <div className="bg-white/90 dark:bg-[#1e1e3c]/90 backdrop-blur-xl rounded-3xl shadow-2xl border border-white/20 overflow-hidden">
                <Header />

                <AddTodo onAdd={handleAdd} isLoading={isLoading} />

                <FilterTabs filter={filter} setFilter={setFilter} counts={stats} />

                <TodoList
                    todos={todos}
                    onToggle={handleToggle}
                    onDelete={handleDelete}
                    onUpdate={handleUpdate}
                    onReorder={handleReorder}
                />

                <div className="flex justify-between items-center px-8 py-4 bg-black/5 dark:bg-black/20 border-t border-white/10 text-xs text-gray-500 dark:text-gray-400">
                    <span>{stats.active}개 남음</span>
                    <button
                        onClick={handleClearCompleted}
                        className="hover:text-red-500 transition-colors"
                    >
                        완료된 항목 삭제
                    </button>
                </div>
            </div>

            {error && (
                <div className="fixed bottom-4 right-4 bg-red-500 text-white px-4 py-3 rounded-lg shadow-lg animate-in slide-in-from-bottom-5 fade-in duration-300 flex items-center gap-2">
                    <span>⚠️</span> {error}
                    <button onClick={() => setError(null)} className="ml-2 opacity-80 hover:opacity-100">✕</button>
                </div>
            )}

            <p className="text-center mt-6 text-sm text-gray-500 dark:text-gray-400">
                💡 더블클릭으로 수정 | 드래그로 순서 변경
            </p>
        </div>
    );
}

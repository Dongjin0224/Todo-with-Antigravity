import { Plus } from "lucide-react";
import { useState } from "react";

interface AddTodoProps {
    onAdd: (text: string) => Promise<void>;
    isLoading: boolean;
}

export default function AddTodo({ onAdd, isLoading }: AddTodoProps) {
    const [text, setText] = useState("");
    const [isShaking, setIsShaking] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!text.trim()) {
            setIsShaking(true);
            setTimeout(() => setIsShaking(false), 500);
            return;
        }
        await onAdd(text);
        setText("");
    };

    return (
        <form onSubmit={handleSubmit} className="p-6 pb-4">
            <div
                className={`flex gap-3 p-1.5 rounded-xl border transition-all duration-300 focus-within:ring-2 focus-within:ring-indigo-500/50 bg-white dark:bg-white/5 border-gray-200 dark:border-white/10 ${isShaking ? "animate-shake ring-2 ring-red-500/50 border-red-500" : "focus-within:border-indigo-500"
                    }`}
            >
                <input
                    type="text"
                    value={text}
                    onChange={(e) => setText(e.target.value)}
                    placeholder="새로운 할 일을 입력하세요..."
                    className="flex-1 bg-transparent px-4 py-3 text-[15px] outline-none placeholder:text-gray-400 dark:text-white"
                    disabled={isLoading}
                />
                <button
                    type="submit"
                    disabled={isLoading}
                    className="flex items-center gap-1.5 bg-gradient-to-br from-indigo-500 to-indigo-600 text-white px-5 py-3 rounded-lg font-semibold text-sm hover:-translate-y-0.5 hover:shadow-lg hover:shadow-indigo-500/30 active:translate-y-0 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
                >
                    <Plus className="w-4.5 h-4.5" />
                    <span className="hidden sm:inline">추가</span>
                </button>
            </div>
        </form>
    );
}

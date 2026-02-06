import { Todo } from "@/lib/api";
import { cn } from "@/lib/utils";
import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { Check, Trash2, GripVertical } from "lucide-react";
import { useState } from "react";

interface TodoItemProps {
    todo: Todo;
    onToggle: (id: number) => void;
    onDelete: (id: number) => void;
    onUpdate: (id: number, text: string) => void;
}

export default function TodoItem({ todo, onToggle, onDelete, onUpdate }: TodoItemProps) {
    const [isEditing, setIsEditing] = useState(false);
    const [editText, setEditText] = useState(todo.text);

    const {
        attributes,
        listeners,
        setNodeRef,
        transform,
        transition,
        isDragging,
    } = useSortable({ id: todo.id });

    const style = {
        transform: CSS.Transform.toString(transform),
        transition,
    };

    const handleUpdate = () => {
        if (editText.trim() !== todo.text) {
            onUpdate(todo.id, editText.trim());
        }
        setIsEditing(false);
    };

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === "Enter") handleUpdate();
        if (e.key === "Escape") {
            setEditText(todo.text);
            setIsEditing(false);
        }
    };

    return (
        <div
            ref={setNodeRef}
            style={style}
            className={cn(
                "group flex items-center gap-3 p-4 border-b border-gray-100 dark:border-white/5 bg-white dark:bg-transparent transition-all hover:bg-gray-50 dark:hover:bg-white/5",
                isDragging && "opacity-50 bg-gray-100 dark:bg-white/10 z-50 relative shadow-xl rounded-lg border-none",
                todo.completed && "opacity-75"
            )}
        >
            {/* Drag Handle */}
            <div
                {...attributes}
                {...listeners}
                className="cursor-grab active:cursor-grabbing text-gray-300 dark:text-gray-600 hover:text-gray-500 dark:hover:text-gray-400"
            >
                <GripVertical className="w-4 h-4" />
            </div>

            {/* Checkbox */}
            <button
                onClick={() => onToggle(todo.id)}
                className={cn(
                    "flex-shrink-0 w-6 h-6 rounded-full border-2 flex items-center justify-center transition-all",
                    todo.completed
                        ? "bg-emerald-500 border-emerald-500"
                        : "border-gray-300 dark:border-gray-600 hover:border-indigo-500"
                )}
            >
                <Check className={cn("w-3.5 h-3.5 text-white transition-transform", todo.completed ? "scale-100" : "scale-0")} />
            </button>

            {/* Text */}
            <div className="flex-1 min-w-0">
                {isEditing ? (
                    <input
                        type="text"
                        value={editText}
                        onChange={(e) => setEditText(e.target.value)}
                        onBlur={handleUpdate}
                        onKeyDown={handleKeyDown}
                        autoFocus
                        className="w-full bg-transparent outline-none border-b border-indigo-500 pb-0.5 text-[15px] text-gray-900 dark:text-white"
                    />
                ) : (
                    <span
                        onDoubleClick={() => setIsEditing(true)}
                        className={cn(
                            "block text-[15px] truncate cursor-text select-none",
                            todo.completed ? "text-gray-400 line-through decoration-gray-400" : "text-gray-700 dark:text-gray-200"
                        )}
                    >
                        {todo.text}
                    </span>
                )}
            </div>

            {/* Delete Button */}
            <button
                onClick={() => onDelete(todo.id)}
                className="text-gray-400 hover:text-red-500 hover:bg-red-50 dark:hover:bg-red-500/10 p-1.5 rounded-md opacity-0 group-hover:opacity-100 transition-all"
                aria-label="Delete"
            >
                <Trash2 className="w-4.5 h-4.5" />
            </button>
        </div>
    );
}

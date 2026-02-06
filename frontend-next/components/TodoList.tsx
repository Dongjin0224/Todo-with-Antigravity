import { Todo } from "@/lib/api";
import {
    DndContext,
    closestCenter,
    KeyboardSensor,
    PointerSensor,
    useSensor,
    useSensors,
    DragEndEvent,
} from "@dnd-kit/core";
import {
    arrayMove,
    SortableContext,
    sortableKeyboardCoordinates,
    verticalListSortingStrategy,
} from "@dnd-kit/sortable";
import TodoItem from "./TodoItem";

interface TodoListProps {
    todos: Todo[];
    onToggle: (id: number) => void;
    onDelete: (id: number) => void;
    onUpdate: (id: number, text: string) => void;
    onReorder: (todos: Todo[]) => void;
}

export default function TodoList({ todos, onToggle, onDelete, onUpdate, onReorder }: TodoListProps) {
    const sensors = useSensors(
        useSensor(PointerSensor),
        useSensor(KeyboardSensor, {
            coordinateGetter: sortableKeyboardCoordinates,
        })
    );

    const handleDragEnd = (event: DragEndEvent) => {
        const { active, over } = event;

        if (over && active.id !== over.id) {
            const oldIndex = todos.findIndex((t) => t.id === active.id);
            const newIndex = todos.findIndex((t) => t.id === over.id);

            const reordered = arrayMove(todos, oldIndex, newIndex);

            // Update displayOrder based on new index
            const updated = reordered.map((item, index) => ({
                ...item,
                displayOrder: index
            }));

            onReorder(updated);
        }
    };

    if (todos.length === 0) {
        return (
            <div className="flex flex-col items-center justify-center py-16 px-4 text-center">
                <div className="text-5xl mb-4 animate-bounce">🎯</div>
                <p className="text-lg font-semibold text-gray-900 dark:text-white mb-1">할 일이 없습니다</p>
                <span className="text-sm text-gray-500 dark:text-gray-400">위에서 새로운 할 일을 추가해보세요!</span>
            </div>
        );
    }

    return (
        <DndContext
            sensors={sensors}
            collisionDetection={closestCenter}
            onDragEnd={handleDragEnd}
        >
            <SortableContext items={todos.map(t => t.id)} strategy={verticalListSortingStrategy}>
                <div className="max-h-[400px] overflow-y-auto custom-scrollbar">
                    {todos.map((todo) => (
                        <TodoItem
                            key={todo.id}
                            todo={todo}
                            onToggle={onToggle}
                            onDelete={onDelete}
                            onUpdate={onUpdate}
                        />
                    ))}
                </div>
            </SortableContext>
            <style jsx global>{`
        .custom-scrollbar::-webkit-scrollbar {
          width: 6px;
        }
        .custom-scrollbar::-webkit-scrollbar-track {
          background: transparent;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb {
          background: rgba(156, 163, 175, 0.5);
          border-radius: 3px;
        }
      `}</style>
        </DndContext>
    );
}

import { cn } from "@/lib/utils";

interface FilterTabsProps {
    filter: string;
    setFilter: (filter: string) => void;
    counts: { total: number; active: number; completed: number };
}

export default function FilterTabs({ filter, setFilter, counts }: FilterTabsProps) {
    const tabs = [
        { id: "all", label: "전체", count: counts.total },
        { id: "active", label: "진행중", count: counts.active },
        { id: "completed", label: "완료", count: counts.completed },
    ];

    return (
        <div className="flex px-8 py-0 gap-2 border-b border-white/10">
            {tabs.map((tab) => (
                <button
                    key={tab.id}
                    onClick={() => setFilter(tab.id)}
                    className={cn(
                        "relative flex-1 py-3.5 px-4 text-[13px] font-medium transition-colors flex justify-center items-center gap-1.5",
                        filter === tab.id
                            ? "text-gray-900 dark:text-white"
                            : "text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200"
                    )}
                >
                    {tab.label}
                    <span className="inline-flex items-center justify-center min-w-[20px] h-5 px-1.5 rounded-full text-[11px] bg-black/5 dark:bg-white/10">
                        {tab.count}
                    </span>
                    {filter === tab.id && (
                        <div className="absolute bottom-0 left-1/2 -translate-x-1/2 w-full h-0.5 bg-gradient-to-r from-indigo-500 to-pink-500 rounded-full" />
                    )}
                </button>
            ))}
        </div>
    );
}

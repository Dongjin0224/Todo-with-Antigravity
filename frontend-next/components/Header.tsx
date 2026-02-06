import { Moon, Sun } from "lucide-react";
import { useEffect, useState } from "react";
import { cn } from "@/lib/utils";

export default function Header() {
    const [theme, setTheme] = useState<"light" | "dark">("dark");

    useEffect(() => {
        // Check initial preference
        if (localStorage.theme === 'light' || (!('theme' in localStorage) && window.matchMedia('(prefers-color-scheme: light)').matches)) {
            setTheme('light');
            document.documentElement.classList.remove('dark');
        } else {
            setTheme('dark');
            document.documentElement.classList.add('dark');
        }
    }, []);

    const toggleTheme = () => {
        const newTheme = theme === "light" ? "dark" : "light";
        setTheme(newTheme);
        if (newTheme === "dark") {
            document.documentElement.classList.add("dark");
            localStorage.theme = "dark";
        } else {
            document.documentElement.classList.remove("dark");
            localStorage.theme = "light";
        }
    };

    const today = new Date().toLocaleDateString("ko-KR", {
        year: "numeric",
        month: "long",
        day: "numeric",
        weekday: "long",
    });

    return (
        <header className="p-8 pb-6 border-b border-white/10 bg-gradient-to-br from-indigo-500/20 to-pink-500/10 dark:from-indigo-500/20 dark:to-pink-500/10 bg-white/50 dark:bg-transparent transition-colors">
            <div className="flex justify-between items-center mb-1">
                <h1 className="text-2xl font-bold tracking-tight text-gray-900 dark:text-white">📝 할 일 목록</h1>
                <button
                    onClick={toggleTheme}
                    className="p-2 rounded-full hover:bg-black/5 dark:hover:bg-white/10 transition-colors"
                    aria-label="Toggle Theme"
                >
                    {theme === "light" ? <Sun className="w-5 h-5 text-amber-500" /> : <Moon className="w-5 h-5 text-indigo-400" />}
                </button>
            </div>
            <p className="text-sm text-gray-500 dark:text-gray-400">{today}</p>
        </header>
    );
}

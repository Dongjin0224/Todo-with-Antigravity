'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/useAuthStore';
import TodoContainer from "@/components/TodoContainer";

export default function Home() {
  const { isAuthenticated } = useAuthStore();
  const router = useRouter();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    if (!isAuthenticated) {
      router.push('/auth/login');
    }
  }, [isAuthenticated, router]);

  if (!mounted) return null; // Avoid hydration mismatch
  if (!isAuthenticated) return null; // Will redirect

  return (
    <main className="min-h-screen flex items-center justify-center bg-[#0f0f23] p-4 font-[family-name:var(--font-geist-sans)]">
      <TodoContainer />
    </main>
  );
}

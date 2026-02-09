'use client';

import { useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useAuthStore } from '@/store/useAuthStore';

export default function OAuthCallbackPage() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const loginUser = useAuthStore((state) => state.login);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const accessToken = searchParams.get('accessToken');
        const refreshToken = searchParams.get('refreshToken');

        if (accessToken && refreshToken) {
            // JWT 토큰을 파싱하여 사용자 정보 추출
            try {
                const payload = JSON.parse(atob(accessToken.split('.')[1]));
                const user = {
                    email: payload.sub,
                    nickname: payload.nickname || payload.sub.split('@')[0],
                    role: payload.role || 'USER'
                };

                loginUser(user, accessToken, refreshToken);
                router.push('/');
            } catch (err) {
                setError('토큰 파싱 중 오류가 발생했습니다.');
            }
        } else {
            setError('OAuth 인증에 실패했습니다.');
        }
    }, [searchParams, loginUser, router]);

    if (error) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-gray-50 dark:bg-gray-900">
                <div className="text-center">
                    <p className="text-red-500">{error}</p>
                    <button
                        onClick={() => router.push('/auth/login')}
                        className="mt-4 rounded-md bg-indigo-600 px-4 py-2 text-white hover:bg-indigo-500"
                    >
                        로그인 페이지로 돌아가기
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="flex min-h-screen items-center justify-center bg-gray-50 dark:bg-gray-900">
            <div className="text-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto"></div>
                <p className="mt-4 text-gray-600 dark:text-gray-400">로그인 처리 중...</p>
            </div>
        </div>
    );
}

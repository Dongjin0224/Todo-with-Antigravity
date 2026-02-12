'use client';

import { useEffect, useMemo } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useAuthStore } from '@/store/useAuthStore';

type OAuthCallbackPayload = {
    error: string | null;
    accessToken: string | null;
    refreshToken: string | null;
    email: string | null;
    nickname: string | null;
    role: string | null;
};

export default function OAuthCallbackPage() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const loginUser = useAuthStore((state) => state.login);

    const payload = useMemo<OAuthCallbackPayload>(() => {
        const errorParam = searchParams.get('error');
        if (errorParam) {
            return {
                error: decodeURIComponent(errorParam),
                accessToken: null,
                refreshToken: null,
                email: null,
                nickname: null,
                role: null,
            };
        }

        const accessToken = searchParams.get('accessToken');
        const refreshToken = searchParams.get('refreshToken');
        const email = searchParams.get('email');
        const nickname = searchParams.get('nickname');
        const role = searchParams.get('role');

        if (!accessToken || !refreshToken || !email || !nickname || !role) {
            return {
                error: 'OAuth 인증에 실패했습니다.',
                accessToken: null,
                refreshToken: null,
                email: null,
                nickname: null,
                role: null,
            };
        }

        return {
            error: null,
            accessToken,
            refreshToken,
            email,
            nickname,
            role,
        };
    }, [searchParams]);

    useEffect(() => {
        if (payload.error) {
            return;
        }

        if (!payload.accessToken || !payload.refreshToken || !payload.email || !payload.nickname || !payload.role) {
            return;
        }

        // URL에서 토큰/개인정보 쿼리를 즉시 제거
        window.history.replaceState({}, '', '/auth/oauth/callback');

        loginUser(
            {
                email: payload.email,
                nickname: payload.nickname,
                role: payload.role,
            },
            payload.accessToken,
            payload.refreshToken
        );

        router.replace('/');
    }, [payload, loginUser, router]);

    if (payload.error) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-gray-50 dark:bg-gray-900">
                <div className="text-center">
                    <p className="text-red-500">{payload.error}</p>
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
                <div className="mx-auto h-12 w-12 animate-spin rounded-full border-b-2 border-indigo-600"></div>
                <p className="mt-4 text-gray-600 dark:text-gray-400">로그인 처리 중...</p>
            </div>
        </div>
    );
}

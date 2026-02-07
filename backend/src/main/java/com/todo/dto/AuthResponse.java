package com.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String grantType;
    private String accessToken;
<<<<<<< HEAD
    private String refreshToken;
=======
    private Long accessTokenExpiresIn;
>>>>>>> origin/main
    private String nickname;
    private String email;
    private String role;
}

package br.com.uork.appuork.dto.login;

public record LoginResponse(String accessToken, Long expiresIn) {
}

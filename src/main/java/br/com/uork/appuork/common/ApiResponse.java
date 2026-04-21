package br.com.uork.appuork.common;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {}

package br.com.uork.appuork.dto.usuario;

import java.time.Instant;

public record FotoUploadResponseDTO(
        String uploadUrl,
        String objectKey,
        Instant expiresAt
) {}

package br.com.uork.appuork.dto.localizacao;

import java.time.Instant;

public record LocalizacaoPrestadorDTO(
        double latitude,
        double longitude,
        Instant atualizadaEm
) {}

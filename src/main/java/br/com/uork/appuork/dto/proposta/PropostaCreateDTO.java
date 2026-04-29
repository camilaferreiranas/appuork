package br.com.uork.appuork.dto.proposta;

import java.math.BigDecimal;

public record PropostaCreateDTO(
        Long prestadorId,
        String email,
        String descricao,
        BigDecimal valor
) {}

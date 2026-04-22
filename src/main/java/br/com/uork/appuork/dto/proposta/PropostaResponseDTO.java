package br.com.uork.appuork.dto.proposta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PropostaResponseDTO(
        Long id,
        String nomeUsuario,
        String nomePrestador,
        String descricao,
        BigDecimal valor,
        String status,
        LocalDateTime dataCriacao
) {}

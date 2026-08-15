package br.com.uork.appuork.dto.proposta;

import br.com.uork.appuork.models.enuns.StatusProposta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoricoClienteDTO(
        Long propostaId,
        Long prestadorId,
        String titulo,
        String descricao,
        String nomePrestador,
        BigDecimal valor,
        StatusProposta status,
        LocalDateTime dataCriacao
) {}

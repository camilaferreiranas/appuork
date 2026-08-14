package br.com.uork.appuork.dto.demanda;

import br.com.uork.appuork.models.enuns.StatusProposta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DemandaProfissionalDTO(
        Long propostaId,
        String titulo,
        String descricao,
        String nomeCliente,
        BigDecimal valor,
        StatusProposta status,
        LocalDateTime dataCriacao
) {}

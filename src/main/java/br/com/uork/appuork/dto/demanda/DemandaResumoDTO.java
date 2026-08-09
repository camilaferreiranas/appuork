package br.com.uork.appuork.dto.demanda;

import br.com.uork.appuork.models.enuns.StatusProposta;

import java.math.BigDecimal;

public record DemandaResumoDTO(
        Long propostaId,
        String titulo,
        StatusProposta status,
        BigDecimal valor
) {}

package br.com.uork.appuork.dto.demanda;

import java.math.BigDecimal;

public record DetalheDemandaDTO(
        Long propostaId,
        String titulo,
        String nomeCliente,
        BigDecimal orcamento,
        Double distancia,
        String descricao,
        String nomePrestador
) {}

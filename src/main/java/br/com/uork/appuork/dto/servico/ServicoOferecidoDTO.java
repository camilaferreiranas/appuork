package br.com.uork.appuork.dto.servico;

import java.math.BigDecimal;

public record ServicoOferecidoDTO(
        String titulo,
        String descricao,
        BigDecimal valorMedio,
        Double avaliacao
) {}

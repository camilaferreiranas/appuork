package br.com.uork.appuork.dto.home;

import br.com.uork.appuork.dto.demanda.DemandaResumoDTO;

import java.math.BigDecimal;
import java.util.List;

public record listaDemandaDRO(
        Integer novasDemandas,
        Integer emAndamento,
        Integer concluido,
        BigDecimal faturamentoUltimos30Dias,
        List<DemandaResumoDTO> demandasProximas
) {}
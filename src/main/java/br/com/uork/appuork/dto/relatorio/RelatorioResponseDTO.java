package br.com.uork.appuork.dto.relatorio;

import java.util.List;
import java.util.Map;

public record RelatorioResponseDTO(
        Double totalRecebido,
        Double mesAtual,
        Integer propostasConcluidas,
        Integer propostasEmAndamento,
        Float avaliacao,
        Map<String, Double> evolucaoMensal,
        Integer quantidadeDemandas,
        List<TransacaoRecenteDTO> transacoesRecentes
) {
}

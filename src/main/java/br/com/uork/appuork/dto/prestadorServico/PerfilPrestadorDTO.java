package br.com.uork.appuork.dto.prestadorServico;

import br.com.uork.appuork.dto.servico.ServicoOferecidoDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PerfilPrestadorDTO(
        Long id,
        String nome,
        String descricao,
        String cidade,
        String estado,
        String dataCriacao,

        Double notaMedia,
        Integer totalAvaliacoes,
        Double percentualConclusao,
        BigDecimal totalGanho,

        String telefone,
        String email,

        List<ServicoOferecidoDTO> servicos
) {}

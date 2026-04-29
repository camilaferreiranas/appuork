package br.com.uork.appuork.dto.prestadorServico;

import java.util.List;

public record PrestadorDetalheDTO(
        Long id,
        String nome,
        String descricao,
        List<String> categorias,
        Double mediaAvaliacoes,
        Integer totalAvaliacoes,
        Boolean ativo
) {}

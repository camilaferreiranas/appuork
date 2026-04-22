package br.com.uork.appuork.dto.prestadorServico;

import java.util.List;

public record PrestadorListDTO(
        Long id,
        String nome,
        List<String> categorias,
        Double mediaAvaliacoes
) {}

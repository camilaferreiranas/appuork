package br.com.uork.appuork.dto.prestadorServico;

import java.util.List;

public record PrestadorResponseDTO(
        Long id,
        String nomeUsuario,
        String descricao,
        List<String> categorias,
        Boolean ativo
) {}

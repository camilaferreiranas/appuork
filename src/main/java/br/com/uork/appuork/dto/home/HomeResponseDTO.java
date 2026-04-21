package br.com.uork.appuork.dto.home;

import br.com.uork.appuork.models.Categoria;

import java.util.List;

public record HomeResponseDTO(
        String nomeUsuario,
        List<CategoriaResponseDTO> categorias
) {}

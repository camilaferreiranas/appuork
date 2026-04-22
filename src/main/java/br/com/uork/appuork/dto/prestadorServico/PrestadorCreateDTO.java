package br.com.uork.appuork.dto.prestadorServico;

import java.util.List;

public record PrestadorCreateDTO(
        String descricao,
        List<Long> categoriasIds
) {}
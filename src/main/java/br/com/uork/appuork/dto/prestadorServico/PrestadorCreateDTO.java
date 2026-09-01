package br.com.uork.appuork.dto.prestadorServico;

import br.com.uork.appuork.dto.usuario.EnderecoResponseDTO;

import java.util.List;

public record PrestadorCreateDTO(
        String descricao,
        List<Long> categoriasIds,
        EnderecoResponseDTO endereco
) {}

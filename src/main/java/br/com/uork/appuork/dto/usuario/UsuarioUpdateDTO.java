package br.com.uork.appuork.dto.usuario;

import br.com.uork.appuork.models.Endereco;

public record UsuarioUpdateDTO(
        String email,
        String senha,
        EnderecoResponseDTO endereco
) {}

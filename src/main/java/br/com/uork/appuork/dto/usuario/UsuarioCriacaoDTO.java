package br.com.uork.appuork.dto.usuario;

import br.com.uork.appuork.models.enuns.TipoPessoa;

public record UsuarioCriacaoDTO(
        String nome,
        String sobrenome,
        String email,
        String senha,
        TipoPessoa tipoPessoa,
        String documento,
        String telefone,
        EnderecoResponseDTO endereco
) {}

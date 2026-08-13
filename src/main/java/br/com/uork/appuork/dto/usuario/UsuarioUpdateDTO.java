package br.com.uork.appuork.dto.usuario;

import br.com.uork.appuork.models.enuns.TipoPessoa;

public record UsuarioUpdateDTO(
        String nome,
        String sobrenome,
        String email,
        String senha,
        TipoPessoa tipoPessoa,
        String documento,
        String telefone,
        EnderecoResponseDTO endereco
) {
    public UsuarioUpdateDTO(String email, String senha, EnderecoResponseDTO endereco) {
        this(null, null, email, senha, null, null, null, endereco);
    }
}

package br.com.uork.appuork.dto.usuario;

public record PerfilResponseDTO(
        Long id,
        String nome,
        String sobrenome,
        String email,
        String tipoPessoa,
        String documento,
        EnderecoResponseDTO endereco
) {}
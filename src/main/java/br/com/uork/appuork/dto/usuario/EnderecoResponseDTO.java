package br.com.uork.appuork.dto.usuario;

public record EnderecoResponseDTO(
        String rua,
        String numero,
        String bairro,
        String cidade,
        String estado,
        String cep
) {}

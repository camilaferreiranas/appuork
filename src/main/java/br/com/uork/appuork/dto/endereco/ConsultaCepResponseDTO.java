package br.com.uork.appuork.dto.endereco;

public record ConsultaCepResponseDTO(
        String cep,
        String rua,
        String bairro,
        String cidade,
        String estado
) {}

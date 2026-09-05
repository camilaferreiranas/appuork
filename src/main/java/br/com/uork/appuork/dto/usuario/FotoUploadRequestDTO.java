package br.com.uork.appuork.dto.usuario;

public record FotoUploadRequestDTO(
        String contentType,
        Long tamanhoBytes
) {}

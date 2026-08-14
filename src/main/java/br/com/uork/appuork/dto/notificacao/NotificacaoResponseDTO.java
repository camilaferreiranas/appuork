package br.com.uork.appuork.dto.notificacao;

import java.time.LocalDateTime;

public record NotificacaoResponseDTO(
        Long id,
        String titulo,
        String mensagem,
        boolean lida,
        LocalDateTime dataCriacao,
        Long propostaId
) {}

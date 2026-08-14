package br.com.uork.appuork.dto.notificacao;

import br.com.uork.appuork.models.enuns.ContextoNotificacao;

public record NotificacaoRealtimeDTO(
        ContextoNotificacao contexto,
        NotificacaoResponseDTO notificacao
) {}

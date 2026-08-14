package br.com.uork.appuork.events;

import br.com.uork.appuork.dto.notificacao.NotificacaoResponseDTO;
import br.com.uork.appuork.models.enuns.ContextoNotificacao;

public record NotificacaoCriadaEvent(
        String destinatario,
        ContextoNotificacao contexto,
        NotificacaoResponseDTO notificacao
) {}

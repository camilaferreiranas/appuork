package br.com.uork.appuork.service;

import br.com.uork.appuork.events.NotificacaoCriadaEvent;
import br.com.uork.appuork.dto.notificacao.NotificacaoRealtimeDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificacaoRealtimePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificacaoRealtimePublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enviar(NotificacaoCriadaEvent event) {
        messagingTemplate.convertAndSendToUser(
                event.destinatario(),
                "/queue/notificacoes",
                new NotificacaoRealtimeDTO(event.contexto(), event.notificacao())
        );
    }
}

package br.com.uork.appuork.dto.notificacao;

import java.util.List;

public record NotificacoesDTO(
        long naoLidas,
        List<NotificacaoResponseDTO> notificacoes
) {}

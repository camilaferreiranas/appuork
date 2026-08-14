package br.com.uork.appuork.controller;

import br.com.uork.appuork.common.ApiResponse;
import br.com.uork.appuork.dto.notificacao.NotificacaoResponseDTO;
import br.com.uork.appuork.dto.notificacao.NotificacoesDTO;
import br.com.uork.appuork.service.NotificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<NotificacoesDTO>> listar(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Notificações carregadas com sucesso",
                        notificacaoService.listar(jwt.getSubject())
                )
        );
    }

    @GetMapping("/cliente")
    public ResponseEntity<ApiResponse<NotificacoesDTO>> listarCliente(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Notificações do cliente carregadas com sucesso",
                        notificacaoService.listarCliente(jwt.getSubject())
                )
        );
    }

    @PatchMapping("/{id}/lida")
    public ResponseEntity<ApiResponse<NotificacaoResponseDTO>> marcarComoLida(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Notificação marcada como lida",
                        notificacaoService.marcarComoLida(id, jwt.getSubject())
                )
        );
    }
}

package br.com.uork.appuork.service;

import br.com.uork.appuork.dto.notificacao.NotificacaoResponseDTO;
import br.com.uork.appuork.dto.notificacao.NotificacoesDTO;
import br.com.uork.appuork.models.Notificacao;
import br.com.uork.appuork.models.Proposta;
import br.com.uork.appuork.models.Usuario;
import br.com.uork.appuork.repository.NotificacaoRepository;
import br.com.uork.appuork.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository,
                              UsuarioRepository usuarioRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public void criarNotificacaoDeProposta(Proposta proposta) {
        Notificacao notificacao = new Notificacao();
        notificacao.setDestinatario(proposta.getPrestadorServico().getUsuario());
        notificacao.setProposta(proposta);
        notificacao.setTitulo("Nova proposta recebida");
        notificacao.setMensagem(
                proposta.getUsuario().getNome() + " enviou uma proposta: " + proposta.getTitulo()
        );
        notificacao.setLida(false);
        notificacao.setDataCriacao(LocalDateTime.now());
        notificacaoRepository.save(notificacao);
    }

    @Transactional(readOnly = true)
    public NotificacoesDTO listar(String email) {
        Usuario usuario = buscarUsuario(email);
        var notificacoes = notificacaoRepository
                .findByDestinatarioOrderByDataCriacaoDesc(usuario)
                .stream()
                .map(this::toResponse)
                .toList();

        return new NotificacoesDTO(
                notificacaoRepository.countByDestinatarioAndLidaFalse(usuario),
                notificacoes
        );
    }

    @Transactional
    public NotificacaoResponseDTO marcarComoLida(Long notificacaoId, String email) {
        Usuario usuario = buscarUsuario(email);
        Notificacao notificacao = notificacaoRepository
                .findByIdAndDestinatario(notificacaoId, usuario)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada"));

        notificacao.setLida(true);
        return toResponse(notificacaoRepository.save(notificacao));
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    private NotificacaoResponseDTO toResponse(Notificacao notificacao) {
        return new NotificacaoResponseDTO(
                notificacao.getId(),
                notificacao.getTitulo(),
                notificacao.getMensagem(),
                notificacao.isLida(),
                notificacao.getDataCriacao(),
                notificacao.getProposta().getId()
        );
    }
}

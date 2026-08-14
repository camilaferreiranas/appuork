package br.com.uork.appuork.service;

import br.com.uork.appuork.dto.notificacao.NotificacaoResponseDTO;
import br.com.uork.appuork.dto.notificacao.NotificacoesDTO;
import br.com.uork.appuork.events.NotificacaoCriadaEvent;
import br.com.uork.appuork.models.Notificacao;
import br.com.uork.appuork.models.Proposta;
import br.com.uork.appuork.models.Usuario;
import br.com.uork.appuork.models.enuns.StatusProposta;
import br.com.uork.appuork.models.enuns.ContextoNotificacao;
import br.com.uork.appuork.repository.NotificacaoRepository;
import br.com.uork.appuork.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ApplicationEventPublisher eventPublisher;

    public NotificacaoService(NotificacaoRepository notificacaoRepository,
                              UsuarioRepository usuarioRepository,
                              ApplicationEventPublisher eventPublisher) {
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.eventPublisher = eventPublisher;
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
        Notificacao salva = notificacaoRepository.save(notificacao);
        publicarEvento(salva, ContextoNotificacao.PRESTADOR);
    }

    public void criarNotificacaoDePropostaAceita(Proposta proposta) {
        Notificacao notificacao = new Notificacao();
        notificacao.setDestinatario(proposta.getUsuario());
        notificacao.setProposta(proposta);
        notificacao.setTitulo("Proposta aceita");
        notificacao.setMensagem(
                proposta.getPrestadorServico().getUsuario().getNome()
                        + " aceitou sua proposta: "
                        + proposta.getTitulo()
        );
        notificacao.setLida(false);
        notificacao.setDataCriacao(LocalDateTime.now());
        Notificacao salva = notificacaoRepository.save(notificacao);
        publicarEvento(salva, ContextoNotificacao.CLIENTE);
    }

    @Transactional(readOnly = true)
    public NotificacoesDTO listar(String email) {
        Usuario usuario = buscarUsuario(email);
        var notificacoes = notificacaoRepository
                .findByDestinatarioAndStatusProposta(usuario, StatusProposta.PENDENTE)
                .stream()
                .map(this::toResponse)
                .toList();

        return new NotificacoesDTO(
                notificacaoRepository.countNaoLidasByDestinatarioAndStatusProposta(
                        usuario,
                        StatusProposta.PENDENTE
                ),
                notificacoes
        );
    }

    @Transactional(readOnly = true)
    public NotificacoesDTO listarCliente(String email) {
        Usuario usuario = buscarUsuario(email);
        var notificacoes = notificacaoRepository
                .findNotificacoesDoCliente(usuario)
                .stream()
                .map(this::toResponse)
                .toList();

        return new NotificacoesDTO(
                notificacaoRepository.countNaoLidasDoCliente(usuario),
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

    private void publicarEvento(Notificacao notificacao, ContextoNotificacao contexto) {
        eventPublisher.publishEvent(new NotificacaoCriadaEvent(
                notificacao.getDestinatario().getEmail(),
                contexto,
                toResponse(notificacao)
        ));
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

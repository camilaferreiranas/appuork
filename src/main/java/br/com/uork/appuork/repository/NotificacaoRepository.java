package br.com.uork.appuork.repository;

import br.com.uork.appuork.models.Notificacao;
import br.com.uork.appuork.models.Usuario;
import br.com.uork.appuork.models.enuns.StatusProposta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    @Query("""
            select n from Notificacao n
            where n.destinatario = :destinatario
              and n.proposta.status = :status
            order by n.dataCriacao desc
            """)
    List<Notificacao> findByDestinatarioAndStatusProposta(
            @Param("destinatario") Usuario destinatario,
            @Param("status") StatusProposta status);

    @Query("""
            select count(n) from Notificacao n
            where n.destinatario = :destinatario
              and n.lida = false
              and n.proposta.status = :status
            """)
    long countNaoLidasByDestinatarioAndStatusProposta(
            @Param("destinatario") Usuario destinatario,
            @Param("status") StatusProposta status);

    @Query("""
            select n from Notificacao n
            where n.destinatario = :destinatario
              and n.proposta.usuario = :destinatario
            order by n.dataCriacao desc
            """)
    List<Notificacao> findNotificacoesDoCliente(
            @Param("destinatario") Usuario destinatario);

    @Query("""
            select count(n) from Notificacao n
            where n.destinatario = :destinatario
              and n.proposta.usuario = :destinatario
              and n.lida = false
            """)
    long countNaoLidasDoCliente(
            @Param("destinatario") Usuario destinatario);

    Optional<Notificacao> findByIdAndDestinatario(Long id, Usuario destinatario);
}

package br.com.uork.appuork.repository;

import br.com.uork.appuork.models.Notificacao;
import br.com.uork.appuork.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findByDestinatarioOrderByDataCriacaoDesc(Usuario destinatario);
    long countByDestinatarioAndLidaFalse(Usuario destinatario);
    Optional<Notificacao> findByIdAndDestinatario(Long id, Usuario destinatario);
}

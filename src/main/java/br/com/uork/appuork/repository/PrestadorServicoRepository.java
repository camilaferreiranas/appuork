package br.com.uork.appuork.repository;

import br.com.uork.appuork.models.PrestadorServico;
import br.com.uork.appuork.models.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrestadorServicoRepository extends JpaRepository<PrestadorServico, Long> {

    boolean existsByUsuario(Usuario usuario);

    Optional<PrestadorServico> findByUsuario(Usuario usuario);

    Page<PrestadorServico> findByAtivoTrue(Pageable pageable);
}

package br.com.uork.appuork.repository;

import br.com.uork.appuork.models.Proposta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropostaRepository extends JpaRepository<Proposta, Long> {


    Page<Proposta> findAll(Pageable pageable);
}

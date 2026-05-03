package br.com.uork.appuork.repository;

import br.com.uork.appuork.models.PrestadorServico;
import br.com.uork.appuork.models.Proposta;
import br.com.uork.appuork.models.enuns.StatusProposta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PropostaRepository extends JpaRepository<Proposta, Long> {

    Page<Proposta> findAll(Pageable pageable);

    @Query("SELECT SUM(p.valor) FROM Proposta p WHERE p.prestadorServico = :prestador AND p.status = :status")
    BigDecimal somarValorPorStatus(@Param("prestador") PrestadorServico prestador, @Param("status") StatusProposta status);

    @Query("SELECT SUM(p.valor) FROM Proposta p WHERE p.prestadorServico = :prestador AND p.status = :status AND p.dataCriacao >= :inicio")
    BigDecimal somarValorPorStatusEPeriodo(@Param("prestador") PrestadorServico prestador, @Param("status") StatusProposta status, @Param("inicio") LocalDateTime inicio);

    @Query("SELECT COUNT(p) FROM Proposta p WHERE p.prestadorServico = :prestador AND p.status = :status")
    Integer contarPorStatus(@Param("prestador") PrestadorServico prestador, @Param("status") StatusProposta status);

    @Query("SELECT YEAR(p.dataCriacao), MONTH(p.dataCriacao), SUM(p.valor) FROM Proposta p WHERE p.prestadorServico = :prestador AND p.status = :status GROUP BY YEAR(p.dataCriacao), MONTH(p.dataCriacao) ORDER BY YEAR(p.dataCriacao) ASC, MONTH(p.dataCriacao) ASC")
    List<Object[]> evolucaoMensal(@Param("prestador") PrestadorServico prestador, @Param("status") StatusProposta status);

    @Query("SELECT COUNT(p) FROM Proposta p WHERE p.prestadorServico = :prestador AND p.dataCriacao >= :inicio")
    Integer contarDemandas(@Param("prestador") PrestadorServico prestador, @Param("inicio") LocalDateTime inicio);

    @Query("SELECT p FROM Proposta p WHERE p.prestadorServico = :prestador AND p.status = :status ORDER BY p.dataCriacao DESC")
    List<Proposta> transacoesRecentes(@Param("prestador") PrestadorServico prestador, @Param("status") StatusProposta status, Pageable pageable);
}

package br.com.uork.appuork.repository;

import br.com.uork.appuork.models.Proposta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PropostaRepository extends JpaRepository<Proposta, Long> {


    @Query("""
    SELECT COUNT(p)
    FROM Proposta p
    WHERE p.prestadorServico.id = :prestadorId
    AND p.status = 'PENDENTE'
""")
    Integer contarNovasDemandas(Long prestadorId);


    @Query("""
    SELECT COUNT(p)
    FROM Proposta p
    WHERE p.prestadorServico.id = :prestadorId
    AND p.status = 'ACEITA'
""")
    Integer contarEmAndamento(Long prestadorId);

    @Query("""
    SELECT COALESCE(SUM(p.valor), 0)
    FROM Proposta p
    WHERE p.prestadorServico.id = :prestadorId
    AND p.status = 'FINALIZADA'
    AND p.dataCriacao >= :dataInicio
""")
    BigDecimal faturamentoUltimos30Dias(
            @Param("prestadorId") Long prestadorId,
            @Param("dataInicio") LocalDateTime dataInicio
    );

    @Query("""
    SELECT p
    FROM Proposta p
    WHERE p.prestadorServico.id = :prestadorId
    AND p.status IN ('PENDENTE', 'ACEITA')
    ORDER BY p.dataCriacao DESC
""")
    List<Proposta> buscarDemandasProximas(Long prestadorId);

    @Query("""
    SELECT COUNT(p)
    FROM Proposta p
    WHERE p.prestadorServico.id = :prestadorId
    AND p.status = 'FINALIZADA'
""")
    Integer contarDemandasConcluida(Long prestadorId);


}

package br.com.uork.appuork.repository;

import br.com.uork.appuork.models.PrestadorServico;
import br.com.uork.appuork.models.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrestadorServicoRepository extends JpaRepository<PrestadorServico, Long> {

    boolean existsByUsuario(Usuario usuario);

    Optional<PrestadorServico> findByUsuario(Usuario usuario);

    Page<PrestadorServico> findByAtivoTrue(Pageable pageable);

    @Query(
            value = """
                    SELECT DISTINCT p.*
                    FROM prestador_servico p
                    JOIN usuario u ON u.id = p.usuario_id
                    JOIN prestador_categoria pc ON pc.prestador_id = p.id
                    JOIN categoria c ON c.id = pc.categoria_id
                    WHERE p.ativo = true
                      AND (:categoriaId IS NULL OR c.id = :categoriaId)
                      AND (:usuarioIdExcluir IS NULL OR u.id <> :usuarioIdExcluir)
                      AND (
                          :termoBusca IS NULL
                          OR translate(
                              lower(concat_ws(' ', u.nome, u.sobrenome, p.descricao, c.nome)),
                              'áàâãäéèêëíìîïóòôõöúùûüç',
                              'aaaaaeeeeiiiiooooouuuuc'
                          ) LIKE concat('%', :termoBusca, '%')
                      )
                    """,
            countQuery = """
                    SELECT count(DISTINCT p.id)
                    FROM prestador_servico p
                    JOIN usuario u ON u.id = p.usuario_id
                    JOIN prestador_categoria pc ON pc.prestador_id = p.id
                    JOIN categoria c ON c.id = pc.categoria_id
                    WHERE p.ativo = true
                      AND (:categoriaId IS NULL OR c.id = :categoriaId)
                      AND (:usuarioIdExcluir IS NULL OR u.id <> :usuarioIdExcluir)
                      AND (
                          :termoBusca IS NULL
                          OR translate(
                              lower(concat_ws(' ', u.nome, u.sobrenome, p.descricao, c.nome)),
                              'áàâãäéèêëíìîïóòôõöúùûüç',
                              'aaaaaeeeeiiiiooooouuuuc'
                          ) LIKE concat('%', :termoBusca, '%')
                      )
                    """,
            nativeQuery = true
    )
    Page<PrestadorServico> buscarPorCategoria(
            @Param("categoriaId") Long categoriaId,
            @Param("usuarioIdExcluir") Long usuarioIdExcluir,
            @Param("termoBusca") String termoBusca,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT DISTINCT p.*
                    FROM prestador_servico p
                    JOIN usuario u ON u.id = p.usuario_id
                    JOIN prestador_categoria pc ON pc.prestador_id = p.id
                    JOIN categoria c ON c.id = pc.categoria_id
                    WHERE p.ativo = true
                      AND (:categoriaId IS NULL OR c.id = :categoriaId)
                      AND (:usuarioIdExcluir IS NULL OR u.id <> :usuarioIdExcluir)
                      AND (
                          :termoBusca IS NULL
                          OR translate(
                              lower(concat_ws(' ', u.nome, u.sobrenome, p.descricao, c.nome)),
                              'áàâãäéèêëíìîïóòôõöúùûüç',
                              'aaaaaeeeeiiiiooooouuuuc'
                          ) LIKE concat('%', :termoBusca, '%')
                      )
                    """,
            nativeQuery = true
    )
    List<PrestadorServico> buscarPorCategoria(
            @Param("categoriaId") Long categoriaId,
            @Param("usuarioIdExcluir") Long usuarioIdExcluir,
            @Param("termoBusca") String termoBusca
    );

}

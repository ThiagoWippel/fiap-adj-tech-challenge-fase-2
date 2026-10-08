package br.com.fiap.restaurante.infrastructure.persistence.repository;

import br.com.fiap.restaurante.infrastructure.persistence.entity.RestauranteEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositório de restaurantes. As buscas filtram removido_em nulo e trazem o dono
 * e o tipo dele na mesma consulta; os turnos vêm em lote (ver RestauranteEntity).
 */
public interface RestauranteRepository extends JpaRepository<RestauranteEntity, Long> {

    @EntityGraph(attributePaths = {"dono", "dono.tipo"})
    Optional<RestauranteEntity> findByIdAndRemovidoEmIsNull(Long id);

    @EntityGraph(attributePaths = {"dono", "dono.tipo"})
    Page<RestauranteEntity> findByNomeContainingAndRemovidoEmIsNull(String nome, Pageable paginacao);

    @EntityGraph(attributePaths = {"dono", "dono.tipo"})
    Page<RestauranteEntity> findByNomeContainingAndTipoCozinhaAndRemovidoEmIsNull(String nome, String tipoCozinha,
                                                                                  Pageable paginacao);

    @EntityGraph(attributePaths = {"dono", "dono.tipo"})
    Page<RestauranteEntity> findByDonoIdAndRemovidoEmIsNull(Long donoId, Pageable paginacao);

    long countByDonoIdAndRemovidoEmIsNull(Long donoId);

    boolean existsByIdAndRemovidoEmIsNull(Long id);
}

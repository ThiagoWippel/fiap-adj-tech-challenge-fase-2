package br.com.fiap.restaurante.infrastructure.persistence.repository;

import br.com.fiap.restaurante.infrastructure.persistence.entity.ItemCardapioEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

/**
 * Repositório de itens do cardápio. Tudo filtra removido_em nulo. Pela colação
 * da tabela, a comparação de nomes não diferencia maiúsculas, acentos nem espaço
 * no fim.
 */
public interface ItemCardapioRepository extends JpaRepository<ItemCardapioEntity, Long> {

    Optional<ItemCardapioEntity> findByIdAndRemovidoEmIsNull(Long id);

    Page<ItemCardapioEntity> findByRestauranteIdAndRemovidoEmIsNull(Long restauranteId, Pageable paginacao);

    Page<ItemCardapioEntity> findByRestauranteIdAndApenasNoLocalAndRemovidoEmIsNull(Long restauranteId,
                                                                                    Boolean apenasNoLocal,
                                                                                    Pageable paginacao);

    boolean existsByRestauranteIdAndNomeAndRemovidoEmIsNull(Long restauranteId, String nome);

    boolean existsByRestauranteIdAndNomeAndRemovidoEmIsNullAndIdNot(Long restauranteId, String nome, Long id);

    // SELECT ... FOR UPDATE só na linha do próprio registro, sem as associações
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ItemCardapioEntity> findParaAlterarByIdAndRemovidoEmIsNull(Long id);
}

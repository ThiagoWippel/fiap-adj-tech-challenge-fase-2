package br.com.fiap.restaurante.infrastructure.persistence.repository;

import br.com.fiap.restaurante.infrastructure.persistence.entity.UsuarioEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

/**
 * As buscas filtram removido_em nulo e já trazem o tipo junto, numa consulta só.
 * As verificações de unicidade não filtram: usuário removido tem e-mail, login e
 * documento nulos. A colação da tabela não diferencia maiúsculas nem acentos.
 */
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    @EntityGraph(attributePaths = "tipo")
    Optional<UsuarioEntity> findByIdAndRemovidoEmIsNull(Long id);

    @EntityGraph(attributePaths = "tipo")
    Optional<UsuarioEntity> findByLoginAndRemovidoEmIsNull(String login);

    @EntityGraph(attributePaths = "tipo")
    List<UsuarioEntity> findByNomeContainingAndRemovidoEmIsNullOrderByNomeAsc(String nome);

    @EntityGraph(attributePaths = "tipo")
    Page<UsuarioEntity> findByNomeContainingAndRemovidoEmIsNull(String nome, Pageable paginacao);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByLogin(String login);

    boolean existsByLoginAndIdNot(String login, Long id);

    boolean existsByDocumento(String documento);

    boolean existsByDocumentoAndIdNot(String documento, Long id);

    long countByTipoIdAndRemovidoEmIsNull(Long tipoId);

    @EntityGraph(attributePaths = "tipo")
    Page<UsuarioEntity> findByTipoIdAndRemovidoEmIsNull(Long tipoId, Pageable paginacao);

    // SELECT ... FOR UPDATE só na linha do próprio registro, sem as associações
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UsuarioEntity> findParaAlterarByIdAndRemovidoEmIsNull(Long id);
}

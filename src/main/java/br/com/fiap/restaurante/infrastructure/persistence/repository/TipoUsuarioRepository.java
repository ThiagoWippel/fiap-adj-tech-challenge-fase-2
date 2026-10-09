package br.com.fiap.restaurante.infrastructure.persistence.repository;

import br.com.fiap.restaurante.infrastructure.persistence.entity.TipoUsuarioEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

/**
 * Repositório de tipos de usuário. Pela colação da tabela, as buscas por nome e
 * por código não diferenciam maiúsculas nem acentos.
 */
public interface TipoUsuarioRepository extends JpaRepository<TipoUsuarioEntity, Long> {

    Optional<TipoUsuarioEntity> findByCodigo(String codigo);

    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, Long id);

    // SELECT ... FOR UPDATE só na linha do próprio registro, sem as associações
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TipoUsuarioEntity> findParaAlterarById(Long id);
}

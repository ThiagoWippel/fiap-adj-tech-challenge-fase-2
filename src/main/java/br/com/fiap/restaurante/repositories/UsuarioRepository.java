package br.com.fiap.restaurante.repositories;

import br.com.fiap.restaurante.entities.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Acesso a dados de usuários, independente do subtipo.
 *
 * Por operar sobre a raiz da hierarquia, as consultas aqui alcançam clientes
 * e donos de restaurante indistintamente - que é exatamente o comportamento
 * desejado para busca por nome, autenticação e verificação de unicidade.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca por nome parcial, sem diferenciar maiúsculas de minúsculas.
     *
     * Não há índice sobre a coluna "nome" de propósito: a consulta gerada
     * procura o termo em qualquer posição, e índices de árvore só são
     * aproveitados em buscas por prefixo. Criar o índice daria a impressão
     * de otimização sem produzir nenhuma.
     */
    Page<Usuario> findByNomeContainingIgnoreCase(String nome, Pageable paginacao);

    /**
     * Mesma consulta, sem paginação. Atende a versão 1 da busca, que devolve
     * uma lista simples. O Spring Data distingue as duas pelo tipo de retorno.
     */
    List<Usuario> findByNomeContainingIgnoreCase(String nome);

    /**
     * Usado pelo serviço de autenticação.
     */
    Optional<Usuario> findByLogin(String login);

    // --- Verificações de unicidade no cadastro ---

    boolean existsByEmail(String email);

    boolean existsByLogin(String login);

    // --- Verificações de unicidade na atualização ---
    //
    // A pergunta correta é "existe OUTRO usuário com este e-mail?", e não
    // "existe algum usuário com este e-mail?". Sem excluir o próprio registro
    // da busca, salvar um cadastro sem alterar o e-mail resultaria em conflito
    // do usuário consigo mesmo.

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByLoginAndIdNot(String login, Long id);
}

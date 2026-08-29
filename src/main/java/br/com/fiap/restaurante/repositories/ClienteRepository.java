package br.com.fiap.restaurante.repositories;

import br.com.fiap.restaurante.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Consultas específicas de cliente.
 *
 * Existe separado de UsuarioRepository por uma razão concreta: o CPF é um
 * atributo de Cliente, não de Usuário. Um método derivado não pode referenciar
 * um campo ausente na entidade sobre a qual o repositório opera.
 *
 * O resultado atende ao Princípio da Segregação de Interfaces: quem só precisa
 * autenticar um usuário não passa a depender de métodos sobre CPF.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Verificação de unicidade no cadastro.
     *
     * Não existe equivalente para a atualização porque o documento é imutável
     * após o cadastro: AtualizarUsuarioRequest não o aceita.
     */
    boolean existsByCpf(String cpf);
}
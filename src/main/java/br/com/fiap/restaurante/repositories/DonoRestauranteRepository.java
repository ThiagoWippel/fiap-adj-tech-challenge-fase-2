package br.com.fiap.restaurante.repositories;

import br.com.fiap.restaurante.entities.DonoRestaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Consultas específicas de dono de restaurante.
 *
 * Mesma justificativa do ClienteRepository: o CNPJ pertence a esta subclasse.
 */
@Repository
public interface DonoRestauranteRepository extends JpaRepository<DonoRestaurante, Long> {

    /**
     * Verificação de unicidade no cadastro. Assim como o CPF, o CNPJ não pode
     * ser alterado depois do cadastro, então não há versão para a atualização.
     */
    boolean existsByCnpj(String cnpj);
}
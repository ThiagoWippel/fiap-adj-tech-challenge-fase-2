package br.com.fiap.restaurante.repositories;

import br.com.fiap.restaurante.entities.DonoRestaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Consultas especificas de dono de restaurante.
 *
 * Mesma justificativa do ClienteRepository: o CNPJ pertence a esta subclasse.
 */
@Repository
public interface DonoRestauranteRepository extends JpaRepository<DonoRestaurante, Long> {

    /**
     * Verificacao de unicidade no cadastro. Assim como o CPF, o CNPJ nao pode
     * ser alterado depois do cadastro, entao nao ha versao para a atualizacao.
     */
    boolean existsByCnpj(String cnpj);
}
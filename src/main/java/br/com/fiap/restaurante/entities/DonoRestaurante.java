package br.com.fiap.restaurante.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Usuário proprietário de um ou mais estabelecimentos.
 *
 * Identificado por CNPJ. Nas próximas fases do projeto, é nesta classe que a
 * associação com a entidade Restaurante será declarada - motivo pelo qual a
 * herança foi preferida a um simples campo de tipo.
 */
@Entity
@DiscriminatorValue("DONO_RESTAURANTE")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DonoRestaurante extends Usuario {

    // Apenas dígitos, mesma justificativa do CPF.
    @Column(name = "cnpj", length = 14, unique = true)
    private String cnpj;

    public DonoRestaurante(String nome, String email, String login, String senha,
                           Endereco endereco, String cnpj) {
        super(nome, email, login, senha, endereco);
        this.cnpj = cnpj;
    }

    @Override
    public TipoUsuario getTipo() {
        return TipoUsuario.DONO_RESTAURANTE;
    }

    @Override
    public String getDocumento() {
        return this.cnpj;
    }
}

package br.com.fiap.restaurante.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Endereço gravado nas colunas endereco_* da própria tabela.
 */
@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EnderecoEmbeddable {

    @Column(name = "endereco_rua", length = 150)
    private String rua;

    @Column(name = "endereco_numero", length = 10)
    private String numero;

    @Column(name = "endereco_complemento", length = 60)
    private String complemento;

    @Column(name = "endereco_bairro", length = 80)
    private String bairro;

    @Column(name = "endereco_cidade", length = 80)
    private String cidade;

    @Column(name = "endereco_estado", length = 2)
    private String estado;

    @Column(name = "endereco_cep", length = 8)
    private String cep;
}

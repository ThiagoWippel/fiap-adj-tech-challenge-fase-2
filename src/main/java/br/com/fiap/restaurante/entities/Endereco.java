package br.com.fiap.restaurante.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Endereço do usuário, mapeado como objeto de valor.
 *
 * Não possui identidade própria nem ciclo de vida independente: não existe
 * "um endereço" no sistema, existe "o endereço de um usuário". Por isso é um
 * @Embeddable, e não uma entidade - seus campos viram colunas da própria
 * tabela de usuário, sem tabela adicional nem relacionamento.
 *
 * O prefixo "endereco_" nos nomes das colunas agrupa visualmente os campos na
 * tabela. Caso este objeto passasse a ser reutilizado por outra entidade, o
 * prefixo seria movido para @AttributeOverrides na entidade que o embute.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Endereco {

    @Column(name = "endereco_rua", nullable = false, length = 150)
    private String rua;

    // Texto, e não número: existem valores como "S/N" e "123-A".
    @Column(name = "endereco_numero", nullable = false, length = 10)
    private String numero;

    @Column(name = "endereco_complemento", length = 60)
    private String complemento;

    @Column(name = "endereco_bairro", nullable = false, length = 80)
    private String bairro;

    @Column(name = "endereco_cidade", nullable = false, length = 80)
    private String cidade;

    @Column(name = "endereco_estado", nullable = false, length = 2)
    private String estado;

    // Armazenado apenas com dígitos, normalizado na entrada.
    @Column(name = "endereco_cep", nullable = false, length = 9)
    private String cep;
}

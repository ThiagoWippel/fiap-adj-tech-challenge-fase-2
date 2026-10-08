package br.com.fiap.restaurante.domain.valueobject;

import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;

import java.math.BigDecimal;

/**
 * Preço de um item: maior que zero, com no máximo duas casas decimais e até
 * 99.999.999,99, o que cabe numa coluna DECIMAL(10,2). Valor com mais casas é
 * recusado, nunca arredondado. Fica sempre com duas casas: 39.9 vira 39.90.
 */
public record Preco(BigDecimal valor) {

    private static final BigDecimal MAXIMO = new BigDecimal("99999999.99");

    public Preco {
        if (valor == null) {
            throw new ValidacaoDeDominioException("O preço é obrigatório.");
        }
        if (valor.signum() <= 0) {
            throw new ValidacaoDeDominioException("O preço deve ser maior que zero.");
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            throw new ValidacaoDeDominioException("O preço deve ter no máximo duas casas decimais.");
        }
        if (valor.compareTo(MAXIMO) > 0) {
            throw new ValidacaoDeDominioException("O preço deve ser de no máximo 99.999.999,99.");
        }
        // Com no máximo duas casas, a mudança de escala é exata
        valor = valor.setScale(2);
    }
}

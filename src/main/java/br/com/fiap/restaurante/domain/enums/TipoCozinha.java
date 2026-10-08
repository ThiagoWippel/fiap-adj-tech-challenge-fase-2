package br.com.fiap.restaurante.domain.enums;

import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Tipo de cozinha do restaurante. É enum, e não tabela, porque a lista só muda
 * junto com o código; o tipo de usuário, que tem CRUD, é tabela.
 */
public enum TipoCozinha {

    BRASILEIRA, ITALIANA, PIZZARIA, JAPONESA, CHINESA, ARABE, MEXICANA, PORTUGUESA, FRANCESA, HAMBURGUERIA,
    LANCHES, CHURRASCARIA, FRUTOS_DO_MAR, VEGETARIANA, VEGANA, DOCES_E_SOBREMESAS, CAFETERIA, OUTRA;

    /** Converte o texto recebido, sem diferenciar maiúsculas. */
    public static TipoCozinha de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ValidacaoDeDominioException("O tipo de cozinha é obrigatório.");
        }
        String normalizado = valor.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(tipo -> tipo.name().equals(normalizado))
                .findFirst()
                .orElseThrow(() -> new ValidacaoDeDominioException("O tipo de cozinha " + valor.trim()
                        + " não existe. Valores aceitos: " + aceitos() + "."));
    }

    private static String aceitos() {
        return Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", "));
    }
}

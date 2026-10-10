package br.com.fiap.restaurante.domain.valueobject;

import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;

import java.util.regex.Pattern;

/**
 * Regra comum aos textos digitados pelo usuário: nada de caracteres de controle,
 * como o nulo, nem dos que invertem a direção da escrita, usados para disfarçar um
 * nome na tela. Nomes e endereços têm uma linha só; a descrição aceita quebra de
 * linha e tabulação.
 */
public final class TextoLivre {

    private static final Pattern CONTROLE_EM_UMA_LINHA = Pattern.compile("[\\p{Cc}\\u202A-\\u202E\\u2066-\\u2069]");
    private static final Pattern CONTROLE_EM_VARIAS_LINHAS =
            Pattern.compile("[\\p{Cc}&&[^\\n\\r\\t]]|[\\u202A-\\u202E\\u2066-\\u2069]");

    private TextoLivre() {
    }

    /** Valor nulo passa: a obrigatoriedade é conferida por quem chama. */
    public static void exigirUmaLinha(String valor, String campo) {
        if (valor != null && CONTROLE_EM_UMA_LINHA.matcher(valor).find()) {
            throw new ValidacaoDeDominioException(
                    "O campo " + campo + " não aceita quebra de linha nem caracteres de controle.");
        }
    }

    /** Como {@link #exigirUmaLinha}, mas aceita quebra de linha e tabulação. */
    public static void exigirSemControle(String valor, String campo) {
        if (valor != null && CONTROLE_EM_VARIAS_LINHAS.matcher(valor).find()) {
            throw new ValidacaoDeDominioException("O campo " + campo + " não aceita caracteres de controle.");
        }
    }
}

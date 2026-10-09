package br.com.fiap.restaurante.domain.valueobject;

import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;

import java.nio.charset.StandardCharsets;

/**
 * Senha digitada pelo usuário, antes de ser codificada. O limite de 72 vem do
 * BCrypt, que conta bytes e recusa o que passa disso: uma letra acentuada ocupa
 * dois.
 */
public record SenhaEmTexto(String valor) {

    public SenhaEmTexto {
        if (valor == null || valor.isBlank() || valor.length() < 8 || valor.length() > 72) {
            throw new ValidacaoDeDominioException("A senha deve ter entre 8 e 72 caracteres.");
        }
        if (valor.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ValidacaoDeDominioException(
                    "A senha deve ter no máximo 72 bytes. Letras acentuadas contam como dois.");
        }
    }

    @Override
    public String toString() {
        return "SenhaEmTexto[****]";
    }
}

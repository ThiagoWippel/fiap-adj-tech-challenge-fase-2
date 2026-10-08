package br.com.fiap.restaurante.domain.valueobject;

import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;

/**
 * Senha digitada pelo usuário, antes de ser codificada. O limite de 72
 * caracteres vem do BCrypt, que ignora o que passa disso.
 */
public record SenhaEmTexto(String valor) {

    public SenhaEmTexto {
        if (valor == null || valor.isBlank() || valor.length() < 8 || valor.length() > 72) {
            throw new ValidacaoDeDominioException("A senha deve ter entre 8 e 72 caracteres.");
        }
    }

    @Override
    public String toString() {
        return "SenhaEmTexto[****]";
    }
}

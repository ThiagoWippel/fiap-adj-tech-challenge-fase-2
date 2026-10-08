package br.com.fiap.restaurante.application.port;

/**
 * Codificação e conferência de senhas. Os casos de uso nunca gravam a senha em
 * texto.
 */
public interface IPasswordEncoder {

    String codificar(String senha);

    boolean confere(String senha, String senhaCodificada);
}

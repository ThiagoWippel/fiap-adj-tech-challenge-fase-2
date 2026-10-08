package br.com.fiap.restaurante.application.port;

/**
 * Codificação e conferência de senhas. Os casos de uso nunca gravam a senha em
 * texto.
 */
public interface IPasswordEncoder {

    String codificar(String senha);

    /**
     * Com {@code senhaCodificada} nula (login inexistente), faz uma comparação de
     * descarte e devolve false. Assim o login inexistente leva o mesmo tempo que
     * uma senha errada, e o tempo de resposta não revela quais logins existem.
     */
    boolean confere(String senha, String senhaCodificada);
}

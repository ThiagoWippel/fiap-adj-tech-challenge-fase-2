package br.com.fiap.restaurante.application.exception;

/**
 * Lançada por um caso de uso quando a operação conflita com o estado atual dos
 * dados: um e-mail já cadastrado, um tipo de usuário em uso, um dono que não é
 * do tipo Dono de Restaurante.
 */
public class ConflitoDeDadosException extends RuntimeException {

    public ConflitoDeDadosException(String mensagem) {
        super(mensagem);
    }
}

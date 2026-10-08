package br.com.fiap.restaurante.application.exception;

/**
 * A operação conflita com os dados atuais: e-mail já cadastrado, tipo de usuário
 * em uso, dono que não é do tipo Dono de Restaurante etc.
 */
public class ConflitoDeDadosException extends RuntimeException {

    public ConflitoDeDadosException(String mensagem) {
        super(mensagem);
    }
}

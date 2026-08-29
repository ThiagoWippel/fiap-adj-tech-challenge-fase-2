package br.com.fiap.restaurante.services.exceptions;

/**
 * A requisição é válida, mas conflita com o estado atual do banco.
 * Mapeada para HTTP 409.
 *
 * Usada para violações de unicidade: e-mail, login, CPF ou CNPJ já cadastrados.
 */
public class ConflitoDeDadosException extends RuntimeException {

    public ConflitoDeDadosException(String mensagem) {
        super(mensagem);
    }
}

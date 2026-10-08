package br.com.fiap.restaurante.interfaceadapter.presenter;

/**
 * Endereço na resposta da API, com o CEP só com dígitos.
 */
public record EnderecoResponse(String rua, String numero, String complemento, String bairro, String cidade,
                               String estado, String cep) {
}

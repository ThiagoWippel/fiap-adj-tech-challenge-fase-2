package br.com.fiap.restaurante.interfaceadapter.datasource;

/**
 * Endereço no formato trocado com a origem de dados, com o CEP só com dígitos.
 */
public record DadosEndereco(String rua, String numero, String complemento, String bairro, String cidade,
                            String estado, String cep) {
}

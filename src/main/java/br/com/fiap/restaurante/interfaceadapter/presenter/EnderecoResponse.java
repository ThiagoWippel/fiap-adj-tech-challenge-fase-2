package br.com.fiap.restaurante.interfaceadapter.presenter;

import br.com.fiap.restaurante.domain.valueobject.Endereco;

/**
 * Endereço na resposta da API, com o CEP só com dígitos.
 */
public record EnderecoResponse(String rua, String numero, String complemento, String bairro, String cidade,
                               String estado, String cep) {

    static EnderecoResponse de(Endereco endereco) {
        return new EnderecoResponse(endereco.rua(), endereco.numero(), endereco.complemento(), endereco.bairro(),
                endereco.cidade(), endereco.estado(), endereco.cep());
    }
}

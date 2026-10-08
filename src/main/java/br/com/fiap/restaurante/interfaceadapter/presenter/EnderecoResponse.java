package br.com.fiap.restaurante.interfaceadapter.presenter;

public record EnderecoResponse(String rua, String numero, String complemento, String bairro, String cidade,
                               String estado, String cep) {
}

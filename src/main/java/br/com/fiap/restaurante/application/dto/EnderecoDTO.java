package br.com.fiap.restaurante.application.dto;

import br.com.fiap.restaurante.domain.valueobject.Endereco;

/**
 * Endereço como chega ao caso de uso, ainda sem validação. O objeto de valor
 * criado por {@link #paraEndereco()} é quem valida.
 */
public record EnderecoDTO(String rua, String numero, String complemento, String bairro, String cidade,
                          String estado, String cep) {

    public Endereco paraEndereco() {
        return new Endereco(rua, numero, complemento, bairro, cidade, estado, cep);
    }
}

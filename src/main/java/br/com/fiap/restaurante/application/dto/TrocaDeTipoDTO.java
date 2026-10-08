package br.com.fiap.restaurante.application.dto;

/**
 * Troca de tipo de um usuário: o código do novo tipo e o documento que ele exige,
 * CNPJ para DONO_RESTAURANTE e CPF para os demais.
 */
public record TrocaDeTipoDTO(Long id, String tipo, String documento) {
}

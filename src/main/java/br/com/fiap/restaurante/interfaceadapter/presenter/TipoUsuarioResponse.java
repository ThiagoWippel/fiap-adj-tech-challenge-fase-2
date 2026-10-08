package br.com.fiap.restaurante.interfaceadapter.presenter;

/**
 * Tipo de usuário na resposta da API. {@code sistema} indica os tipos Cliente e
 * Dono de Restaurante, que não podem ser excluídos.
 */
public record TipoUsuarioResponse(Long id, String nome, String codigo, boolean sistema) {
}

package br.com.fiap.restaurante.application.dto;

/**
 * Dados da troca de senha: o usuário, a senha atual, conferida antes da troca, e a
 * nova senha.
 */
public record TrocaDeSenhaDTO(Long id, String senhaAtual, String novaSenha) {
}

package br.com.fiap.restaurante.application.dto;

/**
 * Dados da atualização de usuário. Senha e tipo ficam de fora: têm operações
 * próprias.
 */
public record AtualizacaoDeUsuarioDTO(Long id, String nome, String email, String login, EnderecoDTO endereco) {
}

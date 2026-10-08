package br.com.fiap.restaurante.application.dto;

public record AtualizacaoDeUsuarioDTO(Long id, String nome, String email, String login, EnderecoDTO endereco) {
}

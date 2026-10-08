package br.com.fiap.restaurante.application.dto;

/**
 * Dados do cadastro de usuário. {@code tipo} é o código do tipo (CLIENTE,
 * DONO_RESTAURANTE...). CPF e CNPJ vêm em campos separados, como na Fase 1, e
 * só o exigido pelo tipo é usado.
 */
public record NovoUsuarioDTO(String nome, String email, String login, String senha, String tipo,
                             String cpf, String cnpj, EnderecoDTO endereco) {
}

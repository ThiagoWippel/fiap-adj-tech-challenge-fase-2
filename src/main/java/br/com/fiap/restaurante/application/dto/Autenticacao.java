package br.com.fiap.restaurante.application.dto;

import br.com.fiap.restaurante.domain.entity.Usuario;

/**
 * Resultado do login: o usuário autenticado e o token emitido para ele.
 */
public record Autenticacao(Usuario usuario, TokenDeAcesso token) {
}

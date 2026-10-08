package br.com.fiap.restaurante.application.dto;

import br.com.fiap.restaurante.domain.entity.Usuario;

public record Autenticacao(Usuario usuario, TokenDeAcesso token) {
}

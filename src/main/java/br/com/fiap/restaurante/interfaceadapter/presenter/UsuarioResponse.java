package br.com.fiap.restaurante.interfaceadapter.presenter;

import java.time.LocalDateTime;

/**
 * Dados públicos do usuário, no mesmo formato da Fase 1. Não existe campo de
 * senha. {@code tipo} é o código do tipo.
 */
public record UsuarioResponse(Long id, String nome, String email, String login, String tipo, String documento,
                              EnderecoResponse endereco, LocalDateTime dataCriacao,
                              LocalDateTime dataUltimaAlteracao) {
}

package br.com.fiap.restaurante.interfaceadapter.presenter;

import java.time.LocalDateTime;

/**
 * Resposta do login: quem autenticou e o token. E-mail, documento e endereço
 * ficam de fora, como na Fase 1.
 */
public record LoginResponse(Long id, String nome, String tipo, String token, LocalDateTime expiraEm) {
}

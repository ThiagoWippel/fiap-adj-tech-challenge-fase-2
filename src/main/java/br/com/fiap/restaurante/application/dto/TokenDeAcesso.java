package br.com.fiap.restaurante.application.dto;

import java.time.LocalDateTime;

/**
 * Token emitido no login e o momento em que ele expira.
 */
public record TokenDeAcesso(String valor, LocalDateTime expiraEm) {
}

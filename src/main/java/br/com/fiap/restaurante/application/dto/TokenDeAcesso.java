package br.com.fiap.restaurante.application.dto;

import java.time.LocalDateTime;

public record TokenDeAcesso(String valor, LocalDateTime expiraEm) {
}

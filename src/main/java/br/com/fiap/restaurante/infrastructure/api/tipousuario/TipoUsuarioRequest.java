package br.com.fiap.restaurante.infrastructure.api.tipousuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corpo do cadastro e da renomeação de tipo de usuário: só o nome. O código é
 * gerado no cadastro e não muda depois.
 */
public record TipoUsuarioRequest(
        @Schema(example = "Entregador")
        @NotBlank(message = "O nome do tipo é obrigatório.")
        @Size(min = 3, max = 50, message = "O nome do tipo deve ter entre 3 e 50 caracteres.")
        String nome) {
}

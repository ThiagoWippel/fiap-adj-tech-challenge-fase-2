package br.com.fiap.restaurante.infrastructure.api.autenticacao;

import br.com.fiap.restaurante.application.dto.CredenciaisDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corpo da requisição de login.
 */
public record LoginRequest(
        @Schema(example = "maria.silva")
        @NotBlank(message = "O login é obrigatório.")
        @Size(max = 50, message = "O login deve ter no máximo 50 caracteres.")
        String login,

        @Schema(example = "SenhaSegura123")
        @NotBlank(message = "A senha é obrigatória.")
        @Size(max = 72, message = "A senha deve ter no máximo 72 caracteres.")
        String senha) {

    public CredenciaisDTO paraDTO() {
        return new CredenciaisDTO(login, senha);
    }
}

package br.com.fiap.restaurante.infrastructure.api.autenticacao;

import br.com.fiap.restaurante.application.dto.CredenciaisDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "maria.silva")
        @NotBlank(message = "O login é obrigatório.")
        String login,

        @Schema(example = "SenhaSegura123")
        @NotBlank(message = "A senha é obrigatória.")
        String senha) {

    public CredenciaisDTO paraDTO() {
        return new CredenciaisDTO(login, senha);
    }
}

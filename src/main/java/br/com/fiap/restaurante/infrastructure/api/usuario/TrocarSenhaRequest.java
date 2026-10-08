package br.com.fiap.restaurante.infrastructure.api.usuario;

import br.com.fiap.restaurante.application.dto.TrocaDeSenhaDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TrocarSenhaRequest(
        @Schema(example = "SenhaSegura123")
        @NotBlank(message = "A senha atual é obrigatória.")
        String senhaAtual,

        @Schema(example = "SenhaNova456")
        @NotBlank(message = "A nova senha é obrigatória.")
        @Size(min = 8, max = 72, message = "A nova senha deve ter entre 8 e 72 caracteres.")
        String novaSenha) {

    public TrocaDeSenhaDTO paraDTO(Long id) {
        return new TrocaDeSenhaDTO(id, senhaAtual, novaSenha);
    }
}

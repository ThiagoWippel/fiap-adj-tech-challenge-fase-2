package br.com.fiap.restaurante.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados para troca de senha.
 *
 * A senha atual é exigida como proteção contra alteração indevida em sessão
 * deixada aberta. O enunciado não menciona essa exigência - trata-se de
 * decisão de segurança, documentada no Swagger e na coleção Postman para que
 * quem for testar o endpoint saiba do requisito.
 */
@Schema(description = "Dados para troca de senha")
public record TrocarSenhaRequest(

        @Schema(example = "SenhaAntiga123")
        @NotBlank(message = "A senha atual e obrigatoria")
        String senhaAtual,

        @Schema(example = "SenhaNova456")
        @NotBlank(message = "A nova senha e obrigatoria")
        @Size(min = 8, max = 72, message = "A nova senha deve ter entre 8 e 72 caracteres")
        String novaSenha
) {
}

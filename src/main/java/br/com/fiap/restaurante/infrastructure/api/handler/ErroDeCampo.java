package br.com.fiap.restaurante.infrastructure.api.handler;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Campo rejeitado na validação, listado na extensão {@code erros} da resposta.
 */
@Schema(description = "Falha de validação em um campo")
public record ErroDeCampo(

        @Schema(example = "email")
        String campo,

        @Schema(example = "O e-mail informado não é válido.")
        String mensagem) {
}

package br.com.fiap.restaurante.infrastructure.api.handler;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Falha de validação em um campo, na extensão {@code erros} da resposta.
 *
 * <p>Sem ela, uma requisição com três campos inválidos teria de resumir tudo numa
 * frase, e o cliente não saberia quais campos corrigir.
 */
@Schema(description = "Falha de validação em um campo")
public record ErroDeCampo(

        @Schema(example = "email")
        String campo,

        @Schema(example = "O e-mail informado não é válido.")
        String mensagem) {
}

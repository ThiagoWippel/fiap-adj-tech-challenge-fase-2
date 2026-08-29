package br.com.fiap.restaurante.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Detalhe de uma falha de validação em um campo específico.
 *
 * A RFC 7807 define cinco campos padrão e permite extensões. Este objeto compõe
 * a extensão "erros" da resposta: sem ele, uma requisição com três campos
 * inválidos teria de espremer tudo em uma única frase, e o cliente não
 * conseguiria destacar quais campos corrigir.
 */
@Schema(description = "Falha de validacao em um campo")
public record ErroDeCampo(

        @Schema(example = "email")
        String campo,

        @Schema(example = "O e-mail informado nao e valido")
        String mensagem
) {
}

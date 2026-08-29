package br.com.fiap.restaurante.dtos.response;

import br.com.fiap.restaurante.entities.TipoUsuario;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resposta da validação de login.
 *
 * Devolve apenas o mínimo para identificar quem autenticou. Não é uma consulta
 * de perfil: e-mail e endereço ficam de fora porque a operação responde "estas
 * credenciais conferem?", não "quem é este usuário?".
 */
@Schema(description = "Resultado da validacao de credenciais")
public record LoginResponse(

        @Schema(example = "1")
        Long id,

        @Schema(example = "Maria Silva")
        String nome,

        @Schema(example = "CLIENTE")
        TipoUsuario tipo
) {
}

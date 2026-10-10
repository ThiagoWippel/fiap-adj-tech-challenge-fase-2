package br.com.fiap.restaurante.infrastructure.api.usuario;

import br.com.fiap.restaurante.application.dto.TrocaDeTipoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corpo da troca de tipo: o código do novo tipo e o documento que ele exige. Se
 * o documento é CPF ou CNPJ sai do tamanho, sem a máscara: 11 ou 14 caracteres.
 */
public record TrocarTipoRequest(
        @Schema(example = "DONO_RESTAURANTE", description = "Código do novo tipo")
        @NotBlank(message = "O tipo é obrigatório.")
        @Size(max = 50, message = "O tipo deve ter no máximo 50 caracteres.")
        String tipo,

        @Schema(example = "12.ABC.345/01DE-35",
                description = "CNPJ para DONO_RESTAURANTE, CPF para os demais. O CNPJ pode ser alfanumérico.")
        @NotBlank(message = "O documento é obrigatório.")
        @Size(max = 18, message = "O documento deve ter no máximo 18 caracteres, contando a máscara.")
        String documento) {

    public TrocaDeTipoDTO paraDTO(Long id) {
        return new TrocaDeTipoDTO(id, tipo, documento);
    }
}

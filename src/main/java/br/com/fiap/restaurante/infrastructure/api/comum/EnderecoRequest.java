package br.com.fiap.restaurante.infrastructure.api.comum;

import br.com.fiap.restaurante.application.dto.EnderecoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Endereço recebido pela API. Aqui se confere presença e formato; a lista de UFs
 * e a normalização do CEP ficam no objeto de valor do domínio.
 */
public record EnderecoRequest(
        @Schema(example = "Rua das Flores")
        @NotBlank(message = "A rua é obrigatória.")
        @Size(max = 150, message = "A rua deve ter no máximo 150 caracteres.")
        String rua,

        @Schema(example = "123", description = "Aceita valores como S/N e 123-A")
        @NotBlank(message = "O número é obrigatório.")
        @Size(max = 10, message = "O número deve ter no máximo 10 caracteres.")
        String numero,

        @Schema(example = "Apto 45")
        @Size(max = 60, message = "O complemento deve ter no máximo 60 caracteres.")
        String complemento,

        @Schema(example = "Centro")
        @NotBlank(message = "O bairro é obrigatório.")
        @Size(max = 80, message = "O bairro deve ter no máximo 80 caracteres.")
        String bairro,

        @Schema(example = "Itajaí")
        @NotBlank(message = "A cidade é obrigatória.")
        @Size(max = 80, message = "A cidade deve ter no máximo 80 caracteres.")
        String cidade,

        @Schema(example = "SC")
        @NotBlank(message = "O estado é obrigatório.")
        @Pattern(regexp = "[A-Za-z]{2}", message = "O estado deve ser a sigla da UF, como SC.")
        String estado,

        @Schema(example = "88301-000", description = "Com ou sem hífen")
        @NotBlank(message = "O CEP é obrigatório.")
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "O CEP deve ter 8 dígitos.")
        String cep) {

    public EnderecoDTO paraDTO() {
        return new EnderecoDTO(rua, numero, complemento, bairro, cidade, estado, cep);
    }
}

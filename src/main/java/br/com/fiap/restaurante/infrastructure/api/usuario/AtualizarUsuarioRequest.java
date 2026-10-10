package br.com.fiap.restaurante.infrastructure.api.usuario;

import br.com.fiap.restaurante.application.dto.AtualizacaoDeUsuarioDTO;
import br.com.fiap.restaurante.infrastructure.api.comum.EnderecoRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Atualização de dados. Senha e tipo têm endpoints próprios.
 */
public record AtualizarUsuarioRequest(
        @Schema(example = "Laura Mendes Costa")
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 3, max = 120, message = "O nome deve ter entre 3 e 120 caracteres.")
        String nome,

        @Schema(example = "laura.costa@exemplo.com")
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail informado não é válido.")
        @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres.")
        String email,

        @Schema(example = "laura.costa")
        @NotBlank(message = "O login é obrigatório.")
        @Pattern(regexp = "[A-Za-z0-9._-]{4,50}",
                message = "O login deve ter de 4 a 50 caracteres: letras, números, ponto, hífen ou sublinhado.")
        String login,

        @NotNull(message = "O endereço é obrigatório.")
        @Valid
        EnderecoRequest endereco) {

    public AtualizacaoDeUsuarioDTO paraDTO(Long id) {
        return new AtualizacaoDeUsuarioDTO(id, nome, email, login, endereco.paraDTO());
    }
}

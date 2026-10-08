package br.com.fiap.restaurante.infrastructure.api.usuario;

import br.com.fiap.restaurante.application.dto.NovoUsuarioDTO;
import br.com.fiap.restaurante.infrastructure.api.comum.EnderecoRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;

/**
 * Cadastro de usuário, com os mesmos campos da Fase 1. O tipo define qual
 * documento é exigido: CNPJ para DONO_RESTAURANTE, CPF para os demais.
 */
public record CriarUsuarioRequest(
        @Schema(example = "Maria Silva")
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 3, max = 120, message = "O nome deve ter entre 3 e 120 caracteres.")
        String nome,

        @Schema(example = "maria.silva@exemplo.com")
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail informado não é válido.")
        @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres.")
        String email,

        @Schema(example = "maria.silva")
        @NotBlank(message = "O login é obrigatório.")
        @Pattern(regexp = "[A-Za-z0-9._-]{4,50}",
                message = "O login deve ter de 4 a 50 caracteres: letras, números, ponto, hífen ou sublinhado.")
        String login,

        @Schema(example = "SenhaSegura123")
        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
        String senha,

        @Schema(example = "CLIENTE", description = "Código do tipo de usuário")
        @NotBlank(message = "O tipo do usuário é obrigatório.")
        String tipo,

        @Schema(example = "123.456.789-09", description = "Obrigatório para todos os tipos, menos DONO_RESTAURANTE")
        @CPF(message = "O CPF informado não é válido.")
        String cpf,

        @Schema(example = "11.222.333/0001-81", description = "Obrigatório para DONO_RESTAURANTE")
        @CNPJ(message = "O CNPJ informado não é válido.")
        String cnpj,

        @NotNull(message = "O endereço é obrigatório.")
        @Valid
        EnderecoRequest endereco) {

    public NovoUsuarioDTO paraDTO() {
        return new NovoUsuarioDTO(nome, email, login, senha, tipo, cpf, cnpj, endereco.paraDTO());
    }
}

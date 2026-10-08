package br.com.fiap.restaurante.infrastructure.api.restaurante;

import br.com.fiap.restaurante.application.dto.AtualizacaoDeRestauranteDTO;
import br.com.fiap.restaurante.application.dto.NovoRestauranteDTO;
import br.com.fiap.restaurante.application.dto.TurnoDTO;
import br.com.fiap.restaurante.infrastructure.api.comum.EnderecoRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Corpo do cadastro e da atualização de restaurante. Na atualização, os turnos
 * substituem os anteriores e um {@code donoId} diferente transfere o restaurante.
 */
public record RestauranteRequest(
        @Schema(example = "Cantina da Nona")
        @NotBlank(message = "O nome do restaurante é obrigatório.")
        @Size(min = 2, max = 120, message = "O nome do restaurante deve ter entre 2 e 120 caracteres.")
        String nome,

        @NotNull(message = "O endereço é obrigatório.")
        @Valid
        EnderecoRequest endereco,

        @Schema(example = "ITALIANA", description = "BRASILEIRA, ITALIANA, PIZZARIA, JAPONESA, CHINESA, ARABE, "
                + "MEXICANA, PORTUGUESA, FRANCESA, HAMBURGUERIA, LANCHES, CHURRASCARIA, FRUTOS_DO_MAR, VEGETARIANA, "
                + "VEGANA, DOCES_E_SOBREMESAS, CAFETERIA ou OUTRA")
        @NotBlank(message = "O tipo de cozinha é obrigatório.")
        String tipoCozinha,

        @Schema(example = "7", description = "Usuário do tipo Dono de Restaurante")
        @NotNull(message = "O dono do restaurante é obrigatório.")
        Long donoId,

        @NotEmpty(message = "Informe ao menos um turno de funcionamento.")
        @Valid
        List<TurnoRequest> horarios) {

    public NovoRestauranteDTO paraNovoDTO() {
        return new NovoRestauranteDTO(nome, endereco.paraDTO(), tipoCozinha, turnos(), donoId);
    }

    public AtualizacaoDeRestauranteDTO paraAtualizacaoDTO(Long id) {
        return new AtualizacaoDeRestauranteDTO(id, nome, endereco.paraDTO(), tipoCozinha, turnos(), donoId);
    }

    private List<TurnoDTO> turnos() {
        return horarios.stream().map(TurnoRequest::paraDTO).toList();
    }
}

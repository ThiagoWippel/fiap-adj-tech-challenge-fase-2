package br.com.fiap.restaurante.infrastructure.api.itemcardapio;

import br.com.fiap.restaurante.application.dto.AtualizacaoDeItemCardapioDTO;
import br.com.fiap.restaurante.application.dto.NovoItemCardapioDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Corpo do cadastro e da atualização de item. O restaurante vem da rota.
 */
public record ItemCardapioRequest(
        @Schema(example = "Feijoada", description = "Espaços sobrando são removidos")
        @NotBlank(message = "O nome do item é obrigatório.")
        @Size(max = 100, message = "O nome do item deve ter entre 2 e 100 caracteres.")
        String nome,

        @Schema(example = "Feijoada completa com farofa, couve e laranja.")
        @NotBlank(message = "A descrição do item é obrigatória.")
        @Size(max = 500, message = "A descrição do item deve ter no máximo 500 caracteres.")
        String descricao,

        @Schema(example = "39.90", description = "Maior que zero, com no máximo duas casas decimais")
        @NotNull(message = "O preço é obrigatório.")
        @DecimalMin(value = "0", inclusive = false, message = "O preço deve ser maior que zero.")
        @DecimalMax(value = "99999999.99", message = "O preço deve ser de no máximo 99.999.999,99.")
        @Digits(integer = 8, fraction = 2, message = "O preço deve ter no máximo duas casas decimais.")
        BigDecimal preco,

        @Schema(example = "false", description = "Disponível para pedir só no restaurante. Sem valor padrão.")
        @NotNull(message = "Informe se o item está disponível só para consumo no local.")
        Boolean apenasNoLocal,

        @Schema(example = "fotos/feijoada.jpg", description = "Caminho relativo ou URL de uma imagem")
        @NotBlank(message = "O caminho da foto é obrigatório.")
        @Size(max = 255, message = "O caminho da foto deve ter no máximo 255 caracteres.")
        @Pattern(regexp = "(?i).+\\.(jpg|jpeg|png|webp)",
                message = "O caminho da foto deve terminar em .jpg, .jpeg, .png ou .webp.")
        String caminhoFoto) {

    public NovoItemCardapioDTO paraNovoDTO(Long restauranteId) {
        return new NovoItemCardapioDTO(restauranteId, nome, descricao, preco, apenasNoLocal, caminhoFoto);
    }

    public AtualizacaoDeItemCardapioDTO paraAtualizacaoDTO(Long restauranteId, Long itemId) {
        return new AtualizacaoDeItemCardapioDTO(restauranteId, itemId, nome, descricao, preco, apenasNoLocal,
                caminhoFoto);
    }
}

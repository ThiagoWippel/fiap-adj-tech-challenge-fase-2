package br.com.fiap.restaurante.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Envelope de resultados paginados.
 *
 * Existe para não serializar diretamente o objeto Page do Spring Data, cujo
 * formato JSON não tem estabilidade garantida entre versões. Declarando a
 * estrutura aqui, o contrato da API deixa de depender de detalhe interno do
 * framework.
 */
@Schema(description = "Resultado paginado")
public record PaginaResponse<T>(

        List<T> conteudo,

        @Schema(example = "0")
        int pagina,

        @Schema(example = "10")
        int tamanho,

        @Schema(example = "42")
        long totalElementos,

        @Schema(example = "5")
        int totalPaginas,

        @Schema(example = "false")
        boolean ultima
) {

    public static <T> PaginaResponse<T> de(Page<T> pagina) {
        return new PaginaResponse<>(
                pagina.getContent(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages(),
                pagina.isLast()
        );
    }
}

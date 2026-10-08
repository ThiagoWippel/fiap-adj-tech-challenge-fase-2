package br.com.fiap.restaurante.infrastructure.api.problema;

/**
 * Descrição de um tipo de problema, devolvida em /problemas/{identificador}, o
 * endereço que aparece no campo {@code type} das respostas de erro.
 */
public record TipoDeProblemaResponse(String identificador, String titulo, int status, String descricao) {

    public static TipoDeProblemaResponse de(TipoDeProblema tipo) {
        return new TipoDeProblemaResponse(tipo.identificador(), tipo.titulo(), tipo.status(), tipo.descricao());
    }
}

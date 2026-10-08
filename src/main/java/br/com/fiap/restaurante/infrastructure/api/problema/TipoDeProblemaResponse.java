package br.com.fiap.restaurante.infrastructure.api.problema;

public record TipoDeProblemaResponse(String identificador, String titulo, int status, String descricao) {

    public static TipoDeProblemaResponse de(TipoDeProblema tipo) {
        return new TipoDeProblemaResponse(tipo.identificador(), tipo.titulo(), tipo.status(), tipo.descricao());
    }
}

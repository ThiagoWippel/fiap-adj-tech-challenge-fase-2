package br.com.fiap.restaurante.infrastructure.api.problema;

import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

/**
 * Catálogo dos tipos de problema que a API devolve.
 *
 * <p>Cada tipo tem um identificador estável, usado no campo {@code type} das
 * respostas de erro, um título fixo, o status HTTP e uma descrição que a
 * aplicação serve em {@code /problemas/{identificador}}. O título é constante
 * para o tipo; o que muda de uma ocorrência para outra vai no {@code detail}.
 */
public enum TipoDeProblema {

    DADOS_INVALIDOS("dados-invalidos", "Dados inválidos", 400,
            "Um ou mais dados enviados não respeitam as regras de formato ou de valor. "
                    + "Quando a falha vem da validação dos campos, a extensão erros lista cada campo rejeitado."),

    REGRA_DE_NEGOCIO("regra-de-negocio", "Regra de negócio violada", 400,
            "Os dados estão bem formados, mas a combinação enviada viola uma regra de negócio, "
                    + "como um documento incompatível com o tipo do usuário."),

    REQUISICAO_INVALIDA("requisicao-invalida", "Requisição inválida", 400,
            "A requisição não pôde ser interpretada: JSON malformado, parâmetro obrigatório ausente "
                    + "ou valor de tipo errado."),

    CREDENCIAIS_INVALIDAS("credenciais-invalidas", "Credenciais inválidas", 401,
            "Login e senha não conferem. A mensagem é a mesma para login inexistente e senha errada, "
                    + "para não revelar quais logins existem."),

    RECURSO_NAO_ENCONTRADO("recurso-nao-encontrado", "Recurso não encontrado", 404,
            "O recurso pedido não existe, foi removido ou a rota chamada não existe."),

    METODO_NAO_PERMITIDO("metodo-nao-permitido", "Método não permitido", 405,
            "A rota existe, mas não aceita o método HTTP usado na requisição."),

    MIDIA_NAO_SUPORTADA("midia-nao-suportada", "Tipo de mídia não suportado", 415,
            "O corpo da requisição foi enviado num formato que a rota não aceita. Envie application/json."),

    CONFLITO_DE_DADOS("conflito-de-dados", "Conflito de dados", 409,
            "A operação conflita com o estado atual dos dados: um valor que precisa ser único já está em uso, "
                    + "ou o registro está ligado a outro que impede a operação."),

    ERRO_INTERNO("erro-interno", "Erro interno", 500,
            "Uma falha inesperada impediu a requisição de ser concluída. O detalhe fica registrado no log "
                    + "da aplicação, com o mesmo momento informado na resposta.");

    private static final String CAMINHO = "/problemas/{identificador}";

    private final String identificador;
    private final String titulo;
    private final int status;
    private final String descricao;

    TipoDeProblema(String identificador, String titulo, int status, String descricao) {
        this.identificador = identificador;
        this.titulo = titulo;
        this.status = status;
        this.descricao = descricao;
    }

    public String identificador() {
        return identificador;
    }

    public String titulo() {
        return titulo;
    }

    public int status() {
        return status;
    }

    public String descricao() {
        return descricao;
    }

    /** Endereço absoluto deste tipo de problema, a partir da base configurada. */
    public URI uri(URI base) {
        return UriComponentsBuilder.fromUri(base).path(CAMINHO).build(identificador);
    }

    public static Optional<TipoDeProblema> porIdentificador(String identificador) {
        return Arrays.stream(values())
                .filter(tipo -> tipo.identificador.equals(identificador))
                .findFirst();
    }

    /**
     * Tipo usado para os erros gerados pelo próprio Spring, que só informam o
     * status. Cada status do framework tem um tipo; os demais caem no tipo
     * genérico da sua faixa.
     */
    public static TipoDeProblema paraErroDoFramework(int status) {
        return switch (status) {
            case 404 -> RECURSO_NAO_ENCONTRADO;
            case 405 -> METODO_NAO_PERMITIDO;
            case 415 -> MIDIA_NAO_SUPORTADA;
            default -> status >= 500 ? ERRO_INTERNO : REQUISICAO_INVALIDA;
        };
    }
}

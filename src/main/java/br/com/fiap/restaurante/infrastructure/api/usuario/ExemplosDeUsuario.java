package br.com.fiap.restaurante.infrastructure.api.usuario;

/**
 * Exemplos de resposta de usuário usados na documentação OpenAPI.
 */
final class ExemplosDeUsuario {

    static final String USUARIO = """
            {
              "id": 1,
              "nome": "Maria Silva",
              "email": "maria.silva@exemplo.com",
              "login": "maria.silva",
              "tipo": "CLIENTE",
              "documento": "12345678909",
              "endereco": {
                "rua": "Rua das Flores",
                "numero": "123",
                "complemento": "Apto 45",
                "bairro": "Centro",
                "cidade": "Itajaí",
                "estado": "SC",
                "cep": "88301000"
              },
              "dataCriacao": "2026-10-08T10:30:00",
              "dataUltimaAlteracao": "2026-10-08T10:30:00"
            }""";

    static final String LISTA = "[" + USUARIO + "]";

    static final String PAGINA = "{\"conteudo\": " + LISTA + """
            ,
              "pagina": 0,
              "tamanho": 10,
              "totalElementos": 1,
              "totalPaginas": 1,
              "ultima": true
            }""";

    private ExemplosDeUsuario() {
    }
}

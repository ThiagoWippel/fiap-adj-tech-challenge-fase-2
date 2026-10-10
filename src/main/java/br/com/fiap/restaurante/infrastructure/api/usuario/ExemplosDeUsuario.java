package br.com.fiap.restaurante.infrastructure.api.usuario;

/**
 * Exemplos de resposta de usuário usados na documentação OpenAPI.
 */
final class ExemplosDeUsuario {

    static final String USUARIO = """
            {
              "id": 1,
              "nome": "Laura Mendes",
              "email": "laura.mendes@exemplo.com",
              "login": "laura.mendes",
              "tipo": "CLIENTE",
              "documento": "24681357928",
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

    static final String USUARIO_DONO = """
            {
              "id": 1,
              "nome": "Laura Mendes",
              "email": "laura.mendes@exemplo.com",
              "login": "laura.mendes",
              "tipo": "DONO_RESTAURANTE",
              "documento": "12ABC34501DE35",
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
              "dataUltimaAlteracao": "2026-10-09T08:15:00"
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

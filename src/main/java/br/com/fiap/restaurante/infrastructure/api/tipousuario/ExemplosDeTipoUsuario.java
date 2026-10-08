package br.com.fiap.restaurante.infrastructure.api.tipousuario;

/**
 * Exemplos de resposta de tipo de usuário usados na documentação OpenAPI.
 */
final class ExemplosDeTipoUsuario {

    static final String TIPO = """
            {
              "id": 3,
              "nome": "Entregador",
              "codigo": "ENTREGADOR",
              "sistema": false
            }""";

    static final String PAGINA = """
            {
              "conteudo": [
                { "id": 1, "nome": "Cliente", "codigo": "CLIENTE", "sistema": true },
                { "id": 2, "nome": "Dono de Restaurante", "codigo": "DONO_RESTAURANTE", "sistema": true },
                { "id": 3, "nome": "Entregador", "codigo": "ENTREGADOR", "sistema": false }
              ],
              "pagina": 0,
              "tamanho": 10,
              "totalElementos": 3,
              "totalPaginas": 1,
              "ultima": true
            }""";

    static final String PAGINA_DE_USUARIOS = """
            {
              "conteudo": [
                {
                  "id": 7,
                  "nome": "Bruno Lima",
                  "email": "bruno@exemplo.com",
                  "login": "bruno.lima",
                  "tipo": "ENTREGADOR",
                  "documento": "52998224725",
                  "endereco": {
                    "rua": "Rua das Flores",
                    "numero": "123",
                    "complemento": null,
                    "bairro": "Centro",
                    "cidade": "Itajaí",
                    "estado": "SC",
                    "cep": "88301000"
                  },
                  "dataCriacao": "2026-10-08T10:30:00",
                  "dataUltimaAlteracao": "2026-10-08T10:30:00"
                }
              ],
              "pagina": 0,
              "tamanho": 10,
              "totalElementos": 1,
              "totalPaginas": 1,
              "ultima": true
            }""";

    private ExemplosDeTipoUsuario() {
    }
}

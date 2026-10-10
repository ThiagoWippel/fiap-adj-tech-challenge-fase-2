package br.com.fiap.restaurante.infrastructure.api.restaurante;

/**
 * Exemplos de resposta de restaurante usados na documentação OpenAPI.
 */
public final class ExemplosDeRestaurante {

    public static final String RESTAURANTE = """
            {
              "id": 9,
              "nome": "Cantina da Nona",
              "endereco": {
                "rua": "Rua Hercílio Luz",
                "numero": "120",
                "complemento": null,
                "bairro": "Centro",
                "cidade": "Itajaí",
                "estado": "SC",
                "cep": "88301000"
              },
              "tipoCozinha": "ITALIANA",
              "horarios": [
                { "diaSemana": "SEGUNDA", "abertura": "11:00", "fechamento": "15:00" },
                { "diaSemana": "SEGUNDA", "abertura": "18:00", "fechamento": "23:00" },
                { "diaSemana": "SEXTA", "abertura": "18:00", "fechamento": "02:00" }
              ],
              "dono": { "id": 7, "nome": "Laura Mendes" },
              "dataCriacao": "2026-10-08T10:30:00",
              "dataUltimaAlteracao": "2026-10-08T10:30:00"
            }""";

    public static final String PAGINA = "{\"conteudo\": [" + RESTAURANTE + """
            ],
              "pagina": 0,
              "tamanho": 10,
              "totalElementos": 1,
              "totalPaginas": 1,
              "ultima": true
            }""";

    private ExemplosDeRestaurante() {
    }
}

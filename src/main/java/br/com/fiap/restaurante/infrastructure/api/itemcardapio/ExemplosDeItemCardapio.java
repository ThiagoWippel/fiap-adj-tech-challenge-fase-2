package br.com.fiap.restaurante.infrastructure.api.itemcardapio;

/**
 * Exemplos de resposta de item do cardápio usados na documentação OpenAPI.
 */
final class ExemplosDeItemCardapio {

    static final String ITEM = """
            {
              "id": 5,
              "restauranteId": 9,
              "nome": "Feijoada",
              "descricao": "Feijoada completa com farofa, couve e laranja.",
              "preco": 39.90,
              "apenasNoLocal": false,
              "caminhoFoto": "fotos/feijoada.jpg",
              "dataCriacao": "2026-10-08T10:30:00",
              "dataUltimaAlteracao": "2026-10-08T10:30:00"
            }""";

    static final String PAGINA = "{\"conteudo\": [" + ITEM + """
            ],
              "pagina": 0,
              "tamanho": 10,
              "totalElementos": 1,
              "totalPaginas": 1,
              "ultima": true
            }""";

    private ExemplosDeItemCardapio() {
    }
}

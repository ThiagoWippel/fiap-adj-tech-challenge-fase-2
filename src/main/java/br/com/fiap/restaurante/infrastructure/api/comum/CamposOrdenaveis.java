package br.com.fiap.restaurante.infrastructure.api.comum;

import java.util.Set;

/**
 * Campos aceitos no parâmetro sort de cada listagem: só os que a resposta mostra.
 */
public final class CamposOrdenaveis {

    public static final Set<String> USUARIO =
            Set.of("id", "nome", "email", "login", "dataCriacao", "dataUltimaAlteracao");
    public static final Set<String> TIPO_USUARIO = Set.of("id", "nome", "codigo");
    public static final Set<String> RESTAURANTE =
            Set.of("id", "nome", "tipoCozinha", "dataCriacao", "dataUltimaAlteracao");
    public static final Set<String> ITEM_CARDAPIO =
            Set.of("id", "nome", "preco", "dataCriacao", "dataUltimaAlteracao");

    private CamposOrdenaveis() {
    }
}

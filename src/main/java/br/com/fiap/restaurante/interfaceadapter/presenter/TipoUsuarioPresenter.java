package br.com.fiap.restaurante.interfaceadapter.presenter;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.domain.entity.TipoUsuario;

/**
 * Converte tipos de usuário na resposta da API.
 */
public final class TipoUsuarioPresenter {

    private TipoUsuarioPresenter() {
    }

    public static TipoUsuarioResponse paraResposta(TipoUsuario tipo) {
        return new TipoUsuarioResponse(tipo.getId(), tipo.getNome(), tipo.getCodigo(), tipo.ehDeSistema());
    }

    public static PaginaResponse<TipoUsuarioResponse> paraPagina(Pagina<TipoUsuario> pagina) {
        return new PaginaResponse<>(pagina.conteudo().stream().map(TipoUsuarioPresenter::paraResposta).toList(),
                pagina.numero(), pagina.tamanho(), pagina.totalElementos(), pagina.totalPaginas(), pagina.ultima());
    }
}

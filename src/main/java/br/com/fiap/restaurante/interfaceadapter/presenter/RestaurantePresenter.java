package br.com.fiap.restaurante.interfaceadapter.presenter;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.domain.entity.Restaurante;
import br.com.fiap.restaurante.domain.valueobject.Turno;

import java.time.format.DateTimeFormatter;

/**
 * Converte restaurantes na resposta da API.
 */
public final class RestaurantePresenter {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private RestaurantePresenter() {
    }

    public static RestauranteResponse paraResposta(Restaurante restaurante) {
        return new RestauranteResponse(restaurante.getId(), restaurante.getNome(),
                EnderecoResponse.de(restaurante.getEndereco()), restaurante.getTipoCozinha().name(),
                restaurante.getHorarios().turnos().stream().map(RestaurantePresenter::paraResposta).toList(),
                new DonoResponse(restaurante.getDono().getId(), restaurante.getDono().getNome()),
                UsuarioPresenter.emSegundos(restaurante.getDataCriacao()),
                UsuarioPresenter.emSegundos(restaurante.getDataUltimaAlteracao()));
    }

    public static PaginaResponse<RestauranteResponse> paraPagina(Pagina<Restaurante> pagina) {
        return new PaginaResponse<>(pagina.conteudo().stream().map(RestaurantePresenter::paraResposta).toList(),
                pagina.numero(), pagina.tamanho(), pagina.totalElementos(), pagina.totalPaginas(), pagina.ultima());
    }

    private static TurnoResponse paraResposta(Turno turno) {
        return new TurnoResponse(turno.dia().name(), turno.abertura().format(HORA), turno.fechamento().format(HORA));
    }
}

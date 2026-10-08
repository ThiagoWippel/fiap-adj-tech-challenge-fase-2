package br.com.fiap.restaurante.application.usecase.tipousuario;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.gateway.ITipoUsuarioGateway;
import br.com.fiap.restaurante.domain.entity.TipoUsuario;

/**
 * Lista os tipos de usuário, paginados.
 */
public class ListarTiposUsuarioUseCase {

    private final ITipoUsuarioGateway tipos;

    private ListarTiposUsuarioUseCase(ITipoUsuarioGateway tipos) {
        this.tipos = tipos;
    }

    public static ListarTiposUsuarioUseCase create(ITipoUsuarioGateway tipos) {
        return new ListarTiposUsuarioUseCase(tipos);
    }

    public Pagina<TipoUsuario> run(PedidoDePagina pedido) {
        return tipos.listar(pedido);
    }
}

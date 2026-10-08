package br.com.fiap.restaurante.application.usecase.tipousuario;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.gateway.ITipoUsuarioGateway;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.domain.entity.Usuario;

/**
 * Lista, paginados, os usuários ativos de um tipo.
 */
public class ListarUsuariosDoTipoUseCase {

    private final ITipoUsuarioGateway tipos;
    private final IUsuarioGateway usuarios;

    private ListarUsuariosDoTipoUseCase(ITipoUsuarioGateway tipos, IUsuarioGateway usuarios) {
        this.tipos = tipos;
        this.usuarios = usuarios;
    }

    public static ListarUsuariosDoTipoUseCase create(ITipoUsuarioGateway tipos, IUsuarioGateway usuarios) {
        return new ListarUsuariosDoTipoUseCase(tipos, usuarios);
    }

    public Pagina<Usuario> run(Long tipoId, PedidoDePagina pedido) {
        tipos.buscarPorId(tipoId).orElseThrow(() -> BuscarTipoUsuarioPorIdUseCase.tipoNaoEncontrado(tipoId));
        return usuarios.buscarPorTipo(tipoId, pedido);
    }
}

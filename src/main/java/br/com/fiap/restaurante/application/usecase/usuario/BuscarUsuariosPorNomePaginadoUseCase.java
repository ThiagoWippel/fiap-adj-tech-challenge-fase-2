package br.com.fiap.restaurante.application.usecase.usuario;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.domain.entity.Usuario;

/**
 * Busca por trecho do nome, paginada (contrato da v2).
 */
public class BuscarUsuariosPorNomePaginadoUseCase {

    private final IUsuarioGateway usuarios;

    private BuscarUsuariosPorNomePaginadoUseCase(IUsuarioGateway usuarios) {
        this.usuarios = usuarios;
    }

    public static BuscarUsuariosPorNomePaginadoUseCase create(IUsuarioGateway usuarios) {
        return new BuscarUsuariosPorNomePaginadoUseCase(usuarios);
    }

    public Pagina<Usuario> run(String nome, PedidoDePagina pedido) {
        return usuarios.buscarPorNome(BuscarUsuariosPorNomeUseCase.termo(nome), pedido);
    }
}

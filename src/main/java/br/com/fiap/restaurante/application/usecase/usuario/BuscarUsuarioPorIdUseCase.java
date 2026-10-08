package br.com.fiap.restaurante.application.usecase.usuario;

import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.domain.entity.Usuario;

/**
 * Consulta um usuário pelo id. Usuário removido é tratado como inexistente.
 */
public class BuscarUsuarioPorIdUseCase {

    private final IUsuarioGateway usuarios;

    private BuscarUsuarioPorIdUseCase(IUsuarioGateway usuarios) {
        this.usuarios = usuarios;
    }

    public static BuscarUsuarioPorIdUseCase create(IUsuarioGateway usuarios) {
        return new BuscarUsuarioPorIdUseCase(usuarios);
    }

    public Usuario run(Long id) {
        return usuarios.buscarPorId(id).orElseThrow(() -> usuarioNaoEncontrado(id));
    }

    public static RecursoNaoEncontradoException usuarioNaoEncontrado(Long id) {
        return new RecursoNaoEncontradoException("Usuário " + id + " não encontrado.");
    }
}

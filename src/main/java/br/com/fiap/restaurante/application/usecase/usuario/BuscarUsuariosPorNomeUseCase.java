package br.com.fiap.restaurante.application.usecase.usuario;

import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.domain.entity.Usuario;

import java.util.List;

/**
 * Busca por trecho do nome, devolvendo a lista inteira (contrato da v1). Sem
 * termo, devolve todos os usuários ativos.
 */
public class BuscarUsuariosPorNomeUseCase {

    private final IUsuarioGateway usuarios;

    private BuscarUsuariosPorNomeUseCase(IUsuarioGateway usuarios) {
        this.usuarios = usuarios;
    }

    public static BuscarUsuariosPorNomeUseCase create(IUsuarioGateway usuarios) {
        return new BuscarUsuariosPorNomeUseCase(usuarios);
    }

    public List<Usuario> run(String nome) {
        return usuarios.buscarPorNome(termo(nome));
    }

    static String termo(String nome) {
        return nome == null ? "" : nome.trim();
    }
}

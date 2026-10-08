package br.com.fiap.restaurante.application.usecase.tipousuario;

import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.application.gateway.ITipoUsuarioGateway;
import br.com.fiap.restaurante.domain.entity.TipoUsuario;

/**
 * Consulta um tipo de usuário pelo id.
 */
public class BuscarTipoUsuarioPorIdUseCase {

    private final ITipoUsuarioGateway tipos;

    private BuscarTipoUsuarioPorIdUseCase(ITipoUsuarioGateway tipos) {
        this.tipos = tipos;
    }

    public static BuscarTipoUsuarioPorIdUseCase create(ITipoUsuarioGateway tipos) {
        return new BuscarTipoUsuarioPorIdUseCase(tipos);
    }

    public TipoUsuario run(Long id) {
        return tipos.buscarPorId(id).orElseThrow(() -> tipoNaoEncontrado(id));
    }

    /** Erro de tipo inexistente, procurado pelo id ou pelo código. */
    public static RecursoNaoEncontradoException tipoNaoEncontrado(Object idOuCodigo) {
        return new RecursoNaoEncontradoException("Tipo de usuário " + idOuCodigo + " não encontrado.");
    }
}

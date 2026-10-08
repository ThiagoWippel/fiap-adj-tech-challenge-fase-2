package br.com.fiap.restaurante.application.usecase.usuario;

import br.com.fiap.restaurante.application.dto.TrocaDeTipoDTO;
import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.application.gateway.ITipoUsuarioGateway;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.application.usecase.tipousuario.BuscarTipoUsuarioPorIdUseCase;
import br.com.fiap.restaurante.domain.entity.TipoUsuario;
import br.com.fiap.restaurante.domain.entity.Usuario;
import br.com.fiap.restaurante.domain.valueobject.Documento;

/**
 * Troca o tipo do usuário na mesma conta: id, login, senha e histórico ficam;
 * mudam o tipo e o documento. Pedir o tipo e o documento que o usuário já tem é
 * aceito, sem gravar nada. Quem tem restaurante ativo continua Dono de
 * Restaurante até transferir ou excluir o restaurante.
 */
public class TrocarTipoDoUsuarioUseCase {

    private final IUsuarioGateway usuarios;
    private final ITipoUsuarioGateway tipos;
    private final IRestauranteGateway restaurantes;
    private final ITransactionManager transacao;

    private TrocarTipoDoUsuarioUseCase(IUsuarioGateway usuarios, ITipoUsuarioGateway tipos,
                                       IRestauranteGateway restaurantes, ITransactionManager transacao) {
        this.usuarios = usuarios;
        this.tipos = tipos;
        this.restaurantes = restaurantes;
        this.transacao = transacao;
    }

    public static TrocarTipoDoUsuarioUseCase create(IUsuarioGateway usuarios, ITipoUsuarioGateway tipos,
                                                    IRestauranteGateway restaurantes, ITransactionManager transacao) {
        return new TrocarTipoDoUsuarioUseCase(usuarios, tipos, restaurantes, transacao);
    }

    public Usuario run(TrocaDeTipoDTO dados) {
        return transacao.executar(() -> {
            Usuario usuario = usuarios.buscarPorId(dados.id())
                    .orElseThrow(() -> BuscarUsuarioPorIdUseCase.usuarioNaoEncontrado(dados.id()));
            TipoUsuario novoTipo = tipos.buscarPorCodigo(dados.tipo())
                    .orElseThrow(() -> BuscarTipoUsuarioPorIdUseCase.tipoNaoEncontrado(dados.tipo()));
            Documento documento = dados.documento() == null ? null : Documento.de(dados.documento());

            if (documento != null && usuario.temTipoEDocumento(novoTipo, documento)) {
                return usuario;
            }

            if (usuario.ehDonoDeRestaurante() && !novoTipo.ehDonoDeRestaurante()) {
                garantirQueNaoTemRestauranteAtivo(usuario.getId());
            }

            usuario.trocarTipo(novoTipo, documento);
            if (usuarios.existeDocumentoEmOutroUsuario(documento.numero(), usuario.getId())) {
                throw new ConflitoDeDadosException("O documento informado já está cadastrado.");
            }
            return usuarios.atualizar(usuario);
        });
    }

    // Restaurante ativo só tem dono do tipo Dono de Restaurante
    private void garantirQueNaoTemRestauranteAtivo(Long id) {
        long ativos = restaurantes.contarAtivosPorDono(id);
        if (ativos > 0) {
            throw new ConflitoDeDadosException("O usuário " + id + " é responsável por "
                    + ExcluirUsuarioUseCase.restaurantesAtivos(ativos)
                    + " e só deixa de ser Dono de Restaurante depois de transferir ou excluir "
                    + (ativos == 1 ? "o restaurante." : "os restaurantes."));
        }
    }
}

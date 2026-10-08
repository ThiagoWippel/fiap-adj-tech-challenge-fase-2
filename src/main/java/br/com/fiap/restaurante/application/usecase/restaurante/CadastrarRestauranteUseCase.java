package br.com.fiap.restaurante.application.usecase.restaurante;

import br.com.fiap.restaurante.application.dto.NovoRestauranteDTO;
import br.com.fiap.restaurante.application.dto.TurnoDTO;
import br.com.fiap.restaurante.application.exception.ConflitoDeDadosException;
import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.application.port.ITransactionManager;
import br.com.fiap.restaurante.application.usecase.usuario.BuscarUsuarioPorIdUseCase;
import br.com.fiap.restaurante.domain.entity.Restaurante;
import br.com.fiap.restaurante.domain.entity.Usuario;
import br.com.fiap.restaurante.domain.enums.TipoCozinha;
import br.com.fiap.restaurante.domain.valueobject.QuadroDeHorarios;

import java.util.List;

/**
 * Cadastra um restaurante. O dono precisa existir, estar ativo e ser do tipo Dono
 * de Restaurante.
 */
public class CadastrarRestauranteUseCase {

    private final IRestauranteGateway restaurantes;
    private final IUsuarioGateway usuarios;
    private final ITransactionManager transacao;

    private CadastrarRestauranteUseCase(IRestauranteGateway restaurantes, IUsuarioGateway usuarios,
                                        ITransactionManager transacao) {
        this.restaurantes = restaurantes;
        this.usuarios = usuarios;
        this.transacao = transacao;
    }

    public static CadastrarRestauranteUseCase create(IRestauranteGateway restaurantes, IUsuarioGateway usuarios,
                                                     ITransactionManager transacao) {
        return new CadastrarRestauranteUseCase(restaurantes, usuarios, transacao);
    }

    public Restaurante run(NovoRestauranteDTO dados) {
        return transacao.executar(() -> {
            Usuario dono = donoValido(usuarios, dados.donoId(), "cadastrar");
            Restaurante restaurante = Restaurante.create(dados.nome(),
                    dados.endereco() == null ? null : dados.endereco().paraEndereco(),
                    TipoCozinha.de(dados.tipoCozinha()), quadro(dados.horarios()), dono);
            return restaurantes.incluir(restaurante);
        });
    }

    /**
     * Busca o dono e confere o tipo antes de criar o restaurante, para devolver 404
     * ou 409 com uma orientação, em vez do erro genérico da entidade.
     */
    static Usuario donoValido(IUsuarioGateway usuarios, Long donoId, String acao) {
        Usuario dono = usuarios.buscarPorId(donoId)
                .orElseThrow(() -> BuscarUsuarioPorIdUseCase.usuarioNaoEncontrado(donoId));
        if (!dono.ehDonoDeRestaurante()) {
            throw new ConflitoDeDadosException("O usuário " + donoId + " não é Dono de Restaurante. "
                    + "Troque o tipo dele antes de " + acao + " o restaurante.");
        }
        return dono;
    }

    static QuadroDeHorarios quadro(List<TurnoDTO> turnos) {
        return new QuadroDeHorarios(turnos == null ? null : turnos.stream().map(TurnoDTO::paraTurno).toList());
    }
}

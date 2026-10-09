package br.com.fiap.restaurante.interfaceadapter.gateway;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.gateway.IRestauranteGateway;
import br.com.fiap.restaurante.domain.entity.Restaurante;
import br.com.fiap.restaurante.domain.enums.DiaSemana;
import br.com.fiap.restaurante.domain.enums.TipoCozinha;
import br.com.fiap.restaurante.domain.valueobject.QuadroDeHorarios;
import br.com.fiap.restaurante.domain.valueobject.Turno;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosRestaurante;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosTurno;
import br.com.fiap.restaurante.interfaceadapter.datasource.IRestauranteDataSource;

import java.util.Optional;

/**
 * Gateway de restaurantes: converte entre a entidade de domínio e os dados da
 * origem de dados, inclusive o dono e os turnos.
 */
public class RestauranteGateway implements IRestauranteGateway {

    private final IRestauranteDataSource dataSource;

    private RestauranteGateway(IRestauranteDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static RestauranteGateway create(IRestauranteDataSource dataSource) {
        return new RestauranteGateway(dataSource);
    }

    @Override
    public Restaurante incluir(Restaurante restaurante) {
        return paraRestaurante(dataSource.incluir(paraDados(restaurante)));
    }

    @Override
    public Restaurante atualizar(Restaurante restaurante) {
        return paraRestaurante(dataSource.atualizar(paraDados(restaurante)));
    }

    @Override
    public Optional<Restaurante> buscarPorId(Long id) {
        return dataSource.buscarPorId(id).map(RestauranteGateway::paraRestaurante);
    }

    @Override
    public Optional<Restaurante> buscarPorIdParaAlterar(Long id) {
        return dataSource.buscarPorIdParaAlterar(id).map(RestauranteGateway::paraRestaurante);
    }

    @Override
    public Pagina<Restaurante> listar(String nome, TipoCozinha tipoCozinha, PedidoDePagina pedido) {
        return dataSource.listar(nome, tipoCozinha == null ? null : tipoCozinha.name(), pedido)
                .map(RestauranteGateway::paraRestaurante);
    }

    @Override
    public Pagina<Restaurante> buscarPorDono(Long donoId, PedidoDePagina pedido) {
        return dataSource.buscarPorDono(donoId, pedido).map(RestauranteGateway::paraRestaurante);
    }

    @Override
    public long contarAtivosPorDono(Long donoId) {
        return dataSource.contarAtivosPorDono(donoId);
    }

    @Override
    public boolean existeAtivo(Long id) {
        return dataSource.existeAtivo(id);
    }

    @Override
    public void remover(Long id) {
        dataSource.remover(id);
    }

    private static DadosRestaurante paraDados(Restaurante restaurante) {
        return new DadosRestaurante(restaurante.getId(), restaurante.getNome(),
                UsuarioGateway.paraDados(restaurante.getEndereco()), restaurante.getTipoCozinha().name(),
                restaurante.getHorarios().turnos().stream()
                        .map(turno -> new DadosTurno(turno.dia().name(), turno.abertura(), turno.fechamento()))
                        .toList(),
                UsuarioGateway.paraDados(restaurante.getDono()), restaurante.getDataCriacao(),
                restaurante.getDataUltimaAlteracao());
    }

    private static Restaurante paraRestaurante(DadosRestaurante dados) {
        QuadroDeHorarios horarios = new QuadroDeHorarios(dados.horarios().stream()
                .map(turno -> new Turno(DiaSemana.valueOf(turno.diaSemana()), turno.abertura(), turno.fechamento()))
                .toList());
        return Restaurante.create(dados.id(), dados.nome(), UsuarioGateway.paraEndereco(dados.endereco()),
                TipoCozinha.valueOf(dados.tipoCozinha()), horarios, UsuarioGateway.paraUsuario(dados.dono()),
                dados.dataCriacao(), dados.dataUltimaAlteracao());
    }
}

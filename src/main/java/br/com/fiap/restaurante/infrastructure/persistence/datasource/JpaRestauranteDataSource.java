package br.com.fiap.restaurante.infrastructure.persistence.datasource;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.infrastructure.persistence.entity.EnderecoEmbeddable;
import br.com.fiap.restaurante.infrastructure.persistence.entity.HorarioFuncionamentoEntity;
import br.com.fiap.restaurante.infrastructure.persistence.entity.RestauranteEntity;
import br.com.fiap.restaurante.infrastructure.persistence.repository.RestauranteRepository;
import br.com.fiap.restaurante.infrastructure.persistence.repository.UsuarioRepository;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosEndereco;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosRestaurante;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosTurno;
import br.com.fiap.restaurante.interfaceadapter.datasource.IRestauranteDataSource;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Origem de dados de restaurantes com Spring Data JPA. As gravações rodam na
 * transação do caso de uso; as leituras abrem uma transação só de leitura,
 * porque os turnos são carregados depois da consulta principal.
 */
@Component
public class JpaRestauranteDataSource implements IRestauranteDataSource {

    private final RestauranteRepository restaurantes;
    private final UsuarioRepository usuarios;
    private final AuditingHandler auditoria;

    public JpaRestauranteDataSource(RestauranteRepository restaurantes, UsuarioRepository usuarios,
                                    AuditingHandler auditoria) {
        this.restaurantes = restaurantes;
        this.usuarios = usuarios;
        this.auditoria = auditoria;
    }

    @Override
    public DadosRestaurante incluir(DadosRestaurante dados) {
        RestauranteEntity entidade = new RestauranteEntity();
        copiar(dados, entidade);
        return paraDados(restaurantes.save(entidade));
    }

    @Override
    public DadosRestaurante atualizar(DadosRestaurante dados) {
        RestauranteEntity entidade = restaurantes.findById(dados.id()).orElseThrow();
        copiar(dados, entidade);
        // Trocar só os turnos não altera a linha do restaurante, e a auditoria não
        // perceberia. O PUT conta como alteração mesmo assim.
        auditoria.markModified(entidade);
        return paraDados(restaurantes.saveAndFlush(entidade));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DadosRestaurante> buscarPorId(Long id) {
        return restaurantes.findByIdAndRemovidoEmIsNull(id).map(JpaRestauranteDataSource::paraDados);
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<DadosRestaurante> listar(String nome, String tipoCozinha, PedidoDePagina pedido) {
        var paginacao = Paginas.paraPageRequest(pedido);
        var pagina = tipoCozinha == null
                ? restaurantes.findByNomeContainingAndRemovidoEmIsNull(nome, paginacao)
                : restaurantes.findByNomeContainingAndTipoCozinhaAndRemovidoEmIsNull(nome, tipoCozinha, paginacao);
        return Paginas.paraPagina(pagina, JpaRestauranteDataSource::paraDados);
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<DadosRestaurante> buscarPorDono(Long donoId, PedidoDePagina pedido) {
        return Paginas.paraPagina(restaurantes.findByDonoIdAndRemovidoEmIsNull(donoId, Paginas.paraPageRequest(pedido)),
                JpaRestauranteDataSource::paraDados);
    }

    @Override
    public long contarAtivosPorDono(Long donoId) {
        return restaurantes.countByDonoIdAndRemovidoEmIsNull(donoId);
    }

    @Override
    public void remover(Long id) {
        RestauranteEntity entidade = restaurantes.findById(id).orElseThrow();
        entidade.setRemovidoEm(LocalDateTime.now());
        restaurantes.save(entidade);
    }

    private void copiar(DadosRestaurante dados, RestauranteEntity entidade) {
        DadosEndereco endereco = dados.endereco();
        entidade.setNome(dados.nome());
        entidade.setTipoCozinha(dados.tipoCozinha());
        entidade.setEndereco(new EnderecoEmbeddable(endereco.rua(), endereco.numero(), endereco.complemento(),
                endereco.bairro(), endereco.cidade(), endereco.estado(), endereco.cep()));
        entidade.setDono(usuarios.findById(dados.dono().id()).orElseThrow());
        entidade.substituirHorarios(dados.horarios().stream().map(JpaRestauranteDataSource::paraEntidade).toList());
    }

    private static HorarioFuncionamentoEntity paraEntidade(DadosTurno turno) {
        HorarioFuncionamentoEntity horario = new HorarioFuncionamentoEntity();
        horario.setDiaSemana(turno.diaSemana());
        horario.setAbertura(turno.abertura());
        horario.setFechamento(turno.fechamento());
        return horario;
    }

    private static DadosRestaurante paraDados(RestauranteEntity entidade) {
        EnderecoEmbeddable endereco = entidade.getEndereco();
        return new DadosRestaurante(entidade.getId(), entidade.getNome(),
                new DadosEndereco(endereco.getRua(), endereco.getNumero(), endereco.getComplemento(),
                        endereco.getBairro(), endereco.getCidade(), endereco.getEstado(), endereco.getCep()),
                entidade.getTipoCozinha(),
                entidade.getHorarios().stream()
                        .map(horario -> new DadosTurno(horario.getDiaSemana(), horario.getAbertura(),
                                horario.getFechamento()))
                        .toList(),
                JpaUsuarioDataSource.paraDados(entidade.getDono()), entidade.getDataCriacao(),
                entidade.getDataUltimaAlteracao());
    }
}

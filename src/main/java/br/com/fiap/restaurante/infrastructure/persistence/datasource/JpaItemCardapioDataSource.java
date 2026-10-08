package br.com.fiap.restaurante.infrastructure.persistence.datasource;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.infrastructure.persistence.entity.ItemCardapioEntity;
import br.com.fiap.restaurante.infrastructure.persistence.repository.ItemCardapioRepository;
import br.com.fiap.restaurante.interfaceadapter.datasource.DadosItemCardapio;
import br.com.fiap.restaurante.interfaceadapter.datasource.IItemCardapioDataSource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Origem de dados de itens do cardápio com Spring Data JPA. As gravações rodam na
 * transação do caso de uso.
 */
@Component
public class JpaItemCardapioDataSource implements IItemCardapioDataSource {

    private final ItemCardapioRepository itens;

    public JpaItemCardapioDataSource(ItemCardapioRepository itens) {
        this.itens = itens;
    }

    @Override
    public DadosItemCardapio incluir(DadosItemCardapio dados) {
        ItemCardapioEntity entidade = new ItemCardapioEntity();
        entidade.setRestauranteId(dados.restauranteId());
        copiar(dados, entidade);
        return paraDados(itens.saveAndFlush(entidade));
    }

    @Override
    public DadosItemCardapio atualizar(DadosItemCardapio dados) {
        ItemCardapioEntity entidade = itens.findById(dados.id()).orElseThrow();
        copiar(dados, entidade);
        return paraDados(itens.saveAndFlush(entidade));
    }

    @Override
    public Optional<DadosItemCardapio> buscarPorId(Long id) {
        return itens.findByIdAndRemovidoEmIsNull(id).map(JpaItemCardapioDataSource::paraDados);
    }

    @Override
    public Pagina<DadosItemCardapio> listar(Long restauranteId, Boolean apenasNoLocal, PedidoDePagina pedido) {
        var paginacao = Paginas.paraPageRequest(pedido);
        var pagina = apenasNoLocal == null
                ? itens.findByRestauranteIdAndRemovidoEmIsNull(restauranteId, paginacao)
                : itens.findByRestauranteIdAndApenasNoLocalAndRemovidoEmIsNull(restauranteId, apenasNoLocal, paginacao);
        return Paginas.paraPagina(pagina, JpaItemCardapioDataSource::paraDados);
    }

    @Override
    public boolean existeNomeAtivo(Long restauranteId, String nome) {
        return itens.existsByRestauranteIdAndNomeAndRemovidoEmIsNull(restauranteId, nome);
    }

    @Override
    public boolean existeNomeAtivoEmOutroItem(Long restauranteId, String nome, Long itemId) {
        return itens.existsByRestauranteIdAndNomeAndRemovidoEmIsNullAndIdNot(restauranteId, nome, itemId);
    }

    @Override
    public void remover(Long id) {
        ItemCardapioEntity entidade = itens.findById(id).orElseThrow();
        entidade.setRemovidoEm(LocalDateTime.now());
        itens.save(entidade);
    }

    private static void copiar(DadosItemCardapio dados, ItemCardapioEntity entidade) {
        entidade.setNome(dados.nome());
        entidade.setDescricao(dados.descricao());
        entidade.setPreco(dados.preco());
        entidade.setApenasNoLocal(dados.apenasNoLocal());
        entidade.setCaminhoFoto(dados.caminhoFoto());
    }

    private static DadosItemCardapio paraDados(ItemCardapioEntity entidade) {
        return new DadosItemCardapio(entidade.getId(), entidade.getRestauranteId(), entidade.getNome(),
                entidade.getDescricao(), entidade.getPreco(), entidade.getApenasNoLocal(), entidade.getCaminhoFoto(),
                entidade.getDataCriacao(), entidade.getDataUltimaAlteracao());
    }
}

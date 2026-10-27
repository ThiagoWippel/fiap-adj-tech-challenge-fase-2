package br.com.fiap.restaurante.infrastructure.api.usuario;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.application.dto.PedidoDePagina;
import br.com.fiap.restaurante.application.port.IPasswordEncoder;
import br.com.fiap.restaurante.interfaceadapter.datasource.IRestauranteDataSource;
import br.com.fiap.restaurante.interfaceadapter.datasource.ITipoUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.datasource.IUsuarioDataSource;
import br.com.fiap.restaurante.interfaceadapter.presenter.PaginaResponse;
import br.com.fiap.restaurante.interfaceadapter.presenter.UsuarioResponse;
import br.com.fiap.restaurante.suporte.DadosDeExemplo;
import br.com.fiap.restaurante.suporte.TransacaoImediata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Busca paginada de usuários da v2")
class UsuarioV2ApiControllerTest {

    @Test
    @DisplayName("USU-28 · a busca devolve 200 com a página e os metadados, na ordenação pedida")
    void deveBuscarPaginado() {
        /* arrange */
        IUsuarioDataSource usuarios = mock(IUsuarioDataSource.class);
        PedidoDePagina pedido = new PedidoDePagina(1, 5, List.of(new PedidoDePagina.Ordem("email", false)));
        when(usuarios.buscarPorNome("maria", pedido))
                .thenReturn(new Pagina<>(List.of(DadosDeExemplo.maria()), 1, 5, 6, 2));
        UsuarioV2ApiController api = new UsuarioV2ApiController(usuarios, mock(ITipoUsuarioDataSource.class),
                mock(IRestauranteDataSource.class), mock(IPasswordEncoder.class), new TransacaoImediata());

        /* act */
        ResponseEntity<PaginaResponse<UsuarioResponse>> resposta =
                api.buscarPorNome("maria", PageRequest.of(1, 5, Sort.by(Sort.Order.desc("email"))));

        /* assert */
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resposta.getBody().conteudo()).extracting(UsuarioResponse::id).containsExactly(7L);
        assertThat(resposta.getBody().totalElementos()).isEqualTo(6);
        assertThat(resposta.getBody().ultima()).isTrue();
    }
}

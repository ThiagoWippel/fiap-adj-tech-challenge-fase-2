package br.com.fiap.restaurante.interfaceadapter.presenter;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.domain.entity.Usuario;
import br.com.fiap.restaurante.domain.valueobject.Documento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static br.com.fiap.restaurante.suporte.Exemplos.CPF;
import static br.com.fiap.restaurante.suporte.Exemplos.SENHA_CODIFICADA;
import static br.com.fiap.restaurante.suporte.Exemplos.cliente;
import static br.com.fiap.restaurante.suporte.Exemplos.endereco;
import static br.com.fiap.restaurante.suporte.Exemplos.maria;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Presenter de usuário")
class UsuarioPresenterTest {

    @Test
    @DisplayName("USU-18 · a resposta traz os dados públicos do usuário, com o código do tipo e sem a senha")
    void deveMontarARespostaDoUsuario() {
        /* act */
        UsuarioResponse resposta = UsuarioPresenter.paraResposta(maria());

        /* assert */
        assertThat(resposta).isEqualTo(new UsuarioResponse(7L, "Maria Silva", "maria@exemplo.com", "maria.silva",
                "CLIENTE", CPF,
                new EnderecoResponse("Rua das Flores", "123", "Apto 45", "Centro", "Itajaí", "SC", "88301000"),
                LocalDateTime.of(2026, 10, 1, 10, 0, 0), LocalDateTime.of(2026, 10, 5, 14, 30, 0)));
    }

    @Test
    @DisplayName("USU-25 · as datas saem sem frações de segundo")
    void deveCortarAsFracoesDeSegundo() {
        /* arrange */
        LocalDateTime comMicros = LocalDateTime.of(2026, 10, 1, 10, 0, 5, 123_456_000);
        Usuario usuario = Usuario.create(7L, "Maria Silva", "maria@exemplo.com", "maria.silva", SENHA_CODIFICADA,
                endereco(), cliente(), Documento.cpf(CPF), comMicros, comMicros);

        /* act */
        UsuarioResponse resposta = UsuarioPresenter.paraResposta(usuario);

        /* assert */
        assertThat(resposta.dataCriacao()).isEqualTo(LocalDateTime.of(2026, 10, 1, 10, 0, 5));
        assertThat(resposta.dataUltimaAlteracao()).isEqualTo(LocalDateTime.of(2026, 10, 1, 10, 0, 5));
    }

    @Test
    @DisplayName("USU-18 · usuário ainda sem datas sai com as datas vazias")
    void deveAceitarUsuarioSemDatas() {
        /* arrange */
        Usuario novo = Usuario.create("Maria Silva", "maria@exemplo.com", "maria.silva", SENHA_CODIFICADA,
                endereco(), cliente(), Documento.cpf(CPF));

        /* act */
        UsuarioResponse resposta = UsuarioPresenter.paraResposta(novo);

        /* assert */
        assertThat(resposta.dataCriacao()).isNull();
        assertThat(resposta.dataUltimaAlteracao()).isNull();
    }

    @Test
    @DisplayName("USU-27 · lista e página são convertidas item a item, com os metadados da página")
    void deveConverterListaEPagina() {
        /* act */
        List<UsuarioResponse> lista = UsuarioPresenter.paraLista(List.of(maria()));
        PaginaResponse<UsuarioResponse> pagina =
                UsuarioPresenter.paraPagina(new Pagina<>(List.of(maria()), 2, 10, 21, 3));

        /* assert */
        assertThat(lista).extracting(UsuarioResponse::id).containsExactly(7L);
        assertThat(pagina.conteudo()).extracting(UsuarioResponse::id).containsExactly(7L);
        assertThat(pagina).extracting(PaginaResponse::pagina, PaginaResponse::tamanho, PaginaResponse::totalElementos,
                PaginaResponse::totalPaginas, PaginaResponse::ultima).containsExactly(2, 10, 21L, 3, true);
    }
}

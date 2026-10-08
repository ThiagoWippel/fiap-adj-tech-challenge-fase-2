package br.com.fiap.restaurante.interfaceadapter.presenter;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.domain.entity.Usuario;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Converte usuários na resposta da API: um só, uma lista ou uma página.
 */
public final class UsuarioPresenter {

    private UsuarioPresenter() {
    }

    public static UsuarioResponse paraResposta(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getLogin(),
                usuario.getTipo().getCodigo(), usuario.getDocumento().numero(),
                EnderecoResponse.de(usuario.getEndereco()),
                emSegundos(usuario.getDataCriacao()), emSegundos(usuario.getDataUltimaAlteracao()));
    }

    public static List<UsuarioResponse> paraLista(List<Usuario> usuarios) {
        return usuarios.stream().map(UsuarioPresenter::paraResposta).toList();
    }

    public static PaginaResponse<UsuarioResponse> paraPagina(Pagina<Usuario> pagina) {
        return new PaginaResponse<>(paraLista(pagina.conteudo()), pagina.numero(), pagina.tamanho(),
                pagina.totalElementos(), pagina.totalPaginas(), pagina.ultima());
    }

    // O banco guarda microssegundos; a API mostra até os segundos.
    static LocalDateTime emSegundos(LocalDateTime data) {
        return data == null ? null : data.truncatedTo(ChronoUnit.SECONDS);
    }
}

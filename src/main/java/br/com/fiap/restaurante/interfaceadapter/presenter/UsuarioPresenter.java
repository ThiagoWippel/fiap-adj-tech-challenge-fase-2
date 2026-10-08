package br.com.fiap.restaurante.interfaceadapter.presenter;

import br.com.fiap.restaurante.application.dto.Pagina;
import br.com.fiap.restaurante.domain.entity.Usuario;
import br.com.fiap.restaurante.domain.valueobject.Endereco;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public final class UsuarioPresenter {

    private UsuarioPresenter() {
    }

    public static UsuarioResponse paraResposta(Usuario usuario) {
        Endereco endereco = usuario.getEndereco();
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getLogin(),
                usuario.getTipo().getCodigo(), usuario.getDocumento().numero(),
                new EnderecoResponse(endereco.rua(), endereco.numero(), endereco.complemento(), endereco.bairro(),
                        endereco.cidade(), endereco.estado(), endereco.cep()),
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

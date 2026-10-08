package br.com.fiap.restaurante.domain.entity;

import br.com.fiap.restaurante.domain.exception.RegraDeNegocioException;
import br.com.fiap.restaurante.domain.exception.ValidacaoDeDominioException;
import br.com.fiap.restaurante.domain.valueobject.Documento;
import br.com.fiap.restaurante.domain.valueobject.Endereco;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Usuário do sistema. A senha guardada aqui já vem codificada.
 *
 * <p>O documento depende do tipo: Dono de Restaurante usa CNPJ, os demais tipos
 * usam CPF. As datas são preenchidas pela persistência e só lidas aqui.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario {

    private static final String FORMATO_EMAIL = "[^\\s@]+@[^\\s@]+\\.[^\\s@]+";
    private static final String FORMATO_LOGIN = "[A-Za-z0-9._-]{4,50}";

    @EqualsAndHashCode.Include
    private Long id;
    private String nome;
    private String email;
    private String login;
    private String senha;
    private Endereco endereco;
    private TipoUsuario tipo;
    private Documento documento;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaAlteracao;

    private Usuario() {
    }

    public static Usuario create(String nome, String email, String login, String senhaCodificada,
                                 Endereco endereco, TipoUsuario tipo, Documento documento) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setLogin(login);
        usuario.setSenha(senhaCodificada);
        usuario.setEndereco(endereco);
        usuario.definirTipo(tipo, documento);
        return usuario;
    }

    public static Usuario create(Long id, String nome, String email, String login, String senhaCodificada,
                                 Endereco endereco, TipoUsuario tipo, Documento documento,
                                 LocalDateTime dataCriacao, LocalDateTime dataUltimaAlteracao) {
        Usuario usuario = create(nome, email, login, senhaCodificada, endereco, tipo, documento);
        usuario.id = id;
        usuario.dataCriacao = dataCriacao;
        usuario.dataUltimaAlteracao = dataUltimaAlteracao;
        return usuario;
    }

    public boolean ehDonoDeRestaurante() {
        return tipo.ehDonoDeRestaurante();
    }

    /**
     * Troca o tipo na mesma conta. O documento vem junto porque o novo tipo pode
     * exigir outro: o documento antigo é descartado.
     */
    public void trocarTipo(TipoUsuario novoTipo, Documento novoDocumento) {
        definirTipo(novoTipo, novoDocumento);
    }

    public boolean temTipoEDocumento(TipoUsuario outroTipo, Documento outroDocumento) {
        return tipo.equals(outroTipo) && documento.equals(outroDocumento);
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().length() < 3 || nome.trim().length() > 120) {
            throw new ValidacaoDeDominioException("O nome deve ter entre 3 e 120 caracteres.");
        }
        this.nome = nome.trim();
    }

    public void setEmail(String email) {
        if (email == null || email.trim().length() > 255 || !email.trim().matches(FORMATO_EMAIL)) {
            throw new ValidacaoDeDominioException("O e-mail informado não é válido.");
        }
        this.email = email.trim();
    }

    public void setLogin(String login) {
        if (login == null || !login.trim().matches(FORMATO_LOGIN)) {
            throw new ValidacaoDeDominioException(
                    "O login deve ter de 4 a 50 caracteres: letras, números, ponto, hífen ou sublinhado.");
        }
        this.login = login.trim();
    }

    public void setSenha(String senhaCodificada) {
        if (senhaCodificada == null || senhaCodificada.isBlank()) {
            throw new ValidacaoDeDominioException("A senha é obrigatória.");
        }
        this.senha = senhaCodificada;
    }

    public void setEndereco(Endereco endereco) {
        if (endereco == null) {
            throw new ValidacaoDeDominioException("O endereço é obrigatório.");
        }
        this.endereco = endereco;
    }

    private void definirTipo(TipoUsuario tipo, Documento documento) {
        if (tipo == null) {
            throw new ValidacaoDeDominioException("O tipo do usuário é obrigatório.");
        }
        String exigido = tipo.ehDonoDeRestaurante() ? "CNPJ" : "CPF";
        if (documento == null) {
            throw new RegraDeNegocioException(
                    "O " + exigido + " é obrigatório para usuários do tipo " + tipo.getNome() + ".");
        }
        if (tipo.ehDonoDeRestaurante() != documento.ehCnpj()) {
            throw new RegraDeNegocioException(
                    "Usuário do tipo " + tipo.getNome() + " deve informar " + exigido + ".");
        }
        this.tipo = tipo;
        this.documento = documento;
    }
}

package br.com.fiap.restaurante.services;

import br.com.fiap.restaurante.dtos.request.AtualizarUsuarioRequest;
import br.com.fiap.restaurante.dtos.request.CriarUsuarioRequest;
import br.com.fiap.restaurante.dtos.request.TrocarSenhaRequest;
import br.com.fiap.restaurante.dtos.response.UsuarioResponse;
import br.com.fiap.restaurante.entities.Endereco;
import br.com.fiap.restaurante.entities.TipoUsuario;
import br.com.fiap.restaurante.entities.Usuario;
import br.com.fiap.restaurante.mappers.UsuarioMapper;
import br.com.fiap.restaurante.repositories.ClienteRepository;
import br.com.fiap.restaurante.repositories.DonoRestauranteRepository;
import br.com.fiap.restaurante.repositories.UsuarioRepository;
import br.com.fiap.restaurante.services.exceptions.ConflitoDeDadosException;
import br.com.fiap.restaurante.services.exceptions.CredenciaisInvalidasException;
import br.com.fiap.restaurante.services.exceptions.RecursoNaoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regras de negócio do cadastro de usuários.
 *
 * Responsabilidade única: gerenciar o ciclo de vida do usuário. A validação de
 * credenciais é assunto do AutenticacaoService - são operações que mudam por
 * motivos diferentes e por isso vivem em classes diferentes.
 *
 * Todas as dependências chegam pelo construtor, em campos finais. Não há como
 * construir a classe em estado incompleto, e não é preciso subir o contexto do
 * Spring para testá-la: basta passar dublês no construtor.
 */
@Service
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final DonoRestauranteRepository donoRestauranteRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioFactory usuarioFactory;
    private final UsuarioMapper usuarioMapper;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          ClienteRepository clienteRepository,
                          DonoRestauranteRepository donoRestauranteRepository,
                          PasswordEncoder passwordEncoder,
                          UsuarioFactory usuarioFactory,
                          UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.donoRestauranteRepository = donoRestauranteRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioFactory = usuarioFactory;
        this.usuarioMapper = usuarioMapper;
    }

    // ------------------------------------------------------------------
    // Cadastro
    // ------------------------------------------------------------------

    @Transactional
    public UsuarioResponse cadastrar(CriarUsuarioRequest requisicao) {
        logger.info("Cadastrando usuario do tipo {} com login {}", requisicao.tipo(), requisicao.login());

        validarUnicidadeNoCadastro(requisicao);

        String documento = normalizarDocumento(requisicao);
        validarUnicidadeDoDocumento(requisicao.tipo(), documento);

        Endereco endereco = usuarioMapper.paraEntidade(
                requisicao.endereco(),
                apenasDigitos(requisicao.endereco().cep())
        );

        Usuario usuario = usuarioFactory.criar(
                requisicao,
                passwordEncoder.encode(requisicao.senha()),
                endereco,
                documento
        );

        Usuario salvo = usuarioRepository.save(usuario);
        logger.info("Usuario cadastrado com id {}", salvo.getId());

        return usuarioMapper.paraResposta(salvo);
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return usuarioMapper.paraResposta(buscarEntidade(id));
    }

    /**
     * Busca por nome sem paginação - versão 1 da API.
     *
     * Devolve lista vazia quando nada corresponde ao termo. A coleção filtrada
     * existe como recurso; ela apenas não contém elementos. Responder 404 nesse
     * caso afirmaria que a rota não existe.
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> buscarPorNome(String nome) {
        return usuarioRepository
                .findByNomeContainingIgnoreCase(normalizarTermo(nome))
                .stream()
                .map(usuarioMapper::paraResposta)
                .toList();
    }

    /**
     * Busca por nome com paginação - versão 2 da API.
     */
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> buscarPorNomePaginado(String nome, Pageable paginacao) {
        return usuarioRepository
                .findByNomeContainingIgnoreCase(normalizarTermo(nome), paginacao)
                .map(usuarioMapper::paraResposta);
    }

    // ------------------------------------------------------------------
    // Atualização de dados
    // ------------------------------------------------------------------

    @Transactional
    public UsuarioResponse atualizar(Long id, AtualizarUsuarioRequest requisicao) {
        logger.info("Atualizando dados do usuario {}", id);

        Usuario usuario = buscarEntidade(id);

        // A pergunta é "existe OUTRO usuário com este valor?". Sem excluir o
        // próprio registro da busca, salvar o cadastro sem alterar o e-mail
        // resultaria em conflito do usuário consigo mesmo.
        if (usuarioRepository.existsByEmailAndIdNot(requisicao.email(), id)) {
            logger.warn("Conflito de e-mail na atualizacao do usuario {}", id);
            throw new ConflitoDeDadosException("O e-mail informado ja esta cadastrado");
        }
        if (usuarioRepository.existsByLoginAndIdNot(requisicao.login(), id)) {
            logger.warn("Conflito de login na atualizacao do usuario {}", id);
            throw new ConflitoDeDadosException("O login informado ja esta cadastrado");
        }

        usuario.setNome(requisicao.nome());
        usuario.setEmail(requisicao.email());
        usuario.setLogin(requisicao.login());
        usuario.setEndereco(usuarioMapper.paraEntidade(
                requisicao.endereco(),
                apenasDigitos(requisicao.endereco().cep())
        ));

        // A data de última alteração é preenchida pela auditoria do JPA no
        // momento da sincronização com o banco. Não há atribuição manual.
        return usuarioMapper.paraResposta(usuario);
    }

    // ------------------------------------------------------------------
    // Troca de senha
    // ------------------------------------------------------------------

    @Transactional
    public void trocarSenha(Long id, TrocarSenhaRequest requisicao) {
        logger.info("Solicitacao de troca de senha para o usuario {}", id);

        Usuario usuario = buscarEntidade(id);

        // A comparação ocorre entre hashes. A senha armazenada nunca é revertida.
        if (!passwordEncoder.matches(requisicao.senhaAtual(), usuario.getSenha())) {
            logger.warn("Senha atual incorreta na troca de senha do usuario {}", id);
            throw new CredenciaisInvalidasException("A senha atual informada esta incorreta");
        }

        // Carregar a entidade e alterá-la faz a auditoria disparar. Uma
        // atualização por consulta direta gravaria a senha sem atualizar a data
        // de última alteração - e trocar a senha é, sim, uma alteração.
        usuario.setSenha(passwordEncoder.encode(requisicao.novaSenha()));

        logger.info("Senha alterada para o usuario {}", id);
    }

    // ------------------------------------------------------------------
    // Exclusao
    // ------------------------------------------------------------------

    @Transactional
    public void excluir(Long id) {
        logger.info("Excluindo usuario {}", id);
        usuarioRepository.delete(buscarEntidade(id));
    }

    // ------------------------------------------------------------------
    // Apoio
    // ------------------------------------------------------------------

    private String normalizarTermo(String nome) {
        return (nome == null) ? "" : nome.trim();
    }

    private Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Usuario {} nao encontrado", id);
                    return new RecursoNaoEncontradoException("Usuario nao encontrado para o id " + id);
                });
    }

    private void validarUnicidadeNoCadastro(CriarUsuarioRequest requisicao) {
        // Verificação prévia existe para produzir mensagem legível. A garantia
        // efetiva contra requisições concorrentes vem das restrições do banco,
        // cuja violação é tratada no handler de exceções.
        if (usuarioRepository.existsByEmail(requisicao.email())) {
            logger.warn("Tentativa de cadastro com e-mail ja existente");
            throw new ConflitoDeDadosException("O e-mail informado ja esta cadastrado");
        }
        if (usuarioRepository.existsByLogin(requisicao.login())) {
            logger.warn("Tentativa de cadastro com login ja existente");
            throw new ConflitoDeDadosException("O login informado ja esta cadastrado");
        }
    }

    private void validarUnicidadeDoDocumento(TipoUsuario tipo, String documento) {
        if (documento == null) {
            return;
        }
        boolean jaExiste = (tipo == TipoUsuario.CLIENTE)
                ? clienteRepository.existsByCpf(documento)
                : donoRestauranteRepository.existsByCnpj(documento);

        if (jaExiste) {
            logger.warn("Tentativa de cadastro com documento ja existente");
            throw new ConflitoDeDadosException("O documento informado ja esta cadastrado");
        }
    }

    private String normalizarDocumento(CriarUsuarioRequest requisicao) {
        String bruto = (requisicao.tipo() == TipoUsuario.CLIENTE)
                ? requisicao.cpf()
                : requisicao.cnpj();
        return apenasDigitos(bruto);
    }

    /**
     * Remove pontuação, mantendo apenas dígitos.
     *
     * Sem esta normalização, "123.456.789-09" e "12345678909" seriam gravados
     * como valores distintos e a restrição de unicidade não teria efeito
     * prático algum.
     */
    private String apenasDigitos(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.replaceAll("\\D", "");
    }
}

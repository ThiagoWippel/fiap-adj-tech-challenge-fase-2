package br.com.fiap.restaurante.services;

import br.com.fiap.restaurante.dtos.request.LoginRequest;
import br.com.fiap.restaurante.dtos.response.LoginResponse;
import br.com.fiap.restaurante.entities.Usuario;
import br.com.fiap.restaurante.mappers.UsuarioMapper;
import br.com.fiap.restaurante.repositories.UsuarioRepository;
import br.com.fiap.restaurante.services.exceptions.CredenciaisInvalidasException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Validação de credenciais.
 *
 * Separado do UsuarioService por responsabilidade: gerenciar cadastro e
 * autenticar são operações que mudam por motivos distintos. Uma evolui com as
 * regras de dados pessoais, a outra com as políticas de segurança. Manter as
 * duas na mesma classe faria com que qualquer mudança em uma exigisse
 * reexaminar a outra.
 *
 * O retorno não é booleano. O material de Exceções da fase é explícito quanto a
 * isso: falhas devem ser sinalizadas por exceção, não por valor de status.
 */
@Service
public class AutenticacaoService {

    private static final Logger logger = LoggerFactory.getLogger(AutenticacaoService.class);

    private static final String MENSAGEM_FALHA = "Login ou senha invalidos";

    /**
     * Hash de descarte, usado quando o login não existe.
     *
     * Sem ele, a resposta para um login inexistente seria imediata, enquanto a
     * resposta para uma senha incorreta levaria o tempo do cálculo do hash.
     * Essa diferença permitiria descobrir quais logins existem apenas medindo o
     * tempo de resposta. Manter a mensagem igual não basta se a duração denuncia.
     */
    private static final String HASH_DE_DESCARTE =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;

    public AutenticacaoService(UsuarioRepository usuarioRepository,
                               PasswordEncoder passwordEncoder,
                               UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioMapper = usuarioMapper;
    }

    @Transactional(readOnly = true)
    public LoginResponse autenticar(LoginRequest requisicao) {
        logger.info("Validacao de credenciais solicitada para o login {}", requisicao.login());

        Optional<Usuario> encontrado = usuarioRepository.findByLogin(requisicao.login());

        // A comparação acontece sempre, exista o usuário ou não, para que o
        // tempo de resposta seja equivalente nos dois casos.
        String hashArmazenado = encontrado
                .map(Usuario::getSenha)
                .orElse(HASH_DE_DESCARTE);

        boolean senhaConfere = passwordEncoder.matches(requisicao.senha(), hashArmazenado);

        if (encontrado.isEmpty() || !senhaConfere) {
            // Uma única mensagem para os dois cenários. Distinguir "login não
            // existe" de "senha incorreta" permitiria enumerar os logins
            // cadastrados por tentativa e erro.
            logger.warn("Falha na validacao de credenciais para o login {}", requisicao.login());
            throw new CredenciaisInvalidasException(MENSAGEM_FALHA);
        }

        Usuario usuario = encontrado.get();
        logger.info("Credenciais validadas para o usuario {}", usuario.getId());

        return usuarioMapper.paraRespostaLogin(usuario);
    }
}

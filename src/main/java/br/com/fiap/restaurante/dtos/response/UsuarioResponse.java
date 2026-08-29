package br.com.fiap.restaurante.dtos.response;

import br.com.fiap.restaurante.entities.TipoUsuario;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Representação pública de um usuário.
 *
 * A senha simplesmente não existe aqui. Não há campo a ocultar, nem anotação a
 * lembrar de aplicar - a estrutura torna o vazamento impossível por construção,
 * que é a razão de a API não expor a entidade diretamente.
 *
 * Note a assimetria em relação ao domínio: lá há herança, porque o polimorfismo
 * tem valor no comportamento. Aqui há uma estrutura única, porque a
 * representação externa dos dois tipos é idêntica. Modelo interno e contrato
 * externo não precisam ter a mesma forma.
 */
@Schema(description = "Dados publicos de um usuario")
public record UsuarioResponse(

        @Schema(example = "1")
        Long id,

        @Schema(example = "Maria Silva")
        String nome,

        @Schema(example = "maria.silva@exemplo.com")
        String email,

        @Schema(example = "maria.silva")
        String login,

        @Schema(example = "CLIENTE")
        TipoUsuario tipo,

        // Campo único em vez de cpf e cnpj separados: um usuário possui
        // exatamente um documento, determinado pelo tipo. Dois campos deixariam
        // um deles permanentemente nulo em toda resposta.
        @Schema(example = "12345678909", description = "CPF para cliente, CNPJ para dono de restaurante")
        String documento,

        EnderecoResponse endereco,

        // Sem o formato explícito, o Jackson serializa LocalDateTime com
        // precisão de nanossegundos, enquanto a coluna no banco guarda
        // microssegundos. O objeto recém-criado sairia com nove casas decimais e
        // o mesmo registro, relido do MySQL, com seis - divergência que
        // apareceria nos prints da documentação.
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @Schema(example = "2026-08-21T10:30:00")
        LocalDateTime dataCriacao,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @Schema(example = "2026-08-21T14:45:00")
        LocalDateTime dataUltimaAlteracao
) {
}

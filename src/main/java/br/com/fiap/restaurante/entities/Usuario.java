package br.com.fiap.restaurante.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Raiz da hierarquia de usuários.
 *
 * Estratégia de herança: SINGLE_TABLE. Todos os subtipos compartilham uma
 * única tabela, distinguidos pela coluna discriminadora "tipo_usuario". A
 * alternativa JOINED exigiria junção em toda leitura e triplicaria o número
 * de tabelas; TABLE_PER_CLASS foi descartada por inviabilizar a unicidade de
 * e-mail no sistema inteiro, já que uma restrição de unicidade não abrange
 * tabelas distintas.
 *
 * A classe é abstrata de propósito: não existe "um usuário genérico" no
 * domínio. Todo usuário é um cliente ou um dono de restaurante.
 */
@Entity
@Table(
        name = "usuario",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_usuario_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_usuario_login", columnNames = "login")
        }
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
// Por padrão o JPA nomeia a coluna discriminadora como "DTYPE" e a preenche
// com o nome da classe. Nomear explicitamente torna a tabela legível para
// quem a inspeciona sem conhecer o código.
@DiscriminatorColumn(
        name = "tipo_usuario",
        discriminatorType = DiscriminatorType.STRING,
        length = 20
)
// Habilita o preenchimento automático das datas de criação e alteração.
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
// O JPA exige um construtor sem argumentos para instanciar entidades ao ler
// do banco. Deixá-lo protegido impede que o código da aplicação crie usuários
// em estado inválido, sem passar pelo construtor real.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    // 255 acomoda o limite normativo de 254 caracteres de um endereço de e-mail.
    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 50)
    private String login;

    // Armazena o hash BCrypt, nunca a senha em texto. O hash tem 60 caracteres
    // fixos; a folga até 100 evita acoplar o esquema a um algoritmo específico.
    @Column(nullable = false, length = 100)
    private String senha;

    @Embedded
    private Endereco endereco;

    @CreatedDate
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @LastModifiedDate
    @Column(name = "data_ultima_alteracao", nullable = false)
    private LocalDateTime dataUltimaAlteracao;

    protected Usuario(String nome, String email, String login, String senha, Endereco endereco) {
        this.nome = nome;
        this.email = email;
        this.login = login;
        this.senha = senha;
        this.endereco = endereco;
    }

    /**
     * Tipo do usuário, resolvido por polimorfismo.
     *
     * O valor não é lido da coluna discriminadora: cada subclasse declara o
     * próprio tipo. Adicionar um terceiro tipo de usuário é criar uma classe
     * nova que implementa este método, sem alterar nada do que já existe -
     * o Princípio Aberto/Fechado aplicado ao domínio.
     */
    public abstract TipoUsuario getTipo();

    /**
     * Documento de identificação, resolvido por polimorfismo.
     *
     * Cliente devolve o CPF, DonoRestaurante devolve o CNPJ. Sem este método,
     * qualquer código que precisasse do documento teria de testar o tipo
     * concreto do objeto - exatamente o condicional que a herança elimina.
     */
    public abstract String getDocumento();

    /**
     * Igualdade baseada exclusivamente no identificador.
     *
     * Comparar todos os campos quebraria com entidades gerenciadas pelo
     * Hibernate, que podem ser proxies com atributos ainda não carregados.
     */
    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof Usuario outro)) {
            return false;
        }
        return this.id != null && this.id.equals(outro.getId());
    }

    /**
     * Valor constante de propósito.
     *
     * Uma entidade recém-criada ainda não tem id; se o hashCode dependesse
     * dele, mudaria após a persistência e o objeto se perderia dentro de
     * coleções baseadas em hash.
     */
    @Override
    public int hashCode() {
        return Usuario.class.hashCode();
    }
}

package br.com.fiap.restaurante.application.usecase.usuario;

import br.com.fiap.restaurante.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.restaurante.application.gateway.IUsuarioGateway;
import br.com.fiap.restaurante.domain.entity.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static br.com.fiap.restaurante.suporte.Exemplos.maria;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@DisplayName("Buscar usuário por id")
class BuscarUsuarioPorIdUseCaseTest {

    @Mock
    private IUsuarioGateway usuarios;

    private AutoCloseable mocks;
    private BuscarUsuarioPorIdUseCase useCase;

    @BeforeEach
    void preparar() {
        mocks = MockitoAnnotations.openMocks(this);
        useCase = BuscarUsuarioPorIdUseCase.create(usuarios);
    }

    @AfterEach
    void encerrar() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("USU-24 · devolve o usuário encontrado")
    void deveDevolverOUsuarioEncontrado() {
        /* arrange */
        when(usuarios.buscarPorId(7L)).thenReturn(Optional.of(maria()));

        /* act */
        Usuario usuario = useCase.run(7L);

        /* assert */
        assertThat(usuario.getId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("USU-24 · usuário inexistente ou removido devolve não encontrado")
    void deveRecusarUsuarioInexistente() {
        /* arrange */
        when(usuarios.buscarPorId(99L)).thenReturn(Optional.empty());

        /* act + assert */
        assertThatThrownBy(() -> useCase.run(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário 99 não encontrado.");
    }
}

package br.com.uork.appuork.service;

import br.com.uork.appuork.component.DocumentoValidator;
import br.com.uork.appuork.dto.usuario.EnderecoResponseDTO;
import br.com.uork.appuork.dto.usuario.PerfilResponseDTO;
import br.com.uork.appuork.dto.usuario.UsuarioCriacaoDTO;
import br.com.uork.appuork.dto.usuario.UsuarioUpdateDTO;
import br.com.uork.appuork.exception.DocumentoInvalidoException;
import br.com.uork.appuork.models.Endereco;
import br.com.uork.appuork.models.Usuario;
import br.com.uork.appuork.models.enuns.TipoPessoa;
import br.com.uork.appuork.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    private UsuarioRepository repository;
    private UsuarioService service;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        repository = mock(UsuarioRepository.class);
        service = new UsuarioService(repository, new DocumentoValidator(), encoder);
    }

    private Usuario umUsuario() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setNome("Ana");
        u.setSobrenome("Silva");
        u.setEmail("ana@email.com");
        u.setSenha("$2a$10$abcdefghijklmnopqrstuv");
        u.setTipoPessoa(TipoPessoa.CPF);
        u.setDocumento("52998224725");
        return u;
    }

    @Test
    @DisplayName("buscarPerfil of user without address returns null endereco, no error")
    void perfilSemEndereco() {
        when(repository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(umUsuario()));

        PerfilResponseDTO perfil = service.buscarPerfil("ana@email.com");

        assertNotNull(perfil);
        assertEquals("ana@email.com", perfil.email());
        assertNotNull(perfil.endereco());
        assertNull(perfil.endereco().rua());
        assertNull(perfil.endereco().cep());
    }

    @Test
    @DisplayName("buscarPerfil of user with address returns address fields")
    void perfilComEndereco() {
        Usuario usuario = umUsuario();
        Endereco endereco = new Endereco();
        endereco.setRua("Rua A");
        endereco.setNumero("10");
        endereco.setCidade("Sao");
        usuario.setEndereco(endereco);
        when(repository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(usuario));

        PerfilResponseDTO perfil = service.buscarPerfil("ana@email.com");

        assertNotNull(perfil.endereco());
        assertEquals("Rua A", perfil.endereco().rua());
        assertEquals("10", perfil.endereco().numero());
    }

    @Test
    @DisplayName("criarUsuario rejects invalid document before saving")
    void criarUsuarioRejeitaDocumentoInvalido() {
        UsuarioCriacaoDTO dto = new UsuarioCriacaoDTO(
                "Ana", "Silva", "ana@email.com", "senha123",
                TipoPessoa.CPF, "123", null, null);

        assertThrows(DocumentoInvalidoException.class, () -> service.criarUsuario(dto));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("criarUsuario with valid CPF hashes password and stores clean document")
    void criarUsuarioValido() {
        when(repository.existsByEmail(anyString())).thenReturn(false);
        when(repository.existsByDocumento(anyString())).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UsuarioCriacaoDTO dto = new UsuarioCriacaoDTO(
                "Ana", "Silva", "ana@email.com", "senha123",
                TipoPessoa.CPF, "529.982.247-25", null, null);

        Usuario criado = service.criarUsuario(dto);

        assertNotNull(criado);
        assertTrue(criado.getSenha().startsWith("$2"));
        assertNotEquals("senha123", criado.getSenha());
        assertEquals("52998224725", criado.getDocumento());
        assertNull(criado.getEndereco());
    }

    @Test
    @DisplayName("atualizarPerfil with null senha preserves stored password")
    void atualizarSemSenhaPreserva() {
        Usuario usuario = umUsuario();
        when(repository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(usuario));

        UsuarioUpdateDTO dto = new UsuarioUpdateDTO("ana@email.com", null, null);
        service.atualizarPerfil("ana@email.com", dto);

        assertEquals("$2a$10$abcdefghijklmnopqrstuv", usuario.getSenha());
    }

    @Test
    @DisplayName("atualizarPerfil with blank senha keeps stored password")
    void atualizarSenhaBrancoPreserva() {
        Usuario usuario = umUsuario();
        when(repository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(usuario));

        UsuarioUpdateDTO dto = new UsuarioUpdateDTO("ana@email.com", "   ", null);
        service.atualizarPerfil("ana@email.com", dto);

        assertEquals("$2a$10$abcdefghijklmnopqrstuv", usuario.getSenha());
    }

    @Test
    @DisplayName("atualizarPerfil with password encodes it")
    void atualizarSenhaEncode() {
        Usuario usuario = umUsuario();
        when(repository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(usuario));

        UsuarioUpdateDTO dto = new UsuarioUpdateDTO("ana@email.com", "novaSenha123", null);
        service.atualizarPerfil("ana@email.com", dto);

        assertNotEquals("novaSenha123", usuario.getSenha());
        assertTrue(new BCryptPasswordEncoder().matches("novaSenha123", usuario.getSenha()));
    }

    @Test
    @DisplayName("atualizarPerfil with null address preserves existing address")
    void atualizarSemEnderecoPreserva() {
        Usuario usuario = umUsuario();
        Endereco endereco = new Endereco();
        endereco.setRua("Rua Antiga");
        usuario.setEndereco(endereco);
        when(repository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(usuario));

        UsuarioUpdateDTO dto = new UsuarioUpdateDTO("ana@email.com", null, null);
        service.atualizarPerfil("ana@email.com", dto);

        assertNotNull(usuario.getEndereco());
        assertEquals("Rua Antiga", usuario.getEndereco().getRua());
    }

    @Test
    @DisplayName("atualizarPerfil with address replaces address")
    void atualizarComEndereco() {
        Usuario usuario = umUsuario();
        when(repository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(usuario));

        EnderecoResponseDTO novo = new EnderecoResponseDTO("Rua Nova", "20", "Centro", "Sao", "SP", "01001000");
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO("ana@email.com", null, novo);
        PerfilResponseDTO perfil = service.atualizarPerfil("ana@email.com", dto);

        assertNotNull(usuario.getEndereco());
        assertEquals("Rua Nova", usuario.getEndereco().getRua());
        assertNotNull(perfil.endereco());
        assertEquals("SP", perfil.endereco().estado());
    }

}

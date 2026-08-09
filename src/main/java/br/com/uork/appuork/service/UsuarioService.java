package br.com.uork.appuork.service;

import br.com.uork.appuork.component.DocumentoValidator;
import br.com.uork.appuork.dto.usuario.EnderecoResponseDTO;
import br.com.uork.appuork.dto.usuario.PerfilResponseDTO;
import br.com.uork.appuork.dto.usuario.UsuarioUpdateDTO;
import br.com.uork.appuork.models.Endereco;
import br.com.uork.appuork.models.Usuario;
import br.com.uork.appuork.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final DocumentoValidator documentoValidator;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          DocumentoValidator documentoValidator) {
        this.usuarioRepository = usuarioRepository;
        this.documentoValidator = documentoValidator;
    }

    public List<Usuario> listarUsuario(){
        return usuarioRepository.findAll();
    }

    public Usuario criarUsuario(Usuario usuario) {

        documentoValidator.validar(usuario.getTipoPessoa(), usuario.getDocumento());

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("E-mail já cadastrado");
        }

        if (usuarioRepository.existsByDocumento(usuario.getDocumento())) {
            throw new RuntimeException("Documento já cadastrado");
        }

        usuario.setDocumento(
                usuario.getDocumento().replaceAll("[^\\d]", "") //Remover mascara
        );

        return usuarioRepository.save(usuario);
    }

    public PerfilResponseDTO buscarPerfil(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Endereco endereco = usuario.getEndereco();

        EnderecoResponseDTO enderecoResponse = new EnderecoResponseDTO(
                endereco.getRua(),
                endereco.getNumero(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getEstado(),
                endereco.getCep()
        );

        return new PerfilResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getSobrenome(),
                usuario.getEmail(),
                usuario.getTipoPessoa().name(),
                usuario.getDocumento(),
                enderecoResponse
        );
    }

    public PerfilResponseDTO atualizarPerfil(String emailAtual, UsuarioUpdateDTO dto) {

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailAtual)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!usuario.getEmail().equalsIgnoreCase(dto.email())
                && usuarioRepository.existsByEmailIgnoreCase(dto.email())) {
            throw new RuntimeException("E-mail já cadastrado");
        }

        usuario.setEmail(dto.email());
        usuario.setSenha(dto.senha());

        EnderecoResponseDTO enderecoDto = dto.endereco();

        Endereco endereco = new Endereco();
        endereco.setRua(enderecoDto.rua());
        endereco.setNumero(enderecoDto.numero());
        endereco.setBairro(enderecoDto.bairro());
        endereco.setCidade(enderecoDto.cidade());
        endereco.setEstado(enderecoDto.estado());
        endereco.setCep(enderecoDto.cep());

        usuario.setEndereco(endereco);

        usuarioRepository.save(usuario);

        EnderecoResponseDTO enderecoResponse = new EnderecoResponseDTO(
                endereco.getRua(),
                endereco.getNumero(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getEstado(),
                endereco.getCep()
        );

        return new PerfilResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getSobrenome(),
                usuario.getEmail(),
                usuario.getTipoPessoa().name(),
                usuario.getDocumento(),
                enderecoResponse
        );
    }
}
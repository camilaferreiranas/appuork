package br.com.uork.appuork.service;

import br.com.uork.appuork.component.DocumentoValidator;
import br.com.uork.appuork.dto.usuario.EnderecoResponseDTO;
import br.com.uork.appuork.dto.usuario.PerfilResponseDTO;
import br.com.uork.appuork.dto.usuario.UsuarioCriacaoDTO;
import br.com.uork.appuork.dto.usuario.UsuarioUpdateDTO;
import br.com.uork.appuork.models.Endereco;
import br.com.uork.appuork.models.Usuario;
import br.com.uork.appuork.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final DocumentoValidator documentoValidator;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          DocumentoValidator documentoValidator, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.documentoValidator = documentoValidator;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarUsuario(){
        return usuarioRepository.findAll();
    }

    public Usuario criarUsuario(UsuarioCriacaoDTO dto) {

        DocumentoValidator.validar(dto.tipoPessoa(), dto.documento());

        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("E-mail já cadastrado");
        }

        if (usuarioRepository.existsByDocumento(dto.documento())) {
            throw new RuntimeException("Documento já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setSobrenome(dto.sobrenome());
        usuario.setEmail(dto.email());
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        usuario.setTipoPessoa(dto.tipoPessoa());
        usuario.setDocumento(dto.documento().replaceAll("[^\\d]", ""));
        usuario.setTelefone(dto.telefone());

        if (dto.endereco() != null) {
            Endereco endereco = new Endereco();
            endereco.setRua(dto.endereco().rua());
            endereco.setNumero(dto.endereco().numero());
            endereco.setBairro(dto.endereco().bairro());
            endereco.setCidade(dto.endereco().cidade());
            endereco.setEstado(dto.endereco().estado());
            endereco.setCep(dto.endereco().cep());
            usuario.setEndereco(endereco);
        }

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
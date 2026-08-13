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
                          DocumentoValidator documentoValidator,
                          BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.documentoValidator = documentoValidator;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarUsuario(){
        return usuarioRepository.findAll();
    }

    public Usuario criarUsuario(UsuarioCriacaoDTO dto) {

        documentoValidator.validar(dto.tipoPessoa(), dto.documento());

        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("E-mail já cadastrado");
        }

        String documento = dto.documento().replaceAll("[^\\d]", "");
        if (usuarioRepository.existsByDocumento(documento)) {
            throw new RuntimeException("Documento já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setSobrenome(dto.sobrenome());
        usuario.setEmail(dto.email());
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        usuario.setTipoPessoa(dto.tipoPessoa());
        usuario.setDocumento(documento);
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

        return new PerfilResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getSobrenome(),
                usuario.getEmail(),
                usuario.getTipoPessoa().name(),
                usuario.getDocumento(),
                usuario.getTelefone(),
                toEnderecoResponse(usuario.getEndereco())
        );
    }

    private EnderecoResponseDTO toEnderecoResponse(Endereco endereco) {
        if (endereco == null) {
            return new EnderecoResponseDTO(null, null, null, null, null, null);
        }

        return new EnderecoResponseDTO(
                endereco.getRua(),
                endereco.getNumero(),
                endereco.getBairro(),
                endereco.getCidade(),
                endereco.getEstado(),
                endereco.getCep()
        );
    }

    public PerfilResponseDTO atualizarPerfil(String emailAtual, UsuarioUpdateDTO dto) {

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailAtual)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String novoEmail = dto.email() == null ? usuario.getEmail() : dto.email().trim();
        if (novoEmail.isBlank()) {
            throw new RuntimeException("E-mail é obrigatório");
        }

        if (!usuario.getEmail().equalsIgnoreCase(novoEmail)
                && usuarioRepository.existsByEmailIgnoreCase(novoEmail)) {
            throw new RuntimeException("E-mail já cadastrado");
        }

        if (dto.nome() != null) {
            String nome = dto.nome().trim();
            if (nome.isBlank()) throw new RuntimeException("Nome é obrigatório");
            usuario.setNome(nome);
        }

        if (dto.sobrenome() != null) {
            String sobrenome = dto.sobrenome().trim();
            if (sobrenome.isBlank()) throw new RuntimeException("Sobrenome é obrigatório");
            usuario.setSobrenome(sobrenome);
        }

        if (dto.tipoPessoa() != null && dto.documento() != null) {
            String documento = dto.documento().replaceAll("[^\\d]", "");
            documentoValidator.validar(dto.tipoPessoa(), documento);

            if (!documento.equals(usuario.getDocumento())
                    && usuarioRepository.existsByDocumento(documento)) {
                throw new RuntimeException("Documento já cadastrado");
            }

            usuario.setTipoPessoa(dto.tipoPessoa());
            usuario.setDocumento(documento);
        }

        usuario.setEmail(novoEmail);
        if (dto.telefone() != null) {
            usuario.setTelefone(dto.telefone().trim());
        }

        if (dto.senha() != null && !dto.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dto.senha()));
        }

        if (dto.endereco() != null) {
            EnderecoResponseDTO enderecoDto = dto.endereco();

            Endereco endereco = new Endereco();
            endereco.setRua(enderecoDto.rua());
            endereco.setNumero(enderecoDto.numero());
            endereco.setBairro(enderecoDto.bairro());
            endereco.setCidade(enderecoDto.cidade());
            endereco.setEstado(enderecoDto.estado());
            endereco.setCep(enderecoDto.cep());

            usuario.setEndereco(endereco);
        }

        usuarioRepository.save(usuario);

        return new PerfilResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getSobrenome(),
                usuario.getEmail(),
                usuario.getTipoPessoa().name(),
                usuario.getDocumento(),
                usuario.getTelefone(),
                toEnderecoResponse(usuario.getEndereco())
        );
    }

}

package br.com.uork.appuork.service;

import br.com.uork.appuork.component.DocumentoValidator;
import br.com.uork.appuork.models.TipoPessoa;
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
}
package br.com.uork.appuork.service;

import br.com.uork.appuork.component.DocumentoValidator;
import br.com.uork.appuork.models.TipoPessoa;
import br.com.uork.appuork.models.Usuario;
import br.com.uork.appuork.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final DocumentoValidator validator;
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(DocumentoValidator validator,
                          UsuarioRepository usuarioRepository) {
        this.validator = validator;
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario criarUsuario(Usuario usuario) {
        validator.validar(usuario.getTipoPessoa(), usuario.getDocumento());
        return usuarioRepository.save(usuario);
    }
}
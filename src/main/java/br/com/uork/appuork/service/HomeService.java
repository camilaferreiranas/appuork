package br.com.uork.appuork.service;

import br.com.uork.appuork.dto.home.CategoriaResponseDTO;
import br.com.uork.appuork.dto.home.HomeResponseDTO;
import br.com.uork.appuork.models.Categoria;
import br.com.uork.appuork.models.Usuario;
import br.com.uork.appuork.repository.CategoriaRepository;
import br.com.uork.appuork.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeService {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;

    public HomeService(UsuarioRepository usuarioRepository,
                       CategoriaRepository categoriaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public HomeResponseDTO buscarDadosHome(String email) {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<CategoriaResponseDTO> categorias = categoriaRepository.findAll()
                .stream()
                .map(categoria -> new CategoriaResponseDTO(
                        categoria.getId(),
                        categoria.getNome()
                ))
                .toList();

        return new HomeResponseDTO(usuario.getNome(), categorias);
    }
}

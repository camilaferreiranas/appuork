package br.com.uork.appuork.controller;

import br.com.uork.appuork.common.ApiResponse;
import br.com.uork.appuork.dto.usuario.PerfilResponseDTO;
import br.com.uork.appuork.dto.usuario.UsuarioCriacaoDTO;
import br.com.uork.appuork.dto.usuario.UsuarioResponseDTO;
import br.com.uork.appuork.dto.usuario.UsuarioUpdateDTO;
import br.com.uork.appuork.models.Usuario;
import br.com.uork.appuork.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<Usuario> listarUser(){
        return usuarioService.listarUsuario();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponseDTO>> criar(@RequestBody UsuarioCriacaoDTO dto) {

        Usuario usuarioCriado = usuarioService.criarUsuario(dto);

        UsuarioResponseDTO data = new UsuarioResponseDTO(
                usuarioCriado.getId(),
                usuarioCriado.getNome(),
                usuarioCriado.getEmail()
        );

        ApiResponse<UsuarioResponseDTO> response = new ApiResponse<>(
                true,
                "Usuário cadastrado com sucesso",
                data
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/perfil")
    public ResponseEntity<ApiResponse<PerfilResponseDTO>> buscarPerfil(@RequestParam String email) {
        PerfilResponseDTO data = usuarioService.buscarPerfil(email);

        ApiResponse<PerfilResponseDTO> response = new ApiResponse<>(
                true,
                "Perfil carregado com sucesso",
                data
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/perfil")
    public ResponseEntity<ApiResponse<PerfilResponseDTO>> atualizarPerfil(
            @RequestParam String email,
            @RequestBody UsuarioUpdateDTO dto) {

        PerfilResponseDTO data = usuarioService.atualizarPerfil(email, dto);

        ApiResponse<PerfilResponseDTO> response = new ApiResponse<>(
                true,
                "Perfil atualizado com sucesso",
                data
        );

        return ResponseEntity.ok(response);
    }
}

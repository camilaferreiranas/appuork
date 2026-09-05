package br.com.uork.appuork.controller;

import br.com.uork.appuork.common.ApiResponse;
import br.com.uork.appuork.dto.usuario.FotoUploadConfirmacaoDTO;
import br.com.uork.appuork.dto.usuario.FotoUploadRequestDTO;
import br.com.uork.appuork.dto.usuario.FotoUploadResponseDTO;
import br.com.uork.appuork.dto.usuario.PerfilResponseDTO;
import br.com.uork.appuork.dto.usuario.UsuarioCriacaoDTO;
import br.com.uork.appuork.dto.usuario.UsuarioResponseDTO;
import br.com.uork.appuork.dto.usuario.UsuarioUpdateDTO;
import br.com.uork.appuork.models.Usuario;
import br.com.uork.appuork.service.FotoPerfilStorageService;
import br.com.uork.appuork.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final FotoPerfilStorageService fotoPerfilStorageService;

    public UsuarioController(UsuarioService usuarioService,
                             FotoPerfilStorageService fotoPerfilStorageService) {
        this.usuarioService = usuarioService;
        this.fotoPerfilStorageService = fotoPerfilStorageService;
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
    public ResponseEntity<ApiResponse<PerfilResponseDTO>> buscarPerfil(
            @AuthenticationPrincipal Jwt jwt) {
        PerfilResponseDTO data = usuarioService.buscarPerfil(jwt.getSubject());


        ApiResponse<PerfilResponseDTO> response = new ApiResponse<>(
                true,
                "Perfil carregado com sucesso",
                data
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/perfil")
    public ResponseEntity<ApiResponse<PerfilResponseDTO>> atualizarPerfil(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UsuarioUpdateDTO dto) {

        PerfilResponseDTO data = usuarioService.atualizarPerfil(jwt.getSubject(), dto);

        ApiResponse<PerfilResponseDTO> response = new ApiResponse<>(
                true,
                "Perfil atualizado com sucesso",
                data
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/perfil/foto/upload-url")
    public ResponseEntity<ApiResponse<FotoUploadResponseDTO>> gerarUrlUploadFoto(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody FotoUploadRequestDTO dto) {
        FotoUploadResponseDTO data = fotoPerfilStorageService.gerarUrlUpload(
                jwt.getSubject(),
                dto
        );

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "URL de upload gerada com sucesso",
                data
        ));
    }

    @PatchMapping("/perfil/foto")
    public ResponseEntity<ApiResponse<PerfilResponseDTO>> confirmarFotoPerfil(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody FotoUploadConfirmacaoDTO dto) {
        fotoPerfilStorageService.confirmarUpload(jwt.getSubject(), dto.objectKey());
        PerfilResponseDTO data = usuarioService.buscarPerfil(jwt.getSubject());

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Foto de perfil atualizada com sucesso",
                data
        ));
    }

    @DeleteMapping("/perfil/foto")
    public ResponseEntity<ApiResponse<PerfilResponseDTO>> removerFotoPerfil(
            @AuthenticationPrincipal Jwt jwt) {
        fotoPerfilStorageService.removerFoto(jwt.getSubject());
        PerfilResponseDTO data = usuarioService.buscarPerfil(jwt.getSubject());

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Foto de perfil removida com sucesso",
                data
        ));
    }

}

package br.com.uork.appuork.controller;

import br.com.uork.appuork.common.ApiResponse;
import br.com.uork.appuork.dto.localizacao.LocalizacaoPrestadorDTO;
import br.com.uork.appuork.dto.localizacao.LocalizacaoRequestDTO;
import br.com.uork.appuork.dto.home.listaDemandaDRO;
import br.com.uork.appuork.dto.page.PageResponseDTO;
import br.com.uork.appuork.dto.prestadorServico.*;
import br.com.uork.appuork.service.PrestadorServicoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/prestadores")
public class PrestadorServicoController {

    private final PrestadorServicoService prestadorServicoService;

    public PrestadorServicoController(PrestadorServicoService prestadorServicoService) {
        this.prestadorServicoService = prestadorServicoService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PrestadorResponseDTO>> criarPrestador(
            @RequestParam String email,
            @RequestBody PrestadorCreateDTO dto) {

        PrestadorResponseDTO data = prestadorServicoService.criarPrestador(email, dto);

        ApiResponse<PrestadorResponseDTO> response = new ApiResponse<>(
                true,
                "Prestador criado com sucesso",
                data
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/localizacao")
    public ResponseEntity<ApiResponse<LocalizacaoPrestadorDTO>> atualizarLocalizacao(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody LocalizacaoRequestDTO localizacaoRequest) {

        LocalizacaoPrestadorDTO data = prestadorServicoService.atualizarLocalizacao(
                jwt.getSubject(),
                localizacaoRequest
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Localização do prestador atualizada no cache",
                        data
                )
        );
    }

    @GetMapping("/me/status")
    public ResponseEntity<ApiResponse<Boolean>> verificarCadastroProfissional(
            @AuthenticationPrincipal Jwt jwt) {
        boolean cadastrado = prestadorServicoService
                .usuarioEstaCadastradoComoPrestador(jwt.getSubject());

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Status profissional carregado", cadastrado)
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PrestadorDetalheDTO>> buscarPrestadorPorId(@PathVariable Long id) {

        PrestadorDetalheDTO data = prestadorServicoService.buscarPrestadorPorId(id);

        ApiResponse<PrestadorDetalheDTO> response = new ApiResponse<>(
                true,
                "Detalhes do prestador carregados com sucesso",
                data
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<PrestadorListDTO>>> listarPrestadores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @AuthenticationPrincipal Jwt jwt
    ) {

        Pageable pageable = PageRequest.of(page, size);

        Page<PrestadorListDTO> pagina =
                prestadorServicoService.listarPrestadores(
                        pageable,
                        categoriaId,
                        busca,
                        latitude,
                        longitude,
                        jwt.getSubject()
                );

        PageResponseDTO<PrestadorListDTO> data = new PageResponseDTO<>(
                pagina.getContent(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages(),
                pagina.isFirst(),
                pagina.isLast()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Lista de prestadores", data));
    }

    @GetMapping("/prestadores/{id}/perfil")
    public ResponseEntity<ApiResponse<PerfilPrestadorDTO>> perfil(
            @PathVariable Long id) {

        PerfilPrestadorDTO data =
                prestadorServicoService.buscarPerfil(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Perfil carregado com sucesso",
                        data
                )
        );
    }


}

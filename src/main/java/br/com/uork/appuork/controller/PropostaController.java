package br.com.uork.appuork.controller;

import br.com.uork.appuork.common.ApiResponse;
import br.com.uork.appuork.dto.demanda.DetalheDemandaDTO;
import br.com.uork.appuork.dto.demanda.DemandaProfissionalDTO;
import br.com.uork.appuork.dto.home.listaDemandaDRO;
import br.com.uork.appuork.dto.proposta.PropostaCreateDTO;
import br.com.uork.appuork.dto.proposta.ContatoWhatsAppDTO;
import br.com.uork.appuork.dto.proposta.HistoricoClienteDTO;
import br.com.uork.appuork.dto.proposta.PropostaResponseDTO;
import br.com.uork.appuork.service.PropostaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/propostas")
public class PropostaController {

    private final PropostaService propostaService;

    public PropostaController(PropostaService propostaService) {
        this.propostaService = propostaService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PropostaResponseDTO>> criarProposta(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody PropostaCreateDTO dto) {

        PropostaResponseDTO data = propostaService.criarProposta(dto, jwt.getSubject());

        ApiResponse<PropostaResponseDTO> response = new ApiResponse<>(
                true,
                "Proposta enviada com sucesso",
                data
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/aceitar")
    public ResponseEntity<ApiResponse<PropostaResponseDTO>> aceitar(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        PropostaResponseDTO data = propostaService.aceitarProposta(id, jwt.getSubject());

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Proposta aceita com sucesso",
                data
        ));
    }

    @PatchMapping("/{id}/recusar")
    public ResponseEntity<ApiResponse<PropostaResponseDTO>> recusar(
            @PathVariable Long id,
            @RequestParam String email) {

        PropostaResponseDTO data = propostaService.recusarProposta(id, email);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Proposta recusada com sucesso",
                data
        ));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<PropostaResponseDTO>> cancelar(
            @PathVariable Long id,
            @RequestParam String email) {

        PropostaResponseDTO data = propostaService.cancelarProposta(id, email);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Proposta cancelada com sucesso",
                data
        ));
    }

    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<ApiResponse<PropostaResponseDTO>> finalizar(@PathVariable Long id) {
        PropostaResponseDTO data = propostaService.finalizarProposta(id);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Proposta finalizada com sucesso",
                data
        ));
    }

    @GetMapping("/prestador/demandas")
    public ResponseEntity<ApiResponse<List<DemandaProfissionalDTO>>> listarDemandas(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Demandas carregadas com sucesso",
                propostaService.listarDemandasDoPrestador(jwt.getSubject())
        ));
    }

    @GetMapping("/cliente/historico")
    public ResponseEntity<ApiResponse<List<HistoricoClienteDTO>>> listarHistoricoDoCliente(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Histórico do cliente carregado com sucesso",
                propostaService.listarHistoricoDoCliente(jwt.getSubject())
        ));
    }

    @GetMapping("/{id}/contato-whatsapp")
    public ResponseEntity<ApiResponse<ContatoWhatsAppDTO>> buscarContatoWhatsApp(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Contato do prestador liberado com sucesso",
                propostaService.buscarContatoWhatsApp(id, jwt.getSubject())
        ));
    }

    @GetMapping("/{id}/detalhe-demanda")
    public ResponseEntity<ApiResponse<DetalheDemandaDTO>> buscarDetalheDemanda(@PathVariable Long id) {

        DetalheDemandaDTO data = propostaService.buscarDetalheDemanda(id);

        ApiResponse<DetalheDemandaDTO> response = new ApiResponse<>(
                true,
                "Detalhe da demanda carregado com sucesso",
                data
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/prestadores/{id}")
    public ResponseEntity<ApiResponse<listaDemandaDRO>> homePrestador(@PathVariable Long id) {

        listaDemandaDRO data = propostaService.listaDemanda(id);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Lista de demanda do prestador",
                data
        ));
    }
}

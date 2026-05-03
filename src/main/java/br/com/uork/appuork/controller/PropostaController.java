package br.com.uork.appuork.controller;

import br.com.uork.appuork.common.ApiResponse;
import br.com.uork.appuork.dto.demanda.DetalheDemandaDTO;
import br.com.uork.appuork.dto.proposta.PropostaCreateDTO;
import br.com.uork.appuork.dto.proposta.PropostaResponseDTO;
import br.com.uork.appuork.service.PropostaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/propostas")
public class PropostaController {

    private final PropostaService propostaService;

    public PropostaController(PropostaService propostaService) {
        this.propostaService = propostaService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PropostaResponseDTO>> criarProposta(@RequestBody PropostaCreateDTO dto) {

        PropostaResponseDTO data = propostaService.criarProposta(dto);

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
            @RequestParam String email) {

        PropostaResponseDTO data = propostaService.aceitarProposta(id, email);

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
}

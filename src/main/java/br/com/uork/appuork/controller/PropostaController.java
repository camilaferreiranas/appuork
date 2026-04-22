package br.com.uork.appuork.controller;

import br.com.uork.appuork.common.ApiResponse;
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
}

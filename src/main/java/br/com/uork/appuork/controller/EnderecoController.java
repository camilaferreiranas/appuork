package br.com.uork.appuork.controller;

import br.com.uork.appuork.common.ApiResponse;
import br.com.uork.appuork.dto.endereco.ConsultaCepResponseDTO;
import br.com.uork.appuork.service.CepService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/enderecos")
public class EnderecoController {

    private final CepService cepService;

    public EnderecoController(CepService cepService) {
        this.cepService = cepService;
    }

    @GetMapping("/cep/{cep}")
    public ResponseEntity<ApiResponse<ConsultaCepResponseDTO>> consultarCep(
            @PathVariable String cep) {
        ConsultaCepResponseDTO data = cepService.consultar(cep);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "CEP encontrado", data)
        );
    }
}

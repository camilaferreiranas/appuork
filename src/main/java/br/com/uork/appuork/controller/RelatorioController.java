package br.com.uork.appuork.controller;

import br.com.uork.appuork.dto.relatorio.RelatorioRequestDTO;
import br.com.uork.appuork.dto.relatorio.RelatorioResponseDTO;
import br.com.uork.appuork.service.RelatorioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/relatorio")
public class RelatorioController {


    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }



    @PostMapping
    public ResponseEntity<RelatorioResponseDTO> relatorio(@RequestBody RelatorioRequestDTO dto) {
        return ResponseEntity.ok(relatorioService.gerarRelatorio(dto.email()));
    }
}

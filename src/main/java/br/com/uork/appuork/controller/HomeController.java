package br.com.uork.appuork.controller;

import br.com.uork.appuork.common.ApiResponse;
import br.com.uork.appuork.dto.home.listaDemandaDRO;
import br.com.uork.appuork.dto.home.HomeResponseDTO;
import br.com.uork.appuork.service.HomeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/home")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping
    public HomeResponseDTO buscarDadosHome(@RequestParam String email) {
        return homeService.buscarDadosHome(email);
    }


}



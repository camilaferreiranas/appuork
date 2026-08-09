package br.com.uork.appuork.controller;

import br.com.uork.appuork.dto.home.HomeResponseDTO;
import br.com.uork.appuork.service.HomeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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



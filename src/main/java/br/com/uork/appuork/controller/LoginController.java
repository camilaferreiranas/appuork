package br.com.uork.appuork.controller;


import br.com.uork.appuork.dto.login.LoginRequest;
import br.com.uork.appuork.dto.login.LoginResponse;
import br.com.uork.appuork.service.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }


    @PostMapping
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        var response = loginService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
}

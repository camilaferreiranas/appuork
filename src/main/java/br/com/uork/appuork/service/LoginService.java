package br.com.uork.appuork.service;

import br.com.uork.appuork.dto.login.LoginRequest;
import br.com.uork.appuork.dto.login.LoginResponse;

public interface LoginService {

    LoginResponse login(LoginRequest request);
}

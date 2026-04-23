package br.com.uork.appuork.service;

import br.com.uork.appuork.dto.login.LoginRequest;
import br.com.uork.appuork.dto.login.LoginResponse;
import br.com.uork.appuork.models.Usuario;
import br.com.uork.appuork.repository.UsuarioRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class LoginServiceImpl implements LoginService{

    private final UsuarioRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtEncoder encoder;

    public LoginServiceImpl(UsuarioRepository repository, BCryptPasswordEncoder passwordEncoder, JwtEncoder encoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.encoder = encoder;
    }


    @Override
    public LoginResponse login(LoginRequest request) {

        Usuario usuario = repository.findByEmail(request.email()).orElseThrow(() ->
                new BadCredentialsException("Usuário ou senha inválido"));
        if(!passwordEncoder.matches(request.password(), usuario.getSenha())) {
            throw new BadCredentialsException("Usuário ou senha inválido");
        }

        var now = Instant.now();
        var expiresIn = 3600L;
        var claims = JwtClaimsSet.builder()
                .issuer("appuork")
                .subject(usuario.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn))
                .build();

        var jwt = encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return new LoginResponse(jwt, expiresIn);
    }
}

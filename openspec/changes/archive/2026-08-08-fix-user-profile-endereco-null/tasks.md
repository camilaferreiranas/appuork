## 1. Null-safe profile retrieval

- [x] 1.1 Add a private helper `EnderecoResponseDTO toEnderecoResponse(Endereco endereco)` returning a DTO with null fields when `endereco` is null
- [x] 1.2 Update `buscarPerfil` in `UsuarioService` to use the helper and never dereference a null `Endereco`

## 2. Safe profile update

- [x] 2.1 Update `atualizarPerfil` to only call `setSenha` when `dto.senha()` is non-null and non-blank, always routed through `passwordEncoder.encode(...)`
- [x] 2.2 Update `atualizarPerfil` to only replace the `endereco` when `dto.endereco()` is non-null, otherwise preserve the existing one
- [x] 2.3 Reuse `toEnderecoResponse` in `atualizarPerfil` so the returned profile reflects the null-safe mapping

## 3. Enforced document validation

- [x] 3.1 Change `DocumentoValidator.validar` to throw `DocumentoInvalidoException` when the CPF/CNPJ check fails (or return boolean and let the service throw)
- [x] 3.2 Verify `criarUsuario` rejects invalid documents before persisting (existing `DocumentoInvalidoException` handler returns HTTP 400) — covered by `UsuarioServiceTest.criarUsuarioRejeitaDocumentoInvalido`
- [x] 3.3 Confirm registration of a valid CPF still succeeds and password is BCrypt-encoded — covered by `UsuarioServiceTest.criarUsuarioValido`

## 4. Verification

- [x] 4.1 Build the project (`mvnw compile` + unit tests pass; 15/15 green)
- [ ] 4.2 Smoke-test in runtime: register a user without address, then `POST /login` followed by `GET /usuario/perfil?email=` succeeds (no 500) — requires live backend on LAN (192.168.15.27:8080); not runnable from this environment
- [ ] 4.3 Smoke-test login after profile update with password-only and address-only payloads — requires live backend on LAN; not runnable from this environment
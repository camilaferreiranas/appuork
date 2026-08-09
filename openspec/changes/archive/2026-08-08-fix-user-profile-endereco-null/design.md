## Context

`Usuario` persists `endereco` as a nullable `@Embedded` value. `criarUsuario` only sets an `Endereco` when the DTO includes one, so users registered through the mobile app (which never sends `endereco`) are stored with a null address. Two service methods (`buscarPerfil`, `atualizarPerfil`) dereference `usuario.getEndereco()` unconditionally, causing a `NullPointerException` mapped by `GlobalControllerAdvice` to HTTP 500. Because the mobile app chains `POST /login` → `GET /usuario/perfil` inside a single login call, fresh accounts appear to have a broken login.

Two adjacent defects were found while tracing this: `atualizarPerfil` persists the raw password (`dto.senha()` unencoded, wiping it to null when blank), and `DocumentoValidator.validar` is `void` so it ignores the boolean result of the CPF/CNPJ checks.

## Goals / Non-Goals

**Goals:**
- `buscarPerfil` and `atualizarPerfil` succeed for users with no `endereco`.
- Password updates are optional and always BCrypt-hashed.
- Invalid documents are rejected at registration.
- Zero API contract changes; response shapes identical except `endereco` may be `null`.

**Non-Goals:**
- Changing the `/login` endpoint or JWT flow.
- Altering the `Endereco` model/storage schema.
- Frontend changes (handled in a separate mobile change).

## Decisions

**D1 — Null-safe mapper in `buscarPerfil`.**
Instead of dereferencing `usuario.getEndereco()`, check for null and build `EnderecoResponseDTO` with null fields when absent. Consider a shared private helper `toEnderecoResponse(Endereco e)` reused by `buscarPerfil` and `atualizarPerfil` to keep responses consistent.
- *Alternative:* always initialize an empty `Endereco` at creation in `criarUsuario` (storing null address columns explicitly). Rejected: leaves the stored data shape ambiguous and changes persist behavior; safer to treat address as genuinely optional.

**D2 — Conditional + hashed password update in `atualizarPerfil`.**
Only call `setSenha` when `dto.senha()` is non-null and non-blank, and always route it through `passwordEncoder.encode(...)`. This preserves existing passwords when the client omits a new one and keeps stored values BCrypt-compatible for `LoginServiceImpl.login`.
- Alternative: keep overwriting raw. Rejected: breaks subsequent `passwordEncoder.matches`.

**D3 — Conditional endereco update in `atualizarPerfil`.**
Only build/replace the `Endereco` when `dto.endereco()` is non-null (with all fields non-blank), otherwise leave the existing address untouched. Prevents a profile update from wiping a previously saved address.

**D4 — Effective document validation in `DocumentoValidator`.**
Change `validar(...)` to return the boolean and have `criarUsuario` throw `DocumentoInvalidoException` when it returns false (or have `validar` throw directly). The `GlobalControllerAdvice` already maps `DocumentoInvalidoException` to HTTP 400, so the error contract is preserved.
- Alternative: annotate the request field with Bean Validation `@Pattern`. Rejected: mixing validation approaches; the component already exists and the advice mapping is in place.

## Risks / Trade-offs

- [Existing stored users may already have garbage `endereco` or raw passwords] → The reads become null-safe regardless; password corruption affects only users changed after deploy, as new writes are corrected.
- [Tests may exist asserting current failure behavior] → No tests reference these paths yet (repo has no test sources for these services); add unit tests to lock in null-safety.
- [`.matches()` on regex requires exact format] → `validarCPF`/`validarCNPJ` already anchor the whole string; `.matches()` matches the entire input, so rejection of punctuation/lenient formats becomes default and is working as intended.
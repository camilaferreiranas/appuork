## Why

Freshly registered users cannot log in afterward. The `/login` endpoint succeeds (credentials are valid), but the app-immediately-following profile call `GET /usuario/perfil` throws a `NullPointerException` because newly created accounts have no `endereco`, and `UsuarioService.buscarPerfil` dereferences it unconditionally. The error is surfaced as a failed login in the mobile app.

## What Changes

- Make `UsuarioService.buscarPerfil` null-safe: build the `EnderecoResponseDTO` regardless of whether the user has an `endereco` (return `null` fields when absent).
- Make `UsuarioService.atualizarPerfil` null-safe: only update the endereco when a valid one is provided; **BREAKING** fix it also stop persisting the raw password — it currently stores `dto.senha()` unhashed and can wipe the stored password to `null` when the field is blank, which breaks subsequent logins.
- Make `DocumentoValidator` actually enforce validation: it currently returns `void` and silently ignores the boolean result of the CPF/CNPJ checks, so invalid documents are persisted.
- No endpoint/API contract changes: request/response shapes stay the same, but `perfil` responses may now carry a `null` `endereco`.

## Capabilities

### New Capabilities
- `user-registration`: Registering a user (email/password/document) with real CPF/CNPJ validation, duplicate checks, and correct password hashing at creation.
- `user-profile`: Reading and updating a user's profile, tolerating the absence of an `endereco` and preserving unset passwords.

### Modified Capabilities
<!-- No existing specs in this repo; all listed above are new. -->

## Impact

- Backend code: `br.com.uork.appuork.service.UsuarioService` (`buscarPerfil`, `atualizarPerfil`, `criarUsuario`), `br.com.uork.appuork.component.DocumentoValidator`.
- API behavior: `GET /usuario/perfil` and `PUT /usuario/perfil` no longer return 500 for users without an address; password updates become BCrypt-hashed and optional.
- Consumers: mobile app `services/api.ts` (`getUserProfile`, `updateUserProfile`) and `contexts/auth-context.tsx` rely on this endpoint — the bug fix unblocks login for fresh accounts.
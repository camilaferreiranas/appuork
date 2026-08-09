## ADDED Requirements

### Requirement: Profile read tolerates users without an address
The system SHALL return the profile of any existing user. When the user has no `endereco`, the response MUST contain an `endereco` payload with all-null fields instead of failing with an error.

#### Scenario: Read profile of a user without address
- **WHEN** a GET `/usuario/perfil?email=<email>` is called for a user whose `endereco` is null
- **THEN** the response is 200 OK with an `endereco` payload holding only null fields and the user's basic fields populated

#### Scenario: Read profile of a user with address
- **WHEN** a GET `/usuario/perfil?email=<email>` is called for a user whose `endereco` is populated
- **THEN** the response is 200 OK containing the full address data

### Requirement: Profile update is safe when address or password is missing
The system SHALL update a profile without requiring an address and without breaking the stored password. A null or blank `senha` MUST NOT overwrite the existing password; when a password is provided it MUST be BCrypt-encoded before being persisted.

#### Scenario: Update profile without address or password
- **WHEN** a PUT `/usuario/perfil?email=<email>` is sent with `endereco: null` and `senha: null`
- **THEN** the existing password and address remain unchanged and the response is 200 OK

#### Scenario: Update password only
- **WHEN** a PUT `/usuario/perfil?email=<email>` is sent with a new `senha` and no address
- **THEN** the password is stored BCrypt-encoded and the user can subsequently log in with the new password

#### Scenario: Update address only
- **WHEN** a PUT `/usuario/perfil?email=<email>` is sent with a full `endereco` and a null `senha`
- **THEN** the address is updated, the password is unchanged, and the response contains the new address
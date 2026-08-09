# Capability: user-registration

## Purpose

Register email/password users with real CPF/CNPJ validation, duplicate checks, and BCrypt-hashed passwords stored on creation. TBD details as the product evolves.

## Requirements

### Requirement: User registration with valid document and hashed password

The system SHALL register a new user only when the email and document are not already in use, the document matches the declared `tipoPessoa` (CPF or CNPJ) format, and the password is stored BCrypt-hashed. Invalid documents MUST NOT be persisted.

#### Scenario: Register with valid CPF
- **WHEN** a POST `/usuario` is sent with `tipoPessoa=CPF` and a valid 11-digit CPF
- **THEN** the user is created, the password is BCrypt-encoded, and the response is 201 CREATED

#### Scenario: Register with invalid document format
- **WHEN** a POST `/usuario` is sent with a document that does not match the declared `tipoPessoa` pattern
- **THEN** the registration fails with HTTP 400 and the document is NOT persisted

#### Scenario: Duplicate email
- **WHEN** a POST `/usuario` is sent with an email already registered
- **THEN** the system returns an error and no user is created

### Requirement: Registration tolerates the absence of an address

The system SHALL create a user when `endereco` is not provided, storing no address and leaving the embedded address fields null.

#### Scenario: Signup without address
- **WHEN** a POST `/usuario` is sent without an `endereco` payload
- **THEN** the user is stored with a null `endereco` and the creation succeeds
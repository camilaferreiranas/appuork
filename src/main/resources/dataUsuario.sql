INSERT INTO usuario (nome, sobrenome, email, senha, tipo_pessoa, documento, data_criacao)
SELECT 'Carlos', 'Vale', 'carlos@email.com', '123456', 'CPF', '52998224725', CURRENT_TIMESTAMP
    WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'carlos@email.com');

INSERT INTO usuario (nome, sobrenome, email, senha, tipo_pessoa, documento, data_criacao)
SELECT 'Camila', 'Silva', 'Camila@email.com', '123456', 'CPF', '11144477735', CURRENT_TIMESTAMP
    WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'Camila@email.com');

INSERT INTO usuario (nome, sobrenome, email, senha, tipo_pessoa, documento, data_criacao)
SELECT 'Empresa X', 'LTDA', 'empresa@email.com', '123456', 'CNPJ', '11222333000181', CURRENT_TIMESTAMP
    WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'empresa@email.com');
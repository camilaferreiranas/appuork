INSERT INTO usuario (nome, sobrenome, email, senha, tipo_pessoa, documento)
SELECT 'Carlos', 'Vale', 'carlos@email.com', '123456', 'CPF', '52998224725'
    WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'carlos@email.com');

INSERT INTO usuario (nome, sobrenome, email, senha, tipo_pessoa, documento)
SELECT 'Camila', 'Silva', 'Camila@email.com', '123456', 'CPF', '11144477735'
    WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'Camila@email.com');

INSERT INTO usuario (nome, sobrenome, email, senha, tipo_pessoa, documento)
SELECT 'Empresa X', 'LTDA', 'empresa@email.com', '123456', 'CNPJ', '11222333000181'
    WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE email = 'empresa@email.com');
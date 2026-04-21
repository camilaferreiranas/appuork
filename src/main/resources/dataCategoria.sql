INSERT INTO categoria (nome)
SELECT 'Eletrônica'
    WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = 'Eletrônica');

INSERT INTO categoria (nome)
SELECT 'Beleza'
    WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = 'Beleza');

INSERT INTO categoria (nome)
SELECT 'Limpeza'
    WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = 'Limpeza');

INSERT INTO categoria (nome)
SELECT 'Pintura'
    WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = 'Pintura');

INSERT INTO categoria (nome)
SELECT 'Serviços'
    WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = 'Serviços');

INSERT INTO categoria (nome)
SELECT 'Instalação'
    WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = 'Instalação');

INSERT INTO categoria (nome)
SELECT 'Jardinagem'
    WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = 'Jardinagem');

INSERT INTO categoria (nome)
SELECT 'Reparo'
    WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nome = 'Reparo');
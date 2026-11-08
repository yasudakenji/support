BEGIN;

CREATE TABLE IF NOT EXISTS setor (
    id uuid PRIMARY KEY,
    nome varchar(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS equipamento (
    id uuid PRIMARY KEY,
    nome varchar(50) NOT NULL,
    modelo varchar(50) NOT NULL,
    data_cadastro date NOT NULL,
    horimetro_atual numeric(10,2) NOT NULL,
    setor_id uuid REFERENCES setor(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS plano_manutencao (
    id uuid PRIMARY KEY,
    equipamento_id uuid NOT NULL REFERENCES equipamento(id) ON DELETE CASCADE,
    nome_plano varchar(50) NOT NULL,
    intervalo_horas numeric(10,2) NOT NULL,
    descricao varchar(500) NOT NULL,
    situacao_historico_inicial varchar(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS manutencao (
    id uuid PRIMARY KEY,
    plano_manutencao_id uuid NOT NULL REFERENCES plano_manutencao(id) ON DELETE CASCADE,
    data_manutencao date NOT NULL,
    horimetro_execucao numeric(10,2) NOT NULL,
    descricao_execucao varchar(1000)
);

CREATE TABLE IF NOT EXISTS leitura_horimetro (
    id uuid PRIMARY KEY,
    equipamento_id uuid NOT NULL REFERENCES equipamento(id) ON DELETE CASCADE,
    data_leitura timestamp without time zone NOT NULL,
    valor_horimetro numeric(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS usuario (
    id uuid PRIMARY KEY,
    nome varchar(50) NOT NULL,
    email varchar(254) NOT NULL UNIQUE,
    senha_hash varchar(255) NOT NULL,
    perfil varchar(20) NOT NULL
);

COMMIT;

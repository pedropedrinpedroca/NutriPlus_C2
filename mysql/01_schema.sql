DROP DATABASE IF EXISTS nutriplus;
CREATE DATABASE nutriplus CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE nutriplus;

CREATE TABLE usuarios (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  nome         VARCHAR(120) NOT NULL,
  sexo         ENUM('M','F') NOT NULL,
  idade        SMALLINT NOT NULL CHECK (idade BETWEEN 12 AND 100),
  altura_cm    SMALLINT NOT NULL CHECK (altura_cm BETWEEN 120 AND 230),
  peso_kg      DECIMAL(5,2) NOT NULL CHECK (peso_kg BETWEEN 30 AND 300),
  atividade    ENUM('SED','LEV','MOD','ALT','ATH') NOT NULL,
  objetivo     ENUM('MANUT','BULK','CUT') NOT NULL,
  perc_gordura DECIMAL(5,2),
  criado_em    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE alimentos (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  nome         VARCHAR(160) NOT NULL,
  kcal_100g    DECIMAL(6,2) NOT NULL,
  prot_100g    DECIMAL(6,2) NOT NULL,
  carb_100g    DECIMAL(6,2) NOT NULL,
  gord_100g    DECIMAL(6,2) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE refeicoes (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  usuario_id   BIGINT NOT NULL,
  dia_mes      DATE NOT NULL,
  nome         ENUM('CAFEMANHA','ALMOCO','JANTAR','LANCHE','PRE','POS','OUTRO') NOT NULL,
  CONSTRAINT fk_refeicoes_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
  INDEX idx_ref_usuario_dia_mes (usuario_id, dia_mes)
) ENGINE=InnoDB;

CREATE TABLE consumos (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  refeicao_id  BIGINT NOT NULL,
  alimento_id  BIGINT NOT NULL,
  quantidade_g DECIMAL(8,2) NOT NULL CHECK (quantidade_g > 0),
  logado_em    DATETIME NOT NULL,
  CONSTRAINT fk_consumos_refeicao FOREIGN KEY (refeicao_id) REFERENCES refeicoes(id) ON DELETE RESTRICT,
  CONSTRAINT fk_consumos_alimento FOREIGN KEY (alimento_id) REFERENCES alimentos(id) ON DELETE RESTRICT,
  INDEX idx_cons_refeicao (refeicao_id),
  INDEX idx_cons_alimento (alimento_id)
) ENGINE=InnoDB;

CREATE TABLE registros_agua (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  usuario_id   BIGINT NOT NULL,
  ml           INT NOT NULL CHECK (ml > 0),
  logado_em    DATETIME NOT NULL,
  CONSTRAINT fk_agua_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT,
  INDEX idx_agua_usuario_dia_mes (usuario_id, logado_em)
) ENGINE=InnoDB;

CREATE TABLE metas_diarias (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  usuario_id     BIGINT NOT NULL,
  dia_mes        DATE NOT NULL,
  tdee_kcal      DECIMAL(7,2) NOT NULL,
  meta_kcal      DECIMAL(7,2) NOT NULL,
  meta_prot_g    DECIMAL(6,2) NOT NULL,
  meta_carb_g    DECIMAL(6,2) NOT NULL,
  meta_gord_g    DECIMAL(6,2) NOT NULL,
  meta_agua_ml   INT NOT NULL,
  UNIQUE KEY uq_usuario_dia_mes (usuario_id, dia_mes),
  CONSTRAINT fk_metas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE OR REPLACE VIEW v_kcal_por_consumo AS
SELECT
  c.id,
  c.refeicao_id,
  r.usuario_id,
  DATE(c.logado_em) AS dia,
  (c.quantidade_g * a.kcal_100g / 100.0) AS kcal,
  (c.quantidade_g * a.prot_100g / 100.0) AS prot_g,
  (c.quantidade_g * a.carb_100g / 100.0) AS carb_g,
  (c.quantidade_g * a.gord_100g / 100.0) AS gord_g
FROM consumos c
JOIN refeicoes r  ON r.id = c.refeicao_id
JOIN alimentos a  ON a.id = c.alimento_id;

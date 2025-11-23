USE nutriplus;

INSERT INTO usuarios (nome, sexo, idade, altura_cm, peso_kg, atividade, objetivo, perc_gordura)
VALUES ('Fulano', 'M', 23, 171, 96, 'SED', 'CUT', NULL);

INSERT INTO alimentos (nome, kcal_100g, prot_100g, carb_100g, gord_100g) VALUES
('Arroz branco cozido', 129, 2.7, 28.0, 0.3),
('Feijão carioca cozido', 76, 4.8, 13.6, 0.5),
('Peito de frango grelhado', 165, 31.0, 0.0, 3.6),
('Banana prata', 89, 1.1, 23.0, 0.3),
('Aveia em flocos', 389, 16.9, 66.3, 6.9),
('Azeite de oliva', 884, 0.0, 0.0, 100.0);

SET @u := 1;
INSERT INTO refeicoes (usuario_id, dia_mes, nome) VALUES
(@u, CURDATE(), 'CAFEMANHA'),
(@u, CURDATE(), 'ALMOCO'),
(@u, CURDATE(), 'JANTAR');

INSERT INTO consumos (refeicao_id, alimento_id, quantidade_g, logado_em) VALUES
((SELECT id FROM refeicoes WHERE usuario_id=@u AND dia_mes=CURDATE() AND nome='CAFEMANHA'), 5, 30,  NOW()),
((SELECT id FROM refeicoes WHERE usuario_id=@u AND dia_mes=CURDATE() AND nome='ALMOCO'),    1, 150, NOW()),
((SELECT id FROM refeicoes WHERE usuario_id=@u AND dia_mes=CURDATE() AND nome='ALMOCO'),    2, 100, NOW()),
((SELECT id FROM refeicoes WHERE usuario_id=@u AND dia_mes=CURDATE() AND nome='ALMOCO'),    3, 120, NOW());

INSERT INTO registros_agua (usuario_id, ml, logado_em) VALUES
(@u, 300, NOW()),
(@u, 500, NOW());

INSERT INTO metas_diarias (usuario_id, dia_mes, tdee_kcal, meta_kcal, meta_prot_g, meta_carb_g, meta_gord_g, meta_agua_ml)
VALUES
(@u, CURDATE(), 2500, 2500, 187.5, 250.0, 83.3, 2888)
ON DUPLICATE KEY UPDATE dia_mes=dia_mes;

SELECT
  (SELECT COUNT(*) FROM usuarios)          AS total_usuarios,
  (SELECT COUNT(*) FROM alimentos)         AS total_alimentos,
  (SELECT COUNT(*) FROM refeicoes)         AS total_refeicoes,
  (SELECT COUNT(*) FROM consumos)          AS total_consumos,
  (SELECT COUNT(*) FROM registros_agua)    AS total_registros_agua,
  (SELECT COUNT(*) FROM metas_diarias)     AS total_metas_diarias;

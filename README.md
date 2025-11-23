# README.md

> **Projeto:** NutriPlus — Sistema nutricional em linha de comando (CLI), com **MySQL** e **JDBC (sem ORM)**.
>
> **Disciplina:** Banco de Dados — **Período:** 2025/2
>
> **Professor:** Howard Cruz Roatti
>
> **Equipe:** Bruna Santos Soares Aguiar; Pedro Henrique da Silva Teixeira; Pedro Henrique Pontes Pereira

---

## 1) Sobre o sistema

Aplicativo de console para registro de refeições, consumos e água, cálculo de TDEE e metas diárias (kcal e macronutrientes), e emissão de relatórios (agregação e join). Implementado em **Java 17+** com **JDBC puro** para **MySQL 8+**. Não utiliza ORM.

---

## 2) Atende ao edital — onde está cada item

* **Scripts do banco (criação e carga inicial):** `mysql/01_schema.sql` e `mysql/02_seed.sql`.
* **Diagrama relacional:** `nutriplus_er.drawio` (e `.png`/`.pdf`).
* **Programa com menu CLI:** `src/NutriPlus/*` (classe principal `NutriPlus.Main`). Menus: **Relatórios**, **Inserir**, **Remover**, **Atualizar**, **Sair**.
* **Splash screen:** ao iniciar, exibe nome do sistema, contagem de registros por tabela e identificação do grupo.
* **Relatórios:**

  * (i) **Agregação** – "Calorias por dia" (soma e `GROUP BY`).
  * (ii) **Join** – "Itens da refeição" (consumos × alimentos).
* **Operações CRUD exigidas:** fluxos de **Inserir**, **Remover** (com confirmação e atenção a restrições/cascata) e **Atualizar** (mostra o registro atualizado ao final), cobrindo as entidades exigidas.
* **Repositório GitHub:** *https://github.com/pedropedrinpedroca/NutriPlus_C2*.

---

### 3) VS Code — extensões necessárias

* **Extension Pack for Java** (`ms-vscode.java-pack`)
* **Language Support for Java by Red Hat** (`redhat.java`)
* **Debugger for Java** (`vscjava.vscode-java-debug`)
* **Java Test Runner** (`vscjava.vscode-java-test`)

> No VS Code, abra a pasta do projeto (**File → Open Folder…**), depois use o **Terminal integrado** (**Terminal → New Terminal**) para executar os comandos de build/execução deste README.

* **Linux** (testado em Ubuntu 22.04+)
* **JDK 17+**
* **MySQL 8+** (server + client)

```bash
# Ubuntu/Debian
sudo apt update && sudo apt install -y default-jdk mysql-server mysql-client
```

---

### 4) Ubuntu — instalar MySQL rapidamente

```bash
# 1) Instalar e iniciar o MySQL
sudo apt update
sudo apt install -y mysql-server
sudo systemctl enable --now mysql
sudo systemctl status mysql --no-pager
```

Criar banco e usuário de aplicação (root via socket do Ubuntu):

```bash
# abre o shell do MySQL como root (unix_socket)
sudo mysql
```

Dentro do prompt do MySQL, execute:

```sql
CREATE DATABASE IF NOT EXISTS nutriplus
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'nutriplus'@'%' IDENTIFIED BY 'nutri123';
GRANT ALL PRIVILEGES ON nutriplus.* TO 'nutriplus'@'%';
FLUSH PRIVILEGES;
EXIT;
```

Importar **schema** e **seed** (na raiz do projeto):

```bash
mysql -u nutriplus -p'nutri123' nutriplus < mysql/01_schema.sql
mysql -u nutriplus -p'nutri123' nutriplus < mysql/02_seed.sql
```

> Todos os comandos abaixo são executados na raiz do projeto (diretório `NutriPlus/`).

### 4.1) Criar banco e usuário

Entre no MySQL como **root** e crie o schema/usuário (ajuste a senha se desejar):

```bash
mysql -u root -p
```

```sql
-- (opcional) apaga o database se já existir
DROP DATABASE IF EXISTS nutriplus;

-- cria o database com charset/collation modernos
CREATE DATABASE nutriplus
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

-- cria usuário de aplicação e dá permissão (ajuste a senha)
CREATE USER IF NOT EXISTS 'nutriplus'@'%' IDENTIFIED BY 'nutri123';
GRANT ALL PRIVILEGES ON nutriplus.* TO 'nutriplus'@'%';
FLUSH PRIVILEGES;
```

Saia do MySQL (`\q`).

### 4.2) Executar scripts de **schema** e **seed**

```bash
mysql -u nutriplus -p'nutri123' nutriplus < mysql/01_schema.sql
mysql -u nutriplus -p'nutri123' nutriplus < mysql/02_seed.sql
```

> Também é possível rodar como root (substitua o usuário/senha acima por `-u root -p`).

### 4.3 Variáveis de ambiente (opcional)

O app usa as variáveis abaixo (com **defaults** se não definir):

* `NP_DB_HOST` — host do MySQL (default: `127.0.0.1`)
* `NP_DB_PORT` — porta (default: `3306`)
* `NP_DB_NAME` — nome do banco (default: `nutriplus`)
* `NP_DB_USER` — usuário (default: `nutriplus`)
* `NP_DB_PASSWORD` — senha (default: `nutri123`)

```bash
export NP_DB_HOST=127.0.0.1
export NP_DB_PORT=3306
export NP_DB_NAME=nutriplus
export NP_DB_USER=nutriplus
export NP_DB_PASSWORD=nutri123
```

### 4.4) Compilar

O driver JDBC já está em `lib/mysql-connector-j-9.5.0.jar`.

```bash
mkdir -p out
javac -cp lib/mysql-connector-j-9.5.0.jar -d out $(find src -name "*.java")
```

### 4.5) Executar

Classe principal: **`NutriPlus.Main`**

```bash
java -cp out:lib/mysql-connector-j-9.5.0.jar NutriPlus.Main
```

Ao iniciar, o programa mostra a **splash screen** (nome do sistema, contagens por tabela e identificação da equipe) e, em seguida, o **menu principal**.

---

## 5) Estrutura do repositório

```
NutriPlus/
├─ .vscode/
│  ├─ launch.json
│  └─ settings.json
├─ lib/
│  └─ mysql-connector-j-9.5.0.jar
├─ mysql/
│  ├─ 01_schema.sql
│  └─ 02_seed.sql
├─ src/
│  └─ NutriPlus/
│     ├─ connection/
│     │  └─ MySQLConnection.java
│     ├─ controller/
│     │  └─ AppController.java
│     ├─ model/
│     │  ├─ Alimento.java
│     │  ├─ Consumo.java
│     │  ├─ MetaDiaria.java
│     │  ├─ NivelAtividade.java
│     │  ├─ NomeRefeicao.java
│     │  ├─ Objetivo.java
│     │  ├─ Refeicao.java
│     │  ├─ RegistroAgua.java
│     │  └─ Usuario.java
│     ├─ reports/
│     │  └─ RelatoriosDAO.java
│     ├─ sql/
│     │  ├─ AlimentosDAO.java
│     │  ├─ ConsumosDAO.java
│     │  ├─ MetaDiariasDAO.java
│     │  ├─ RefeicoesDAO.java
│     │  ├─ RegistrosAguaDAO.java
│     │  └─ UsuariosDAO.java
│     ├─ utils/
│     │  ├─ InputUtils.java
│     │  └─ TDEEUtils.java
│     └─ Main.java
├─ nutriplus_er.drawio
├─ nutriplus_er.pdf
├─ nutriplus_er.png
└─ README.md
```

---

## 6) Vídeo demonstrativo (YouTube)

> **https://youtu.be/SphZ5I93iCI**

---

## 7) Dicas e resolução de problemas

* **Conexão recusada:** garanta que o MySQL está rodando (`systemctl status mysql`) e que o host/porta estão corretos.
* **Driver JDBC não encontrado:** confirme `lib/mysql-connector-j-9.5.0.jar` e classpath no `javac/java`.
* **Permissão negada:** confira usuário/senha e privilégios (`GRANT ALL PRIVILEGES ON nutriplus.* TO 'nutriplus'@'%'`).
* **Charset:** o schema usa `utf8mb4`. Se ver caracteres estranhos no terminal, ajuste a fonte/locale.

---

## 8) Licença

Uso acadêmico/educacional.

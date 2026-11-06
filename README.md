# SIGEP — Sistema de Gerenciamento de Pacientes

**Versão 1.0.0 — versão estável para demonstração acadêmica do Checkpoint 3**

O SIGEP é um projeto acadêmico da Universidade de Pernambuco (UPE) voltado à organização de cadastros e processos administrativos de pacientes do CESMUC, em Garanhuns (PE). Esta versão disponibiliza a estrutura inicial do backend REST e a persistência em PostgreSQL, com esquema versionado pelo Flyway.

> **Escopo:** a classificação “estável” refere-se ao ambiente e aos fluxos básicos demonstrados no Checkpoint 3. Esta versão não deve ser considerada pronta para produção ou uso com dados reais de pacientes: autenticação, autorização, proteção de informações sensíveis e testes adicionais ainda são necessários.

## Tecnologias

- Java 21; Spring Boot 4.0.8; Maven Wrapper
- Spring Data JPA / Hibernate; Jakarta Bean Validation
- PostgreSQL 17; Flyway
- Docker e Docker Compose
- Insomnia para testes HTTP; DBeaver para inspeção do banco
- Frontend React previsto no projeto (não é necessário para este roteiro de backend)

## Estrutura relevante

```text
.
├── compose.yaml
├── .env.example
├── sigep-backend/
│   ├── Dockerfile
│   ├── mvnw
│   ├── pom.xml
│   └── src/main/
│       ├── java/br/edu/upe/sigep/
│       └── resources/
│           ├── application.yaml
│           ├── application-dev.yaml
│           └── db/migration/
│               └── V1__criacao_inicial_do_banco.sql
├── sigep-frontend/
└── docs/
```

## Pré-requisitos

- Git instalado para clonar o repositório.

- Docker Engine e Docker Compose Plugin instalados e funcionando.
- DBeaver e Insomnia, opcionais para execução, recomendados para demonstração.
- Portas **5433** e **8080** livres no host.
- Arquivo `.env` configurado na raiz do repositório, com base no `.env.example`. **Nunca versione `.env` nem compartilhe senhas.**

A aplicação usa a rede interna do Compose para conectar o backend ao PostgreSQL em `db:5432`; o DBeaver acessa o banco pela porta publicada `127.0.0.1:5433`.

## Instalação rápida — a partir de um clone novo

O caminho recomendado para iniciar **backend e PostgreSQL juntos** é `docker compose up -d --build`. O Docker Compose cria a rede e o volume necessários; o Flyway executa automaticamente a migration V1 quando o backend inicia. **Não é preciso executar o SQL manualmente no DBeaver.**

### 1. Clonar o repositório

```bash
git clone <URL_DO_REPOSITORIO>
cd <DIRETORIO_CLONADO>
```

Substitua os campos entre `< >` pela URL e pelo diretório reais do projeto. Todos os comandos seguintes devem ser executados na pasta que contém `compose.yaml`.

### 2. Configurar variáveis de ambiente

```bash
cp .env.example .env
```

Abra `.env` e preencha as variáveis exigidas pelo `compose.yaml` e pelos perfis Spring. Em particular, confira `LOCAL_DB_PASSWORD`, `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`. No ambiente Docker, o endereço JDBC deve apontar para o serviço PostgreSQL na rede interna, por exemplo `jdbc:postgresql://db:5432/sigep`, **não** para `localhost:5433`. Use os nomes e valores efetivamente definidos no `.env.example` e no Compose; não publique senhas.

### 3. Construir e iniciar todos os serviços

```bash
docker compose config --quiet
docker compose up -d --build
```

O `--build` constrói a imagem do backend e o `-d` deixa os serviços executando em segundo plano. Na primeira execução com volume vazio, o PostgreSQL inicializa o banco e o Flyway aplica a migration `V1__criacao_inicial_do_banco.sql`. O Hibernate verifica a compatibilidade do esquema (`ddl-auto: validate`). A aplicação expõe a API em `http://localhost:8080`.

**Condição importante:** para que o comando único seja confiável, `backend` deve depender de `db` com `condition: service_healthy`, e `db` deve possuir um `healthcheck` funcional. O PostgreSQL já foi observado como `healthy` neste projeto, mas **confira a cláusula `depends_on` no `compose.yaml`**. Sem essa condição, o Compose pode iniciar o backend antes de o banco aceitar conexões. O comando único é o fluxo desejado, mas deve ser revalidado após um clone limpo.

### 4. Confirmar a inicialização

```bash
docker compose ps
docker compose logs --tail=100 backend
```

Nos logs, procure `Successfully applied 1 migration` (na primeira inicialização) e `Started SigepBackendApplication`. Se algum serviço falhar, acompanhe os logs:

```bash
docker compose logs -f backend
docker compose logs --tail=100 db
```

Em seguida, teste a API:

```bash
curl -i http://localhost:8080/api/especialidades
```

Num banco novo, a resposta esperada é `HTTP/1.1 200` com `[]`. O build atual usa `-DskipTests`: subir os contêineres não substitui a execução da suíte de testes.

### 5. Confirmar as tabelas no DBeaver (opcional)

Conecte ao PostgreSQL com os parâmetros abaixo:

| Parâmetro | Valor |
|---|---|
| Host | `localhost` |
| Porta | `5433` |
| Database | `sigep` |
| Usuário | `sigep` |
| Senha | valor de `LOCAL_DB_PASSWORD` no `.env` |

No editor SQL, execute:

```sql
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
ORDER BY table_name;

SELECT installed_rank, version, description, type, success
FROM public.flyway_schema_history
ORDER BY installed_rank;
```

O esperado após a primeira inicialização bem-sucedida é encontrar **14 tabelas** (13 do domínio e `flyway_schema_history`) e a versão 1 com `success = true`.

### Comandos úteis no dia a dia

```bash
# Parar serviços sem excluir o banco
docker compose down

# Reiniciar serviços existentes, preservando dados
docker compose up -d

# Reconstruir e iniciar após alterações no código
docker compose up -d --build

# Acompanhar logs do backend
docker compose logs -f backend

# Consultar o estado dos serviços
docker compose ps
```

O volume PostgreSQL persiste entre `down` e `up`. O Flyway não reaplica uma migration já registrada com sucesso.

### Diagnóstico manual (somente quando necessário)

Caso a inicialização automática falhe ou você queira observar cada etapa da primeira migração:

```bash
docker compose build backend
docker compose up -d db
docker compose ps
docker compose up --no-build backend
```

O último comando mostra os logs do backend em primeiro plano. Esse procedimento foi usado na validação inicial do SIGEP, mas **não é exigido para o uso normal**.

## Demonstração de API no Insomnia

Configure a URL base `http://localhost:8080`. Crie uma coleção `SIGEP — Checkpoint 3`. Para requisições POST e PUT, use `Body → JSON` e `Content-Type: application/json`.

### Especialidade — roteiro principal

| Etapa | Método | Caminho | Resultado esperado |
|---|---|---|---|
| Consultar banco vazio | GET | `/api/especialidades` | `200 OK`, `[]` |
| Cadastrar | POST | `/api/especialidades` | `201 Created`, registro com ID |
| Listar | GET | `/api/especialidades` | `200 OK`, registro incluído |
| Consultar por ID | GET | `/api/especialidades/{id}` | `200 OK` |
| Atualizar | PUT | `/api/especialidades/{id}` | `200 OK` |
| Inativar | PATCH | `/api/especialidades/{id}/ativo?ativo=false` | `200 OK` |
| Listar ativas | GET | `/api/especialidades/ativas` | Registro inativo não aparece |
| Reativar | PATCH | `/api/especialidades/{id}/ativo?ativo=true` | `200 OK` |

Exemplo de POST:

```json
{
  "nome": "Fisioterapia",
  "descricao": "Atendimento fisioterapeutico"
}
```

Exemplo de PUT (substitua `{id}` pelo identificador retornado no POST):

```json
{
  "nome": "Fisioterapia",
  "descricao": "Atendimento fisioterapeutico especializado"
}
```

No DBeaver, confira o registro:

```sql
SELECT * FROM public.especialidade ORDER BY id;
```

A aplicação utiliza **inativação lógica** nesses cadastros; não há endpoint DELETE para Especialidade, Paciente ou Profissional nesta versão.

### Paciente e Profissional

Endpoints implementados para cada recurso:

| Operação | Paciente | Profissional |
|---|---|---|
| Cadastrar | `POST /api/pacientes` | `POST /api/profissionais` |
| Listar | `GET /api/pacientes` | `GET /api/profissionais` |
| Consultar | `GET /api/pacientes/{id}` | `GET /api/profissionais/{id}` |
| Atualizar | `PUT /api/pacientes/{id}` | `PUT /api/profissionais/{id}` |
| Listar ativos | `GET /api/pacientes/ativos` | `GET /api/profissionais/ativos` |
| Inativar/reativar | `PATCH /api/pacientes/{id}/ativo?ativo=false` | `PATCH /api/profissionais/{id}/ativo?ativo=false` |

Use **dados inteiramente fictícios**, de acordo com os campos exigidos pelos respectivos RequestDTOs. O campo opcional `cadastradoPorId` pode ser omitido no cadastro inicial, sem criar um usuário fictício no banco. Consulte no DBeaver:

```sql
SELECT * FROM public.paciente ORDER BY id;
SELECT * FROM public.profissional ORDER BY id;
```

### Regras de negócio e erros HTTP

Os Services implementam validações, prevenção de duplicidades e preservação de campos de controle. Demonstre, por exemplo, uma tentativa de cadastrar novamente o mesmo nome de especialidade. **Registre o status HTTP realmente retornado**: algumas exceções de negócio ainda não possuem tradução específica para `400`, `404` ou `409`. Não declare esses status como garantidos sem teste.

### Comprovar persistência após reinício

Depois de cadastrar um registro, reinicie somente o backend, sem remover o volume do banco:

```bash
docker compose restart backend
```

Repita o GET no Insomnia e o SELECT no DBeaver. O registro deve permanecer no banco. O Flyway deve reconhecer a V1 como já aplicada, sem recriá-la.

## Recriar o ambiente do zero — operação destrutiva

> **ATENÇÃO:** o procedimento abaixo apaga irreversivelmente todos os registros e tabelas armazenados no volume PostgreSQL do projeto. Faça backup antes, se houver dados a preservar. Execute apenas em ambiente de desenvolvimento/demonstração e confira o projeto Compose ativo.

Na raiz do SIGEP, com a confirmação de que os dados são descartáveis:

```bash
docker compose down -v --remove-orphans
```

O comando remove contêineres, rede do Compose e volumes declarados para o projeto, incluindo os dados do PostgreSQL. Para remover também **apenas a imagem do backend do SIGEP**:

```bash
docker image rm sigep-backend:dev
```

Não é necessário remover a imagem compartilhável `postgres:17` nem executar comandos globais como `docker system prune -a --volumes`.

Confira:

```bash
docker compose ps -a
docker volume ls
docker image ls
```

Em seguida, repita a seção **Instalação rápida — a partir de um clone novo**, começando pela configuração existente e executando `docker compose up -d --build`. O novo volume será inicializado pelo PostgreSQL, e a V1 será aplicada pelo Flyway quando o backend iniciar.
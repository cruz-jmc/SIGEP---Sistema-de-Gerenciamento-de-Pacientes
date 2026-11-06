# SIGEP — Sistema de Gerenciamento de Pacientes

Projeto acadêmico de aplicação web para apoiar a digitalização do gerenciamento de pacientes do **CESMUC — Centro de Especialidades da Saúde da Mulher e da Criança**, unidade de saúde localizada em **Garanhuns, Pernambuco**, voltada ao atendimento especializado de mulheres e crianças.

> **Situação do projeto:** em desenvolvimento. As funcionalidades e integrações descritas neste documento não devem ser interpretadas como prontas para uso em produção.

## 1. Contexto e objetivo

O SIGEP busca transformar processos manuais de gerenciamento de pacientes do CESMUC em fluxos digitais, facilitando o cadastro, a consulta e a organização de informações, apoiando o trabalho administrativo e reduzindo a incidência de erros humanos. O projeto também contempla, em diferentes estágios de desenvolvimento, o gerenciamento de profissionais, especialidades, solicitações de agendamento, planos de tratamento, sessões, escalas e cotas de atendimento.

## 2. Contexto acadêmico

- **Instituição:** Universidade de Pernambuco (UPE).
- **Disciplinas:** Programação Web e Padrões de Projeto.
- **Professores:** Elisson Rocha e Carlos Melo.
- **Equipe:** João Cruz, Letícia Guardiola e Mateus Aguiar.

## 3. Tecnologias

| Componente | Tecnologia | Situação |
|---|---|---|
| Linguagem do backend | Java 21 | Configurada |
| Framework | Spring Boot 4.0.8 | Configurado |
| API HTTP | Spring Web / Spring MVC | Em desenvolvimento |
| Persistência | Spring Data JPA / Hibernate | Em desenvolvimento |
| Banco de dados | PostgreSQL 17 | Configurado via Docker Compose |
| Versionamento do esquema | Flyway | Configurado; migrations iniciais pendentes |
| Build | Maven / Maven Wrapper | Configurado |
| Infraestrutura local | Docker e Docker Compose | Configurada |
| Redução de código repetitivo | Lombok | Em uso |
| Frontend | React | Planejado; aplicação ainda não implementada |

## 4. Arquitetura e organização do repositório

O backend segue uma organização em camadas, com entidades JPA, repositories, services e controllers REST. A introdução de DTOs de entrada e saída, validações e mapeadores faz parte das próximas etapas de desenvolvimento.

```text
.
├── .github/                  # Configurações de colaboração e automação
├── docs/                     # Documentação e modelo relacional
├── docker/                   # Arquivos auxiliares de infraestrutura
├── sigep-backend/
│   ├── Dockerfile
│   ├── pom.xml
│   ├── mvnw
│   └── src/
│       ├── main/
│       │   ├── java/br/edu/upe/sigep/
│       │   │   ├── controller/
│       │   │   ├── dto/
│       │   │   ├── entity/
│       │   │   ├── enums/
│       │   │   ├── mapper/
│       │   │   ├── repository/
│       │   │   └── service/
│       │   └── resources/
│       │       ├── application.yaml
│       │       ├── application-dev.yaml
│       │       ├── application-test.yaml
│       │       └── db/migration/
│       └── test/
├── sigep-frontend/           # Diretório reservado ao frontend
├── compose.yaml
├── .env.example
└── README.md
```

Algumas pastas estão reservadas para implementações futuras e ainda não contêm classes funcionais.

## 5. Funcionalidades e estado de implementação

### Estruturas existentes

- Mapeamentos JPA para pacientes, usuários, profissionais, especialidades, solicitações, planos de tratamento, sessões, escalas, cotas, notificações, realocações e auditoria.
- Repositories para as principais entidades.
- Services com operações cadastrais e algumas regras de negócio, como verificação de duplicidade, revisão de solicitações e controle de cotas.
- Controllers REST para parte dos fluxos de solicitações, sessões, planos, escalas, cotas e auditoria.

### Em desenvolvimento ou pendente de validação

- Correção de incompatibilidades de compilação e consultas derivadas.
- Controllers e contratos REST para cadastros centrais.
- DTOs de requisição e resposta, validações e tratamento padronizado de erros.
- Alinhamento entre entidades JPA e modelo relacional validado.
- Migrations iniciais do Flyway.
- Testes de integração e evidências de CRUD funcionando.
- Segurança, autenticação e autorização adequadas ao tratamento de dados pessoais.
- Interface web em React.

**Importante:** a presença de uma classe ou endpoint no código não significa que seu funcionamento tenha sido comprovado. O sistema ainda não possui evidência documentada de CRUD completo em execução.

## 6. Pré-requisitos

Para utilizar o ambiente local, recomenda-se instalar:

- Git;
- Docker com Docker Compose;
- Java 21, caso deseje executar o backend fora do contêiner.

O repositório inclui o Maven Wrapper (`sigep-backend/mvnw`), que permite executar comandos Maven sem exigir uma instalação global do Maven, desde que o ambiente possua os requisitos do wrapper.

## 7. Configuração do ambiente

Na raiz do repositório, crie seu arquivo `.env` a partir do modelo:

```bash
cp .env.example .env
```

Revise os valores no arquivo `.env` e defina uma senha local para o PostgreSQL. **Não envie `.env`, senhas ou outros segredos para o Git.**

A configuração do Compose utiliza uma rede interna entre o backend e o banco. Por isso, a URL JDBC do backend em contêiner utiliza o hostname `db` e a porta `5432`, enquanto o acesso ao PostgreSQL pelo computador anfitrião utiliza a porta local `5433`, conforme o mapeamento do Compose.

### Subir somente o banco local

```bash
docker compose config --quiet
docker compose up -d db
docker compose ps
```

Para verificar a disponibilidade do PostgreSQL:

```bash
docker compose exec db pg_isready -U sigep -d sigep
```

### Compilar o backend

Execute a partir da raiz do repositório:

```bash
cd sigep-backend
./mvnw clean compile
```

> **Atenção:** a compilação pode falhar no estágio atual devido a inconsistências entre controllers, services e repositories já identificadas na revisão estática. Os erros devem ser corrigidos antes de considerar o backend executável.

### Inicialização completa da API

O `compose.yaml` define os serviços `backend` e `db`. Entretanto, a configuração JPA utiliza `ddl-auto: validate` e o Flyway ainda não possui as migrations iniciais necessárias para preparar um banco vazio. Portanto, **a inicialização completa da API não está validada neste estágio**.

Após a criação e validação das migrations e a correção dos bloqueios de compilação, a equipe deverá testar a inicialização do backend e documentar os endpoints efetivamente funcionais.

## 8. Perfis e persistência

- `application.yaml`: configurações gerais, incluindo JPA, Flyway e porta HTTP.
- `application-dev.yaml`: configurações do ambiente de desenvolvimento e variáveis de conexão.
- `application-test.yaml`: configurações previstas para testes, ainda dependentes de preparação do ambiente correspondente.
- `db/migration/`: diretório destinado aos scripts versionados do Flyway.

As migrations devem refletir o modelo relacional aprovado e os mapeamentos JPA revisados.

## 9. Fluxo de desenvolvimento e Git

- **`main`**: branch de versões estáveis. Alterações devem ocorrer por **Pull Requests**, com pelo menos uma aprovação conforme as regras do repositório.
- **`develop`**: branch de integração e testes, com pushes diretos permitidos pela equipe.
- **`feat/*`**: branches de funcionalidades, normalmente integradas à `develop`.
- **`refactor/*`**: branches para reorganização e melhoria de código ou documentação.

O fluxo usual é desenvolver em uma branch específica, integrar e validar na `develop` e, quando a equipe considerar uma versão estável, abrir um Pull Request de `develop` para `main`.

## 10. Testes e avaliação acadêmica

O projeto é avaliado, entre outros marcos, pelo **Checkpoint 3 — Back-end até Controller**, que considera:

1. Correspondência das entidades JPA com o DER aprovado;
2. Repositories das entidades centrais;
3. Regras de negócio implementadas nos services;
4. Controllers REST com verbos e códigos HTTP coerentes;
5. Evidência de funcionamento de CRUD via Postman, Insomnia ou demonstração.

Ainda é necessário corrigir os bloqueios identificados, executar testes e produzir evidências reproduzíveis de funcionamento. Uma compilação bem-sucedida, isoladamente, não comprova o funcionamento do CRUD.

## 11. Segurança e privacidade

O SIGEP trabalha com um domínio que envolve dados pessoais e potencialmente sensíveis. O desenvolvimento deve priorizar a minimização dos dados retornados pela API, a proteção de credenciais, a validação de entradas e o controle de acesso.

Não utilize dados reais de pacientes em testes ou exemplos públicos. Entidades JPA não devem ser expostas diretamente em respostas HTTP sem revisão dos campos, especialmente informações como hashes de senha e dados pessoais.

## 12. Limitações atuais e próximos passos

1. Corrigir os erros de compilação e as consultas derivadas inconsistentes.
2. Confirmar o alinhamento das entidades com o modelo relacional aprovado.
3. Implementar DTOs e controllers para os cadastros centrais, iniciando por `Especialidade`.
4. Definir tratamento de erros, validações e controles de segurança.
5. Criar e testar as migrations do Flyway em PostgreSQL local.
6. Demonstrar operações CRUD e registrar evidências de testes.
7. Evoluir a integração com o frontend.

## 13. Autoria

**Equipe:** João Cruz, Letícia Guardiola e Mateus Aguiar.  
**Professores:** Elisson Rocha e Carlos Melo.  
**Instituição:** Universidade de Pernambuco (UPE).  
**Disciplinas:** Programação Web e Padrões de Projeto.

# Testes HTTP de usuários e autenticação do SIGEP

## Preparação local

1. Na raiz do repositório, use o `.env` local baseado no `.env.example`. Configure `DB_URL` para `jdbc:postgresql://db:5432/sigep`, `DB_USERNAME` para o usuário do serviço `db` e use a mesma senha em `DB_PASSWORD` e `LOCAL_DB_PASSWORD`. Para inicializar o primeiro coordenador, configure também `SIGEP_ADMIN_NOME`, `SIGEP_ADMIN_EMAIL` e `SIGEP_ADMIN_SENHA`. Se já existe um coordenador, use as credenciais dessa conta. O bootstrap não redefine suas credenciais.
2. Na raiz, execute `docker compose up -d --build backend`. O Compose inicia também `db` e `mailpit`. Aguarde a inicialização do backend com o perfil `dev`. A API usa `http://localhost:8080` e o Mailpit usa `http://localhost:8025`.
3. Abra a pasta `SIGEP-HTTPtests` como coleção no Bruno e selecione o ambiente **Local**. Use o editor do ambiente para preencher os valores dos campos marcados como secretos e salve pelo Bruno. O aplicativo guarda esses valores localmente. Mantenha a marcação de segredo e o `.env` fora do versionamento.
4. Nas preferências de requisições do Bruno, mantenha habilitados o envio e o armazenamento de cookies (`Send Cookies` e `Store Cookies`). Para começar anônimo, remova no gerenciador de cookies os cookies antigos do host de `baseUrl` ou faça logout com CSRF válido caso uma sessão já esteja aberta. Use o mesmo host durante todo o roteiro.

| Variável | Como preencher |
| --- | --- |
| `baseUrl` | Valor já definido no ambiente **Local**. |
| `coordenadorEmail`, `coordenadorSenha` | Credenciais locais do coordenador. Use o e-mail em minúsculas, conforme o cadastro. |
| `funcionarioEmail` | E-mail em minúsculas, sem espaços e ainda não cadastrado. |
| `funcionarioSenha` | Senha que será definida na ativação e reutilizada no login. Use pelo menos 12 caracteres e até 72 bytes UTF-8. |
| `ativacaoToken` | Copie do Mailpit depois do cadastro. |
| `funcionarioId` | Variável de execução preenchida a partir de `id` no cadastro 201. Uma nova tentativa na requisição de cadastro principal substitui ou limpa o ID anterior ao receber a resposta. |
| `csrfToken` | Variável de execução preenchida pelo Bruno na requisição `01 - Obter CSRF`. |

## Demonstração manual

Envie as requisições uma por uma e confira a aba **Tests** após cada resposta. Os números recomeçam em cada pasta. Todos os arquivos destas duas pastas mantêm **Follow Redirects** desabilitado para conferir a resposta original.

| Passo | Pasta e requisição | Resultado esperado |
| --- | --- | --- |
| 1 | Autenticação · 02 Verificar acesso sem login | 302 para `/login`. |
| 2 | Autenticação · 01 Obter CSRF | 200 com `headerName` e `token`. |
| 3 | Autenticação · 03 Login do coordenador | 302 para `/` ou para a consulta de sessão salva pelo Spring Security. |
| 4 | Autenticação · 04 Confirmar sessão do coordenador | 200, e-mail do coordenador, `autenticado: true` e `ROLE_COORDENADOR`. |
| 5 | Autenticação · 01 Obter CSRF | 200, CSRF atualizado para a sessão do coordenador. |
| 6 | Usuários · 01 Cadastrar funcionário | 201, ID capturado, perfil `FUNCIONARIO`, `ativo: false` e `primeiroAcesso: true`. |
| 7 | Usuários · 02 Buscar funcionário por ID e 03 Listar usuários | 200 em ambas, com o funcionário cadastrado. |
| 8 | Usuários · 04 Cadastrar com e-mail duplicado, 05 Cadastrar com dados inválidos e 06 Buscar usuário inexistente | 409 com `erro`, 400 com erros em `nome` e `email`, e 400 com `erro` para o ID de teste `99999`. Se esse ID existir na sua base local, escolha outro ID ausente e ajuste o teste correspondente na requisição. |
| 9 | Autenticação · 07 Logout | 302 para `/login?logout`. |
| 10 | Mailpit | Abra `http://localhost:8025`. Na mensagem mais recente **SIGEP - Ativação de conta** destinada ao e-mail cadastrado, copie o token do corpo e salve-o no campo secreto `ativacaoToken` do ambiente **Local**. O convite expira em 24 horas. |
| 11 | Autenticação · 01 Obter CSRF, depois Usuários · 07 Ativar com convite válido | 200 para renovar o CSRF já sem sessão e 204 sem corpo para ativar a conta. |
| 12 | Autenticação · 05 Login do funcionário | 302 para `/` ou para a consulta de sessão salva. |
| 13 | Autenticação · 06 Confirmar sessão do funcionário | 200, e-mail do funcionário, `autenticado: true` e `ROLE_FUNCIONARIO`. |
| 14 | Usuários · 10 Acessar usuários como funcionário, Autenticação · 01 Obter CSRF e Usuários · 11 Cadastrar sem autorização como funcionário | 403 na consulta, 200 na renovação e 403 no cadastro com CSRF válido. O cadastro usa o mesmo e-mail da demonstração. |
| 15 | Autenticação · 07 Logout | 302 para `/login?logout`. |
| 16 | Autenticação · 08 Verificar acesso após logout | 302 para `/login`. |

### Negativos opcionais

- Logo após o 204 da ativação, execute **Usuários · 08 Reutilizar convite** com o mesmo token e **Usuários · 09 Ativar com token inválido**. Ambos devem retornar 400 com `erro: Convite inválido ou expirado.` e permitem prosseguir para o login do funcionário. Mantenha a senha válida no ambiente. O script monta o corpo JSON das três requisições de ativação com os valores locais, inclusive se a senha contiver aspas ou barras invertidas.
- Depois do passo 16, ainda sem sessão, execute **Autenticação · 01 Obter CSRF** e **09 Login com senha incorreta**. Espere 302 para `/login?error`. O formulário usa uma senha deliberadamente errada de teste. Caso ela coincida com sua senha local, troque o valor de teste na requisição.
- Ainda sem sessão, execute **Autenticação · 10 Login sem CSRF**. Espere 403. A requisição omite o cabeçalho CSRF e o campo de formulário correspondente, mesmo que o cookie ainda esteja no jar. Confira o estado anônimo com **08 Verificar acesso após logout**.

## CSRF e cookies

O script de **01 Obter CSRF** limpa o valor anterior e, após validar o 200 e os campos da resposta, guarda o token do JSON em `csrfToken` com `bru.setVar`. Os POSTs que exigem CSRF enviam `X-XSRF-TOKEN: {{csrfToken}}`. O Bruno gerencia o cookie `XSRF-TOKEN` que acompanha esse cabeçalho e o `JSESSIONID` da sessão. Use o token do JSON para o cabeçalho. O Spring Security mascara esse token e seu valor pode diferir do cookie.

Os scripts de resposta dos logins principais e do logout removem a variável CSRF ao receber 302. Reexecute **01 Obter CSRF** antes do primeiro POST após cada troca de sessão. Consultas GET podem ser feitas antes da renovação. Se você limpar cookies, reiniciar o cliente ou perder a sessão, recomece a partir do estado apropriado e obtenha um novo CSRF.

Na primeira execução manual, confira se o jar armazena e envia os cookies de `baseUrl` e se a sessão é preservada entre o 302 do login e o GET de confirmação. Um 403 inesperado em um POST pode indicar que o cabeçalho e o cookie CSRF não pertencem à mesma sessão. Renove o CSRF e confira o papel retornado pela consulta de sessão. Evite compartilhar capturas ou traces que exibam credenciais, tokens ou cookies.

## Repetição com outro funcionário

Encerre a sessão atual, confirme o estado anônimo e preencha um novo `funcionarioEmail` e a senha escolhida. Limpe o valor local de `ativacaoToken` e repita o roteiro desde o passo 1. Após o novo 201, use apenas o convite destinado ao novo e-mail. O cadastro duplicado funciona usando o mesmo e-mail do cadastro bem-sucedido dessa rodada.

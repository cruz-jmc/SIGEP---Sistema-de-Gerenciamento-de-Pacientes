# Testes HTTP de usuários e autenticação do SIGEP

## Preparação local

1. Configure o `.env` local com `DB_URL=jdbc:postgresql://db:5432/sigep`, `DB_USERNAME`, `DB_PASSWORD` e `LOCAL_DB_PASSWORD` conforme o Compose. Para inicializar o coordenador, configure também `SIGEP_ADMIN_NOME`, `SIGEP_ADMIN_EMAIL` e `SIGEP_ADMIN_SENHA`. Se a conta já existir, use suas credenciais atuais.
2. Inicie o ambiente local com `docker compose up -d --build backend`. A API usa `http://localhost:8080`.
3. Abra `SIGEP-HTTPtests` no Bruno, selecione **Local** e preencha os campos secretos pelo editor do ambiente. Mantenha esses valores no armazenamento local do Bruno.
4. Habilite **Send Cookies** e **Store Cookies**. Use o mesmo host em todo o roteiro e limpe os cookies antigos desse host para iniciar sem sessão. As requisições das duas pastas mantêm **Follow Redirects** desabilitado.

| Variável | Valor local |
| --- | --- |
| `coordenadorEmail`, `coordenadorSenha` | Credenciais atuais do coordenador. |
| `funcionarioEmail`, `funcionarioNovoEmail` | Dois e-mails distintos, em minúsculas e ainda disponíveis. |
| `funcionarioSenha` | Senha inicial usada no cadastro e no login. |
| `coordenadorNovoEmail`, `coordenadorNovaSenha` | Credenciais para a troca. Use um e-mail em minúsculas. |
| `funcionarioId`, `funcionarioEmailAtual`, `csrfToken` | Variáveis de execução preenchidas pelos scripts da coleção. |

As senhas novas exigem pelo menos 12 caracteres e até 72 bytes UTF-8.

## Roteiro manual

| Passo | Requisições | Resultado esperado |
| --- | --- | --- |
| 1 | Autenticação · 02 Verificar acesso sem login, 01 Obter CSRF, 03 Login do coordenador e 04 Confirmar sessão do coordenador | Acesso anônimo redireciona para `/login`. Login retorna 302 e a sessão retorna 200 com `ROLE_COORDENADOR`. |
| 2 | Autenticação · 01 Obter CSRF, depois Usuários · 01 Cadastrar funcionário | 201 com ID, `FUNCIONARIO`, `ativo: true` e ID do criador. |
| 3 | Usuários · 02 Buscar funcionário por ID, 03 Listar usuários, 04 Cadastrar com e-mail duplicado, 05 Cadastrar com dados inválidos e 06 Buscar usuário inexistente | 200 nas consultas, 409 no cadastro duplicado, 400 com erros de validação e 400 para ID inexistente. Ajuste o ID de teste `99999` se ele existir na base local. |
| 4 | Autenticação · 07 Logout, 08 Verificar acesso após logout, 01 Obter CSRF, 05 Login do funcionário e 06 Confirmar sessão do funcionário | Logout redireciona para `/login?logout`. O funcionário autentica com a senha inicial e a consulta retorna 200 com `ROLE_FUNCIONARIO`. |
| 5 | Autenticação · 01 Obter CSRF, depois Usuários · 10 Acessar usuários como funcionário e 11 Cadastrar sem autorização como funcionário | 403 em ambas as requisições de usuários, com CSRF válido no cadastro. |
| 6 | Autenticação · 07 Logout, 01 Obter CSRF, 03 Login do coordenador, 01 Obter CSRF, depois Usuários · 07 Atualizar funcionário, 08 Desativar funcionário e 12 Consultar funcionário desativado | 200 com nome e e-mail atualizados, 204 na desativação e 200 com `ativo: false` na consulta. |
| 7 | Usuários · 09 Atualizar credenciais do coordenador, depois Autenticação · 08 Verificar acesso após logout | 200 na troca e 302 para `/login` na consulta seguinte. |
| 8 | Autenticação · 01 Obter CSRF e 11 Login de funcionário desativado | 302 para `/login?error`, mesmo usando a senha correta. |
| 9 | Atualize os segredos locais `coordenadorEmail` e `coordenadorSenha`. Execute Autenticação · 01 Obter CSRF, 03 Login do coordenador e 04 Confirmar sessão do coordenador | 302 no novo login e 200 com o e-mail atualizado. |

Em estado anônimo, **09 Login com senha incorreta** espera 302 para `/login?error` após obter CSRF. **10 Login sem CSRF** espera 403, inclusive com o cookie no jar.

## CSRF e cookies

**01 Obter CSRF** guarda o campo `token` do JSON em `csrfToken`. O Spring Security mascara esse valor. Envie-o em `X-XSRF-TOKEN` junto com o cookie `XSRF-TOKEN` da mesma emissão, usando o jar do Bruno. O valor bruto do cookie no cabeçalho, o cabeçalho ausente e o pareamento de token e cookie de emissões diferentes retornam 403.

O cookie `JSESSIONID` mantém a autenticação entre requisições. Login bem-sucedido, logout e troca de credenciais limpam o cookie CSRF. Execute **01 Obter CSRF** antes da primeira mutação depois de cada uma dessas operações. Consultas GET podem ser feitas antes da renovação.

Se uma mutação retornar 403, confira primeiro a sessão em `/api/auth/me`. Se o perfil estiver correto, obtenha outro CSRF e confira se o Bruno envia o cookie correspondente no mesmo host. A troca de credenciais revoga as outras sessões do coordenador no próximo acesso. A desativação tem o mesmo efeito nas sessões do funcionário. Uma alteração de e-mail também exige novo login para a sessão afetada.

## Invalidar sessões com cookies independentes

Use duas instâncias do Bruno com armazenamento de cookies independente, por exemplo em perfis distintos do sistema operacional. Abra a mesma coleção e selecione o ambiente **Local** nas duas instâncias. Mantenha **Send Cookies** e **Store Cookies** ligados e **Follow Redirects** desligado. Confira que cada login mantém seu próprio `JSESSIONID`. Execute **01 Obter CSRF** dentro de cada instância antes de seu login e antes das mutações após login. Cada instância deve enviar seu próprio cookie `XSRF-TOKEN` com o token obtido nela.

1. **Desativação.** Na instância A, entre como coordenador e cadastre um funcionário. Na B, entre como esse funcionário e execute **06 Confirmar sessão do funcionário** para confirmar HTTP 200. Na A, obtenha CSRF e execute **08 Desativar funcionário** (204) e **12 Consultar funcionário desativado** (200 com `ativo: false`). Na B, mantenha o cookie da sessão original e execute **08 Verificar acesso após logout** como a próxima requisição. Espere 302 para `/login`. Na B, preencha `funcionarioNovoEmail` com o e-mail da conta desativada, obtenha CSRF e execute **11 Login de funcionário desativado**. Espere 302 para `/login?error`.
2. **Troca de credenciais.** Entre como coordenador nas duas instâncias e execute **04 Confirmar sessão do coordenador** em ambas (200). Para verificar a revogação por troca de senha isoladamente, preencha `coordenadorNovoEmail` com o e-mail atual e escolha uma nova senha. Na A, obtenha CSRF e execute **09 Atualizar credenciais do coordenador** (200). Na B, mantenha o cookie original e execute **08 Verificar acesso após logout** como a próxima requisição. Espere 302 para `/login`.
3. **Login após a troca.** Na B, obtenha CSRF e reenvie **03 Login do coordenador** com a senha anterior. Confira o status 302 e o cabeçalho `Location` terminando em `/login?error`. Os testes dessa requisição esperam sucesso, por isso o teste de destino falha nessa tentativa deliberada. Atualize os segredos locais com as novas credenciais, obtenha CSRF de novo, repita **03 Login do coordenador** e execute **04 Confirmar sessão do coordenador**. Espere 200 na confirmação.

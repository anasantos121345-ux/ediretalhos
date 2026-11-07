# edhi-backend

API REST do painel da éDhi Retalhos (`../edhi-painel.html`). Java 21+, Spring Boot 4.1.2, PostgreSQL.

## Rodar

1. Crie o banco no PostgreSQL: `CREATE DATABASE edhi_db;` (usuário e senha `postgres`, ajuste em `src/main/resources/application.properties`).
2. Nesta pasta: `./mvnw spring-boot:run` (no Windows: `mvnw.cmd spring-boot:run`).
3. A API sobe em `http://localhost:8080`. As tabelas são criadas sozinhas (`ddl-auto=update`).
4. Abra o `../edhi-painel.html` no navegador.

Testes: `./mvnw test`. Eles usam um banco H2 em memória, então não precisam do PostgreSQL.

## Estrutura (`br.com.edhi.painel`)

| Camada | Arquivos |
|---|---|
| model | `Venda`, `Pedido`, `Feira`, `Usuario`, `Sessao` |
| model/enums | `Produto`, `Canal`, `StatusPedido`, `ItemConferencia`, `Papel` |
| model/dto | `VendaRequest`, `PedidoRequest`, `FeiraRequest`, `BackupDTO` |
| repository | `VendaRepository`, `PedidoRepository`, `FeiraRepository`, `UsuarioRepository`, `SessaoRepository` |
| service | `VendaService`, `PedidoService`, `FeiraService`, `BackupService`, `AuthService`, `LimiteTentativas` |
| controller | `VendaController`, `PedidoController`, `FeiraController`, `PainelController`, `AuthController` |
| config | `CorsConfig`, `AutenticacaoInterceptor` (confere o token), `AdminInicial` (cria a conta da Edilene) |
| exception | `RegraNegocioException`, `ApiExceptionHandler` (erros viram `{ "mensagem": "..." }`) |

Os enums usam no JSON os mesmos textos do painel (`"Ecobag"`, `"WhatsApp e Instagram"`, `"Em produção"`). Também aceitam o nome da constante (`"PANO_DE_COPA"`), sem diferenciar maiúsculas nem acentos.

O painel calcula sozinho o Resumo, a leitura das feiras, as legendas e a planilha CSV a partir das listas que a API devolve.

## Endpoints

**Login** `/api/auth`
- `POST /cadastro` `{ "nome", "email", "senha" }`: cria conta de cliente (201) e já devolve o login
- `POST /login` `{ "email", "senha" }`: devolve `{ "token", "expiraEm", "usuario": { "id", "nome", "email", "papel" } }`
- `GET /eu`: quem está logado
- `POST /sair`: encerra este login

Todo o resto pede o cabeçalho `Authorization: Bearer <token>`. Vendas, pedidos, feiras, backup e dados só abrem para o papel `ADMIN` (401 sem login, 403 para cliente).

**Vendas** `/api/vendas`: `POST /`, `GET /` (mais recentes primeiro), `DELETE /{id}`

```json
{ "data": "2026-11-07", "cliente": "Faculdade", "produto": "Ecobag",
  "quantidade": 140, "valor": 3000, "canal": "Empresa e brinde" }
```

**Pedidos** `/api/pedidos`: `POST /`, `GET /` (em aberto primeiro, por prazo), `DELETE /{id}`
- `PATCH /{id}/avancar`: Novo → Em produção → Pronto para conferir → Entregue
- `PATCH /{id}/conferencia/{medida|estampa|cheiro}`: marca ou desmarca
- `POST /{id}/lancar-venda`: cria a venda do pedido entregue

```json
{ "cliente": "Igreja", "peca": "Tapete", "quantidade": 2,
  "valor": 520, "canal": "Feira", "prazo": "2026-11-20" }
```

**Feiras** `/api/feiras`: `POST /`, `GET /` (ordenadas pelo retorno), `DELETE /{id}`

**Dados** `/api`
- `GET /backup` e `POST /backup`: mesmo formato do arquivo de backup do painel. O `POST` substitui tudo.
- `DELETE /dados`: apaga tudo

## Regras de negócio

- Valor, custo e vendas não podem ser negativos; quantidade mínima 1; cliente, peça e canal obrigatórios (erro 400).
- Venda sem data recebe a data de hoje.
- Um pedido só passa para "Entregue" com medida, estampa e cheiro conferidos (409).
- Só um pedido entregue vira venda, e só uma vez (409). Depois disso ele não muda mais de estado.
- Apagar um pedido que já virou venda mantém a venda.
- Feira sem custo de vaga tem retorno `null` e vai para o fim da lista.

## Login

- A conta da Edilene (`ADMIN`) é criada na primeira vez que a API sobe, com os dados de `edhi.admin.*` em `application.properties`. Depois disso a senha não muda mais por lá.
- A senha padrão é `1234567`. Antes de usar de verdade, suba a API pela primeira vez com outra: `set EDHI_ADMIN_SENHA=uma-senha-forte` (Windows) antes do `mvnw.cmd spring-boot:run`.
- Senhas são guardadas com BCrypt. O token é aleatório e o banco guarda só o hash SHA-256 dele. O login dura 7 dias (`edhi.sessao.horas`).
- 5 senhas erradas seguidas para o mesmo e-mail travam o login dele por 15 minutos (429).
- E-mail é comparado sem diferenciar maiúsculas. Senha errada e e-mail inexistente dão a mesma resposta.

## Pontos de atenção

- Fora do `localhost`, use HTTPS: sem ele, senha e token viajam abertos pela rede.
- O CORS está aberto para qualquer origem (`CorsConfig`), para o HTML funcionar aberto direto do arquivo.
- O painel chama `http://localhost:8080/api`. Para usar outro endereço, abra-o uma vez com `?api=http://servidor:8080/api` no fim da URL; o endereço fica salvo no navegador.

## Deploy no Render

O `Dockerfile` e o `render.yaml` ficam na raiz do repositório. Um único serviço serve o site (`index.html`), o painel (`edhi-painel.html`) e a API em `/api`, ligado a um PostgreSQL do Render.

1. Em [render.com](https://render.com), entre com o GitHub.
2. **New > Blueprint**, escolha o repositório `ediretalhos` e clique em **Apply**.
3. Quando o Render pedir `EDHI_ADMIN_SENHA`, digite a senha da Edilene. No ar, a API não sobe com a senha padrão `1234567`.
4. Espere o build (alguns minutos). O site fica em `https://edhi-retalhos.onrender.com` (ou o nome que o Render mostrar).

Cada `git push` na `main` faz um deploy novo sozinho.

No plano grátis o serviço dorme depois de 15 minutos sem visitas e leva cerca de 1 minuto para acordar. O banco grátis do Render vence depois de 30 dias: antes disso, passe para um plano pago ou baixe o backup pelo painel.


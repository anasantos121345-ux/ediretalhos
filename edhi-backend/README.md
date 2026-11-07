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
| model | `Venda`, `Pedido`, `Feira` |
| model/enums | `Produto`, `Canal`, `StatusPedido`, `ItemConferencia` |
| model/dto | `VendaRequest`, `PedidoRequest`, `FeiraRequest`, `BackupDTO` |
| repository | `VendaRepository`, `PedidoRepository`, `FeiraRepository` |
| service | `VendaService`, `PedidoService`, `FeiraService`, `BackupService` |
| controller | `VendaController`, `PedidoController`, `FeiraController`, `PainelController` |
| exception | `RegraNegocioException`, `ApiExceptionHandler` (erros viram `{ "mensagem": "..." }`) |

Os enums usam no JSON os mesmos textos do painel (`"Ecobag"`, `"WhatsApp e Instagram"`, `"Em produção"`). Também aceitam o nome da constante (`"PANO_DE_COPA"`), sem diferenciar maiúsculas nem acentos.

O painel calcula sozinho o Resumo, a leitura das feiras, as legendas e a planilha CSV a partir das listas que a API devolve.

## Endpoints

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

## Pontos de atenção

- Não há autenticação: quem acessar a porta 8080 lê e altera tudo. Antes de colocar na internet, é preciso um login.
- O CORS está aberto para qualquer origem (`CorsConfig`), para o HTML funcionar aberto direto do arquivo.
- O painel chama `http://localhost:8080/api`. Para usar outro endereço, abra-o uma vez com `?api=http://servidor:8080/api` no fim da URL; o endereço fica salvo no navegador.
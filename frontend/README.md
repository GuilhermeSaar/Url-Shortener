# Frontend — GS.

Protótipo Vue 3 com Composition API (`<script setup>`), Vue Router 4, Pinia, Axios, Vite 7 e Tailwind CSS 4. Implementa os requisitos de `../docs/Requisitos-front.md`.

> **Integração em desenvolvimento:** o backend atual exige JWT em `/api/**`. O frontend ainda não possui cadastro/login nem envia o token; por isso, o painel de estatísticas recebe `401 Unauthorized`. A criação, a cópia e o histórico local permanecem disponíveis. Consulte o [README principal](../README.md) para o estado atual e os exemplos autenticados da API.

## Executar

Requer Node.js 20.19+ ou 22.12+ e o backend funcionando em `http://localhost:8080`.

```bash
cd frontend
npm install
npm run dev
```

Abra o endereço exibido pelo Vite (normalmente `http://localhost:5173`). As chamadas a `/shorten` e `/api` são encaminhadas ao backend por proxy. `changeOrigin: true` permite que a URL curta gerada use a origem do backend, onde o redirecionamento funciona. Os links curtos abrem com `<a>` e não passam pelo Vue Router.

## Rotas e recursos

- `/`: tela única com cápsula expansível para criar links, painel de estatísticas e histórico dos últimos 20 links em uma gaveta lateral. Toda a interface segue a identidade clara e fosca.
- `/?code=aB3xYz`: seleciona um link no painel da própria home. Os endereços antigos `/stats` e `/stats/:code` redirecionam para essa mesma tela.
- Navegação adaptada para celular, foco visível, labels e mensagens acessíveis.

Na tela inicial, clique em **Encurtar link** para abrir o campo com foco automático. **Enter** envia e **Esc** recolhe a cápsula preservando o texto ou resultado; durante o envio, aguarde a conclusão. Com o resultado ativo e o foco na cápsula, **Espaço** copia o link quando o foco está fora de campos, links e botões. O botão **Histórico** abre uma gaveta modal com cópia, acesso ao link e estatísticas; **Esc** ou um clique fora dela fecha o painel. As animações respeitam a preferência por movimento reduzido. Não há feedback sonoro.

Ao criar um link, o painel ao lado consulta suas estatísticas automaticamente. Também é possível buscar por código ou URL curta e selecionar **Estatísticas** no histórico, sem trocar de página. O painel mostra cliques, data de criação e destino, com atualização manual e estados de carregamento, erro e link não encontrado. Antes de consultar um link, as métricas aparecem como traços, sem dados fictícios. No celular, criação e estatísticas se organizam em uma coluna.

O Pinia grava o histórico com a chave `elo:link-history:v1` no `localStorage`. Os dados ficam neste navegador e não representam uma listagem do backend. A data do histórico é a data local de criação da entrada; o painel de estatísticas usa a data devolvida pela API. A aplicação continua funcionando se o armazenamento estiver indisponível.

## Organização

```text
src/
  components/   # Cópia, painel de estatísticas e gaveta de histórico
  router/       # Rotas da aplicação
  services/     # Axios e tratamento de erros da API
  stores/       # Histórico local em Pinia
  utils/        # Validação, códigos e datas
  views/        # HomeView: espaço único para criar e acompanhar
  App.vue       # Estrutura da aplicação
  style.css     # Tailwind e identidade visual
```

## Compilar e publicar

```bash
npm run build
npm run preview
```

`build` gera `dist/`. O proxy é exclusivo do servidor de desenvolvimento: `preview` apenas permite inspecionar os arquivos compilados. Em produção, encaminhe `/shorten` e `/api` à API pelo servidor HTTP ou defina `VITE_API_BASE_URL` na compilação (copie `.env.example` para `.env.local`). Uma API em outra origem precisa permitir CORS. Variáveis `VITE_*` são públicas.

O servidor que hospedar o frontend deve entregar `index.html` para rotas como `/stats/aB3xYz`. O redirecionamento `/{code}` permanece sob responsabilidade do backend; a publicação conjunta no Spring Boot exigirá definir esse roteamento e a integração do build. O backend e o build Maven não foram alterados.

O Tailwind 4 usa o plugin oficial `@tailwindcss/vite` e a importação no CSS, sem configuração PostCSS adicional: https://tailwindcss.com/docs/installation/using-vite.

## Contrato e limitações da integração

A integração segue `docs/Requisitos-front.md`, conferido com os controllers Java.

- `POST /shorten` envia `{ "url": "https://example.com" }` como JSON e lê a URL completa de `response`. Os erros `400` do campo `url` aparecem no formulário; falhas sem esse JSON recebem uma mensagem genérica.
- `GET /api/url/{code}` devolve `date`, `clickCount` e `urlOriginal`. `404` exibe o estado de link inexistente/expirado. Respostas incompletas recebem mensagem de erro em vez de exibir métricas inválidas.
- A API não devolve código ou URL curta nas estatísticas. Para abrir/copiar esse link, a tela utiliza a URL informada na busca ou aquela existente no histórico local. Não deduzimos o domínio público a partir do código.
- A API não informa expiração, não distingue link expirado de inexistente no `404` e não oferece listagem de links. O histórico local pode conter links que já não existem.
- `date` não tem fuso horário; o frontend formata a data recebida sem presumir que seja UTC. `clickCount` deve caber em um inteiro seguro do JavaScript para ser exibido sem perda de precisão.
- Somente os erros de validação e recurso não encontrado têm um corpo padronizado pelo backend. Timeout, indisponibilidade e outros formatos de erro também são tratados no frontend.
- A URL de destino exibida no painel abre o destino diretamente. Apenas a ação de abrir a URL curta (`<a href="...">`) passa pelo redirecionamento `302` e contabiliza o clique.
- A cópia automática depende da Clipboard API, normalmente disponível em HTTPS ou localhost. Se falhar, a interface orienta a cópia manual.

## Verificação da integração

```bash
npm test
npm run build
```

Os testes usam o pipeline real do Axios com um adapter controlado para verificar payload, endpoints, respostas `400`/`404`, cancelamento e formatos inesperados. Também renderizam a home a partir de `App.vue` sem código selecionado ou com código inválido, e verificam o redirecionamento das URLs antigas e a seleção de links sem trocar de tela ou reiniciar a rolagem. Eles não substituem uma verificação no navegador e com o backend e o PostgreSQL em execução.

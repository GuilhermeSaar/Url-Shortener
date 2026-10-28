# Frontend — GS.

Protótipo Vue 3 com Composition API (`<script setup>`), Vue Router 4, Pinia, Axios, Vite 7 e Tailwind CSS 4. Implementa os requisitos de `../docs/Requisitos-front.md`.

> O frontend cadastra e autentica usuários pela própria interface. Após o login, o JWT fica somente em memória e é enviado a `/api/**` como `Authorization: Bearer <token>`, que é o contrato do Spring Security. Atualizar a página, fechar a aba ou abrir uma nova aba exige novo login.

## Executar

Requer Node.js 20.19+ ou 22.12+ e o backend funcionando em `http://localhost:8080`.

```bash
cd frontend
npm install
npm run dev
```

Abra o endereço exibido pelo Vite (normalmente `http://localhost:5173`). As chamadas a `/shorten`, `/login`, `/register` e `/api` são encaminhadas ao backend por proxy. `changeOrigin: true` permite que a URL curta gerada use a origem do backend, onde o redirecionamento funciona. Os links curtos abrem com `<a>` e não passam pelo Vue Router.

## Rotas e recursos

- `/`: tela pública com a cápsula expansível para criar e copiar links.
- `/entrar`: tela de login e cadastro para acessar as métricas.
- `/stats`: tela autenticada com painel de estatísticas e histórico dos últimos 20 links em uma gaveta lateral.
- `/stats?code=aB3xYz`: seleciona um link no painel de métricas. Os endereços antigos `/?code=aB3xYz` e `/stats/aB3xYz` encaminham para essa tela, passando pelo login quando necessário.
- Navegação adaptada para celular, foco visível, labels e mensagens acessíveis.

Na tela inicial, clique em **Encurtar link** para abrir o campo com foco automático. **Enter** envia e **Esc** recolhe a cápsula preservando o texto ou resultado; durante o envio, aguarde a conclusão. Com o resultado ativo e o foco na cápsula, **Espaço** copia o link quando o foco está fora de campos, links e botões. Na tela de métricas, o botão **Histórico** abre uma gaveta modal com cópia, acesso ao link e estatísticas; **Esc** ou um clique fora dela fecha o painel. As animações respeitam a preferência por movimento reduzido.

Criar um link não consulta estatísticas. A ação **Ver métricas** abre a tela autenticada; visitantes passam por login/cadastro, preservando o código escolhido. Nessa tela, é possível buscar por código ou URL curta e selecionar **Estatísticas** no histórico. O painel mostra cliques, data de criação e destino, com atualização manual e estados de carregamento, erro e link não encontrado. Ao sair, o usuário volta à home. Uma resposta `401` nas métricas encerra a sessão local e solicita novo login.

O histórico e o token de sessão ficam apenas em memória. Eles existem enquanto a aba permanece aberta e são descartados ao atualizar a página, fechar a aba ou abrir o app em outra aba. O histórico não representa uma listagem do backend. A data do histórico é a data local de criação da entrada; o painel de estatísticas usa a data devolvida pela API.

## Organização

```text
src/
  components/   # Cabeçalho, cópia, painel de estatísticas e gaveta de histórico
  router/       # Rotas e proteção de acesso às métricas
  services/     # Axios e tratamento de erros da API
  stores/       # Histórico local em Pinia
  utils/        # Validação, códigos e datas
  views/        # HomeView, AuthView e StatsView
  App.vue       # Estrutura da aplicação
  style.css     # Tailwind e identidade visual
```

## Compilar e publicar

```bash
npm run build
npm run preview
```

`build` gera `dist/`. O proxy é exclusivo do servidor de desenvolvimento: `preview` apenas permite inspecionar os arquivos compilados. Em produção, encaminhe `/shorten`, `/login`, `/register` e `/api` à API pelo servidor HTTP ou defina `VITE_API_BASE_URL` na compilação (copie `.env.example` para `.env.local`). Uma API em outra origem precisa permitir CORS. Variáveis `VITE_*` são públicas.

O servidor que hospedar o frontend deve entregar `index.html` para rotas como `/stats/aB3xYz`. O redirecionamento `/{code}` permanece sob responsabilidade do backend; a publicação conjunta no Spring Boot exigirá definir esse roteamento e a integração do build. O backend e o build Maven não foram alterados.

O Tailwind 4 usa o plugin oficial `@tailwindcss/vite` e a importação no CSS, sem configuração PostCSS adicional: https://tailwindcss.com/docs/installation/using-vite.

## Contrato e limitações da integração

A integração segue `docs/Requisitos-front.md`, conferido com os controllers Java.

- `POST /shorten` envia `{ "url": "https://example.com" }` como JSON e lê a URL completa de `response`. Os erros `400` do campo `url` aparecem no formulário; falhas sem esse JSON recebem uma mensagem genérica.
- `GET /api/url/{code}` devolve `date`, `clickCount` e `urlOriginal`. `404` exibe o estado de link inexistente/expirado. Respostas incompletas recebem mensagem de erro em vez de exibir métricas inválidas.
- `POST /register` e `POST /login` enviam e-mail e senha como JSON. Um login válido armazena o JWT; cada chamada a `/api/**` o envia no cabeçalho `Authorization: Bearer <token>`.
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

Os testes usam o pipeline real do Axios com um adapter controlado para verificar payload, endpoints, respostas `400`/`404`, cancelamento e formatos inesperados. Também renderizam as telas reais e verificam a separação da criação e das métricas, a proteção das rotas, a preservação do código no login e o bloqueio após logout. A integração real depende do backend e do PostgreSQL em execução.

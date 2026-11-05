# Frontend — GS.

Vue 3 com Composition API, Vue Router 4, Pinia para autenticação, Axios, Vite 7 e Tailwind CSS 4.

## Executar

Requer Node.js 20.19+ ou 22.12+ e o backend em `http://localhost:8080`.

```bash
cd frontend
npm install
npm run dev
```

Abra o endereço exibido pelo Vite, normalmente `http://localhost:5173`. As chamadas a `/api`, `/login`, `/logout` e `/register` são encaminhadas ao backend por proxy.

## Navegação

- `/`: encurtador público com a cápsula expansível. Pessoas autenticadas são encaminhadas para **Meus links**.
- `/entrar`: login e cadastro. Após entrar, a interface abre **Meus links**, preservando uma seleção de link recebida na URL.
- `/meus-links`: página autenticada com criação compacta, total de links, soma dos cliques e lista dos links da conta, ordenada do mais recente para o mais antigo.
- `/meus-links?code=abc123`: abre os detalhes desse link na própria lista.
- `/stats`, `/stats/:code` e `/?code=abc123`: endereços anteriores encaminham à nova página, passando pelo login quando necessário.

Cada linha mostra o link curto e as ações de copiar e expandir detalhes. O destino, a data de criação e os cliques aparecem somente ao expandir. O botão **Atualizar** consulta novamente o servidor. Após criar um link, a resposta da API fica disponível para cópia e o histórico é atualizado. Se a atualização falhar, a interface preserva os dados já exibidos e oferece uma nova tentativa.

Contas sem links recebem uma orientação para criar o primeiro. Carregamento, falhas e seleções inválidas têm mensagens próprias. Em telas menores, as ações de cada link ficam abaixo dos seus dados.

O histórico vem de `GET /api/urls` e fica apenas no estado temporário da tela; não é salvo no Pinia, `localStorage`, `sessionStorage` ou IndexedDB. Ao sair, os dados são limpos e requisições pendentes são canceladas. Cada nova sessão consulta os links da conta atual.

O JWT fica em um cookie `HttpOnly` definido pelo backend. O JavaScript não recebe nem armazena o token. A interface verifica a sessão antes de liberar as rotas autenticadas. O logout invalida os tokens anteriores da conta em todos os dispositivos.

## Organização

```text
src/
  components/   # Cabeçalho, cópia e lista de links com detalhes
  composables/  # Consulta e ciclo de vida do histórico do servidor
  router/       # Rotas, compatibilidade e proteção de acesso
  services/     # Axios e validação das respostas da API
  stores/       # Autenticação em Pinia
  utils/        # Validação de links, códigos e datas
  views/        # HomeView, AuthView e LinksView
  App.vue       # Estrutura da aplicação
  style.css     # Tailwind e identidade visual responsiva
```

## Contrato da API

- `POST /api/shorten` envia `{ "url": "https://example.com" }` e lê a URL curta completa de `response`.
- `GET /api/urls` retorna os links do usuário autenticado com `code`, `date`, `clickCount` e `urlOriginal`. A interface monta o endereço `/api/{code}` usando a origem configurada para a API, ou a origem atual quando utiliza o proxy.
- `GET /api/url/{code}` continua disponível no cliente de API para consultar as estatísticas de um link pertencente à conta. A nova lista já recebe as estatísticas pelo histórico.
- `POST /register` e `POST /login` enviam e-mail e senha como JSON. O navegador envia o cookie nas requisições seguintes.
- `GET /api/auth/session` verifica a sessão e `POST /logout` encerra a autenticação.

A data devolvida pelo backend não possui fuso; o frontend não presume que seja UTC. A API não fornece uma série temporal de cliques, por isso a interface mostra apenas os totais disponíveis. Abrir o link curto contabiliza um clique; abrir diretamente o destino não passa pelo redirecionamento. A cópia automática depende da Clipboard API, normalmente disponível em HTTPS ou localhost; se falhar, a interface orienta a cópia manual.

## Compilar e publicar

```bash
npm run build
npm run preview
```

O build gera `dist/`. O proxy é exclusivo do desenvolvimento. Em produção, encaminhe `/api`, `/login`, `/logout` e `/register` à API pelo servidor HTTP ou defina `VITE_API_BASE_URL` na compilação. Uma API em outra origem precisa permitir CORS com credenciais. Variáveis `VITE_*` são públicas.

O servidor do frontend deve entregar `index.html` para suas rotas, incluindo `/meus-links`. Os caminhos `/api/{code}` devem chegar ao backend para executar o redirecionamento público.

## Verificação

```bash
npm test
npm run build
```

Os testes verificam o cliente Axios, a renderização das telas e da lista, os redirecionamentos, o isolamento entre contas, a atualização após criação e o descarte de respostas atrasadas. As requisições são controladas nos testes; a integração real depende do backend e do PostgreSQL em execução.

# Requisitos e Documentação Técnica: Frontend do Encurtador de URL

> **Estado da integração:** este documento descreve o protótipo sem conta. O backend passou a exigir JWT para consultar `/api/**`, e a integração de cadastro/login e envio do token no frontend ainda está pendente. Na configuração atual, o painel recebe `401 Unauthorized`. Consulte o [estado atual do projeto](../README.md#estado-atual).

## 1. Visão Geral e Escopo

Este documento descreve o protótipo **GS.** conforme a implementação atual em `frontend/`. A interface reúne criação de links e consulta de estatísticas em **uma única tela**, com identidade **clara e fosca**, destaques em verde e histórico em uma gaveta lateral.

A home contém uma cápsula expansível para encurtar links e um painel de estatísticas sempre presente na composição. Em telas maiores, os dois painéis aparecem lado a lado; em telas de até `800px`, ficam em uma coluna. Tela única significa um único espaço de uso, com rolagem quando necessária.

O protótipo utiliza o backend existente, normalmente em `http://localhost:8080`, sem exigir conta. As estatísticas vêm da API; o histórico pertence apenas ao navegador utilizado.

## 2. Stack Tecnológica

| Responsabilidade | Implementação |
| --- | --- |
| Framework | Vue.js 3, Composition API e `<script setup>` |
| Roteamento | Vue Router 4, com histórico do navegador |
| Estado | Pinia 3 e persistência local do histórico |
| HTTP | Axios 1, com timeout de 15 segundos |
| Estilização | Tailwind CSS 4 e CSS próprio em `src/style.css` |
| Integração do Tailwind | Plugin `@tailwindcss/vite` e `@import "tailwindcss"` |
| Build e desenvolvimento | Vite 7 e `@vitejs/plugin-vue` |
| Ícones | `lucide-vue-next` |
| Testes | Executor nativo do Node.js; renderização Vue para os testes da home |

As versões declaradas e os comandos disponíveis estão em `frontend/package.json`; as versões resolvidas estão em `frontend/package-lock.json`. O ambiente requer Node.js da série 20 a partir de `20.19.0`, ou versão igual ou superior a `22.12.0`, conforme `engines.node`.

## 3. Identidade Visual e Composição

### 3.1 Aparência

| Elemento | Direção implementada |
| --- | --- |
| Fundo | Base clara `#f5f7f4`, gradientes radiais suaves e pontilhado discreto |
| Texto principal | Verde escuro, com base `#263d35` |
| Ação principal da cápsula | Verde `#527658`, com ícone claro |
| Painéis e cápsula | Superfícies claras semitransparentes, blur, bordas suaves e sombras discretas |
| Forma | Cápsula compacta no estado inicial e cartões com cantos arredondados |
| Tipografia | Pilha `Inter`, `Segoe UI`, `sans-serif`, sem carregamento obrigatório de fonte externa |
| Erros | Texto avermelhado, com base `#a0443a` |
| Foco | Contorno visível em verde `#2e755a` nos controles aplicáveis |

As cores e dimensões específicas de cada componente ficam em `frontend/src/style.css`. A mesma identidade visual se aplica à criação, às estatísticas, à cópia e ao histórico.

### 3.2 Organização da Tela

- Marca **GS.** e identificação discreta do espaço de links.
- Apresentação breve: **“Seu link, do início ao clique.”**
- Painel **Criar**, com a cápsula **“Encurtar link”** e seus estados.
- Painel **Acompanhar**, com busca por código ou link curto e apresentação das métricas.
- Nota discreta sobre uso sem conta e armazenamento local.
- Botão flutuante **Histórico**, no canto inferior direito, com contagem dos links quando houver entradas.

As transições da cápsula e da gaveta respeitam `prefers-reduced-motion`. Não há feedback sonoro implementado.

## 4. Comportamentos Funcionais

### 4.1 Criação de Link na Cápsula

1. **Inicial:** cápsula compacta com o ícone de adição e o texto “Encurtar link”.
2. **Entrada:** ao clicar, a cápsula expande suavemente e o campo recebe foco. Aceita URLs HTTP ou HTTPS válidas, com endereço de destino e sem espaços.
3. **Validação:** o frontend remove espaços das extremidades e valida a URL antes do envio. Entradas inválidas exibem erro junto ao campo.
4. **Envio:** o campo fica somente para leitura, a ação de envio mostra carregamento e novos envios são bloqueados até a conclusão. A cápsula não pode ser recolhida durante esse processamento.
5. **Sucesso:** o formulário dá lugar ao link curto, à indicação do destino original, à cópia e às ações “Encurtar outro” e “Estatísticas”. A URL curta é a devolvida pela API, preservando seu domínio.
6. **Integração:** o link criado entra no histórico local e suas estatísticas são consultadas automaticamente no painel da mesma home.
7. **Falha:** o erro é apresentado na cápsula, preservando o endereço informado para permitir nova tentativa.

Recolher e reabrir a cápsula preserva o texto ou resultado atual. “Encurtar outro” limpa o resultado e o campo, mantendo a cápsula aberta com foco para uma nova entrada.

### 4.2 Consulta de Estatísticas na Home

O painel permanece na mesma tela da ferramenta de criação e aceita:

- Consulta automática após a criação de um link.
- Busca manual por código alfanumérico ou URL HTTP/HTTPS cujo caminho contenha um único código.
- Seleção pela ação “Estatísticas” do resultado ou de uma entrada do histórico.
- Abertura da home com o parâmetro `code` na URL.

Ao selecionar um link pelo resultado ou histórico, o foco é direcionado ao título do painel, com rolagem quando necessária. A consulta automática após encurtar preserva o foco no resultado da cápsula.

| Estado | Comportamento |
| --- | --- |
| Sem seleção | “Nenhum link selecionado”, métricas com traços e orientação para criar ou buscar um link |
| Carregando | Indicador visual e mensagem de consulta em andamento |
| Sucesso | Código selecionado, total de cliques, data de criação e URL de destino |
| Código inválido | Mensagem de validação junto à busca, sem consultar a API com esse código |
| Não encontrado | Mensagem de link inexistente ou possivelmente expirado para resposta `404` |
| Falha de consulta | Mensagem de erro e ação para tentar novamente |

O painel oferece atualização manual do código selecionado. Consultar novamente o mesmo código refaz a requisição. Ao trocar a seleção, a consulta anterior é cancelada para impedir que uma resposta antiga substitua a seleção atual. Não há atualização periódica automática.

A home deve renderizar normalmente sem `code`, com `code` vazio ou inválido. O estado inicial não depende de um link previamente selecionado e não usa métricas fictícias.

### 4.3 Cópia e Atalhos

A cópia utiliza `navigator.clipboard.writeText`, com confirmação visual e mensagem de status. Caso falhe, a interface orienta a selecionar o link e copiar manualmente. A disponibilidade da Clipboard API depende do navegador e do contexto, normalmente HTTPS ou localhost.

| Atalho | Contexto e ação |
| --- | --- |
| Enter | No formulário de criação, envia a URL; na busca de estatísticas, consulta o código ou link informado |
| Esc | Com foco na cápsula, recolhe sem perder o conteúdo, desde que não esteja enviando |
| Espaço | Com o resultado ativo e o foco na cápsula, copia o link; não intercepta campos, links, botões ou conteúdo editável |
| Esc na gaveta | Fecha o histórico, conforme o comportamento do diálogo modal |

O atalho de cópia não se aplica quando o foco está no painel de estatísticas ou no histórico. Os controles também podem ser acionados diretamente por seus botões.

### 4.4 Histórico Local em Gaveta

O botão flutuante abre uma gaveta lateral modal, implementada com `<dialog>`. É possível fechá-la pelo botão de fechar, pela tecla Esc ou clicando fora do painel.

O histórico:

- Mantém até **20 links recentes**, em ordem de criação local, com novas entradas no início.
- Usa a chave **`elo:link-history:v1`** no `localStorage`, por meio da store Pinia.
- Armazena URL curta, URL original, código e data local da criação da entrada.
- Evita duplicação pela URL curta e valida as entradas recuperadas do armazenamento.
- Exibe link curto, destino original e data, com ações de abrir o link curto, copiar e consultar estatísticas.
- Fecha a gaveta e carrega o painel da home ao selecionar “Estatísticas”, sem trocar de página.
- Apresenta um estado vazio com ação para iniciar a criação do primeiro link.
- Continua funcionando em memória se não conseguir persistir os dados, com aviso na gaveta.

Esse histórico não é uma listagem fornecida pelo backend e não é sincronizado entre dispositivos. A implementação não oferece exclusão individual de entradas nem limpeza do histórico pela interface.

## 5. Integração com o Backend

### 5.1 Endereçamento e Requisições

Em desenvolvimento, as chamadas usam caminhos relativos e passam pelo proxy do Vite para `http://localhost:8080`. A variável `VITE_API_BASE_URL` permite configurar outra base da API na compilação.

O cliente utiliza `Accept: application/json`, envia o corpo de criação como JSON e limita as requisições a 15 segundos.

### 5.2 Criar Link Curto

**Endpoint:** `POST /shorten`

Corpo:

```json
{
  "url": "https://exemplo.com/artigo"
}
```

**Sucesso:** `201 Created`, com a URL completa no campo `response`. Exemplo para o backend local:

```json
{
  "response": "http://localhost:8080/aB3xYz"
}
```

O frontend valida a URL devolvida e o código contido nela antes de apresentá-la ou adicioná-la ao histórico. Não monta a URL curta a partir da origem do frontend.

Para erros `400`, as mensagens válidas de `errors` referentes ao campo `url` aparecem no formulário. Respostas sem esse formato recebem uma mensagem apropriada de falha.

### 5.3 Consultar Estatísticas

**Endpoint:** `GET /api/url/{code}`

**Sucesso:** `200 OK`. Exemplo:

```json
{
  "date": "2026-10-20T10:30:00",
  "clickCount": 7,
  "urlOriginal": "https://exemplo.com/artigo"
}
```

O frontend verifica se a data é interpretável, se o destino é HTTP/HTTPS e se o total de cliques é um inteiro seguro não negativo. Respostas incompletas ou inválidas exibem erro em vez de métricas inconsistentes.

A data é formatada em `pt-BR`. Como a API usa uma data sem indicação de fuso horário, o frontend não presume que ela esteja em UTC. Os cliques usam formatação numérica em `pt-BR`.

### 5.4 Redirecionamento e Cliques

**Endpoint:** `GET /{code}`, com resposta `302 Found` para o destino original.

Abrir um link curto usa uma âncora HTML direta, em nova aba, com `rel="noopener noreferrer"`. A navegação não passa pelo Vue Router e permite ao backend contabilizar o clique.

Consultar estatísticas não acessa o redirecionamento. Abrir a URL de destino exibida no painel também não passa pelo link curto e, portanto, não registra esse clique no encurtador.

### 5.5 Tratamento de Erros e Limitações da API

- Erros de validação, timeout, indisponibilidade, respostas inesperadas e recursos não encontrados recebem mensagens na interface.
- A resposta `404` não permite distinguir um link inexistente de um expirado.
- A API não fornece listagem de links, data de expiração ou a URL curta na resposta de estatísticas.
- As ações de abrir/copiar o link curto no painel dependem de sua URL completa ter sido informada na busca ou estar no histórico local. Apenas o código não permite deduzir o domínio público do link.
- A URL da home conserva o código selecionado, não a URL completa informada na busca. Após recarregar, uma URL curta que não esteja no histórico pode precisar ser informada novamente para habilitar sua abertura e cópia no painel.
- Entradas do histórico local podem apontar para links que já não existem no backend.

## 6. Rotas e Compatibilidade

| Endereço | Comportamento atual |
| --- | --- |
| `/` | Home única, com criação e painel de estatísticas inicialmente sem seleção |
| `/?code=aB3xYz` | Mesma home, consultando as estatísticas do código informado |
| `/stats` | Redireciona para a home, com âncora `#link-stats` |
| `/stats/aB3xYz` | Redireciona para `/?code=aB3xYz#link-stats` |
| Caminhos desconhecidos do frontend | Redirecionam para `/` |

Os endereços `/stats` existem para compatibilidade e não renderizam uma página separada. A implementação usa apenas `HomeView` como tela.

Selecionar ou buscar um código atualiza o parâmetro `code` com `router.replace`, sem criar um passo adicional no histórico de navegação e sem voltar automaticamente ao topo da home. A âncora dos endereços antigos permite localizar o painel de estatísticas.

## 7. Organização do Código

```text
frontend/
  src/
    components/
      CopyButton.vue       # Cópia e confirmação
      HistoryDrawer.vue    # Gaveta de histórico local
      StatsPanel.vue       # Busca e métricas dentro da home
    router/
      index.js             # Instância do Vue Router
      routes.js            # Rotas, compatibilidade e rolagem
    services/api.js        # Axios, contrato e mensagens de erro
    stores/history.js      # Histórico local em Pinia
    utils/links.js         # Validação, extração de código e datas
    views/HomeView.vue     # Tela única e fluxo da cápsula
    App.vue                # Estrutura principal e acesso ao conteúdo
    main.js                # Inicialização de Vue, Pinia e Router
    style.css              # Tailwind e identidade visual compartilhada
  tests/
    api.test.js            # Contrato e falhas da API com adapter controlado
    home.test.js           # Renderização da home sem seleção ou com código inválido
    router.test.js         # Compatibilidade e seleção na mesma tela
  vite.config.js           # Plugins e proxy de desenvolvimento
```

## 8. Desenvolvimento, Build e Publicação

Na pasta `frontend/`:

```bash
npm install
npm run dev
```

O Vite informa o endereço local disponível, normalmente `http://localhost:5173`. A configuração atual utiliza os plugins Vue e Tailwind e encaminha `/shorten` e `/api` para o backend:

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [vue(), tailwindcss()],
  server: {
    proxy: {
      '/shorten': { target: 'http://localhost:8080', changeOrigin: true },
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
})
```

`changeOrigin: true` permite que o backend gere o link curto com a origem do serviço de redirecionamento durante o desenvolvimento.

Para compilar e inspecionar o resultado:

```bash
npm run build
npm run preview
```

O build gera `frontend/dist/`. O proxy acima é exclusivo do servidor de desenvolvimento. Na publicação, o servidor HTTP deve encaminhar as chamadas de API ao backend ou o build deve definir `VITE_API_BASE_URL`; se houver origens diferentes, a API precisa permitir CORS. As variáveis `VITE_*` são públicas.

O servidor de hospedagem deve entregar `index.html` para os caminhos do frontend, incluindo os endereços antigos `/stats/...`. O redirecionamento de URLs curtas em `/{code}` permanece sob responsabilidade do backend e precisa ser preservado na configuração da hospedagem. O protótipo não integra automaticamente o build Vite ao build Maven ou aos arquivos estáticos do Spring Boot.

## 9. Acessibilidade e Verificação

A interface utiliza rótulos de campos, nomes acessíveis nos botões de ícone, indicação de entradas inválidas, mensagens de alerta/status, foco visível e atalho para pular ao conteúdo. A abertura da cápsula, a apresentação do resultado e a seleção explícita de estatísticas gerenciam o foco. O histórico utiliza o comportamento modal do diálogo nativo.

Verificações automatizadas disponíveis, executadas em `frontend/`:

```bash
npm test
npm run build
```

A cobertura atual inclui:

- Payload e endpoints pelo pipeline real do Axios, com adapter controlado.
- Respostas `400` e `404`, cancelamento, falhas de conexão e formatos inesperados.
- Renderização dos componentes reais da home sem código, com código vazio e com código inválido, prevenindo a regressão de tela em branco na inicialização.
- Redirecionamento dos endereços antigos e seleção de códigos na mesma tela, preservando o comportamento de rolagem.

O teste de renderização usa Vue no ambiente de testes e não comprova o layout no navegador. Interações, responsividade, Clipboard API, foco e animações exigem conferência no navegador; a integração real depende do backend e do banco de dados em execução.

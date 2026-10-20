# Documento de Requisitos

## Encurtador de URLs (URL Shortener)

> **Especificação funcional e técnica** para um serviço de encurtamento de URLs seguro, performático e confiável.  
> Cor de destaque da identidade visual: **#88E788**.

| Escopo | Plataforma | Capacidade diária |
|---|---|---|
| Criação, redirecionamento e estatísticas | Java · Spring Boot · PostgreSQL · Docker Compose | Até 100.000 novas URLs por dia |

## 1. Visão geral do projeto

O objetivo deste projeto é desenvolver um serviço de encurtamento de URLs robusto, seguro e performático. O sistema visa converter URLs longas em identificadores curtos de alta legibilidade, garantindo redirecionamento rápido e rastreamento básico de cliques.

## 2. Requisitos funcionais (RF)

### RF01 — Encurtamento de URLs

O sistema deve receber uma URL válida com protocolo HTTP ou HTTPS e retornar um link encurtado exclusivo.

### RF02 — Redirecionamento

O sistema deve redirecionar solicitações feitas a um link encurtado para a URL original correspondente, utilizando redirecionamento HTTP temporário (**302 Found**).

### RF03 — Contagem de acessos

O sistema deve contabilizar os acessos aos links encurtados, garantindo a consistência do contador diante de requisições concorrentes.

### RF04 — Consulta de estatísticas

O sistema deve permitir consultar a data de criação e a quantidade de acessos de um link encurtado.

### RF05 — Tratamento de links inexistentes

O sistema deve informar adequadamente quando um código solicitado não corresponder a um link cadastrado.

### RF06 — Retenção de dados

Cada link encurtado deve ser criado com data de expiração definida para **90 dias** a partir de sua criação. A data de expiração deve poder ser alterada enquanto o link estiver ativo. Após a expiração, o link não deve mais redirecionar, deve ser tratado como inexistente e seus dados — URL original, código, data de criação e estatísticas de acesso — devem ser removidos de forma definitiva.

## 3. Requisitos não funcionais (RNF)

### 3.1 Códigos e integridade

#### RNF01 — Formato do código

Os códigos encurtados devem possuir exatamente seis caracteres, utilizando o alfabeto Base62 (0-9, a-z, A-Z), totalizando **56.800.235.584 combinações possíveis**.

#### RNF02 — Geração e ofuscação de códigos

Os códigos encurtados devem ser derivados de identificadores numéricos sequenciais únicos gerados pelo banco de dados, transformados por um mecanismo de ofuscação reversível (ex.: Hashids) com o alfabeto Base62, garantindo que os IDs internos não sejam expostos e mantendo o tamanho padrão do código.

#### RNF03 — Unicidade e confiabilidade

A unicidade dos códigos encurtados deve ser garantida nativamente pelo banco de dados por meio de chaves primárias e restrições de unicidade, eliminando a ocorrência de colisões e a necessidade de retentativas na camada de aplicação.

### 3.2 Capacidade e tráfego

#### RNF04 — Volume de criação

O sistema deve ser dimensionado para um volume projetado de até **100.000 novas URLs por dia**.

#### RNF05 — Capacidade acumulada

A solução deve considerar aproximadamente **365 milhões de URLs cadastradas**, sem exigir a execução contínua do projeto durante esse período.

### 3.3 Desempenho e infraestrutura

#### RNF07 — Latência

O tempo de resposta dos redirecionamentos deve ter como meta um **p95 inferior a 50 ms**, medido no ambiente de testes sob carga documentada, sem incluir o carregamento da página de destino.

#### RNF08 — Persistência

O PostgreSQL deve ser utilizado como banco de dados relacional, com restrição de unicidade e índices adequados para consultas pelo código encurtado e para a identificação de links expirados.

#### RNF09 — Conteinerização

A aplicação e o banco de dados devem ser executáveis por meio do Docker Compose, com configuração reproduzível do ambiente.

## 4. Restrições tecnológicas

| Componente | Tecnologia |
|---|---|
| Linguagem | Java |
| Framework | Spring Boot |
| API | REST sobre HTTP |
| Banco de dados | PostgreSQL |
| Infraestrutura local | Docker Compose |

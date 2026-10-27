# Comprovação de latência de redirecionamento — RNF07

**Data da execução:** 15 de outubro de 2026  
**Ferramenta:** k6, execução local  
**Objetivo:** verificar que o p95 das respostas de redirecionamento permanece inferior a 50 ms sob carga documentada.

## Cenário executado

O teste reproduzível está em [`load-test/RNF07-test.js`](../load-test/RNF07-test.js). Ele executa requisições `GET /{codigo}` contra uma URL curta existente, valida o retorno `302 Found` e a presença do cabeçalho `Location`.

O script usa `redirects: 0`; portanto, a medição termina na resposta do encurtador e não inclui o carregamento da página de destino.

| Parâmetro | Valor |
|---|---:|
| Taxa de chegada | 50 requisições por segundo |
| Duração | 2 minutos |
| Requisições previstas | 6.000 |
| Requisições concluídas | 6.000 |
| Endpoint | `http://localhost:8080/{codigo}` |

Comando de reprodução:

```bash
BASE_URL=http://localhost:8080 SHORT_CODE=WXftDz k6 run load-test/RNF07-test.js
```

## Resultado

| Métrica | Resultado |
|---|---:|
| p95 de `http_req_duration` | **4,47 ms** |
| p90 de `http_req_duration` | 4,15 ms |
| Média | 3,75 ms |
| Máximo | 157,72 ms |
| Falhas HTTP | 0,00% (0 de 6.000) |
| Verificações de `302` e `Location` | 100,00% (12.000 de 12.000) |

Os limiares configurados no k6 foram aprovados: `p(95) < 50 ms`, taxa de falhas HTTP inferior a 1% e taxa de verificações bem-sucedidas superior a 99%.

## Conclusão

No ambiente local e no perfil de carga descrito, o redirecionamento apresentou p95 de **4,47 ms**, abaixo da meta de 50 ms. Todas as 6.000 requisições retornaram `302 Found` com cabeçalho `Location`, sem falhas HTTP. Assim, o RNF07 foi atendido para o cenário documentado.

O resultado se aplica às condições desta execução. Mudanças de infraestrutura, configuração, volume de dados ou perfil de tráfego devem ser reavaliadas executando o mesmo script.

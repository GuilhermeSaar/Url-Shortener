# Comprovação de volume de criação — RNF04

**Data da execução:** 15 de outubro de 2026  
**Ferramenta:** k6, execução local  
**Objetivo:** verificar a capacidade de criação diante da projeção de até 100.000 novas URLs por dia.

## Cenário executado

O teste reproduzível está em [`load-test/RNF04-test.js`](../load-test/RNF04-test.js). Ele envia requisições `POST /shorten` com URLs distintas e valida a criação com `201 Created` e a presença da URL encurtada na resposta.

| Etapa | Duração | Carga configurada |
|---|---:|---:|
| Aumento gradual | 30 s | até 5 VUs |
| Carga sustentada | 2 min | 20 VUs |
| Redução gradual | 30 s | até 0 VUs |

Comando de reprodução:

```bash
k6 run load-test/RNF04-test.js
```

## Resultado

| Métrica | Resultado |
|---|---:|
| Duração total | 3 minutos |
| Criações concluídas | 1.829 |
| Vazão média | 10,14 criações/s |
| Equivalência diária projetada | 876.184 criações/dia |
| Meta diária | 100.000 criações/dia |
| Margem sobre a meta | 8,76 vezes |
| Criações com `201 Created` e resposta válida | 100,00% |
| Falhas HTTP | 0,00% (0 de 1.829) |
| p95 de duração HTTP | 8,02 ms |

A meta de 100.000 criações por dia equivale a aproximadamente 1,16 criação por segundo em média. A vazão observada no teste foi 10,14 criações por segundo. A equivalência diária é uma projeção aritmética da vazão média medida: `10,141023 × 86.400 = 876.184` criações/dia.

## Conclusão

No ambiente local e no perfil de carga descrito, a criação de URLs atingiu uma vazão média superior à necessária para 100.000 novas URLs por dia, sem falhas HTTP e com todas as respostas de criação validadas. O RNF04 foi atendido para o cenário documentado.

O teste mede três minutos de execução e a projeção diária pressupõe a manutenção de condições equivalentes de infraestrutura, banco de dados e perfil de uso. Alterações relevantes nessas condições devem ser reavaliadas com o mesmo script.

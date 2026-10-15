# Comprovação de capacidade acumulada — RNF05

**Data da coleta:** 15 de outubro de 2026  
**Banco de dados:** PostgreSQL  
**Objetivo:** comprovar, por meio de uma massa persistida e de projeção documentada, a capacidade para aproximadamente 365 milhões de URLs cadastradas.

## Massa e integridade

Foram inseridos e verificados **1.790.676** registros na tabela `urls`.

| Verificação | Resultado |
|---|---:|
| URLs cadastradas | 1.790.676 |
| Menor ID | 1 |
| Maior ID | 1.790.676 |
| Códigos distintos | 1.790.676 |
| Colisões observadas | 0 |

## Armazenamento e projeção

Na massa medida, a tabela ocupou 159.465.472 bytes e seus índices 104.914.944 bytes, totalizando 264.454.144 bytes.

| Componente | Massa medida | Projeção para 365 milhões |
|---|---:|---:|
| Tabela `urls` | 152 MB | 30,27 GiB |
| Índices | 100 MB | 19,92 GiB |
| Total | 252 MB | **50,20 GiB / 53,90 GB** |
| Total com 30% de folga | — | **65,26 GiB / 70,08 GB** |

A projeção aplica o fator **203,833636** (`365.000.000 / 1.790.676`) aos valores medidos. Ela pressupõe que os registros futuros, incluindo o tamanho das URLs e os índices existentes, tenham perfil equivalente ao da massa de teste. A reserva de 30% cobre crescimento e manutenção de tabela e índices; o dimensionamento do ambiente deve prever espaço adicional e separado para WAL, backups e espaço temporário do PostgreSQL.

## Consulta por código e manutenção

A consulta por código utilizou `Index Scan` no índice único `uk2u4r9fm5ie5cwne6am0q0u0wk`, com tempo de execução de **0,047 ms** na coleta e quatro buffers acessados (`shared hit=1`, `read=3`).

As estatísticas da tabela registraram 1.790.676 linhas vivas e 96.890 tuplas mortas, aproximadamente 5,4% das linhas vivas. O PostgreSQL executou `autovacuum` em 15/10/2026 às 20:47 UTC e `autoanalyze` às 20:49 UTC, evidenciando a atuação da manutenção automática sobre a massa de teste. Essas estatísticas devem ser monitoradas durante a operação, especialmente devido às atualizações do contador de acessos.

## Conclusão

A massa de 1.790.676 URLs não apresentou colisões de código, manteve a consulta indexada por código e permitiu estimar aproximadamente **53,90 GB** para tabela e índices em uma base de 365 milhões de URLs. Considerando 30% de folga, a capacidade mínima estimada para esses objetos é de **70,08 GB**, além das reservas operacionais de WAL, backups e manutenção.

Com um ambiente dimensionado para essas reservas e mantendo-se o perfil de dados utilizado na medição, a solução demonstra capacidade projetada para o volume acumulado estabelecido pelo RNF05.

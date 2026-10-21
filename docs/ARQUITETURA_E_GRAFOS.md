# ARQUITETURA E FUNDAMENTOS DE GRAFOS — GRAPHQUEST

## Documento Técnico e Acadêmico

Este documento detalha as decisões de engenharia de software, modelagem da estrutura de dados e análise de algoritmos do projeto **GraphQuest — A Jornada dos Grafos**.

---

## 1. MODELAGEM FORMAL DO GRAFO DA MASMORRA

Formalmente, a masmorra do jogo é um **Grafo Não-Direcionado e Ponderado**:
$$G = (V, E)$$

Onde:
- **$V$ (Conjunto de Vértices):** 13 salas identificadas de 1 a 13.
- **$E$ (Conjunto de Arestas):** 19 corredores bidirecionais entre pares $\{u, v\} \subset V$.

### 1.1. Lista de Vértices ($V$)
| ID | Nome da Sala | Categoria | Propriedades Especiais |
|:--:|:-------------|:---------:|:-----------------------|
| 1 | Entrada da Cripta | INICIAL | Ponto seguro de partida do herói |
| 2 | Corredor dos Ecos | COMUM | Vértice de distribuição norte/sul/leste |
| 3 | Câmara dos Tesouros | TESOURO | Baú com +100 pontos de tesouro |
| 4 | Armaria Abandonada | COMUM | Conecta com o salão e a passagem soterrada |
| 5 | Santuário de Cristal | ESPECIAL | Concede a "Bênção do Cristal" |
| 6 | Passagem Soterrada | BLOQUEADA | Desmoronamento permanente intransitável |
| 7 | Salão dos Pilares | COMUM | Hub central de trânsito |
| 8 | Biblioteca Oculta | ESPECIAL | Guarda a "Chave Mística" do portão |
| 9 | Galeria Dourada | TESOURO | Baú Nobre com +300 pontos de tesouro |
| 10 | Portão de Ferro | BLOQUEADA | Portão trancado (requer Chave Mística) |
| 11 | Abismo Esquecido | COMUM | Ponte estreita sobre o desfiladeiro |
| 12 | Cofre das Joias | TESOURO | Safiras raras com +200 pontos de tesouro |
| 13 | Portal da Liberdade | SAIDA | Destino final (conclusão e vitória) |

### 1.2. Lista de Arestas ($E$)

As 19 conexões são bidirecionais. A maioria tem custo 1; os trechos perigosos {7,9} e {9,11} têm custo 6, a rota do cofre {9,12} custa 4 e o atalho {11,13} custa 2. O custo é usado por Dijkstra; BFS conta passagens sem somar esses pesos.
1. $\{1, 2\}$
2. $\{2, 3\}$, $\{2, 4\}$, $\{2, 7\}$
3. $\{3, 5\}$
4. $\{4, 6\}$, $\{4, 7\}$
5. $\{5, 8\}$
6. $\{6, 11\}$ *(Desmoronamento bloqueia a passagem)*
7. $\{7, 8\}$, $\{7, 9\}$, $\{7, 10\}$
8. $\{8, 9\}$
9. $\{9, 11\}$, $\{9, 12\}$
10. $\{10, 11\}$ *(Portão de ferro trancado)*
11. $\{11, 12\}$, $\{11, 13\}$
12. $\{12, 13\}$

---

## 2. ESTRUTURA DE DADOS: LISTA DE ADJACÊNCIA

A representação computacional do grafo foi realizada em Java utilizando uma lista de vizinhos e um mapa auxiliar de pesos:
```java
private final Map<Sala, List<Sala>> listaAdjacencia;
private final Map<Sala, Map<Sala, Integer>> pesosArestas;
```

### Por que Lista de Adjacência e não Matriz de Adjacência?

1. **Grafo Esparso:**
   - O número máximo de arestas em um grafo simples com 13 vértices seria:
     $$E_{\text{max}} = \frac{V(V - 1)}{2} = \frac{13 \times 12}{2} = 78$$
   - A masmorra possui apenas 19 arestas ativas.
   - Uma **Matriz de Adjacência** exigiria uma tabela $13 \times 13 = 169$ células na memória, onde mais de 75% dos valores seriam nulos/zeros.
   - A **Lista de Adjacência** aloca memória proporcional a $O(V + E)$, sendo adequada para grafos esparsos.

2. **Complexidade de Consulta de Vizinhos:**
   - Na matriz de adjacência, para saber quais salas estão conectadas à sala atual, seria necessário inspecionar todas as $V$ colunas da linha correspondente: $O(V)$.
   - Na lista de adjacência, o acesso à lista pelo `Map` é $O(1)$ médio e a iteração percorre exatamente o grau do vértice: $O(\text{grau}(u))$.

---

## 3. BFS E DIJKSTRA

### 3.1. BFS: menor quantidade de passagens
O algoritmo implementado na classe `br.com.graphquest.grafo.Grafo` utiliza:
- **Fila Explícita:** `Queue<Sala> fila = new ArrayDeque<>();`
- **Conjunto de Visitados:** `Set<Sala> visitados = new LinkedHashSet<>();` (preserva a ordem exibida na demonstração).
- **Mapa de Predecessores:** `Map<Sala, Sala> predecessores = new HashMap<>();`
- **Controle de Obstáculos:** nós bloqueados são ignorados. Uma sala que exige item fica transitável quando o inventário contém o item necessário.

### 3.2. Prova informal de otimalidade da BFS
Seja $d(s, v)$ a menor distância (em número de arestas) da origem $s$ até um vértice $v$:
1. A BFS inicializa a fila com a origem $s$, tendo $d(s, s) = 0$.
2. Todos os vértices com $d = 1$ são enfileirados e marcados antes de qualquer vértice com $d = 2$.
3. Pelo princípio da Fila FIFO, um vértice $v$ só é desenfileirado após todos os vértices de distâncias menores terem sido processados.
4. Consequentemente, quando o destino $t$ for alcançado pela primeira vez, a distância registrada até ele é estritamente mínima.

### 3.3. Dijkstra: menor custo total

Dijkstra usa uma fila de prioridade para escolher a sala de menor distância acumulada e relaxa os pesos das arestas. Como os custos são positivos, encontra o menor custo total em $O((V+E)\log V)$ com heap binário. A implementação usa `PriorityQueue` e reconstrói a rota por predecessores. Na configuração inicial, as passagens perigosas 7–9 e 9–11 custam 6, a rota do cofre 9–12 custa 4 e o atalho 11–13 custa 2. A chave torna transitável a rota pelo Portão 10; a BFS compara a quantidade de arestas enquanto Dijkstra pode preferir a rota de menor custo.

### 3.4. Complexidade da BFS
- **Tempo:**
  - Cada vértice entra e sai da fila no máximo 1 vez: $O(V)$.
  - Cada aresta é percorrida no máximo 2 vezes (uma para cada extremidade na lista de adjacência): $O(E)$.
  - **Tempo Total: $O(V + E)$**.
- **Espaço Auxiliar:**
  - A fila armazena no máximo $V$ vértices: $O(V)$.
  - O conjunto `visitados` armazena até $V$ referências: $O(V)$.
  - O mapa `predecessores` armazena até $V$ entradas: $O(V)$.
  - **Espaço auxiliar do cálculo: $O(V)$**. O registro didático opcional guarda eventos compactos, em $O(V + E)$; ele não armazena uma cópia integral da fila e dos visitados em cada evento.

### 3.5. Demonstração interativa do BFS

`Grafo.buscarMenorCaminho` executa a BFS e registra, no mesmo laço, eventos de início, retirada da fila, análise de vizinhos, descobertas, bloqueios e reconstrução do caminho. `PassoBFS` mantém esses deltas compactos; `ResultadoBFS.criarEstadoPasso` reproduz os eventos até uma posição para a interface exibir fila, visitados, vizinhos e novas descobertas. Assim, a tela não executa um segundo algoritmo nem anima uma rota pronta como se fosse uma busca. O diálogo oferece anterior, próximo, execução automática, pausa e reinício.

Ao fechar a demonstração, o mapa remove os destaques temporários. Se a partida mudar, o diálogo é fechado para impedir que a interface apresente um rastro antigo.

---

## 4. PADRÕES DE PROJETO E ARQUITETURA

O projeto adota uma arquitetura inspirada no modelo MVC (Model-View-Controller) simplificado:
- **Modelo:** `Sala` e `TipoSala` contêm o estado do domínio e dos vértices.
- **Estrutura de Dados:** `Grafo`, `ResultadoBFS` e `ResultadoDijkstra` encapsulam as relações de adjacência e os algoritmos de busca, sem dependência gráfica.
- **Controlador / Domínio do Jogo:** `Jogo` coordena as regras de transição de salas, inventário, tesouros, missões, tempo e pontuação.
- **Progressão:** `Missao` mantém estado e recompensa de cada objetivo; `ResumoAventura` congela as estatísticas ao alcançar a saída.
- **Visualização (View):** `TelaPrincipal`, `PainelMapa`, `PainelInformacoes` cuidam exclusivamente da renderização visual e da interação com o usuário via Swing.
- **Padrão Observer:** `JogoListener` permite que os componentes visuais recebam notificações desacopladas a cada movimentação ou cálculo de rota.

### Missões, atalhos e resumo

As missões usam condições do domínio que já existem no grafo: obter a chave na Biblioteca, destrancar o vértice do Portão, visitar o Santuário e coletar os três tesouros. O avanço é recalculado após cada movimento; uma missão opcional só credita sua recompensa na primeira conclusão. Abrir o portão altera `Sala.isBloqueada`, portanto BFS e Dijkstra consultam o mesmo estado que permite a movimentação. A saída possui rotas alternativas e não exige a chave.

O cronômetro pertence ao estado da partida e reinicia junto com o mapa. Na vitória, `ResumoAventura` registra tempo, pontos, movimentos, exploração, tesouros, missões e uma classificação com critérios visíveis. Os atalhos WASD/setas selecionam um vizinho existente pela direção das coordenadas; a tecla B executa a BFS. Nenhum atalho cria arestas ou movimenta para uma sala que não seja adjacente.

### Campanha, combate e recursos

A campanha começa na topologia fixa de 13 salas e avança por oito fases. Da segunda fase em diante, `MasmorraPadrao.criarMasmorraProcedural` cria mapas crescentes de 16 a 34 vértices, com uma espinha conectada para garantir caminho entre entrada e portal e arestas extras para exploração. O controlador `Jogo` preserva pontuação, movimentos, cronômetro e recursos, distribui encontros, armadilhas e segredos, e posiciona chefes nas fases 4 e 8. O resumo agrega os 188 vértices possíveis da campanha.

`Inimigo` modela vida, dano e classificação de chefe sem depender da interface. Ao entrar em uma câmara guardada, o movimento fica suspenso até o combate terminar. Ataques, poções e bombas consomem o turno; equipamentos e melhorias alteram dano, resistência, vida e preparação. Baús protegidos só são entregues depois da vitória e repõem itens consumíveis. Chefes marcam marcos de progressão; o último precisa ser derrotado para vencer.

Cada fase possui uma armadilha que causa dano uma única vez. Duas quedas permitem recuar à entrada com 55 PV, perdendo pontos e enfraquecendo o guardião; uma nova queda encerra a expedição. Portais recuperam vida. `PerfilJogador` registra compras e recompensas localmente, com gravação atômica e identificadores para impedir pagamento duplicado.

Na interface, a barra de vida, a etapa atual, os consumíveis e os controles de combate ficam no painel lateral. O mapa muda a paleta por fase e sinaliza guardiões, chefe e armadilhas. O temporizador de animação existente permanece ativo somente enquanto o painel está visível; a lógica de combate e dano continua isolada do Java2D.

# ROTEIRO DE APRESENTAÇÃO ACADÊMICA — GRAPHQUEST

## Atividade ADO de Estrutura de Dados (Java e Grafos)

Este documento foi elaborado para guiar a apresentação da equipe perante o professor e a turma, alinhado aos 10 slides contidos no arquivo `apresentacao/GraphQuest_ADO.pptx`.

---

### SLIDE 1: Capa e Identificação
- **O que falar:**
  "Boa noite, professor(a) e colegas. Hoje apresentamos o projeto **GraphQuest — A Jornada dos Grafos**, desenvolvido como atividade prática da disciplina de Estrutura de Dados. O projeto consiste em um jogo desktop de exploração de masmorra desenvolvido em Java puro com Swing, onde aplicamos de forma concreta e visual a estrutura de dados Grafo e o algoritmo de Busca em Largura (BFS)."
- **Destaque:** Apresentar os integrantes da equipe e a turma.

---

### SLIDE 2: Contexto e Problema Abordado
- **O que falar:**
  "O problema clássico que nos propusemos a resolver é a navegação ótima em ambientes fechados com bifurcações, obstáculos e objetivos múltiplos. Em jogos e sistemas de roteamento reais, como encontrar a menor rota até a saída sem entrar em ciclos e contornando desmoronamentos? Para resolver isso sem 'gambiarras' ou cálculos visuais artificiais, modelamos o labirinto usando a teoria matemática de Grafos."
- **Destaque:** A separação estrita entre a lógica matemática do grafo e a camada visual da interface.
- **Jogabilidade:** O placar mostra objetivo e progresso; o cristal bônus muda de sala ao reiniciar a partida.
- **Feedback:** Deslocamento animado, avisos visuais e resumo de desempenho ao vencer tornam as ações mais fáceis de acompanhar.

---

### SLIDE 3: Conceitos Fundamentais de Grafos
- **O que falar:**
  "Um Grafo $G$ é formalmente definido pelo par $(V, E)$:
  - **Vértices ($V$):** Cada câmara ou sala da masmorra é um vértice.
  - **Arestas ($E$):** Cada passagem ou corredor navegável entre duas salas é uma aresta.
  - **Grafo Não-Direcionado:** O tráfego é bidirecional. Se você vai da Entrada para o Corredor, pode voltar pelo mesmo caminho.
  - **Grafo Ponderado:** Os corredores têm custos positivos, exibidos no mapa."
- **Destaque:** BFS minimiza o número de passagens; Dijkstra minimiza a soma dos custos. São objetivos diferentes e as rotas podem divergir.

---

### SLIDE 4: Representação do Mapa como Grafo
- **O que falar:**
  "A nossa masmorra é composta por **13 salas (vértices)** e **19 corredores (arestas)**:
  - Sala 1: Entrada da Cripta (ponto de partida).
  - Salas Comuns: Corredores de transição.
  - Salas de Tesouro: Baús com pontuações (+100, +200, +300 pts) coletáveis apenas uma vez.
  - Salas Especiais: A Biblioteca Oculta (sala 8), que contém a 'Chave Mística'.
  - Obstáculos: Sala 6 (Passagem Soterrada, desmoronamento permanente) e Sala 10 (Portão de Ferro, que só abre se o jogador tiver a Chave Mística).
  - A primeira fase tem 13 salas; as sete seguintes crescem de 16 até 34 vértices conectados.
  - A campanha tem oito fases, com chefes nas fases 4 e 8, além de monstros e relíquias nas câmaras."
- **Destaque:** O jogador não pode atravessar paredes nem pular para salas desconectadas; a movimentação respeita 100% as arestas da lista de adjacência.

---

### SLIDE 5: Tecnologias e Arquitetura do Software
- **O que falar:**
  "Optamos por tecnologias nativas e robustas:
  - **Java 17/21 LTS** utilizando recursos da biblioteca padrão, sem Spring Boot ou dependências de internet.
  - **Java Swing e Java2D (Graphics2D):** Todo o desenho do mapa é vetorial com antialiasing, gradientes e animações de pulso.
  - **Arquitetura Modular:** Separada em pacotes com responsabilidades bem definidas: `modelo` (Sala, TipoSala), `grafo` (Grafo, ResultadoBFS e ResultadoDijkstra), `jogo` (regras e estado da partida) e `ui` (telas e painéis)."

---

### SLIDE 6: Estrutura de Dados — Lista de Adjacência
- **O que falar:**
  "No código, a representação escolhida foi a **Lista de Adjacência**:
  `Map<Sala, List<Sala>> listaAdjacencia;` e mapa auxiliar de pesos das arestas.
  Por que não usamos uma Matriz de Adjacência?
  1. **Consumo de Memória:** Uma masmorra é um grafo esparso (poucas arestas por vértice). A matriz gastaria $O(V^2)$, enquanto a lista de adjacência gasta apenas $O(V + E)$.
  2. **Eficiência de Consulta:** Para listar vizinhos, acessamos a lista da sala em O(1) médio e percorremos O(grau). A verificação de uma aresta também percorre a lista e custa O(grau)."

---

### SLIDE 7: Algoritmo de Busca em Largura (BFS)
- **O que falar:**
  "A BFS visita o grafo em camadas a partir da origem e minimiza o número de passagens. Implementamos fila, conjunto de visitados e mapa de predecessores. Dijkstra usa fila de prioridade para minimizar o custo total dos corredores. O mapa destaca BFS em dourado e Dijkstra em azul. O portão é considerado transitável quando a chave está no inventário."
- **Complexidade:** BFS: tempo $O(V + E)$ e espaço $O(V)$. Dijkstra: tempo $O((V + E)\log V)$ e espaço $O(V)$.

---

### SLIDE 8: Demonstração da Aplicação em Funcionamento
- **O que falar:**
  "A campanha atravessa três mapas temáticos. Guardiões protegem câmaras e baús, o combate por turnos usa ataques, poções e bombas, armadilhas causam dano, e o Dragão das Sombras protege o portal final. O painel exibe fase, vida e recursos."
- **Escolha estratégica:** O caminho pelo Cofre oferece tesouros, mas custa mais para Dijkstra. A chave abre o Portão e revela o atalho de menor custo; a saída também pode ser alcançada sem a chave.
- **Controles:** Clique nas salas, use WASD/setas para vizinhos conectados ou B para acionar a BFS. A demonstração acadêmica do slide anterior mostra o algoritmo passo a passo.

---

### SLIDE 9: Validação e Testes Automatizados
- **O que falar:**
  "Desenvolvemos uma bateria standalone de 27 testes automatizados cobrindo o grafo, a BFS, as missões, a progressão de campanha e o combate:
  1. Caminho simples entre salas.
  2. Escolha garantida do menor caminho em rotas com alternativas de diferentes tamanhos.
  3. Destino inalcançável em grafos desconexos.
  4. Origem igual ao destino (distância zero).
  5. Exclusão estrita de salas bloqueadas da rota.
  6. Ausência de loops infinitos em grafos cíclicos.
  7. Reinício da partida e integridade do grafo após o reset.
  8. Coleta única de tesouro e pontuação.
  9. Registro dos eventos e reconstrução da BFS.
  10. Preservação de estado após movimento inválido.
  11. Chave, portão e reinício completo.
  12. Condição de vitória.
  13. Identidade dos vértices e rejeição de ID duplicado.
  14. Dijkstra com pesos.
  15. Evento bônus de coleta única.
  16. Rejeição de origens/destinos inválidos e grafo vazio.
  17. Ausência de arestas duplicadas.
  18. A chave não abre um bloqueio permanente.
  Também podemos explorar as rotas opcionais: o Santuário recompensa o desvio, a chave abre o portão e os três baús concluem uma missão de bônus. Os controles WASD/setas usam apenas vizinhos existentes; B aciona a BFS. Ao concluir, o jogo mostra tempo, pontuação, exploração, tesouros, missões e classificação.

  Testamos também que a saída continua alcançável sem a chave. A classificação Mestre exige explorar 15 salas e encontrar ao menos dois terços dos tesouros; Aventureiro exige 10 salas e um objetivo opcional.

  Na última execução do script, os 27 testes standalone passaram. Também cobrimos combate, consumíveis e armadilhas. Os testes JUnit dependem do Maven, que não estava instalado no ambiente de desenvolvimento."

---

### SLIDE 10: Considerações Finais
- **O que falar:**
  "Como considerações finais, o GraphQuest cumpriu integralmente todos os requisitos do enunciado:
  - O grafo é real, funcional e utilizado pela mecânica do jogo.
  - A interface gráfica é agradável, temática e didática.
  - A apresentação PowerPoint e todos os códigos-fonte foram entregues.
  Como melhoria futura, poderíamos gerar masmorras proceduralmente com o algoritmo de Kruskal."
- **Encerramento:** "Agradecemos a atenção do professor e dos colegas e estamos abertos a perguntas!"

---

### PERGUNTAS FREQUENTES DO PROFESSOR (COMO RESPONDER)

1. **P: Por que a BFS garante o menor caminho e não a DFS (Busca em Profundidade)?**
   - **R:** "Porque a BFS explora em camadas e garante a menor quantidade de arestas. A DFS pode aprofundar um ramo e encontrar uma rota mais longa primeiro. Para menor soma de pesos, usamos Dijkstra."

2. **P: Como as salas bloqueadas são tratadas pelo algoritmo?**
   - **R:** "A passagem soterrada é permanente e fica fora das rotas. O portão só pode ser atravessado quando a chave está no inventário; tanto BFS quanto Dijkstra recebem esse inventário ao calcular a rota."

3. **P: Se o grafo fosse ponderado, a BFS ainda funcionaria?**
   - **R:** "A BFS do jogo minimiza a quantidade de passagens, sem considerar seus pesos. Dijkstra considera os pesos positivos e minimiza o custo total; por isso, a rota pode ser diferente."

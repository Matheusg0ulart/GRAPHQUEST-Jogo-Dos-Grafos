package br.com.graphquest.grafo;

import br.com.graphquest.modelo.Sala;

import java.util.*;

/**
 * Grafo não-direcionado com lista de adjacência e pesos por aresta.
 * 
 * Nesta implementação:
 * - Vértices (V): Representam as Salas da masmorra.
 * - Arestas (E): Representam as passagens bidirecionais navegáveis entre duas salas.
 * 
 * Implementação puramente em Java padrão, sem dependência de bibliotecas externas
 * e desacoplada da camada visual (Swing / Java2D).
 * 
 * Complexidade dos Algoritmos:
 * - Adição de Vértice: O(1) médio, com índice por ID para rejeitar duplicatas
 * - Adição de Aresta: O(grau) para impedir duplicatas
 * - Consulta do mapa de vizinhos: O(1) médio; testar uma aresta: O(grau)
 * - Busca em Largura (BFS):
 *     * Tempo: O(V + E), percorrendo cada vértice e aresta no máximo uma vez.
 *     * Espaço do cálculo: O(V); registro opcional de eventos didáticos: O(V + E).
 * - Dijkstra com fila de prioridade: O((V + E) log V), espaço O(V).
 */
public class Grafo {
    // Lista de Adjacência: mapeia cada Sala para a lista de Salas vizinhas conectadas
    private final Map<Sala, List<Sala>> listaAdjacencia;
    private final Map<Sala, Map<Sala, Integer>> pesosArestas;
    private final Map<Integer, Sala> verticesPorId;
    private record EntradaDijkstra(Sala sala, int custo) { }

    public Grafo() {
        this.listaAdjacencia = new LinkedHashMap<>();
        this.pesosArestas = new LinkedHashMap<>();
        this.verticesPorId = new HashMap<>();
    }

    /**
     * Adiciona um novo vértice (Sala) ao grafo.
     * 
     * @param sala A sala a ser adicionada como nó do grafo.
     */
    public void adicionarVertice(Sala sala) {
        if (sala == null) {
            throw new IllegalArgumentException("A sala (vértice) não pode ser nula.");
        }
        Sala existente = verticesPorId.get(sala.getId());
        if (existente != null && existente != sala) {
            throw new IllegalArgumentException("Já existe uma sala com o ID " + sala.getId() + ".");
        }
        listaAdjacencia.putIfAbsent(sala, new ArrayList<>());
        pesosArestas.putIfAbsent(sala, new LinkedHashMap<>());
        verticesPorId.putIfAbsent(sala.getId(), sala);
    }

    /**
     * Adiciona uma aresta não-direcionada entre duas salas (conexão bidirecional).
     * Se os vértices ainda não estiverem no grafo, eles serão inseridos automaticamente.
     * 
     * @param origem  Primeira sala conectada.
     * @param destino Segunda sala conectada.
     */
    public void adicionarAresta(Sala origem, Sala destino) {
        adicionarAresta(origem, destino, 1);
    }

    /** Adiciona uma passagem bidirecional com custo positivo. */
    public void adicionarAresta(Sala origem, Sala destino, int peso) {
        if (origem == null || destino == null) {
            throw new IllegalArgumentException("As salas de origem e destino não podem ser nulas.");
        }
        if (peso <= 0) throw new IllegalArgumentException("O custo da aresta deve ser positivo.");
        adicionarVertice(origem);
        adicionarVertice(destino);
        if (origem == destino) {
            return; // Grafos simples na masmorra não possuem laços
        }

        List<Sala> vizinhosOrigem = listaAdjacencia.get(origem);
        List<Sala> vizinhosDestino = listaAdjacencia.get(destino);

        if (!vizinhosOrigem.contains(destino)) {
            vizinhosOrigem.add(destino);
        }
        if (!vizinhosDestino.contains(origem)) {
            vizinhosDestino.add(origem);
        }
        pesosArestas.get(origem).put(destino, peso);
        pesosArestas.get(destino).put(origem, peso);
    }

    public int obterPeso(Sala origem, Sala destino) {
        if (!contemVertice(origem) || !contemVertice(destino)) return Integer.MAX_VALUE;
        Map<Sala, Integer> pesos = pesosArestas.get(origem);
        return pesos == null ? Integer.MAX_VALUE : pesos.getOrDefault(destino, Integer.MAX_VALUE);
    }

    /**
     * Retorna a lista de salas vizinhas conectadas diretamente à sala informada.
     * 
     * @param sala Vértice a ser consultado.
     * @return Lista imutável de vizinhos diretos, ou lista vazia caso a sala não exista.
     */
    public List<Sala> obterVizinhos(Sala sala) {
        if (!contemVertice(sala)) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(listaAdjacencia.get(sala));
    }

    /**
     * Verifica se existe uma aresta direta conectando duas salas.
     */
    public boolean saoVizinhos(Sala s1, Sala s2) {
        if (!contemVertice(s1) || !contemVertice(s2)) {
            return false;
        }
        return listaAdjacencia.get(s1).contains(s2);
    }

    /** Confere se a instância recebida é o vértice registrado, não apenas se o ID coincide. */
    public boolean contemVertice(Sala sala) {
        return sala != null && localizarVertice(sala) == sala;
    }

    private Sala localizarVertice(Sala sala) {
        return sala == null ? null : verticesPorId.get(sala.getId());
    }

    /**
     * Retorna o conjunto de todos os vértices registrados no grafo.
     */
    public Set<Sala> obterTodosVertices() {
        return Collections.unmodifiableSet(listaAdjacencia.keySet());
    }

    /**
     * Retorna a quantidade total de vértices (V).
     */
    public int obterQuantidadeVertices() {
        return listaAdjacencia.size();
    }

    /**
     * Retorna a quantidade total de arestas (E).
     * Como o grafo é não-direcionado, cada aresta aparece duas vezes na lista de adjacência.
     */
    public int obterQuantidadeArestas() {
        int somaGraus = 0;
        for (List<Sala> vizinhos : listaAdjacencia.values()) {
            somaGraus += vizinhos.size();
        }
        return somaGraus / 2;
    }

    /**
     * Executa o algoritmo de Busca em Largura (BFS - Breadth-First Search) para encontrar
     * o menor caminho (com menor número de arestas) entre a sala de origem e a sala de destino.
     * 
     * REQUISITOS OBRIGATÓRIOS ATENDIDOS:
 * 1. Fila explícita: {@code Queue<Sala> fila = new ArrayDeque<>()}
 * 2. Conjunto de visitados: {@code Set<Sala> visitados = new LinkedHashSet<>()}
     * 3. Mapeamento de predecessores: {@code Map<Sala, Sala> predecessores = new HashMap<>()}
     * 4. Reconstrução iterativa do caminho a partir do destino retroagindo até a origem.
     * 5. Salas bloqueadas permanentes não são transitadas; salas trancadas exigem o item correspondente.
     * 
     * @param origem  Sala de onde o jogador partirá.
     * @param destino Sala de destino almejada (ex: Saída da masmorra).
     * @return {@link ResultadoBFS} contendo o caminho, a métrica de arestas e explicação didática.
     */
    public ResultadoBFS buscarMenorCaminho(Sala origem, Sala destino) {
        return buscarMenorCaminho(origem, destino, Collections.emptySet());
    }

    /** BFS com salas trancadas liberadas quando a chave correspondente está no inventário. */
    public ResultadoBFS buscarMenorCaminho(Sala origem, Sala destino, Set<String> inventario) {
        Set<String> itens = inventario == null ? Collections.emptySet() : inventario;
        if (origem == null || destino == null) {
            return new ResultadoBFS(false, origem, destino, Collections.emptyList(), Collections.emptyList(),
                    "Origem ou destino nulo.", "Não foi possível iniciar a busca: nós inválidos.",
                    Collections.singletonList(novoPasso(PassoBFS.Tipo.ERRO,
                            "Origem ou destino nulo.", null, null, Collections.emptyList(), false)));
        }

        if (!contemVertice(origem) || !contemVertice(destino)) {
            return new ResultadoBFS(false, origem, destino, Collections.emptyList(), Collections.emptyList(),
                    "Sala não pertence ao grafo da masmorra.", "Um dos nós informados não existe no grafo.",
                    Collections.singletonList(novoPasso(PassoBFS.Tipo.ERRO,
                            "Origem ou destino não pertence ao grafo.", null, null, Collections.emptyList(), false)));
        }

        // Cenário 1: Origem já é o destino
        if (estaBloqueadaPara(origem, itens)) {
            return new ResultadoBFS(false, origem, destino, Collections.emptyList(), Collections.emptyList(),
                    "A sala de origem está bloqueada (" + origem.getMotivoBloqueio() + ").",
                    "A busca não pode iniciar a partir de uma sala intransitável.",
                    Collections.singletonList(novoPasso(PassoBFS.Tipo.ERRO,
                            "A origem está bloqueada.", origem, null, Collections.emptyList(), false)));
        }
        if (estaBloqueadaPara(destino, itens)) {
            return new ResultadoBFS(false, origem, destino, Collections.emptyList(), Collections.emptyList(),
                    "A sala de destino está inacessível (" + destino.getMotivoBloqueio() + ").",
                    "O destino selecionado possui uma barreira intransitável.",
                    Collections.singletonList(novoPasso(PassoBFS.Tipo.ERRO,
                            "O destino está bloqueado.", destino, null, Collections.emptyList(), false)));
        }

        if (origem.equals(destino)) {
            List<Sala> caminhoTrivial = Collections.singletonList(origem);
            return new ResultadoBFS(true, origem, destino, caminhoTrivial, caminhoTrivial,
                    "Você já está na sala de destino!",
                    "Origem e destino coincidem. Distância: 0 arestas.",
                    List.of(novoPasso(PassoBFS.Tipo.INICIO, "Fila inicializada com " + rotulo(origem) + ".",
                                    null, origem, Collections.emptyList(), false),
                            novoPasso(PassoBFS.Tipo.PROCESSAMENTO, "Processando " + rotulo(origem) + ".",
                                    origem, null, Collections.emptyList(), false),
                            novoPasso(PassoBFS.Tipo.CAMINHO_RECONSTRUIDO,
                                    "Origem e destino são a mesma sala. Busca concluída.", null, null, caminhoTrivial, true)));
        }

        // Cenário 2: Origem ou Destino estão bloqueados

        // --- 1. Estruturas Fundamentais da BFS ---
        Queue<Sala> fila = new ArrayDeque<>();
        Set<Sala> visitados = new LinkedHashSet<>();
        Map<Sala, Sala> predecessores = new HashMap<>();
        List<Sala> ordemExploracao = new ArrayList<>();
        List<PassoBFS> passos = new ArrayList<>();

        // Inicialização com o nó de origem
        fila.add(origem);
        visitados.add(origem);
        passos.add(novoPasso(PassoBFS.Tipo.INICIO, "Fila inicializada com " + rotulo(origem) + ".",
                null, origem, Collections.emptyList(), false));

        boolean encontrouDestino = false;

        // --- 2. Laço de Exploração em Largura ---
        while (!fila.isEmpty()) {
            Sala atual = fila.poll();
            ordemExploracao.add(atual);
            passos.add(novoPasso(PassoBFS.Tipo.PROCESSAMENTO, "Processando " + rotulo(atual) + ".",
                    atual, null, Collections.emptyList(), false));

            // Ao alcançar o destino, interrompe a busca para reconstrução imediata
            if (atual.equals(destino)) {
                encontrouDestino = true;
                break;
            }

            List<Sala> vizinhos = listaAdjacencia.getOrDefault(atual, Collections.emptyList());
            for (Sala vizinho : vizinhos) {
                // REGRA FUNDAMENTAL: Salas bloqueadas (obstáculos) não podem ser transitadas
                if (estaBloqueadaPara(vizinho, itens)) {
                    passos.add(novoPasso(PassoBFS.Tipo.VIZINHO_BLOQUEADO,
                            rotulo(vizinho) + " ignorada: passagem bloqueada.", atual, vizinho,
                            Collections.emptyList(), false));
                    continue;
                }

                // Se o vizinho ainda não foi visitado, enfileira e registra seu predecessor
                if (!visitados.contains(vizinho)) {
                    visitados.add(vizinho);
                    predecessores.put(vizinho, atual);
                    fila.add(vizinho);
                    passos.add(novoPasso(PassoBFS.Tipo.VIZINHO_DESCOBERTO,
                            rotulo(vizinho) + " descoberta e adicionada à fila.", atual, vizinho,
                            Collections.emptyList(), false));

                    if (vizinho.equals(destino)) {
                        encontrouDestino = true;
                        passos.add(novoPasso(PassoBFS.Tipo.DESTINO_ENCONTRADO,
                                "Destino encontrado em " + rotulo(vizinho) + ".", atual, vizinho,
                                Collections.emptyList(), false));
                        break;
                    }
                } else {
                    passos.add(novoPasso(PassoBFS.Tipo.VIZINHO_VISITADO,
                            rotulo(vizinho) + " já havia sido visitada.", atual, vizinho,
                            Collections.emptyList(), false));
                }
            }

            if (encontrouDestino) {
                break;
            }
        }

        // --- 3. Processamento do Resultado e Reconstrução do Caminho ---
        if (!encontrouDestino) {
            String explicacao = String.format(
                    "A BFS explorou %d salas na fila mas não encontrou caminho até '%s'.\n" +
                    "Possível causa: Todas as rotas estão bloqueadas por obstáculos ou desmoronamentos.",
                    ordemExploracao.size(), destino.getNome()
            );
            passos.add(novoPasso(PassoBFS.Tipo.SEM_CAMINHO,
                    "Fila vazia. Destino inalcançável com os bloqueios atuais.", null, null,
                    Collections.emptyList(), true));
            return new ResultadoBFS(false, origem, destino, Collections.emptyList(), ordemExploracao,
                    "Não há caminho transitável até a saída!", explicacao, passos);
        }

        // Reconstrução do caminho partindo do destino até a origem usando a tabela de predecessores
        LinkedList<Sala> caminho = new LinkedList<>();
        Sala passo = destino;
        while (passo != null) {
            caminho.addFirst(passo);
            passo = predecessores.get(passo);
        }

        int distanciaArestas = caminho.size() - 1;
        passos.add(novoPasso(PassoBFS.Tipo.CAMINHO_RECONSTRUIDO,
                "Caminho reconstruído: " + formatarCaminho(caminho) + ".", null, null, caminho, true));
        String explicacao = gerarExplicacaoDidaticaBFS(origem, destino, caminho, ordemExploracao, distanciaArestas);

        return new ResultadoBFS(true, origem, destino, caminho, ordemExploracao,
                String.format("Caminho encontrado com sucesso: %d arestas (passos).", distanciaArestas),
                explicacao, passos);
    }

    private PassoBFS novoPasso(PassoBFS.Tipo tipo, String descricao, Sala atual,
                               Sala afetada, List<Sala> caminho, boolean concluido) {
        return new PassoBFS(tipo, descricao, atual, afetada, caminho, concluido);
    }

    private String rotulo(Sala sala) { return "Sala " + sala.getId() + " — " + sala.getNome(); }

    private String formatarCaminho(List<Sala> caminho) {
        return String.join(" → ", caminho.stream().map(s -> String.valueOf(s.getId())).toList());
    }

    private boolean estaBloqueadaPara(Sala sala, Set<String> inventario) {
        return sala.isBloqueada() && (sala.isBloqueioPermanente() || sala.getItemNecessario() == null
                || !inventario.contains(sala.getItemNecessario()));
    }

    /** Dijkstra: encontra a rota transitável de menor soma de custos. */
    public ResultadoDijkstra buscarMenorCusto(Sala origem, Sala destino, Set<String> inventario) {
        Set<String> itens = inventario == null ? Collections.emptySet() : inventario;
        if (!contemVertice(origem) || !contemVertice(destino) || estaBloqueadaPara(origem, itens)
                || estaBloqueadaPara(destino, itens)) {
            return ResultadoDijkstra.falha(origem, destino, "Origem ou destino inválido/bloqueado.");
        }
        Map<Sala, Integer> distancias = new HashMap<>();
        Map<Sala, Sala> predecessores = new HashMap<>();
        Set<Sala> finalizados = new HashSet<>();
        List<Sala> exploradas = new ArrayList<>();
        for (Sala sala : listaAdjacencia.keySet()) distancias.put(sala, Integer.MAX_VALUE);
        distancias.put(origem, 0);
        PriorityQueue<EntradaDijkstra> fila = new PriorityQueue<>(Comparator.comparingInt(EntradaDijkstra::custo));
        fila.add(new EntradaDijkstra(origem, 0));
        while (!fila.isEmpty()) {
            EntradaDijkstra entrada = fila.poll();
            Sala atual = entrada.sala();
            if (entrada.custo() != distancias.get(atual)) continue;
            if (!finalizados.add(atual)) continue;
            exploradas.add(atual);
            if (atual.equals(destino)) break;
            for (Sala vizinho : listaAdjacencia.getOrDefault(atual, Collections.emptyList())) {
                if (estaBloqueadaPara(vizinho, itens) || finalizados.contains(vizinho)) continue;
                int custoAresta = obterPeso(atual, vizinho);
                if (distancias.get(atual) != Integer.MAX_VALUE
                        && distancias.get(atual) + custoAresta < distancias.get(vizinho)) {
                    distancias.put(vizinho, distancias.get(atual) + custoAresta);
                    predecessores.put(vizinho, atual);
                    fila.add(new EntradaDijkstra(vizinho, distancias.get(vizinho)));
                }
            }
        }
        if (distancias.get(destino) == Integer.MAX_VALUE) {
            return new ResultadoDijkstra(false, origem, destino, Collections.emptyList(), exploradas,
                    Integer.MAX_VALUE, "Não há rota transitável até o destino.");
        }
        LinkedList<Sala> caminho = new LinkedList<>();
        for (Sala passo = destino; passo != null; passo = predecessores.get(passo)) caminho.addFirst(passo);
        return new ResultadoDijkstra(true, origem, destino, caminho, exploradas, distancias.get(destino),
                "Menor custo total: " + distancias.get(destino) + " unidades de custo.");
    }

    /**
     * Gera uma explicação textual didática e rica sobre a execução da BFS
     * para demonstração acadêmica perante o professor.
     */
    private String gerarExplicacaoDidaticaBFS(Sala origem, Sala destino, List<Sala> caminho,
                                              List<Sala> ordemExploracao, int distancia) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DEMONSTRAÇÃO ACADÊMICA: BUSCA EM LARGURA (BFS) ===\n\n");
        sb.append(String.format("• Origem: %s\n", origem.getNome()));
        sb.append(String.format("• Destino: %s\n", destino.getNome()));
        sb.append(String.format("• Menor Distância: %d aresta(s) / passagem(ns)\n", distancia));
        sb.append(String.format("• Vértices Explorados na Fila: %d nós\n\n", ordemExploracao.size()));

        sb.append("ROTA ÓTIMA CALCULADA:\n");
        for (int i = 0; i < caminho.size(); i++) {
            Sala s = caminho.get(i);
            sb.append(String.format("  [%d] %s (%s)", (i + 1), s.getNome(), s.getTipo().getNomeExibicao()));
            if (i < caminho.size() - 1) {
                sb.append("\n       ↓  (Aresta navegável)\n");
            }
        }

        sb.append("\n\nPOR QUE ESTE É O MENOR CAMINHO?\n");
        sb.append("A Busca em Largura (BFS) explora o grafo em 'camadas' ou níveis de distância:\n");
        sb.append("1. Primeiro examina todos os vizinhos a 1 aresta da origem;\n");
        sb.append("2. Depois todos a 2 arestas, depois a 3, e assim sucessivamente;\n");
        sb.append("3. A BFS trata cada corredor como uma passagem (um passo), sem somar seus custos.\n");
        sb.append("   Por isso, garante o MENOR número de arestas, mesmo que os pesos sejam diferentes.\n");
        sb.append("   Para minimizar a soma dos pesos, use Dijkstra.\n\n");

        sb.append("ANÁLISE DE COMPLEXIDADE:\n");
        sb.append("• Tempo: O(V + E) = O(")
          .append(obterQuantidadeVertices()).append(" + ")
          .append(obterQuantidadeArestas()).append(") operações lineares.\n");
        sb.append("• Espaço Auxiliar: O(V) para armazenar Fila, Conjunto de Visitados e Mapa de Predecessores.");

        return sb.toString();
    }

    /**
     * Limpa todas as arestas e vértices do grafo.
     */
    public void limpar() {
        listaAdjacencia.clear();
        pesosArestas.clear();
        verticesPorId.clear();
    }
}

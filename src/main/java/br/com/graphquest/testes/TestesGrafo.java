package br.com.graphquest.testes;

import br.com.graphquest.grafo.Grafo;
import br.com.graphquest.grafo.ResultadoBFS;
import br.com.graphquest.jogo.Jogo;
import br.com.graphquest.modelo.Sala;
import br.com.graphquest.modelo.TipoSala;

import java.util.List;

/**
 * Bateria de Testes Automatizados da Estrutura de Dados Grafo e do Algoritmo BFS.
 * 
 * Executável diretamente via terminal com comando Java puro (sem exigir Maven/JUnit):
 *     java -cp target/classes br.com.graphquest.testes.TestesGrafo
 * 
 * Valida 24 cenários de grafo, buscas, movimentação, missões, tesouros, chave, bloqueios,
 * vitória, reinício e evento bônus sem dependências externas.
 */
public class TestesGrafo {

    private static int totalTestes = 0;
    private static int testesAprovados = 0;

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("   GRAPHQUEST — BATERIA DE TESTES DE ESTRUTURA DE DADOS & BFS");
        System.out.println("======================================================================\n");

        executarTeste("1. Caminho simples entre duas salas conectadas", TestesGrafo::testarCaminhoSimples);
        executarTeste("2. Escolha garantida do menor caminho em rotas alternativas (Menor Arestas)", TestesGrafo::testarMenorCaminhoAlternativas);
        executarTeste("3. Destino inalcançável (Grafo Desconexo)", TestesGrafo::testarDestinoInalcancavel);
        executarTeste("4. Origem igual ao destino (Caso Trivial)", TestesGrafo::testarOrigemIgualDestino);
        executarTeste("5. Exclusão estrita de salas bloqueadas (Obstáculos/Desmoronamento)", TestesGrafo::testarSalasBloqueadas);
        executarTeste("6. Ausência de loops infinitos em grafos com ciclos fechados", TestesGrafo::testarAusenciaLoopsInfinitos);
        executarTeste("7. Reinício da partida e integridade do grafo após reset", TestesGrafo::testarReinicioPartida);
        executarTeste("8. Coleta de tesouros única e regras de pontuação", TestesGrafo::testarMecanicaTesouros);
        executarTeste("9. Registro real dos passos da BFS", TestesGrafo::testarPassosBFS);
        executarTeste("10. Movimento inválido preserva o estado", TestesGrafo::testarMovimentoInvalido);
        executarTeste("11. Chave abre portão e reinício restaura a masmorra", TestesGrafo::testarChaveEReinicio);
        executarTeste("12. Vitória encerra a partida", TestesGrafo::testarCondicaoVitoria);
        executarTeste("13. Salas externas com ID repetido são rejeitadas", TestesGrafo::testarIdentidadeDosVertices);
        executarTeste("14. Dijkstra minimiza os pesos das arestas", TestesGrafo::testarDijkstraPonderado);
        executarTeste("15. Evento bônus é coletado uma única vez", TestesGrafo::testarEventoSurpresa);
        executarTeste("16. BFS rejeita origens e destinos fora do grafo", TestesGrafo::testarNosInvalidos);
        executarTeste("17. Arestas repetidas não duplicam vizinhos", TestesGrafo::testarArestaDuplicada);
        executarTeste("18. Chave não remove bloqueio permanente", TestesGrafo::testarBloqueioPermanente);
        executarTeste("19. Missões do Santuário e do portão pagam recompensa uma única vez", TestesGrafo::testarMissoesOpcionais);
        executarTeste("20. Missão de tesouros conclui após abrir todos os baús", TestesGrafo::testarMissaoTesouros);
        executarTeste("21. Resumo final registra tempo, exploração e classificação", TestesGrafo::testarResumoAventura);
        executarTeste("22. Movimento direcional seleciona somente vizinhos do grafo", TestesGrafo::testarMovimentoDirecional);
        executarTeste("23. A saída pode ser alcançada sem a chave por uma rota alternativa", TestesGrafo::testarSaidaSemChave);
        executarTeste("24. Classificação da campanha considera a exploração acumulada", TestesGrafo::testarClassificacaoMestre);
        executarTeste("25. Campanha avança por oito fases até o chefe final", TestesGrafo::testarCampanhaCompleta);
        executarTeste("26. Combate bloqueia a exploração e consumíveis afetam o inimigo", TestesGrafo::testarCombateEConsumiveis);
        executarTeste("27. Armadilha causa dano apenas uma vez por fase", TestesGrafo::testarArmadilha);

        System.out.println("\n----------------------------------------------------------------------");
        System.out.printf("RESULTADO FINAL: %d de %d testes APROVADOS (%.1f%% de sucesso)%n",
                testesAprovados, totalTestes, (testesAprovados * 100.0) / totalTestes);
        System.out.println("----------------------------------------------------------------------\n");

        if (testesAprovados != totalTestes) {
            System.err.println("ALERTA: Houve falhas nos testes automatizados!");
            System.exit(1);
        } else {
            System.out.println("TODOS OS TESTES FORAM CONCLUÍDOS COM SUCESSO!");
        }
    }

    private static void executarTeste(String nome, RunnableTeste teste) {
        totalTestes++;
        System.out.print("[TESTE " + totalTestes + "] " + nome + "... ");
        try {
            teste.executar();
            testesAprovados++;
            System.out.println("✓ APROVADO");
        } catch (Throwable e) {
            System.out.println("✗ FALHOU!");
            System.out.println("   Detalhe do erro: " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    @FunctionalInterface
    interface RunnableTeste {
        void executar() throws Exception;
    }

    private static void afirmar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError("Falha na validação: " + mensagem);
        }
    }

    // --- Implementação dos Cenários de Teste ---

    /**
     * Teste 1: Caminho simples direto A -> B -> C
     */
    private static void testarCaminhoSimples() {
        Grafo g = new Grafo();
        Sala a = new Sala(1, "A", "", TipoSala.COMUM, 0, 0);
        Sala b = new Sala(2, "B", "", TipoSala.COMUM, 0, 0);
        Sala c = new Sala(3, "C", "", TipoSala.COMUM, 0, 0);

        g.adicionarAresta(a, b);
        g.adicionarAresta(b, c);

        ResultadoBFS res = g.buscarMenorCaminho(a, c);

        afirmar(res.isSucesso(), "A BFS deveria encontrar o caminho de A até C.");
        afirmar(res.getQuantidadeArestas() == 2, "A distância deveria ser exatamente 2 arestas.");
        List<Sala> caminho = res.getCaminho();
        afirmar(caminho.size() == 3, "O caminho deve conter 3 vértices (A, B, C).");
        afirmar(caminho.get(0).equals(a), "Início do caminho deve ser A.");
        afirmar(caminho.get(1).equals(b), "Segundo vértice deve ser B.");
        afirmar(caminho.get(2).equals(c), "Destino deve ser C.");
    }

    /**
     * Teste 2: Grafo com dois caminhos alternativos de comprimentos diferentes.
     * Rota Longa: Origem -> X -> Y -> Z -> Destino (4 arestas)
     * Rota Curta: Origem -> W -> Destino (2 arestas)
     * A BFS DEVE retornar a rota curta de 2 arestas.
     */
    private static void testarMenorCaminhoAlternativas() {
        Grafo g = new Grafo();
        Sala origem = new Sala(1, "Origem", "", TipoSala.INICIAL, 0, 0);
        Sala destino = new Sala(99, "Destino", "", TipoSala.SAIDA, 0, 0);

        Sala x = new Sala(2, "X", "", TipoSala.COMUM, 0, 0);
        Sala y = new Sala(3, "Y", "", TipoSala.COMUM, 0, 0);
        Sala z = new Sala(4, "Z", "", TipoSala.COMUM, 0, 0);
        Sala w = new Sala(5, "W", "", TipoSala.COMUM, 0, 0);

        // Rota Longa (4 arestas)
        g.adicionarAresta(origem, x);
        g.adicionarAresta(x, y);
        g.adicionarAresta(y, z);
        g.adicionarAresta(z, destino);

        // Rota Curta (2 arestas)
        g.adicionarAresta(origem, w);
        g.adicionarAresta(w, destino);

        ResultadoBFS res = g.buscarMenorCaminho(origem, destino);

        afirmar(res.isSucesso(), "Deveria encontrar caminho com sucesso.");
        afirmar(res.getQuantidadeArestas() == 2,
                "A BFS deve encontrar a rota com o menor número de arestas (2 arestas via W), mas retornou: " + res.getQuantidadeArestas());
        afirmar(res.getCaminho().contains(w), "O caminho ótimo deve passar pelo nó W.");
    }

    /**
     * Teste 3: Destino isolado sem nenhuma aresta conectada.
     */
    private static void testarDestinoInalcancavel() {
        Grafo g = new Grafo();
        Sala a = new Sala(1, "A", "", TipoSala.COMUM, 0, 0);
        Sala b = new Sala(2, "B", "", TipoSala.COMUM, 0, 0);
        Sala isolada = new Sala(3, "Isolada", "", TipoSala.COMUM, 0, 0);

        g.adicionarAresta(a, b);
        g.adicionarVertice(isolada); // Nó isolado

        ResultadoBFS res = g.buscarMenorCaminho(a, isolada);

        afirmar(!res.isSucesso(), "A busca para nó isolado não deve ter sucesso.");
        afirmar(res.getCaminho().isEmpty(), "O caminho retornado deve ser vazio.");
    }

    /**
     * Teste 4: Origem igual ao destino.
     */
    private static void testarOrigemIgualDestino() {
        Grafo g = new Grafo();
        Sala s = new Sala(1, "Mesma", "", TipoSala.COMUM, 0, 0);
        g.adicionarVertice(s);

        ResultadoBFS res = g.buscarMenorCaminho(s, s);

        afirmar(res.isSucesso(), "Busca de s para s deve ter sucesso.");
        afirmar(res.getQuantidadeArestas() == 0, "A distância para o próprio nó deve ser 0 arestas.");
        afirmar(res.getCaminho().size() == 1, "O caminho deve conter apenas o próprio nó.");
    }

    /**
     * Teste 5: Salas bloqueadas não podem ser incluídas na rota.
     * Grafo:
     * Origem -> Bloqueada -> Destino (2 arestas, mas BLOQUEADA)
     * Origem -> Contorno1 -> Contorno2 -> Destino (3 arestas, LIVRE)
     * A BFS deve obrigatoriamente desviar pela rota livre de 3 arestas.
     */
    private static void testarSalasBloqueadas() {
        Grafo g = new Grafo();
        Sala origem = new Sala(1, "Origem", "", TipoSala.INICIAL, 0, 0);
        Sala bloqueada = new Sala(2, "Bloqueada", "", TipoSala.BLOQUEADA, 0, 0,
                0, true, true, "Pedras Caídas", null, null);
        Sala destino = new Sala(3, "Destino", "", TipoSala.SAIDA, 0, 0);

        Sala c1 = new Sala(4, "Contorno1", "", TipoSala.COMUM, 0, 0);
        Sala c2 = new Sala(5, "Contorno2", "", TipoSala.COMUM, 0, 0);

        // Caminho bloqueado curto
        g.adicionarAresta(origem, bloqueada);
        g.adicionarAresta(bloqueada, destino);

        // Caminho livre alternativo
        g.adicionarAresta(origem, c1);
        g.adicionarAresta(c1, c2);
        g.adicionarAresta(c2, destino);

        ResultadoBFS res = g.buscarMenorCaminho(origem, destino);

        afirmar(res.isSucesso(), "Deve encontrar a rota alternativa contornando o obstáculo.");
        afirmar(!res.getCaminho().contains(bloqueada), "O caminho NÃO pode conter a sala bloqueada!");
        afirmar(res.getQuantidadeArestas() == 3, "A distância deve ser de 3 arestas pelo contorno.");
    }

    /**
     * Teste 6: Grafo contendo múltiplos ciclos fechados (ex: triângulos e pentágonos).
     * Garante que o conjunto de visitados previne loops infinitos.
     */
    private static void testarAusenciaLoopsInfinitos() {
        Grafo g = new Grafo();
        Sala n1 = new Sala(1, "1", "", TipoSala.COMUM, 0, 0);
        Sala n2 = new Sala(2, "2", "", TipoSala.COMUM, 0, 0);
        Sala n3 = new Sala(3, "3", "", TipoSala.COMUM, 0, 0);
        Sala n4 = new Sala(4, "4", "", TipoSala.COMUM, 0, 0);

        // Ciclo 1-2-3-1
        g.adicionarAresta(n1, n2);
        g.adicionarAresta(n2, n3);
        g.adicionarAresta(n3, n1);

        // Conexão externa
        g.adicionarAresta(n3, n4);

        long inicio = System.currentTimeMillis();
        ResultadoBFS res = g.buscarMenorCaminho(n1, n4);
        long duracao = System.currentTimeMillis() - inicio;

        afirmar(res.isSucesso(), "Deve encontrar caminho no grafo cíclico.");
        afirmar(duracao < 1000, "A execução deve ser quase instantânea sem entrar em loop infinito.");
        afirmar(res.getQuantidadeArestas() == 2, "Menor caminho deve ter 2 arestas (1->3->4).");
    }

    /**
     * Teste 7: Integridade do jogo ao reiniciar partida.
     */
    private static void testarReinicioPartida() {
        Jogo jogo = new Jogo();
        Sala inicial = jogo.getSalaAtual();

        // Faz um movimento
        List<Sala> vizinhos = jogo.getGrafo().obterVizinhos(inicial);
        Sala proxima = null;
        for (Sala v : vizinhos) {
            if (!v.isBloqueada()) {
                proxima = v;
                break;
            }
        }
        afirmar(proxima != null, "Deve haver vizinho acessível.");
        jogo.moverPara(proxima);

        afirmar(!jogo.getSalaAtual().equals(inicial), "Jogador deve ter mudado de sala.");
        afirmar(jogo.getQuantidadeMovimentos() > 0, "Passos devem ter aumentado.");

        // Reinicia
        jogo.reiniciarPartida();

        afirmar(jogo.getSalaAtual().equals(inicial), "Após reiniciar, jogador deve voltar à sala inicial.");
        afirmar(jogo.getPontuacao() == 0, "Pontuação deve ser zerada.");
        afirmar(jogo.getQuantidadeMovimentos() == 0, "Movimentos devem ser zerados.");
        afirmar(jogo.getInventario().isEmpty(), "Inventário deve ser esvaziado.");
        afirmar(!jogo.isPartidaConcluida(), "Partida não pode estar marcada como concluída.");
    }

    /**
     * Teste 8: Mecânica de tesouros únicos e pontuação.
     */
    private static void testarMecanicaTesouros() {
        Sala salaTesouro = new Sala(50, "Cofre", "", TipoSala.TESOURO, 0, 0, 150);
        afirmar(salaTesouro.temTesouro(), "A sala deve ter tesouro ativo inicialmente.");
        afirmar(salaTesouro.getValorTesouro() == 150, "Valor do tesouro deve ser 150.");

        salaTesouro.setTesouroColetado(true);
        afirmar(!salaTesouro.temTesouro(), "Após coletado, a sala não pode mais conceder tesouro.");
        afirmar(salaTesouro.isTesouroColetado(), "O estado deve constar como coletado.");
    }

    private static void testarPassosBFS() {
        Grafo g = new Grafo();
        Sala a = new Sala(1, "A", "", TipoSala.INICIAL, 0, 0);
        Sala b = new Sala(2, "B", "", TipoSala.COMUM, 0, 0);
        Sala c = new Sala(3, "C", "", TipoSala.SAIDA, 0, 0);
        g.adicionarAresta(a, b);
        g.adicionarAresta(b, c);
        ResultadoBFS resultado = g.buscarMenorCaminho(a, c);
        afirmar(resultado.isSucesso(), "A BFS deve encontrar a rota.");
        afirmar(!resultado.getPassos().isEmpty(), "A execução deve registrar etapas.");
        afirmar(resultado.getPassos().get(resultado.getPassos().size() - 1).isConcluido(),
                "A última etapa deve indicar conclusão.");
        afirmar(resultado.getPassos().get(resultado.getPassos().size() - 1).getCaminhoFinal().equals(resultado.getCaminho()),
                "A demonstração e a rota final devem coincidir.");
    }

    private static void testarMovimentoInvalido() {
        Jogo jogo = new Jogo();
        Sala inicial = jogo.getSalaAtual();
        afirmar(!jogo.moverPara(jogo.getMapaSalas().get(13)), "Não deve pular salas desconectadas.");
        afirmar(jogo.getSalaAtual() == inicial, "Posição deve permanecer intacta.");
        afirmar(jogo.getQuantidadeMovimentos() == 0 && jogo.getPontuacao() == 0,
                "Pontuação e movimentos não podem mudar.");
    }

    private static void testarChaveEReinicio() {
        Jogo jogo = new Jogo();
        jogo.moverPara(jogo.getMapaSalas().get(2));
        jogo.moverPara(jogo.getMapaSalas().get(7));
        afirmar(!jogo.moverPara(jogo.getMapaSalas().get(10)), "O portão deve exigir a chave.");
        jogo.moverPara(jogo.getMapaSalas().get(8));
        afirmar(jogo.getInventario().contains("Chave Mística"), "A biblioteca deve conceder a chave.");
        jogo.moverPara(jogo.getMapaSalas().get(7));
        afirmar(jogo.moverPara(jogo.getMapaSalas().get(10)), "A chave deve abrir o portão.");
        jogo.reiniciarPartida();
        afirmar(jogo.getMapaSalas().get(10).isBloqueada(), "O portão deve voltar a ficar trancado.");
        afirmar(jogo.getInventario().isEmpty(), "Inventário deve ser zerado.");
        afirmar(jogo.getMapaSalas().get(9).temTesouro(), "Tesouros devem ser restaurados.");
    }

    private static void testarCondicaoVitoria() {
        Jogo jogo = new Jogo();
        completarCampanha(jogo);
        afirmar(jogo.isPartidaConcluida(), "As oito fases e o chefe devem ser concluídos.");
        afirmar(jogo.getFaseAtual() == 8 && jogo.getResumoAventura() != null, "Resumo só aparece ao vencer a campanha de oito fases.");
    }

    private static void testarIdentidadeDosVertices() {
        Jogo jogo = new Jogo();
        Sala duplicada = new Sala(2, "Sala falsa", "", TipoSala.COMUM, 900, 900);
        afirmar(!jogo.getGrafo().contemVertice(duplicada), "Mesmo ID não torna outra instância um vértice registrado.");
        afirmar(!jogo.moverPara(duplicada), "Movimento deve rejeitar vértice externo.");
        afirmar(jogo.getQuantidadeMovimentos() == 0, "Movimentos não podem mudar.");
    }

    private static void testarDijkstraPonderado() {
        Grafo g = new Grafo();
        Sala a = new Sala(1, "A", "", TipoSala.INICIAL, 0, 0);
        Sala b = new Sala(2, "B", "", TipoSala.COMUM, 0, 0);
        Sala c = new Sala(3, "C", "", TipoSala.COMUM, 0, 0);
        Sala d = new Sala(4, "D", "", TipoSala.SAIDA, 0, 0);
        g.adicionarAresta(a, d, 9);
        g.adicionarAresta(a, b, 1);
        g.adicionarAresta(b, c, 1);
        g.adicionarAresta(c, d, 1);
        afirmar(g.buscarMenorCaminho(a, d).getQuantidadeArestas() == 1,
                "BFS deve minimizar arestas.");
        afirmar(g.buscarMenorCusto(a, d, java.util.Collections.emptySet()).getCustoTotal() == 3,
                "Dijkstra deve minimizar o custo total.");
    }

    private static void testarEventoSurpresa() {
        Jogo jogo = new Jogo();
        int evento = jogo.getMapaSalas().values().stream().filter(jogo::isEventoSurpresaDisponivel)
                .mapToInt(Sala::getId).findFirst().orElse(-1);
        afirmar(evento == 4 || evento == 7 || evento == 11, "Deve existir um evento em sala elegível.");
        int[] ateEvento = evento == 4 ? new int[]{2, 4}
                : evento == 7 ? new int[]{2, 7} : new int[]{2, 7, 9, 11};
        for (int i = 0; i < ateEvento.length - 1; i++) jogo.moverPara(jogo.getMapaSalas().get(ateEvento[i]));
        int pontuacaoAntes = jogo.getPontuacao();
        afirmar(jogo.isEventoSurpresaDisponivel(jogo.getMapaSalas().get(evento)), "Evento deve estar disponível antes de entrar.");
        jogo.moverPara(jogo.getMapaSalas().get(evento));
        afirmar(jogo.getPontuacao() == pontuacaoAntes + 50, "Evento deve conceder 50 pontos.");
        afirmar(jogo.isEventoSurpresaColetado(), "Estado do evento deve ser marcado como coletado.");
    }

    private static void testarNosInvalidos() {
        Grafo vazio = new Grafo();
        Sala a = new Sala(1, "A", "", TipoSala.INICIAL, 0, 0);
        Sala b = new Sala(2, "B", "", TipoSala.SAIDA, 0, 0);
        afirmar(!vazio.buscarMenorCaminho(a, b).isSucesso(), "Grafo vazio não deve aceitar vértices externos.");
        vazio.adicionarVertice(a);
        afirmar(!vazio.buscarMenorCaminho(null, a).isSucesso(), "Origem nula deve ser rejeitada.");
        afirmar(!vazio.buscarMenorCaminho(a, null).isSucesso(), "Destino nulo deve ser rejeitado.");
    }

    private static void testarArestaDuplicada() {
        Grafo g = new Grafo();
        Sala a = new Sala(1, "A", "", TipoSala.INICIAL, 0, 0);
        Sala b = new Sala(2, "B", "", TipoSala.SAIDA, 0, 0);
        g.adicionarAresta(a, b);
        g.adicionarAresta(a, b);
        afirmar(g.obterQuantidadeArestas() == 1, "A aresta não pode ser duplicada.");
        afirmar(g.obterVizinhos(a).size() == 1, "A lista de vizinhos deve conter B uma única vez.");
    }

    private static void testarBloqueioPermanente() {
        Grafo g = new Grafo();
        Sala origem = new Sala(1, "Origem", "", TipoSala.INICIAL, 0, 0);
        Sala bloqueada = new Sala(2, "Rochas", "", TipoSala.BLOQUEADA, 0, 0,
                0, true, true, "Desmoronamento", "Chave Mística", null);
        Sala desvio = new Sala(3, "Desvio", "", TipoSala.COMUM, 0, 0);
        Sala destino = new Sala(4, "Saída", "", TipoSala.SAIDA, 0, 0);
        g.adicionarAresta(origem, bloqueada);
        g.adicionarAresta(bloqueada, destino);
        g.adicionarAresta(origem, desvio);
        g.adicionarAresta(desvio, destino);
        ResultadoBFS resultado = g.buscarMenorCaminho(origem, destino,
                java.util.Set.of("Chave Mística"));
        afirmar(resultado.isSucesso(), "A busca deve encontrar o desvio transitável.");
        afirmar(!resultado.getCaminho().contains(bloqueada), "Chave não pode abrir bloqueio permanente.");
    }

    private static void testarMissoesOpcionais() {
        Jogo jogo = new Jogo();
        moverComCombate(jogo, 2); moverComCombate(jogo, 3); moverComCombate(jogo, 5);
        afirmar(jogo.getPontuacao() >= 175, "Tesouro, guardião e Santuário concedem recompensas.");
        jogo.moverPara(jogo.getMapaSalas().get(8));
        jogo.moverPara(jogo.getMapaSalas().get(7));
        int antes = jogo.getPontuacao();
        jogo.moverPara(jogo.getMapaSalas().get(10));
        afirmar(!jogo.getMapaSalas().get(10).isBloqueada(), "A chave precisa abrir o portão.");
        afirmar(jogo.getPontuacao() == antes + 60, "A missão do portão paga 60 pontos.");
        jogo.moverPara(jogo.getMapaSalas().get(7));
        jogo.moverPara(jogo.getMapaSalas().get(10));
        afirmar(jogo.getPontuacao() == antes + 60, "Reabrir uma passagem já destrancada não paga novamente.");
    }

    private static void testarMissaoTesouros() {
        Jogo jogo = new Jogo();
        int[] rota = {2, 3, 2, 7, 9, 12};
        for (int id : rota) moverComCombate(jogo, id);
        afirmar(jogo.getTesourosEncontrados() == 3, "Os três baús devem estar coletados.");
        afirmar(jogo.getMissoes().stream().anyMatch(m -> m.getTitulo().equals("Abrir os três baús") && m.isConcluida()),
                "Missão opcional precisa estar concluída.");
        afirmar(jogo.getPontuacao() >= 700,
                "A recompensa dos baús, guardião e evento secreto deve ser aplicada.");
    }

    private static void testarResumoAventura() {
        Jogo jogo = new Jogo();
        completarCampanha(jogo);
        var resumo = jogo.getResumoAventura();
        afirmar(resumo != null, "A vitória deve gerar o resumo da aventura.");
        afirmar(resumo.getMovimentos() == jogo.getQuantidadeMovimentos() && resumo.getSalasVisitadas() >= 15,
                "O resumo deve somar a exploração das oito fases.");
        afirmar(resumo.getTempoSegundos() == jogo.getTempoDecorridoSegundos(), "O tempo final precisa ficar congelado.");
        afirmar(resumo.getTotalSalas() == 188, "O resumo deve somar os mapas crescentes das oito fases.");
    }

    private static void testarMovimentoDirecional() {
        Jogo jogo = new Jogo();
        afirmar(!jogo.moverParaDirecao(0, -1) && jogo.getQuantidadeMovimentos() == 0,
                "Direção sem corredor não altera a partida.");
        afirmar(jogo.moverParaDirecao(1, 0) && jogo.getSalaAtual().getId() == 2,
                "Direita deve selecionar o vizinho conectado mais alinhado.");
        afirmar(jogo.moverParaDirecao(0, -1) && jogo.getSalaAtual().getId() == 3,
                "Cima deve selecionar o vizinho superior conectado.");
    }

    private static void testarSaidaSemChave() {
        Jogo jogo = new Jogo();
        completarCampanha(jogo);
        afirmar(!jogo.getInventario().contains("Chave Mística"), "A rota alternativa não deve exigir a chave.");
        afirmar(jogo.isPartidaConcluida(), "A saída deve continuar acessível sem abrir o portão.");
    }

    private static void testarClassificacaoMestre() {
        Jogo jogo = new Jogo();
        int[] rota = {2, 3, 5};
        for (int id : rota) moverComCombate(jogo, id);
        completarCampanha(jogo);
        var resumo = jogo.getResumoAventura();
        afirmar(resumo.getSalasVisitadas() >= 15, "A campanha deve acumular salas visitadas entre fases.");
        afirmar("Aventureiro".equals(resumo.getClassificacao()), "A rota padrão sem busca por tesouros recebe Aventureiro.");
    }

    private static void moverComCombate(Jogo jogo, int id) {
        afirmar(jogo.moverPara(jogo.getMapaSalas().get(id)), "Movimento para sala " + id);
        while (jogo.isEmCombate()) {
            if (jogo.getVida() < 45 && jogo.getPocoes() > 0) jogo.usarPocao();
            else if (jogo.getBombas() > 0 && jogo.getInimigoAtual().getVida() > 28) jogo.usarBomba();
            else jogo.atacar();
        }
    }

    private static void completarCampanha(Jogo jogo) {
        int protecao = 0;
        while (!jogo.isPartidaConcluida()) {
            if (++protecao > 300) throw new AssertionError("Campanha não termina; fase=" + jogo.getFaseAtual()
                    + ", sala=" + jogo.getSalaAtual().getId() + ", combate=" + jogo.isEmCombate());
            int faseDaRota = jogo.getFaseAtual();
            var rota = jogo.getGrafo().buscarMenorCaminho(jogo.getSalaAtual(), jogo.getSalaSaida(), jogo.getInventario());
            afirmar(rota.isSucesso(), "A fase precisa ter uma rota até o portal.");
            List<Sala> caminho = rota.getCaminho();
            for (int i = 1; i < caminho.size() && !jogo.isPartidaConcluida(); i++) {
                moverComCombate(jogo, caminho.get(i).getId());
                if (jogo.getFaseAtual() != faseDaRota) break;
            }
        }
    }

    private static void testarCampanhaCompleta() {
        Jogo jogo = new Jogo();
        moverComCombate(jogo, 2); moverComCombate(jogo, 7); moverComCombate(jogo, 9);
        moverComCombate(jogo, 12); moverComCombate(jogo, 13);
        afirmar(jogo.getFaseAtual() == 2 && !jogo.isPartidaConcluida(), "O primeiro portal deve iniciar a fase 2.");
        afirmar(jogo.getPontuacao() >= 0 && jogo.getVida() > 0, "A progressão preserva a aventura sem derrota permanente.");
    }

    private static void testarCombateEConsumiveis() {
        Jogo jogo = new Jogo();
        jogo.moverPara(jogo.getMapaSalas().get(2));
        jogo.moverPara(jogo.getMapaSalas().get(3));
        afirmar(jogo.isEmCombate(), "O guardião inicia combate ao entrar na sala.");
        afirmar(!jogo.moverPara(jogo.getMapaSalas().get(2)), "O jogador não abandona o combate sem resolver o encontro.");
        int bombas = jogo.getBombas();
        jogo.usarBomba();
        afirmar(jogo.getBombas() == bombas - 1 && !jogo.isEmCombate()
                        && jogo.getMapaSalas().get(3).isTesouroColetado(),
                "A bomba causa dano suficiente para vencer o guardião e liberar o baú.");
    }

    private static void testarArmadilha() {
        Jogo jogo = new Jogo();
        jogo.moverPara(jogo.getMapaSalas().get(2));
        int vida = jogo.getVida();
        jogo.moverPara(jogo.getMapaSalas().get(4));
        afirmar(jogo.getVida() < vida && !jogo.isArmadilhaDisponivel(jogo.getMapaSalas().get(4)),
                "A armadilha reduz a vida e fica desativada após o acionamento.");
    }
}

package br.com.graphquest.grafo;

import br.com.graphquest.modelo.Sala;
import br.com.graphquest.modelo.TipoSala;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes Unitários com JUnit 5 para a estrutura de dados Grafo e algoritmo BFS.
 */
class GrafoTest {

    private Grafo grafo;
    private Sala s1;
    private Sala s2;
    private Sala s3;
    private Sala s4;

    @BeforeEach
    void setup() {
        grafo = new Grafo();
        s1 = new Sala(1, "Sala 1", "", TipoSala.INICIAL, 0, 0);
        s2 = new Sala(2, "Sala 2", "", TipoSala.COMUM, 0, 0);
        s3 = new Sala(3, "Sala 3", "", TipoSala.COMUM, 0, 0);
        s4 = new Sala(4, "Sala 4", "", TipoSala.SAIDA, 0, 0);
    }

    @Test
    @DisplayName("Deve adicionar vértices e arestas bidirecionais corretamente")
    void testAdicionarArestaNaoDirecionada() {
        grafo.adicionarAresta(s1, s2);

        assertTrue(grafo.saoVizinhos(s1, s2));
        assertTrue(grafo.saoVizinhos(s2, s1));
        assertEquals(2, grafo.obterQuantidadeVertices());
        assertEquals(1, grafo.obterQuantidadeArestas());
        assertTrue(grafo.obterVizinhos(s1).contains(s2));
        assertTrue(grafo.obterVizinhos(s2).contains(s1));
    }

    @Test
    @DisplayName("BFS deve encontrar o caminho de menor número de arestas entre nós")
    void testBFSMenorCaminho() {
        // Caminho 1 (longo): s1 -> s2 -> s3 -> s4 (3 arestas)
        grafo.adicionarAresta(s1, s2);
        grafo.adicionarAresta(s2, s3);
        grafo.adicionarAresta(s3, s4);

        // Caminho 2 (curto): s1 -> s4 (1 aresta)
        grafo.adicionarAresta(s1, s4);

        ResultadoBFS resultado = grafo.buscarMenorCaminho(s1, s4);

        assertTrue(resultado.isSucesso());
        assertEquals(1, resultado.getQuantidadeArestas());
        List<Sala> caminho = resultado.getCaminho();
        assertEquals(2, caminho.size());
        assertEquals(s1, caminho.get(0));
        assertEquals(s4, caminho.get(1));
    }

    @Test
    @DisplayName("BFS não deve transitar por salas bloqueadas (obstáculos)")
    void testBFSIgnoraSalasBloqueadas() {
        Sala bloqueada = new Sala(99, "Rochas", "", TipoSala.BLOQUEADA, 0, 0,
                0, true, true, "Desmoronamento", null, null);

        // Caminho curto porém bloqueado
        grafo.adicionarAresta(s1, bloqueada);
        grafo.adicionarAresta(bloqueada, s4);

        // Caminho alternativo livre
        grafo.adicionarAresta(s1, s2);
        grafo.adicionarAresta(s2, s4);

        ResultadoBFS resultado = grafo.buscarMenorCaminho(s1, s4);

        assertTrue(resultado.isSucesso());
        assertFalse(resultado.getCaminho().contains(bloqueada));
        assertEquals(2, resultado.getQuantidadeArestas());
    }

    @Test
    @DisplayName("BFS deve retornar insucesso se destino for inalcançável")
    void testBFSDestinoInalcancavel() {
        Sala isolada = new Sala(50, "Isolada", "", TipoSala.SAIDA, 0, 0);
        grafo.adicionarAresta(s1, s2);
        grafo.adicionarVertice(isolada);

        ResultadoBFS resultado = grafo.buscarMenorCaminho(s1, isolada);

        assertFalse(resultado.isSucesso());
        assertTrue(resultado.getCaminho().isEmpty());
    }

    @Test
    @DisplayName("BFS quando origem e destino são iguais deve ter custo zero")
    void testBFSOrigemIgualDestino() {
        grafo.adicionarVertice(s1);

        ResultadoBFS resultado = grafo.buscarMenorCaminho(s1, s1);

        assertTrue(resultado.isSucesso());
        assertEquals(0, resultado.getQuantidadeArestas());
        assertEquals(1, resultado.getCaminho().size());
    }

    @Test
    @DisplayName("BFS rejeita salas externas mesmo quando o identificador coincide")
    void rejeitaVerticeExternoComMesmoId() {
        grafo.adicionarAresta(s1, s2);
        Sala salaExterna = new Sala(1, "Outra sala 1", "", TipoSala.INICIAL, 500, 500);

        ResultadoBFS resultado = grafo.buscarMenorCaminho(salaExterna, s2);

        assertFalse(resultado.isSucesso());
        assertFalse(grafo.contemVertice(salaExterna));
        assertThrows(IllegalArgumentException.class, () -> grafo.adicionarVertice(salaExterna));
    }

    @Test
    @DisplayName("BFS registra estados reais da fila e reconstrói a rota ao final")
    void registraPassosConsistentesDaBusca() {
        grafo.adicionarAresta(s1, s2);
        grafo.adicionarAresta(s1, s3);
        grafo.adicionarAresta(s2, s4);
        grafo.adicionarAresta(s3, s4);

        ResultadoBFS resultado = grafo.buscarMenorCaminho(s1, s4);

        assertTrue(resultado.isSucesso());
        assertFalse(resultado.getPassos().isEmpty());
        assertEquals(s1, resultado.criarEstadoPasso(0).getFila().get(0));
        assertTrue(java.util.stream.IntStream.range(0, resultado.getPassos().size())
                .mapToObj(resultado::criarEstadoPasso).anyMatch(p -> p.getFila().contains(s3)));
        var ultimoPasso = resultado.getPassos().get(resultado.getPassos().size() - 1);
        assertTrue(ultimoPasso.isConcluido());
        assertEquals(resultado.getCaminho(), ultimoPasso.getCaminhoFinal());
    }

    @Test
    @DisplayName("Dijkstra usa os pesos enquanto BFS minimiza o número de arestas")
    void comparaObjetivosDasRotas() {
        grafo.adicionarAresta(s1, s4, 9);
        grafo.adicionarAresta(s1, s2, 1);
        grafo.adicionarAresta(s2, s3, 1);
        grafo.adicionarAresta(s3, s4, 1);

        assertEquals(1, grafo.buscarMenorCaminho(s1, s4).getQuantidadeArestas());
        assertEquals(3, grafo.buscarMenorCusto(s1, s4, java.util.Collections.emptySet()).getCustoTotal());
    }

    @Test
    @DisplayName("Adição de arestas rejeita pesos nulos ou negativos")
    void rejeitaPesoNaoPositivo() {
        assertThrows(IllegalArgumentException.class, () -> grafo.adicionarAresta(s1, s2, 0));
        assertThrows(IllegalArgumentException.class, () -> grafo.adicionarAresta(s1, s2, -1));
    }

    @Test
    @DisplayName("BFS trata origem nula, destino nulo e grafo vazio")
    void rejeitaExtremosInvalidos() {
        assertFalse(new Grafo().buscarMenorCaminho(s1, s4).isSucesso());
        grafo.adicionarVertice(s1);
        assertFalse(grafo.buscarMenorCaminho(null, s1).isSucesso());
        assertFalse(grafo.buscarMenorCaminho(s1, null).isSucesso());
    }

    @Test
    @DisplayName("A mesma aresta não duplica vizinhos")
    void naoDuplicaAresta() {
        grafo.adicionarAresta(s1, s2);
        grafo.adicionarAresta(s1, s2);
        assertEquals(1, grafo.obterQuantidadeArestas());
        assertEquals(1, grafo.obterVizinhos(s1).size());
    }

    @Test
    @DisplayName("BFS não atravessa obstáculo permanente mesmo com a chave")
    void bloqueioPermanenteIgnoraChave() {
        Sala desmoronamento = new Sala(99, "Desmoronamento", "", TipoSala.BLOQUEADA, 0, 0,
                0, true, true, "Rochas", "Chave Mística", null);
        grafo.adicionarAresta(s1, desmoronamento);
        grafo.adicionarAresta(desmoronamento, s4);
        grafo.adicionarAresta(s1, s2);
        grafo.adicionarAresta(s2, s4);

        ResultadoBFS resultado = grafo.buscarMenorCaminho(s1, s4, java.util.Set.of("Chave Mística"));

        assertTrue(resultado.isSucesso());
        assertFalse(resultado.getCaminho().contains(desmoronamento));
    }
}

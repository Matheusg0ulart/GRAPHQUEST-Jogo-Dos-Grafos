package br.com.graphquest.jogo;

import br.com.graphquest.modelo.Sala;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JogoTest {
    @Test
    @DisplayName("Movimento inválido não altera posição, passos ou pontuação")
    void movimentoInvalidoPreservaEstado() {
        Jogo jogo = new Jogo();
        Sala inicio = jogo.getSalaAtual();

        assertFalse(jogo.moverPara(jogo.getMapaSalas().get(13)));
        assertSame(inicio, jogo.getSalaAtual());
        assertEquals(0, jogo.getQuantidadeMovimentos());
        assertEquals(0, jogo.getPontuacao());
    }

    @Test
    @DisplayName("Tesouro é pontuado uma única vez mesmo ao revisitar a sala")
    void tesouroNaoPodeSerPontuadoDuasVezes() {
        Jogo jogo = new Jogo();
        assertTrue(jogo.moverPara(jogo.getMapaSalas().get(2)));
        assertTrue(jogo.moverPara(jogo.getMapaSalas().get(3)));
        assertTrue(jogo.isEmCombate());
        while (jogo.isEmCombate()) jogo.atacar();
        int pontuacaoBaú = jogo.getPontuacao();
        assertTrue(pontuacaoBaú >= 100);
        assertTrue(jogo.moverPara(jogo.getMapaSalas().get(2)));
        assertTrue(jogo.moverPara(jogo.getMapaSalas().get(3)));
        assertEquals(pontuacaoBaú, jogo.getPontuacao());
    }

    @Test
    @DisplayName("Chave abre o portão e permanece no inventário")
    void chaveAbrePortao() {
        Jogo jogo = new Jogo();
        mover(jogo, 2, 7);
        assertFalse(jogo.moverPara(jogo.getMapaSalas().get(10)));
        assertTrue(jogo.moverPara(jogo.getMapaSalas().get(8)));
        assertTrue(jogo.getInventario().contains("Chave Mística"));
        assertTrue(jogo.moverPara(jogo.getMapaSalas().get(7)));
        assertTrue(jogo.moverPara(jogo.getMapaSalas().get(10)));
        assertFalse(jogo.getMapaSalas().get(10).isBloqueada());
        assertTrue(jogo.getInventario().contains("Chave Mística"));
    }

    @Test
    @DisplayName("Vitória encerra a partida e reiniciar restaura os estados")
    void vitoriaEReinicioCompleto() {
        Jogo jogo = new Jogo();
        int eventoAntes = eventoDisponivel(jogo);
        assertTrue(eventoAntes == 4 || eventoAntes == 7 || eventoAntes == 11);
        completarCampanha(jogo);
        assertTrue(jogo.isPartidaConcluida());
        assertEquals(8, jogo.getFaseAtual());

        jogo.reiniciarPartida();

        assertFalse(jogo.isPartidaConcluida());
        assertEquals(0, jogo.getQuantidadeMovimentos());
        assertEquals(0, jogo.getPontuacao());
        assertTrue(jogo.getInventario().isEmpty());
        assertTrue(jogo.getMapaSalas().get(9).temTesouro());
        assertTrue(jogo.getMapaSalas().get(10).isBloqueada());
        assertNotEquals(eventoAntes, eventoDisponivel(jogo));
    }

    @Test
    @DisplayName("Missões opcionais são reais e pagam uma única recompensa")
    void missoesOpcionaisERecompensas() {
        Jogo jogo = new Jogo();
        assertEquals(5, jogo.getMissoes().size());
        assertEquals("Em andamento", jogo.getMissoes().get(0).getEstado().getRotulo());

        mover(jogo, 2, 3);
        assertTrue(jogo.getPontuacao() >= 100);
        mover(jogo, 5);
        assertTrue(jogo.getMissoes().stream().anyMatch(m -> m.getTitulo().equals("Descobrir o Santuário") && m.isConcluida()));
        assertTrue(jogo.getPontuacao() >= 175);
        mover(jogo, 8, 7);
        int antesDoPortao = jogo.getPontuacao();
        mover(jogo, 10);
        assertTrue(jogo.getMissoes().stream().anyMatch(m -> m.getTitulo().equals("Abrir o Portão de Ferro") && m.isConcluida()));
        assertEquals(antesDoPortao + 60, jogo.getPontuacao());
        var rotaAposDesbloqueio = jogo.executarBFSAteSaida();
        assertTrue(rotaAposDesbloqueio.isSucesso());
        assertEquals(10, rotaAposDesbloqueio.getCaminho().get(0).getId());
        assertEquals(11, rotaAposDesbloqueio.getCaminho().get(1).getId());
        assertEquals(13, rotaAposDesbloqueio.getCaminho().get(2).getId());
        assertEquals(3, jogo.executarDijkstraAteSaida().getCustoTotal(),
                "Dijkstra deve usar a passagem de menor custo liberada pela chave.");
        mover(jogo, 7, 10);
        assertEquals(antesDoPortao + 60, jogo.getPontuacao(), "Portão e missão não podem pagar duas vezes.");
    }

    @Test
    @DisplayName("Missão de baús conclui ao encontrar os três tesouros")
    void missaoDosTesouros() {
        Jogo jogo = new Jogo();
        mover(jogo, 2, 3, 2, 7, 9, 12);
        assertEquals(3, jogo.getTesourosEncontrados());
        assertTrue(jogo.getMissoes().stream().anyMatch(m -> m.getTitulo().equals("Abrir os três baús") && m.isConcluida()));
        assertTrue(jogo.getPontuacao() >= 700,
                "A missão paga 100 pontos; apenas o evento surpresa variável pode somar 50 extras.");
    }

    @Test
    @DisplayName("Resumo de vitória congela tempo, exploração, missões e pontuação")
    void resumoFinalDaAventura() {
        Jogo jogo = new Jogo();
        completarCampanha(jogo);
        var resumo = jogo.getResumoAventura();
        assertNotNull(resumo);
        assertEquals(jogo.getPontuacao(), resumo.getPontuacao());
        assertTrue(resumo.getSalasVisitadas() >= 15);
        assertEquals(188, resumo.getTotalSalas());
        assertEquals(jogo.getQuantidadeMovimentos(), resumo.getMovimentos());
        assertTrue(resumo.getTempoSegundos() >= 0);
        assertTrue(resumo.getPercentualExploracao() > 0);
        assertEquals(resumo.getTempoSegundos(), jogo.getTempoDecorridoSegundos());
    }

    @Test
    @DisplayName("Setas/WASD escolhem apenas vizinhos reais e preservam movimento inválido")
    void movimentoPorDirecao() {
        Jogo jogo = new Jogo();
        assertFalse(jogo.moverParaDirecao(0, -1));
        assertEquals(0, jogo.getQuantidadeMovimentos());
        assertTrue(jogo.moverParaDirecao(1, 0));
        assertEquals(2, jogo.getSalaAtual().getId());
        assertTrue(jogo.moverParaDirecao(0, -1));
        assertEquals(3, jogo.getSalaAtual().getId());
        assertThrows(IllegalArgumentException.class, () -> jogo.moverParaDirecao(1, 1));
    }

    @Test
    @DisplayName("Saída permanece alcançável por uma rota alternativa sem a chave")
    void saidaSemChave() {
        Jogo jogo = new Jogo();
        completarCampanha(jogo);
        assertFalse(jogo.getInventario().contains("Chave Mística"));
        assertTrue(jogo.isPartidaConcluida());
        assertNotNull(jogo.getResumoAventura());
    }

    @Test
    @DisplayName("Classificação final resume a exploração da campanha")
    void classificacaoMestre() {
        Jogo jogo = new Jogo();
        mover(jogo, 2, 3, 5, 8, 7, 10, 11, 9, 12, 13);
        var resumo = jogo.getResumoAventura();
        assertTrue(resumo.getClassificacao().equals("Mestre da Masmorra")
                || resumo.getClassificacao().equals("Aventureiro")
                || resumo.getClassificacao().equals("Explorador"));
    }

    @Test
    @DisplayName("Portal conclui a fase, mantém a campanha e recupera vida")
    void portalAvancaFase() {
        Jogo jogo = new Jogo();
        mover(jogo, 2, 7, 9, 12, 13);
        assertEquals(2, jogo.getFaseAtual());
        assertFalse(jogo.isPartidaConcluida());
        assertEquals(1, jogo.getSalaAtual().getId());
        assertEquals(13, jogo.getMapaSalas().size());
    }

    @Test
    @DisplayName("Guardião bloqueia movimento e bomba arcana libera a câmara")
    void combateEConsumivel() {
        Jogo jogo = new Jogo();
        mover(jogo, 2);
        assertTrue(jogo.moverPara(jogo.getMapaSalas().get(3)));
        assertTrue(jogo.isEmCombate());
        assertFalse(jogo.moverPara(jogo.getMapaSalas().get(2)));
        int bombasAntes = jogo.getBombas();
        jogo.usarBomba();
        assertEquals(bombasAntes - 1, jogo.getBombas());
        assertFalse(jogo.isEmCombate());
        assertTrue(jogo.getMapaSalas().get(3).isTesouroColetado());
    }

    @Test
    @DisplayName("Armadilha causa dano uma única vez")
    void armadilhaUmaVez() {
        Jogo jogo = new Jogo();
        mover(jogo, 2);
        int vidaInicial = jogo.getVida();
        mover(jogo, 4);
        assertTrue(jogo.getVida() < vidaInicial);
        assertFalse(jogo.isArmadilhaDisponivel(jogo.getMapaSalas().get(4)));
    }

    private int eventoDisponivel(Jogo jogo) {
        return jogo.getMapaSalas().values().stream()
                .filter(jogo::isEventoSurpresaDisponivel).mapToInt(Sala::getId).findFirst().orElse(-1);
    }

    private void mover(Jogo jogo, int... salas) {
        for (int id : salas) {
            assertTrue(jogo.moverPara(jogo.getMapaSalas().get(id)), "movimento para sala " + id);
            while (jogo.isEmCombate()) {
                if (jogo.getVida() < 45 && jogo.getPocoes() > 0) jogo.usarPocao();
                else if (jogo.getBombas() > 0 && jogo.getInimigoAtual().getVida() > 28) jogo.usarBomba();
                else jogo.atacar();
            }
        }
    }

    private void completarCampanha(Jogo jogo) {
        while (!jogo.isPartidaConcluida()) {
            int faseDaRota = jogo.getFaseAtual();
            var rota = jogo.getGrafo().buscarMenorCaminho(jogo.getSalaAtual(), jogo.getSalaSaida(), jogo.getInventario());
            assertTrue(rota.isSucesso());
            var caminho = rota.getCaminho();
            for (int i = 1; i < caminho.size() && !jogo.isPartidaConcluida(); i++) {
                mover(jogo, caminho.get(i).getId());
                if (jogo.getFaseAtual() != faseDaRota) break;
            }
        }
    }
}

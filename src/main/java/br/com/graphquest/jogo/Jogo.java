package br.com.graphquest.jogo;

import br.com.graphquest.audio.GerenciadorAudio;
import br.com.graphquest.audio.GerenciadorAudio.EfeitoSonoro;
import br.com.graphquest.grafo.Grafo;
import br.com.graphquest.grafo.ResultadoBFS;
import br.com.graphquest.grafo.ResultadoDijkstra;
import br.com.graphquest.modelo.EventoExploracao;
import br.com.graphquest.modelo.Item;
import br.com.graphquest.modelo.Sala;
import br.com.graphquest.modelo.TipoSala;

import java.util.*;

/**
 * Controlador central do estado da partida no GraphQuest.
 * 
 * Gerencia a localização do jogador, regras de trânsito pelo Grafo,
 * coleta de tesouros e itens, acionamento do algoritmo BFS e condições de vitória.
 */
public class Jogo {
    private Grafo grafo;
    private Sala salaAtual;
    private Sala salaInicial;
    private Sala salaSaida;
    private Map<Integer, Sala> mapaSalas;

    private int pontuacao;
    private int quantidadeMovimentos;
    private final Set<String> inventario;
    private final List<String> historicoMensagens;
    private List<Missao> missoes;
    private Missao missaoPrincipal;
    private Missao missaoChave;
    private Missao missaoPortao;
    private Missao missaoSantuario;
    private Missao missaoTesouros;
    private long instanteInicioNano;
    private long tempoFinalSegundos;
    private ResumoAventura resumoAventura;

    private ResultadoBFS ultimoResultadoBFS;
    private ResultadoDijkstra ultimoResultadoDijkstra;
    private List<Sala> rotaDestacada;
    private boolean rotaDijkstra;
    private boolean partidaConcluida;
    private int versaoPartida;
    private int salaEventoSurpresaId;
    private boolean eventoSurpresaColetado;
    private String mensagemFeedback;
    private long instanteFeedback;

    private int faseAtual;
    private int vida = 100;
    private int vidaMaxima = 100;
    private int pocoes = 2;
    private int bombas = 1;
    private int pocoesExtras;
    private int bombasExtras;
    private int recuosRestantes;
    private int chefesDerrotados;
    private int segredosEncontrados;
    private int objetivosSecundariosConcluidos;
    private boolean recursosLiquidados;
    private boolean campanhaVencida;
    private String idCampanha;
    private PerfilJogador perfil;
    private PerfilJogador.ResultadoMoedas recompensaMoedas;
    private Inimigo inimigoAtual;
    private final Map<Habilidade, Integer> cooldowns = new EnumMap<>(Habilidade.class);
    private boolean emGuarda;
    private boolean esquivaAtiva;
    private int danoSofriNesteCombate;
    private int pocoesUsadasNestaFase;
    private final Set<Integer> armadilhasAcionadas = new HashSet<>();
    private final Set<Integer> salasComInimigoDerrotado = new HashSet<>();
    private final Map<Integer, Inimigo> inimigosDaFase = new HashMap<>();
    private boolean processandoChegada;
    private int salasVisitadasCampanha;
    private int totalSalasCampanha;
    private int tesourosCampanha;
    private int totalTesourosCampanha;
    private int missoesConcluidasCampanha;
    private int missoesOpcionaisCampanha;
    private int totalMissoesCampanha;
    private final int totalFases = 8;
    private final List<Item> itensInventario = new ArrayList<>();
    private Item armaEquipada;
    private Item armaduraEquipada;
    private Item acessorioEquipado;

    private final List<JogoListener> ouvintes;

    public Jogo() { this(new PerfilJogador()); }

    public Jogo(PerfilJogador perfil) {
        this.inventario = new LinkedHashSet<>();
        this.historicoMensagens = new ArrayList<>();
        this.ouvintes = new ArrayList<>();
        this.perfil = Objects.requireNonNull(perfil, "perfil");
        this.vidaMaxima = 100 + 15 * perfil.nivel(PerfilJogador.Melhoria.VITALIDADE);
        inicializarMasmorra();
    }

    /**
     * Inicializa ou restaura o mapa e os estados da masmorra.
     */
    private void inicializarMasmorra() {
        versaoPartida++;
        faseAtual = 1;
        vida = vidaMaxima;
        int[] guardados = perfil.retirarConsumiveisGuardados();
        pocoesExtras = guardados[0];
        bombasExtras = guardados[1];
        pocoes = 2 + perfil.nivel(PerfilJogador.Melhoria.PREPARACAO) + pocoesExtras;
        bombas = 1 + bombasExtras;
        recuosRestantes = 2;
        chefesDerrotados = 0;
        segredosEncontrados = 0;
        objetivosSecundariosConcluidos = 0;
        recursosLiquidados = false;
        campanhaVencida = false;
        idCampanha = UUID.randomUUID().toString();
        recompensaMoedas = null;
        inimigoAtual = null;
        armadilhasAcionadas.clear();
        salasComInimigoDerrotado.clear();
        inimigosDaFase.clear();
        salasVisitadasCampanha = 0;
        totalSalasCampanha = 0;
        tesourosCampanha = 0;
        totalTesourosCampanha = 0;
        missoesConcluidasCampanha = 0;
        missoesOpcionaisCampanha = 0;
        totalMissoesCampanha = 0;
        carregarFase(false);
    }

    private void carregarFase(boolean preservarCampanha) {
        int eventoAnterior = salaEventoSurpresaId;
        MasmorraPadrao.DadosMasmorra dados = faseAtual == 1
                ? MasmorraPadrao.criarMasmorra()
                : MasmorraPadrao.criarMasmorraProcedural(13 + (faseAtual - 1) * 3, idCampanha.hashCode() + faseAtual);
        this.grafo = dados.getGrafo();
        this.salaInicial = dados.getSalaInicial();
        this.salaSaida = dados.getSalaSaida();
        this.mapaSalas = dados.getMapaSalas();

        this.salaAtual = this.salaInicial;
        this.salaAtual.setVisitada(true);

        if (!preservarCampanha) {
            this.pontuacao = 0;
            this.quantidadeMovimentos = 0;
            this.inventario.clear();
            this.historicoMensagens.clear();
            this.instanteInicioNano = System.nanoTime();
        }
        this.ultimoResultadoBFS = null;
        this.ultimoResultadoDijkstra = null;
        this.rotaDestacada = null;
        this.rotaDijkstra = false;
        this.partidaConcluida = false;
        this.tempoFinalSegundos = -1;
        this.resumoAventura = null;
        List<Integer> elegiveis = new ArrayList<>();
        for (Sala s : mapaSalas.values()) if (s != salaInicial && s != salaSaida && !s.isBloqueada()) elegiveis.add(s.getId());
        Random random = new Random(idCampanha.hashCode() + faseAtual * 31L);
        elegiveis.remove(Integer.valueOf(eventoAnterior));
        if (faseAtual == 1) {
            int[] eventosClassicos = {4, 7, 11};
            List<Integer> candidatos = new ArrayList<>();
            for (int id : eventosClassicos) if (id != eventoAnterior) candidatos.add(id);
            this.salaEventoSurpresaId = candidatos.get(random.nextInt(candidatos.size()));
        } else this.salaEventoSurpresaId = elegiveis.get(random.nextInt(elegiveis.size()));
        this.eventoSurpresaColetado = false;
        inimigosDaFase.clear();
        List<Sala> salasCombate = new ArrayList<>(mapaSalas.values());
        salasCombate.remove(salaInicial); salasCombate.remove(salaSaida);
        salasCombate.removeIf(Sala::isBloqueada);
        Collections.shuffle(salasCombate, random);
        int quantidadeInimigos = Math.min(salasCombate.size(), faseAtual == 1 ? 1 : 1 + faseAtual / 2);
        for (int i = 0; i < quantidadeInimigos; i++) {
            Sala s = faseAtual == 1 ? mapaSalas.get(3) : salasCombate.get(i);
            inimigosDaFase.put(s.getId(), new Inimigo(faseAtual == 1 ? "Guardião de Pedra" : "Sentinela da Fase " + faseAtual,
                    faseAtual == 1 ? 48 : 42 + faseAtual * 12, faseAtual == 1 ? 10 : 8 + faseAtual * 2, false));
        }
        if (faseAtual == totalFases || faseAtual % 4 == 0) {
            inimigosDaFase.put(salaSaida.getId(), new Inimigo(faseAtual == totalFases ? "Dragão das Sombras" : "Arauto do Abismo",
                    100 + faseAtual * 12, 15 + faseAtual, true));
        }
        salasComInimigoDerrotado.clear();
        armadilhasAcionadas.clear();
        criarMissoes();
        registrarFeedback("Fase " + faseAtual + "/" + totalFases + " — " + getNomeFase());

        adicionarMensagem((preservarCampanha ? "Nova fase: " : "Bem-vindo ao GraphQuest! ")
                + getNomeFase() + ". Explore, prepare-se e encontre o portal.");
    }

    /**
     * Registra um ouvinte para receber notificações de mudanças de estado.
     */
    public void registrarOuvinte(JogoListener ouvinte) {
        if (ouvinte != null && !ouvintes.contains(ouvinte)) {
            ouvintes.add(ouvinte);
        }
    }

    public void removerOuvinte(JogoListener ouvinte) { ouvintes.remove(ouvinte); }

    private void notificarOuvintes() {
        for (JogoListener ouvinte : ouvintes) {
            ouvinte.onEstadoAtualizado();
        }
    }

    /**
     * Tenta movimentar o jogador para a sala indicada.
     * 
     * Regras aplicadas:
     * 1. A partida não pode estar concluída.
     * 2. Deve existir aresta direta no Grafo entre a sala atual e a sala desejada.
     * 3. Salas bloqueadas impedem a entrada, a menos que o jogador possua a chave necessária.
     * 
     * @param destino Sala para a qual o jogador deseja avançar.
     * @return true se o movimento foi bem-sucedido, false caso contrário.
     */
    public boolean moverPara(Sala destino) {
        if (destino == null) {
            adicionarMensagem("Destino inválido.");
            registrarFeedback("Destino inválido.");
            notificarOuvintes();
            return false;
        }

        if (partidaConcluida) {
            adicionarMensagem("A aventura já foi concluída! Reinicie a partida para jogar novamente.");
            registrarFeedback("A partida terminou. Reinicie para jogar de novo.");
            notificarOuvintes();
            return false;
        }
        if (inimigoAtual != null) {
            registrarFeedback("Derrote o inimigo antes de continuar.");
            notificarOuvintes();
            return false;
        }

        if (!grafo.contemVertice(destino)) {
            adicionarMensagem("A sala selecionada não pertence a esta masmorra.");
            registrarFeedback("Esta sala não faz parte do mapa.");
            notificarOuvintes();
            return false;
        }

        // Validação 1: Existência de aresta na lista de adjacência
        if (!grafo.saoVizinhos(salaAtual, destino)) {
            adicionarMensagem("Movimento inválido! Não há corredor conectando '" +
                    salaAtual.getNome() + "' a '" + destino.getNome() + "'.");
            registrarFeedback("Esse corredor não está conectado à sala atual.");
            notificarOuvintes();
            return false;
        }

        boolean desbloqueouPortao = false;
        // Validação 2: Verificação de bloqueios e uso de itens do inventário
        if (destino.isBloqueada()) {
            if (!destino.isBloqueioPermanente() && destino.getItemNecessario() != null
                    && inventario.contains(destino.getItemNecessario()) && destino.desbloquear()) {
                // Desbloqueia o obstáculo usando o item necessário
                destino.setTipo(TipoSala.COMUM);
                desbloqueouPortao = true;
                adicionarMensagem("Você usou a [" + destino.getItemNecessario() + "] e destrancou o " + destino.getNome() + "!");
            } else {
                adicionarMensagem("Passagem Bloqueada! Motivo: " + destino.getMotivoBloqueio() + ".");
                registrarFeedback("Passagem bloqueada: " + destino.getMotivoBloqueio() + ".");
                notificarOuvintes();
                return false;
            }
        }

        // Movimentação realizada com sucesso
        this.salaAtual = destino;
        this.salaAtual.setVisitada(true);
        this.quantidadeMovimentos++;
        perfil.getConquistas().incrementarEstatistica("passos_dados", 1);

        adicionarMensagem("Você entrou em: " + destino.getNome() + ".");

        if (desbloqueouPortao) registrarFeedback("Portão aberto com a Chave Mística!");
        processarChegada(destino);

        // Se havia uma rota destacada, podemos limpá-la ou mantê-la atualizada
        this.rotaDestacada = null;
        this.ultimoResultadoBFS = null;
        this.ultimoResultadoDijkstra = null;
        this.rotaDijkstra = false;

        notificarOuvintes();
        return true;
    }

    private void processarChegada(Sala destino) {
        if (destino.getId() == idArmadilhaDaFase() && armadilhasAcionadas.add(destino.getId())) {
            vida = Math.max(1, vida - (faseAtual == 1 ? 12 : faseAtual == 2 ? 16 : 20));
            GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.DANO_JOGADOR);
            adicionarMensagem("ARMADILHA! Lâminas antigas causam dano. Você perdeu vida, mas escapou.");
            registrarFeedback("Armadilha: -" + (faseAtual == 1 ? 12 : faseAtual == 2 ? 16 : 20) + " de vida.");
        }
        Inimigo inimigo = salasComInimigoDerrotado.contains(destino.getId()) ? null : inimigosDaFase.get(destino.getId());
        if (inimigo != null && !inimigo.isDerrotado()) {
            inimigoAtual = inimigo;
            danoSofriNesteCombate = 0;
            emGuarda = false;
            esquivaAtiva = false;
            adicionarMensagem("Combate iniciado: " + inimigo.getNome() + " protege esta câmara!");
            registrarFeedback("Combate! Ataque, defenda, use habilidades ou consumíveis.");
            return;
        }
        coletarRecompensas(destino);
        processarEventoDaSala(destino);
        atualizarMissoes();
        if (destino.equals(salaSaida)) concluirPortal();
    }

    private void processarEventoDaSala(Sala destino) {
        if (destino.temEvento()) {
            EventoExploracao ev = destino.getEvento();
            switch (ev.getTipo()) {
                case FONTE_CURA -> {
                    int recuperada = Math.min(35, vidaMaxima - vida);
                    vida += recuperada;
                    ev.setResolvido(true);
                    GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.CURA);
                    adicionarMensagem(ev.getTitulo() + ": " + ev.getNarrativa() + " (+" + recuperada + " PV).");
                    registrarFeedback("Fonte Sagrada: +" + recuperada + " PV restaurados.");
                }
                case BAU_MISTERIOSO -> {
                    int moedasBau = 50 + faseAtual * 10;
                    pontuacao += 60;
                    perfil.adicionarMoedas(moedasBau);
                    ev.setResolvido(true);
                    GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.BAU);
                    adicionarMensagem(ev.getTitulo() + ": Você destrancou o baú e encontrou " + moedasBau + " moedas!");
                    registrarFeedback("Baú misterioso: +" + moedasBau + " moedas!");
                }
                case ALTAR_BENCAO -> {
                    pontuacao += 80;
                    perfil.adicionarMoedas(30);
                    ev.setResolvido(true);
                    GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.HABILIDADE);
                    adicionarMensagem(ev.getTitulo() + ": As runas brilham ao seu toque. Bênção dos Ancestrais concedida (+80 pts)!");
                    registrarFeedback("Bênção recebida: +80 pontos!");
                }
                case MERCADOR_ERRANTE -> {
                    adicionarMensagem(ev.getTitulo() + ": " + ev.getNarrativa());
                    registrarFeedback("Mercador Errante disponível para trocas.");
                }
                default -> {}
            }
        }
    }

    private void coletarRecompensas(Sala destino) {
        if (destino.temTesouro()) {
            int valor = destino.getValorTesouro();
            pontuacao += valor;
            destino.setTesouroColetado(true);
            if (faseAtual == 1) { pocoes++; pocoesExtras++; }
            else { bombas++; bombasExtras++; }
            perfil.adicionarMoedas(valor / 2);
            perfil.getConquistas().incrementarEstatistica("baus_coletados", 1);
            GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.BAU);
            adicionarMensagem("Baú aberto: +" + valor + " pontos e " + (faseAtual == 1 ? "uma poção" : "uma bomba arcana") + ".");
            registrarFeedback("Recompensa coletada: +" + valor + " pontos!");
        }
        if (destino.getItemConcedido() != null && inventario.add(destino.getItemConcedido())) {
            GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.COMPRA);
            adicionarMensagem("Item obtido: [" + destino.getItemConcedido() + "].");
        }
        if (destino.getId() == salaEventoSurpresaId && !eventoSurpresaColetado) {
            eventoSurpresaColetado = true;
            segredosEncontrados++;
            pontuacao += 50;
            perfil.adicionarMoedas(25);
            GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.MOEDA);
            adicionarMensagem("Cristal secreto encontrado: +50 pontos!");
            registrarFeedback("Cristal secreto: +50 pontos!");
        }
    }

    private void concluirPortal() {
        perfil.getConquistas().incrementarEstatistica("fases_concluidas", 1);
        perfil.getConquistas().desbloquear(SistemaConquistas.Conquista.PRIMEIROS_PASSOS);
        if (pocoesUsadasNestaFase == 0) {
            perfil.getConquistas().desbloquear(SistemaConquistas.Conquista.SEM_POCOES);
        }
        long visitadasFase = mapaSalas.values().stream().filter(Sala::isVisitada).count();
        long transitaveis = mapaSalas.values().stream().filter(s -> !s.isBloqueada()).count();
        if (visitadasFase >= transitaveis) {
            perfil.getConquistas().desbloquear(SistemaConquistas.Conquista.CARTOGRAFO);
        }
        if (getTesourosRestantes() == 0) {
            perfil.getConquistas().desbloquear(SistemaConquistas.Conquista.CACADOR_DE_TESOUROS);
        }

        if (faseAtual < totalFases) {
            missaoPrincipal.concluir();
            contabilizarFase();
            int faseAnterior = faseAtual;
            faseAtual++;
            vida = Math.min(vidaMaxima, vida + 30);
            pocoesUsadasNestaFase = 0;
            GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.PORTAO);
            adicionarMensagem("Portal ativado! Fase " + faseAnterior + " concluída. +30 de vida para a próxima etapa.");
            carregarFase(true);
            registrarFeedback("Fase " + faseAtual + "/" + totalFases + ": " + getNomeFase());
            return;
        }
        // Na última fase, o portal só pode ser concluído depois de derrotar o chefe.
        Inimigo chefePortal = inimigosDaFase.get(salaSaida.getId());
        if (chefePortal != null && !chefePortal.isDerrotado()
                && !salasComInimigoDerrotado.contains(salaSaida.getId())) {
            if (inimigoAtual == null) {
                inimigoAtual = chefePortal;
                danoSofriNesteCombate = 0;
                emGuarda = false;
                esquivaAtiva = false;
                adicionarMensagem(chefePortal.getNome() + " desperta e bloqueia o portal!");
            }
            registrarFeedback("Derrote " + chefePortal.getNome() + " para ativar o portal.");
            return;
        }
        if (chefePortal != null && chefePortal.isDerrotado()) salasComInimigoDerrotado.add(salaSaida.getId());
        missaoPrincipal.concluir();
        perfil.getConquistas().desbloquear(SistemaConquistas.Conquista.SENHOR_DOS_GRAFOS);
        finalizarCampanha(true);
    }

    private void finalizarCampanha(boolean venceu) {
        contabilizarFase();
        partidaConcluida = true;
        campanhaVencida = venceu;
        tempoFinalSegundos = getTempoDecorridoSegundos();
        if (!recursosLiquidados) {
            perfil.guardarConsumiveis(pocoesExtras, bombasExtras);
            recursosLiquidados = true;
        }
        int bonusEficiencia = Math.max(0, Math.min(25, 25 - Math.max(0, quantidadeMovimentos - faseAtual * 6) / 2));
        recompensaMoedas = perfil.registrarResultado(idCampanha, venceu, chefesDerrotados,
                objetivosSecundariosConcluidos, segredosEncontrados, bonusEficiencia);
        resumoAventura = criarResumoAventura();
        adicionarMensagem(venceu ? "VITÓRIA! Você venceu a campanha e derrotou o chefe final."
                : "A campanha chegou ao fim. Seu perfil e suas melhorias foram preservados.");
        adicionarMensagem("Recompensa: +" + recompensaMoedas.getTotalRecebido() + " moedas persistentes.");
        registrarFeedback(venceu ? "Campanha concluída! Recompensas salvas." : "Derrota. Recompensa de consolação salva.");
    }

    private void contabilizarFase() {
        salasVisitadasCampanha += (int) mapaSalas.values().stream().filter(Sala::isVisitada).count();
        totalSalasCampanha += mapaSalas.size();
        tesourosCampanha += getTesourosEncontrados();
        totalTesourosCampanha += getTotalTesouros();
        missoesConcluidasCampanha += (int) missoes.stream().filter(Missao::isConcluida).count();
        missoesOpcionaisCampanha += (int) missoes.stream()
                .filter(m -> !m.isPrincipal() && m.isConcluida()).count();
        objetivosSecundariosConcluidos = missoesOpcionaisCampanha;
        totalMissoesCampanha += missoes.size();
    }

    private int idArmadilhaDaFase() { return faseAtual == 1 ? 4 : Math.max(2, (mapaSalas.size() * 2) / 3); }

    /** Move para o vizinho conectado que melhor corresponde à direção pedida. */
    public boolean moverParaDirecao(int dx, int dy) {
        if (Math.abs(dx) + Math.abs(dy) != 1) {
            throw new IllegalArgumentException("A direção deve ser um dos quatro pontos cardeais.");
        }
        Sala escolhido = null;
        double melhorAlinhamento = 0.25;
        for (Sala vizinho : grafo.obterVizinhos(salaAtual)) {
            int vx = vizinho.getX() - salaAtual.getX();
            int vy = vizinho.getY() - salaAtual.getY();
            double comprimento = Math.hypot(vx, vy);
            if (comprimento == 0) continue;
            double alinhamento = (vx * dx + vy * dy) / comprimento;
            if (alinhamento > melhorAlinhamento) {
                escolhido = vizinho;
                melhorAlinhamento = alinhamento;
            }
        }
        if (escolhido == null) {
            String direcao = dx < 0 ? "esquerda" : dx > 0 ? "direita" : dy < 0 ? "cima" : "baixo";
            adicionarMensagem("Não há corredor conectado para " + direcao + ".");
            registrarFeedback("Sem corredor nessa direção.");
            notificarOuvintes();
            return false;
        }
        return moverPara(escolhido);
    }

    private void criarMissoes() {
        missaoPrincipal = new Missao("Alcançar a saída", "Encontre uma rota até o Portal da Liberdade.", true, 1, true);
        if (faseAtual > 1) {
            missaoChave = new Missao("Explorar a fase", "Visite pelo menos metade das câmaras desta região.", 45, Math.max(1, mapaSalas.size() / 2));
            missaoPortao = new Missao("Caçar guardiões", "Derrote os monstros que protegem esta região.", 55, Math.max(1, inimigosDaFase.size()));
            missaoSantuario = new Missao("Revelar os segredos", "Encontre o cristal secreto desta região.", 35, 1);
            missaoTesouros = new Missao("Explorador de relíquias", "Colete todos os baús desta região.", 60, Math.max(1, getTotalTesouros()));
            missoes = new ArrayList<>(List.of(missaoPrincipal, missaoChave, missaoPortao, missaoSantuario, missaoTesouros));
            return;
        }
        missaoChave = new Missao("Encontrar a Chave Mística", "Ela abre o Portão de Ferro e reduz o custo da rota.", 0, 1);
        missaoPortao = new Missao("Abrir o Portão de Ferro", "A chave libera um atalho pelo Abismo Esquecido.", 60, 1);
        missaoSantuario = new Missao("Descobrir o Santuário", "Explore o desvio pela Câmara dos Tesouros.", 75, 1);
        missaoTesouros = new Missao("Abrir os três baús", "Recompensa extra ao encontrar todos os tesouros.", 100,
                Math.max(1, getTotalTesouros()));
        missoes = new ArrayList<>(List.of(missaoPrincipal, missaoChave, missaoPortao, missaoSantuario, missaoTesouros));
    }

    private void atualizarMissoes() {
        if (faseAtual > 1) {
            if (missaoChave.atualizar((int) mapaSalas.values().stream().filter(Sala::isVisitada).count())) {
                pontuacao += missaoChave.getRecompensa(); adicionarMensagem("Objetivo concluído: região explorada. +" + missaoChave.getRecompensa() + " pontos.");
            }
            long derrotados = inimigosDaFase.keySet().stream().filter(salasComInimigoDerrotado::contains).count();
            if (missaoPortao.atualizar((int) derrotados)) { pontuacao += missaoPortao.getRecompensa(); adicionarMensagem("Objetivo concluído: guardiões derrotados."); }
            if (missaoSantuario.atualizar(eventoSurpresaColetado ? 1 : 0)) { pontuacao += missaoSantuario.getRecompensa(); adicionarMensagem("Objetivo concluído: segredo descoberto."); }
            if (missaoTesouros.atualizar(getTesourosEncontrados())) { pontuacao += missaoTesouros.getRecompensa(); adicionarMensagem("Objetivo concluído: relíquias coletadas."); }
            return;
        }
        if (missaoChave.atualizar(inventario.contains("Chave Mística") ? 1 : 0)) {
            adicionarMensagem("Missão concluída: Chave Mística encontrada.");
        }
        if (missaoPortao.atualizar(mapaSalas.get(10).isBloqueada() ? 0 : 1)) {
            pontuacao += missaoPortao.getRecompensa();
            adicionarMensagem("Missão concluída: Portão aberto. +" + missaoPortao.getRecompensa() + " pontos.");
            registrarFeedback("Atalho liberado: +" + missaoPortao.getRecompensa() + " pontos!");
        }
        if (missaoSantuario.atualizar(mapaSalas.get(5).isVisitada() ? 1 : 0)) {
            pontuacao += missaoSantuario.getRecompensa();
            adicionarMensagem("Missão concluída: Santuário descoberto. +" + missaoSantuario.getRecompensa() + " pontos.");
            registrarFeedback("Santuário descoberto: +" + missaoSantuario.getRecompensa() + " pontos!");
        }
        if (missaoTesouros.atualizar(getTesourosEncontrados())) {
            pontuacao += missaoTesouros.getRecompensa();
            adicionarMensagem("Missão concluída: todos os baús encontrados. +" + missaoTesouros.getRecompensa() + " pontos.");
            registrarFeedback("Todos os baús encontrados: +" + missaoTesouros.getRecompensa() + " pontos!");
        }
    }

    private ResumoAventura criarResumoAventura() {
        long visitadas = mapaSalas.values().stream().filter(Sala::isVisitada).count();
        String classificacao;
        String criterio;
        int exploradasTotal = salasVisitadasCampanha;
        int tesourosTotal = tesourosCampanha;
        int bausTotal = totalTesourosCampanha;
        int todasMissoes = totalMissoesCampanha;
        if (exploradasTotal >= 15 && tesourosTotal >= Math.ceil(bausTotal * 2.0 / 3.0)) {
            classificacao = "Mestre da Masmorra";
            criterio = "15+ salas exploradas e pelo menos dois terços dos baús coletados.";
        } else if (exploradasTotal >= 10 && missoesOpcionaisCampanha >= 1) {
            classificacao = "Aventureiro";
            criterio = "10+ salas exploradas e pelo menos um objetivo opcional concluído.";
        } else {
            classificacao = "Explorador";
            criterio = "Classificação inicial; explore mais salas e conclua missões opcionais.";
        }
        return new ResumoAventura(tempoFinalSegundos, pontuacao, exploradasTotal, totalSalasCampanha,
                tesourosTotal, bausTotal, missoesConcluidasCampanha, todasMissoes,
                quantidadeMovimentos, classificacao, criterio);
    }

    public void atacar() {
        if (inimigoAtual == null) { registrarFeedback("Não há inimigo nesta sala."); notificarOuvintes(); return; }
        int dano = getAtaque();
        boolean critico = new Random().nextInt(100) < 18;
        if (critico) {
            dano = (int) Math.round(dano * 1.5);
            GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.CRITICO);
            adicionarMensagem("⚡ ACERTO CRÍTICO! Você desfere um golpe potente em " + inimigoAtual.getNome() + " causando " + dano + " de dano!");
        } else {
            GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.ATAQUE);
            adicionarMensagem("Você ataca " + inimigoAtual.getNome() + " e causa " + dano + " de dano.");
        }
        inimigoAtual.receberDano(dano);
        resolverTurnoCombate();
    }

    public void defender() {
        if (inimigoAtual == null) { registrarFeedback("Não há inimigo nesta sala."); notificarOuvintes(); return; }
        emGuarda = true;
        GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.DEFESA);
        adicionarMensagem("Você assume Postura Defensiva: reduzirá 60% do próximo dano e contra-atacará!");
        registrarFeedback("Postura defensiva adotada.");
        resolverTurnoCombate();
    }

    public void fugir() {
        if (inimigoAtual == null) { registrarFeedback("Não há combate em andamento."); notificarOuvintes(); return; }
        Sala recuo = null;
        for (Sala vizinho : grafo.obterVizinhos(salaAtual)) {
            if (vizinho.isVisitada() && !vizinho.isBloqueada() && (!inimigosDaFase.containsKey(vizinho.getId()) || salasComInimigoDerrotado.contains(vizinho.getId()))) {
                recuo = vizinho;
                break;
            }
        }
        if (recuo == null) recuo = salaInicial;
        inimigoAtual = null;
        emGuarda = false;
        esquivaAtiva = false;
        salaAtual = recuo;
        GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.CLIQUE);
        adicionarMensagem("Você recuou taticamente para " + recuo.getNome() + ". O monstro permanece ferido na câmara.");
        registrarFeedback("Recuo tático realizado!");
        notificarOuvintes();
    }

    public void usarHabilidade(Habilidade hab) {
        if (hab == null) return;
        if (!perfil.possuiHabilidade(hab)) {
            registrarFeedback("Habilidade não desbloqueada. Adquira-a na loja.");
            notificarOuvintes();
            return;
        }
        if (getCooldown(hab) > 0) {
            registrarFeedback("Habilidade em recarga: aguarde " + getCooldown(hab) + " turno(s).");
            notificarOuvintes();
            return;
        }

        switch (hab) {
            case GOLPE_DEVASTADOR -> {
                if (inimigoAtual == null) {
                    registrarFeedback("Esta habilidade requer um alvo em combate.");
                    notificarOuvintes();
                    return;
                }
                int danoBase = 28 + perfil.nivel(PerfilJogador.Melhoria.FORCA) * 4
                        + (perfil.possui(PerfilJogador.Equipamento.LAMINA) ? 8 : 0);
                int danoDevastador = (int) Math.round(danoBase * 1.7);
                inimigoAtual.receberDano(danoDevastador);
                cooldowns.put(hab, hab.getCooldownMaximo());
                GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.HABILIDADE);
                adicionarMensagem("GOLPE DEVASTADOR! Impacto titânico causa " + danoDevastador + " de dano!");
                resolverTurnoCombate();
            }
            case ESCUDO_RUNICO -> {
                emGuarda = true;
                if (inimigoAtual != null) inimigoAtual.receberDano(15);
                cooldowns.put(hab, hab.getCooldownMaximo());
                GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.DEFESA);
                adicionarMensagem("ESCUDO RÚNICO erguido! Barreira mágica ativa e 15 de reflexão arcana!");
                if (inimigoAtual != null) resolverTurnoCombate(); else { registrarFeedback("Escudo rúnico pronto."); notificarOuvintes(); }
            }
            case CURA_ARCANA -> {
                int recuperada = Math.min(35, vidaMaxima - vida);
                vida += recuperada;
                cooldowns.put(hab, hab.getCooldownMaximo());
                GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.CURA);
                adicionarMensagem("CURA ARCANA: +" + recuperada + " PV restaurados.");
                if (inimigoAtual != null) resolverTurnoCombate(); else { registrarFeedback("Cura arcana: +" + recuperada + " PV."); notificarOuvintes(); }
            }
            case PASSO_ESPECTRAL -> {
                esquivaAtiva = true;
                cooldowns.put(hab, hab.getCooldownMaximo());
                GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.HABILIDADE);
                adicionarMensagem("PASSO ESPECTRAL! Você desvia para o éter (esquiva do próximo ataque).");
                if (inimigoAtual != null) resolverTurnoCombate(); else { registrarFeedback("Passo espectral ativado."); notificarOuvintes(); }
            }
            case OLHAR_CARTOGRAFO -> {
                cooldowns.put(hab, hab.getCooldownMaximo());
                GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.HABILIDADE);
                executarBFSAteSaida();
                adicionarMensagem("OLHAR DO CARTÓGRAFO: Topologia do grafo revelada e menor rota calculada pela BFS!");
                registrarFeedback("Mapa iluminado pelo cartógrafo!");
            }
        }
    }

    public int getCooldown(Habilidade hab) {
        return cooldowns.getOrDefault(hab, 0);
    }

    public boolean podeUsarHabilidade(Habilidade hab) {
        return perfil.possuiHabilidade(hab) && getCooldown(hab) == 0;
    }

    public void usarPocao() {
        if (pocoes <= 0) { registrarFeedback("Você não tem poções."); notificarOuvintes(); return; }
        if (vida >= vidaMaxima) { registrarFeedback("Sua vida já está completa."); notificarOuvintes(); return; }
        pocoes--;
        if (pocoesExtras > 0) pocoesExtras--;
        pocoesUsadasNestaFase++;
        perfil.getConquistas().incrementarEstatistica("pocoes_usadas", 1);
        int recuperada = Math.min(40, vidaMaxima - vida);
        vida += recuperada;
        GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.CURA);
        adicionarMensagem("Poção usada: +" + recuperada + " de vida.");
        if (inimigoAtual != null) resolverTurnoCombate(); else { registrarFeedback("Poção usada: +" + recuperada + " de vida."); notificarOuvintes(); }
    }

    public void usarBomba() {
        if (bombas <= 0) { registrarFeedback("Você não tem bombas arcanas."); notificarOuvintes(); return; }
        if (inimigoAtual == null) { registrarFeedback("Bombas só podem ser usadas durante um combate."); notificarOuvintes(); return; }
        bombas--;
        if (bombasExtras > 0) bombasExtras--;
        perfil.getConquistas().incrementarEstatistica("bombas_usadas", 1);
        GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.CRITICO);
        inimigoAtual.receberDano(50);
        adicionarMensagem("Bomba arcana: 50 de dano contra " + inimigoAtual.getNome() + ".");
        resolverTurnoCombate();
    }

    public boolean comprarItemMercador(Item item) {
        if (item == null || perfil.moedas() < item.getPreco()) {
            registrarFeedback("Moedas insuficientes para comprar " + (item != null ? item.getNome() : "") + ".");
            return false;
        }
        if (item.getId().equals("pocao_cura")) {
            if (perfil.comprarPocao()) {
                pocoes++;
                pocoesExtras++;
                GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.COMPRA);
                adicionarMensagem("Mercador: Poção de Cura adquirida!");
                registrarFeedback("Poção de cura comprada!");
                notificarOuvintes();
                return true;
            }
        } else if (item.getId().equals("bomba_arcana")) {
            if (perfil.comprarBomba()) {
                bombas++;
                bombasExtras++;
                GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.COMPRA);
                adicionarMensagem("Mercador: Bomba Arcana adquirida!");
                registrarFeedback("Bomba arcana comprada!");
                notificarOuvintes();
                return true;
            }
        }
        return false;
    }

    private void resolverTurnoCombate() {
        // Reduz recarga das habilidades
        for (Habilidade h : Habilidade.values()) {
            int cd = cooldowns.getOrDefault(h, 0);
            if (cd > 0) cooldowns.put(h, cd - 1);
        }

        if (inimigoAtual != null && inimigoAtual.isDerrotado()) {
            Inimigo derrotado = inimigoAtual;
            int salaDerrotada = salaAtual.getId();
            salasComInimigoDerrotado.add(salaDerrotada);
            pontuacao += derrotado.isChefe() ? 300 : 80;
            perfil.adicionarMoedas(derrotado.isChefe() ? 75 : 20);
            perfil.getConquistas().incrementarEstatistica("inimigos_derrotados", 1);
            if (derrotado.isChefe()) {
                perfil.getConquistas().incrementarEstatistica("chefes_derrotados", 1);
                perfil.getConquistas().desbloquear(SistemaConquistas.Conquista.MATADOR_DE_CHEFES);
            }
            if (danoSofriNesteCombate == 0) {
                perfil.getConquistas().desbloquear(SistemaConquistas.Conquista.IMPECAVEL);
            }
            GerenciadorAudio.getInstancia().tocar(derrotado.isChefe() ? EfeitoSonoro.VITORIA : EfeitoSonoro.ATAQUE);
            adicionarMensagem(derrotado.getNome() + " foi derrotado! +" + (derrotado.isChefe() ? 300 : 80) + " pontos.");
            inimigoAtual = null;
            emGuarda = false;
            esquivaAtiva = false;
            if (derrotado.isChefe()) {
                chefesDerrotados++;
                coletarRecompensas(salaAtual);
                concluirPortal();
            } else {
                coletarRecompensas(salaAtual);
                atualizarMissoes();
                registrarFeedback("Vitória no combate! A câmara está livre.");
            }
        } else {
            if (inimigoAtual != null) {
                if (esquivaAtiva) {
                    esquivaAtiva = false;
                    GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.HABILIDADE);
                    adicionarMensagem("ESQUIVA! O ataque de " + inimigoAtual.getNome() + " passou direto pelo seu rastro espectral!");
                    registrarFeedback("Esquiva com sucesso!");
                } else {
                    int danoRecebido = Math.max(1, inimigoAtual.getDano() - perfil.nivel(PerfilJogador.Melhoria.RESISTENCIA) * 2
                            - (perfil.possui(PerfilJogador.Equipamento.ARMADURA) ? 5 : 0));
                    if (emGuarda) {
                        danoRecebido = Math.max(1, (int) Math.round(danoRecebido * 0.4));
                        inimigoAtual.receberDano(12);
                        adicionarMensagem("Postura Defensiva: dano reduzido para " + danoRecebido + " e você contra-atacou causando 12 de dano!");
                        emGuarda = false;
                    }
                    danoSofriNesteCombate += danoRecebido;
                    vida -= danoRecebido;
                    GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.DANO_JOGADOR);
                    adicionarMensagem(inimigoAtual.getNome() + " revida e causa " + danoRecebido + " de dano.");
                    if (vida <= 0) {
                        if (recuosRestantes <= 0) {
                            finalizarCampanha(false);
                            notificarOuvintes();
                            return;
                        }
                        recuosRestantes--;
                        int perda = Math.min(100, pontuacao);
                        pontuacao -= perda;
                        vida = 55;
                        inimigoAtual.receberDano(12);
                        inimigoAtual = null;
                        emGuarda = false;
                        esquivaAtiva = false;
                        salaAtual = salaInicial;
                        salaAtual.setVisitada(true);
                        adicionarMensagem("Você recuou para a entrada, recuperou-se com 55 de vida e perdeu " + perda + " pontos. O inimigo também ficou ferido.");
                    }
                }
                if (inimigoAtual != null) {
                    registrarFeedback("Combate: " + inimigoAtual.getNome() + " — " + inimigoAtual.getVida() + "/" + inimigoAtual.getVidaMaxima() + " PV.");
                } else {
                    registrarFeedback("Você recuou para a entrada. O inimigo continua ferido na câmara.");
                }
            }
        }
        notificarOuvintes();
    }

    public String getNomeFase() {
        return switch (faseAtual) {
            case 1 -> "Catacumbas de Pedra";
            case 2 -> "Galerias Rubras";
            case 3 -> "Bosque das Almas";
            case 4 -> "Fortaleza Espectral";
            case 5 -> "Abismo Congelado";
            case 6 -> "Cidadela das Cinzas";
            case 7 -> "Templo Celeste";
            default -> "Santuário Astral";
        };
    }
    public int getFaseAtual() { return faseAtual; }
    public int getTotalFases() { return totalFases; }
    public PerfilJogador getPerfil() { return perfil; }
    public PerfilJogador.ResultadoMoedas getRecompensaMoedas() { return recompensaMoedas; }
    public boolean isCampanhaVencida() { return campanhaVencida; }
    public int getRecuosRestantes() { return recuosRestantes; }
    public int getVida() { return vida; }
    public int getVidaMaxima() { return vidaMaxima; }
    public int getPocoes() { return pocoes; }
    public int getBombas() { return bombas; }
    public Inimigo getInimigoAtual() { return inimigoAtual; }
    public boolean isEmCombate() { return inimigoAtual != null; }
    public boolean isArmadilhaDisponivel(Sala sala) {
        return sala != null && sala.getId() == idArmadilhaDaFase() && !armadilhasAcionadas.contains(sala.getId());
    }
    public boolean isInimigoNaSala(Sala sala) {
        if (sala == null || salasComInimigoDerrotado.contains(sala.getId())) return false;
        Inimigo inimigo = inimigosDaFase.get(sala.getId());
        return inimigo != null && !inimigo.isDerrotado();
    }
    public boolean isChefeNaSala(Sala sala) {
        Inimigo inimigo = sala == null ? null : inimigosDaFase.get(sala.getId());
        return inimigo != null && inimigo.isChefe() && isInimigoNaSala(sala);
    }

    /**
     * Executa o algoritmo BFS do Grafo para encontrar o menor caminho
     * da sala atual até o Portal de Saída.
     */
    public ResultadoBFS executarBFSAteSaida() {
        this.rotaDijkstra = false;
        perfil.getConquistas().incrementarEstatistica("buscas_bfs", 1);
        this.ultimoResultadoBFS = grafo.buscarMenorCaminho(salaAtual, salaSaida, inventario);
        if (ultimoResultadoBFS.isSucesso()) {
            this.rotaDestacada = ultimoResultadoBFS.getCaminho();
            perfil.getConquistas().desbloquear(SistemaConquistas.Conquista.MESTRE_BFS);
            GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.HABILIDADE);
            adicionarMensagem("BFS calculada: Menor rota até a saída possui " +
                    ultimoResultadoBFS.getQuantidadeArestas() + " arestas.");
            registrarFeedback("BFS encontrou uma rota com " + ultimoResultadoBFS.getQuantidadeArestas() + " passagens.");
        } else {
            this.rotaDestacada = Collections.emptyList();
            adicionarMensagem("BFS: " + ultimoResultadoBFS.getMensagem());
            registrarFeedback("BFS não encontrou uma rota até a saída.");
        }
        notificarOuvintes();
        return ultimoResultadoBFS;
    }

    /** Calcula e guarda o registro real da BFS para a demonstração interativa. */
    public ResultadoBFS prepararDemonstracaoBFSAteSaida() {
        this.ultimoResultadoBFS = grafo.buscarMenorCaminho(salaAtual, salaSaida, inventario);
        this.ultimoResultadoDijkstra = null;
        this.rotaDestacada = null;
        this.rotaDijkstra = false;
        adicionarMensagem("Demonstração passo a passo da BFS iniciada.");
        registrarFeedback("Veja a BFS explorando o grafo passo a passo.");
        notificarOuvintes();
        return ultimoResultadoBFS;
    }

    /** Calcula e destaca a rota de menor custo total usando Dijkstra. */
    public ResultadoDijkstra executarDijkstraAteSaida() {
        this.rotaDijkstra = true;
        perfil.getConquistas().incrementarEstatistica("buscas_dijkstra", 1);
        this.ultimoResultadoDijkstra = grafo.buscarMenorCusto(salaAtual, salaSaida, inventario);
        this.rotaDestacada = ultimoResultadoDijkstra.isSucesso()
                ? ultimoResultadoDijkstra.getCaminho() : Collections.emptyList();
        if (ultimoResultadoDijkstra.isSucesso()) {
            perfil.getConquistas().desbloquear(SistemaConquistas.Conquista.ESTRATEGISTA_DIJKSTRA);
            GerenciadorAudio.getInstancia().tocar(EfeitoSonoro.HABILIDADE);
        }
        adicionarMensagem("Dijkstra: " + ultimoResultadoDijkstra.getMensagem());
        registrarFeedback(ultimoResultadoDijkstra.isSucesso()
                ? "Dijkstra encontrou a rota de menor custo."
                : "Dijkstra não encontrou uma rota até a saída.");
        notificarOuvintes();
        return ultimoResultadoDijkstra;
    }

    /**
     * Limpa o destaque visual da rota BFS.
     */
    public void limparRotaDestacada() {
        this.rotaDestacada = null;
        this.rotaDijkstra = false;
        registrarFeedback("Destaque de rota removido.");
        notificarOuvintes();
    }

    /**
     * Reinicia toda a partida, restaurando o grafo, o inventário e a posição inicial.
     */
    public void reiniciarPartida() {
        inicializarMasmorra();
        adicionarMensagem("Partida reiniciada. O mapa foi restaurado ao seu estado inicial.");
        registrarFeedback("Partida reiniciada. Uma nova aventura começou!");
        notificarOuvintes();
    }

    public void adicionarMensagem(String msg) {
        historicoMensagens.add(msg);
        // Mantém as últimas 50 mensagens para evitar consumo de memória
        if (historicoMensagens.size() > 50) {
            historicoMensagens.remove(0);
        }
    }

    // --- Getters ---

    public Grafo getGrafo() {
        return grafo;
    }

    public Sala getSalaAtual() {
        return salaAtual;
    }

    public Sala getSalaInicial() {
        return salaInicial;
    }

    public Sala getSalaSaida() {
        return salaSaida;
    }

    public Map<Integer, Sala> getMapaSalas() {
        return mapaSalas;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public int getQuantidadeMovimentos() {
        return quantidadeMovimentos;
    }

    public Set<String> getInventario() {
        return Collections.unmodifiableSet(inventario);
    }

    public List<String> getHistoricoMensagens() {
        return Collections.unmodifiableList(historicoMensagens);
    }

    public ResultadoBFS getUltimoResultadoBFS() {
        return ultimoResultadoBFS;
    }

    public ResultadoDijkstra getUltimoResultadoDijkstra() {
        return ultimoResultadoDijkstra;
    }

    public List<Sala> getRotaDestacada() {
        return rotaDestacada;
    }

    public boolean isRotaDijkstra() { return rotaDijkstra; }

    public boolean isPartidaConcluida() {
        return partidaConcluida;
    }

    public int getVersaoPartida() { return versaoPartida; }

    public boolean isMesmaPartida(int versao, int movimentos) {
        return versaoPartida == versao && quantidadeMovimentos == movimentos;
    }

    public int getTesourosRestantes() {
        return (int) mapaSalas.values().stream().filter(Sala::temTesouro).count();
    }

    public int getTotalTesouros() {
        return (int) mapaSalas.values().stream().filter(sala -> sala.getValorTesouro() > 0).count();
    }

    public int getTesourosEncontrados() { return getTotalTesouros() - getTesourosRestantes(); }

    public List<Missao> getMissoes() { return Collections.unmodifiableList(missoes); }

    public int getMissoesConcluidas() {
        return (int) missoes.stream().filter(Missao::isConcluida).count();
    }

    public int getMissoesOpcionaisConcluidas() {
        return (int) missoes.stream().filter(missao -> !missao.isPrincipal() && missao.isConcluida()).count();
    }

    public long getTempoDecorridoSegundos() {
        return tempoFinalSegundos >= 0 ? tempoFinalSegundos : (System.nanoTime() - instanteInicioNano) / 1_000_000_000L;
    }

    public ResumoAventura getResumoAventura() { return resumoAventura; }

    public boolean isEventoSurpresaDisponivel(Sala sala) {
        return sala != null && sala.getId() == salaEventoSurpresaId && !eventoSurpresaColetado;
    }

    public boolean podeAcessarSala(Sala sala) {
        if (!grafo.contemVertice(sala)) return false;
        return !sala.isBloqueada() || (!sala.isBloqueioPermanente()
                && sala.getItemNecessario() != null && inventario.contains(sala.getItemNecessario()));
    }

    public boolean isEventoSurpresaColetado() { return eventoSurpresaColetado; }

    public String getMensagemFeedback() { return mensagemFeedback; }

    public boolean isFeedbackRecente() { return System.currentTimeMillis() - instanteFeedback < 3200; }

    private void registrarFeedback(String mensagem) {
        mensagemFeedback = mensagem;
        instanteFeedback = System.currentTimeMillis();
    }

    // ========== MÉTODOS DE ATRIBUTOS E EQUIPAMENTO (Fase E — UI) ==========

    /** Retorna o dano base do ataque do herói, incluindo bônus de perfil e equipamentos. */
    public int getAtaque() {
        int base = 28 + perfil.nivel(PerfilJogador.Melhoria.FORCA) * 4
                + (perfil.possui(PerfilJogador.Equipamento.LAMINA) ? 8 : 0);
        if (armaEquipada != null) base += armaEquipada.getBonusAtaque();
        return base;
    }

    /** Retorna a defesa total do herói. */
    public int getDefesa() {
        int base = perfil.nivel(PerfilJogador.Melhoria.RESISTENCIA) * 2
                + (perfil.possui(PerfilJogador.Equipamento.ARMADURA) ? 5 : 0);
        if (armaduraEquipada != null) base += armaduraEquipada.getBonusDefesa();
        if (acessorioEquipado != null) base += acessorioEquipado.getBonusDefesa();
        return base;
    }

    /** Retorna o saldo de moedas atual do perfil. */
    public int getMoedas() { return perfil.moedas(); }

    /** Retorna se o herói está em postura defensiva no turno atual. */
    public boolean isDefendendo() { return emGuarda; }

    /** Retorna a lista de habilidades desbloqueadas pelo jogador. */
    public Set<Habilidade> getHabilidadesDesbloqueadas() { return perfil.getHabilidades(); }

    /** Retorna a recarga restante de uma habilidade em turnos. */
    public int getRecargaRestanteHabilidade(Habilidade hab) { return getCooldown(hab); }

    /** Desbloqueia uma habilidade usando moedas do perfil. */
    public boolean desbloquearHabilidade(Habilidade hab) {
        if (hab == null) return false;
        boolean ok = perfil.comprarHabilidade(hab);
        if (ok) {
            adicionarMensagem("Habilidade desbloqueada: " + hab.getNome() + "!");
            notificarOuvintes();
        }
        return ok;
    }

    // ========== INVENTÁRIO DE ITENS (Fase E) ==========

    /** Retorna a lista de itens no inventário do herói. */
    public List<Item> getItensInventario() { return Collections.unmodifiableList(itensInventario); }

    public Item getArmaEquipada() { return armaEquipada; }
    public Item getArmaduraEquipada() { return armaduraEquipada; }
    public Item getAcessorioEquipado() { return acessorioEquipado; }

    /** Equipa um item do inventário no slot correspondente ao seu tipo. */
    public void equiparItem(Item item) {
        if (item == null || !item.getTipo().isEquipavel()) return;
        if (!itensInventario.contains(item)) return;

        switch (item.getTipo()) {
            case ARMA -> {
                if (armaEquipada != null) armaEquipada.setEquipado(false);
                armaEquipada = item;
            }
            case ARMADURA -> {
                if (armaduraEquipada != null) armaduraEquipada.setEquipado(false);
                armaduraEquipada = item;
                vidaMaxima = 100 + 15 * perfil.nivel(PerfilJogador.Melhoria.VITALIDADE) + item.getBonusVida();
            }
            case ACESSORIO -> {
                if (acessorioEquipado != null) acessorioEquipado.setEquipado(false);
                acessorioEquipado = item;
                vidaMaxima = 100 + 15 * perfil.nivel(PerfilJogador.Melhoria.VITALIDADE) + item.getBonusVida();
            }
            default -> { return; }
        }
        item.setEquipado(true);
        adicionarMensagem("Equipou: " + item.getNome());
        notificarOuvintes();
    }

    /** Remove um item equipado do slot correspondente. */
    public void desequiparItem(Item item) {
        if (item == null) return;
        if (item == armaEquipada) { armaEquipada = null; }
        else if (item == armaduraEquipada) { armaduraEquipada = null; }
        else if (item == acessorioEquipado) { acessorioEquipado = null; }
        item.setEquipado(false);
        vidaMaxima = 100 + 15 * perfil.nivel(PerfilJogador.Melhoria.VITALIDADE)
                + (armaduraEquipada != null ? armaduraEquipada.getBonusVida() : 0)
                + (acessorioEquipado != null ? acessorioEquipado.getBonusVida() : 0);
        if (vida > vidaMaxima) vida = vidaMaxima;
        adicionarMensagem("Desequipou: " + item.getNome());
        notificarOuvintes();
    }

    /** Usa um item consumível do inventário. Retorna true se foi utilizado. */
    public boolean usarItem(Item item) {
        if (item == null || !itensInventario.contains(item)) return false;
        if (item.getTipo() != br.com.graphquest.modelo.TipoItem.CONSUMIVEL) return false;
        if (item.getCura() > 0 && vida >= vidaMaxima) return false;

        if (item.getCura() > 0) {
            int recuperada = Math.min(item.getCura(), vidaMaxima - vida);
            vida += recuperada;
            adicionarMensagem("Usou " + item.getNome() + ": +" + recuperada + " PV.");
        }
        itensInventario.remove(item);
        notificarOuvintes();
        return true;
    }

    /** Adiciona um item ao inventário do herói. */
    public void adicionarItem(Item item) {
        if (item != null) {
            itensInventario.add(item);
            notificarOuvintes();
        }
    }
}

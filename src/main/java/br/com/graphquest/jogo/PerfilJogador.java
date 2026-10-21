package br.com.graphquest.jogo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/** Perfil local persistente: moedas, compras, melhorias e histórico de campanhas. */
public final class PerfilJogador {
    public enum Melhoria {
        VITALIDADE("Vitalidade", "+15 de vida máxima por nível", 65, 3),
        FORCA("Força", "+4 de dano por ataque por nível", 75, 3),
        RESISTENCIA("Resistência", "Reduz 2 de dano recebido por nível", 70, 3),
        EXPLORACAO("Exploração", "+5 moedas de bônus por sala secreta", 55, 3),
        PREPARACAO("Preparação", "+1 poção inicial por nível", 60, 3);

        private final String nome, descricao;
        private final int custoBase, nivelMaximo;
        Melhoria(String nome, String descricao, int custoBase, int nivelMaximo) {
            this.nome = nome; this.descricao = descricao; this.custoBase = custoBase; this.nivelMaximo = nivelMaximo;
        }
        public String getNome() { return nome; }
        public String getDescricao() { return descricao; }
        public int getNivelMaximo() { return nivelMaximo; }
    }

    public enum Equipamento {
        LAMINA("Lâmina Rúnica", "Ataques causam +8 de dano", 140),
        ARMADURA("Cota do Guardião", "Reduz 5 de dano recebido", 150),
        AMULETO("Amuleto de Âmbar", "+10% de moedas de campanha", 120);
        private final String nome, descricao;
        private final int preco;
        Equipamento(String nome, String descricao, int preco) {
            this.nome = nome; this.descricao = descricao; this.preco = preco;
        }
        public String getNome() { return nome; }
        public String getDescricao() { return descricao; }
        public int getPreco() { return preco; }
    }

    private final Path arquivo;
    private final EnumMap<Melhoria, Integer> melhorias = new EnumMap<>(Melhoria.class);
    private final Set<Equipamento> equipamentos = new LinkedHashSet<>();
    private final Set<String> campanhasRecompensadas = new LinkedHashSet<>();
    private final List<String> historico = new ArrayList<>();
    private int moedas = 50;
    private int pocoesGuardadas;
    private int bombasGuardadas;
    private int campanhasVencidas;
    private int campanhasPerdidas;

    public PerfilJogador() { this(caminhoPadrao()); }

    public PerfilJogador(Path arquivo) {
        this.arquivo = arquivo.toAbsolutePath().normalize();
        for (Melhoria m : Melhoria.values()) melhorias.put(m, 0);
        carregar();
    }

    private static Path caminhoPadrao() {
        String configurado = System.getProperty("graphquest.profile");
        return configurado == null || configurado.isBlank()
                ? Path.of("graphquest-data", "perfil.properties") : Path.of(configurado);
    }

    private synchronized void carregar() {
        if (!Files.isRegularFile(arquivo)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(arquivo)) {
            p.load(in);
            moedas = Math.max(0, numero(p, "moedas", 50));
            pocoesGuardadas = Math.max(0, numero(p, "pocoes", 0));
            bombasGuardadas = Math.max(0, numero(p, "bombas", 0));
            campanhasVencidas = Math.max(0, numero(p, "vitorias", 0));
            campanhasPerdidas = Math.max(0, numero(p, "derrotas", 0));
            for (Melhoria m : Melhoria.values()) melhorias.put(m,
                    Math.max(0, Math.min(m.nivelMaximo, numero(p, "melhoria." + m.name(), 0))));
            for (Equipamento e : Equipamento.values()) if (Boolean.parseBoolean(p.getProperty("equip." + e.name(), "false"))) equipamentos.add(e);
            for (String id : p.getProperty("campanhas.resgatadas", "").split(",")) if (!id.isBlank()) campanhasRecompensadas.add(id);
            int quantidade = Math.max(0, Math.min(500, numero(p, "historico.quantidade", 0)));
            for (int i = 0; i < quantidade; i++) {
                String encoded = p.getProperty("historico." + i);
                if (encoded != null) historico.add(new String(Base64.getDecoder().decode(encoded), java.nio.charset.StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            historico.add("Perfil recuperado com valores padrão após erro de leitura.");
        }
    }

    private int numero(Properties p, String chave, int padrao) {
        try { return Integer.parseInt(p.getProperty(chave, String.valueOf(padrao))); }
        catch (NumberFormatException e) { return padrao; }
    }

    private synchronized void salvar() {
        Properties p = new Properties();
        p.setProperty("moedas", String.valueOf(moedas));
        p.setProperty("pocoes", String.valueOf(pocoesGuardadas));
        p.setProperty("bombas", String.valueOf(bombasGuardadas));
        p.setProperty("vitorias", String.valueOf(campanhasVencidas));
        p.setProperty("derrotas", String.valueOf(campanhasPerdidas));
        for (Melhoria m : Melhoria.values()) p.setProperty("melhoria." + m.name(), String.valueOf(nivel(m)));
        for (Equipamento e : Equipamento.values()) p.setProperty("equip." + e.name(), String.valueOf(equipamentos.contains(e)));
        p.setProperty("campanhas.resgatadas", String.join(",", campanhasRecompensadas));
        p.setProperty("historico.quantidade", String.valueOf(historico.size()));
        for (int i = 0; i < historico.size(); i++) p.setProperty("historico." + i,
                Base64.getEncoder().encodeToString(historico.get(i).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        Path temporario = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");
        try {
            Files.createDirectories(arquivo.getParent());
            try (OutputStream out = Files.newOutputStream(temporario)) { p.store(out, "GraphQuest — perfil persistente"); }
            try { Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível salvar o perfil em " + arquivo, e);
        }
    }

    public synchronized boolean comprarMelhoria(Melhoria melhoria) {
        int atual = nivel(melhoria);
        if (atual >= melhoria.nivelMaximo) return false;
        int custo = custoMelhoria(melhoria);
        if (!debitar(custo)) return false;
        melhorias.put(melhoria, atual + 1);
        registrarCompra("Melhoria " + melhoria.nome + " nível " + (atual + 1), custo);
        return true;
    }

    public synchronized boolean comprarPocao() {
        if (!debitar(25)) return false;
        pocoesGuardadas++;
        registrarCompra("Poção de cura", 25);
        return true;
    }

    public synchronized boolean comprarBomba() {
        if (!debitar(35)) return false;
        bombasGuardadas++;
        registrarCompra("Bomba arcana", 35);
        return true;
    }

    public synchronized boolean comprarEquipamento(Equipamento equipamento) {
        if (equipamentos.contains(equipamento) || !debitar(equipamento.preco)) return false;
        equipamentos.add(equipamento);
        registrarCompra(equipamento.nome, equipamento.preco);
        return true;
    }

    private boolean debitar(int valor) {
        if (moedas < valor) return false;
        moedas -= valor;
        return true;
    }

    private void registrarCompra(String item, int valor) {
        historico.add("Compra: " + item + " (-" + valor + " moedas)");
        salvar();
    }

    public synchronized ResultadoMoedas registrarResultado(String id, boolean venceu, int chefes,
                                                             int objetivos, int segredos, int bonusDesempenho) {
        if (campanhasRecompensadas.contains(id)) return new ResultadoMoedas(0, 0, 0, 0, 0, moedas, moedas, true);
        int saldoAnterior = moedas;
        int base = venceu ? 100 : 25;
        int bonusChefes = Math.max(0, chefes) * 25;
        int bonusObjetivos = Math.max(0, objetivos) * 10;
        int bonusSegredos = Math.max(0, segredos) * (10 + nivel(Melhoria.EXPLORACAO) * 5);
        int bonusEficiencia = Math.max(0, Math.min(25, bonusDesempenho));
        if (equipamentos.contains(Equipamento.AMULETO)) {
            base = (int) Math.round(base * 1.10);
            bonusChefes = (int) Math.round(bonusChefes * 1.10);
            bonusObjetivos = (int) Math.round(bonusObjetivos * 1.10);
            bonusSegredos = (int) Math.round(bonusSegredos * 1.10);
        }
        moedas += base + bonusChefes + bonusObjetivos + bonusSegredos + bonusEficiencia;
        if (venceu) campanhasVencidas++; else campanhasPerdidas++;
        campanhasRecompensadas.add(id);
        historico.add((venceu ? "Vitória" : "Derrota") + " — " + id + ": +" + (moedas - saldoAnterior) + " moedas");
        salvar();
        return new ResultadoMoedas(base, bonusChefes, bonusObjetivos, bonusSegredos, bonusEficiencia,
                saldoAnterior, moedas, false);
    }

    public synchronized void guardarConsumiveis(int pocoes, int bombas) {
        pocoesGuardadas += Math.max(0, pocoes);
        bombasGuardadas += Math.max(0, bombas);
        salvar();
    }

    /** Retira do baú persistente os itens que serão levados para uma campanha. */
    public synchronized int[] retirarConsumiveisGuardados() {
        int[] itens = {pocoesGuardadas, bombasGuardadas};
        if (pocoesGuardadas > 0 || bombasGuardadas > 0) {
            pocoesGuardadas = 0;
            bombasGuardadas = 0;
            salvar();
        }
        return itens;
    }

    public synchronized void consumirPocaoGuardada() { if (pocoesGuardadas > 0) { pocoesGuardadas--; salvar(); } }
    public synchronized void consumirBombaGuardada() { if (bombasGuardadas > 0) { bombasGuardadas--; salvar(); } }

    public synchronized int moedas() { return moedas; }
    public synchronized int nivel(Melhoria melhoria) { return melhorias.getOrDefault(melhoria, 0); }
    public synchronized int custoMelhoria(Melhoria melhoria) { return melhoria.custoBase * (nivel(melhoria) + 1); }
    public synchronized boolean possui(Equipamento equipamento) { return equipamentos.contains(equipamento); }
    public synchronized int getPocoesGuardadas() { return pocoesGuardadas; }
    public synchronized int getBombasGuardadas() { return bombasGuardadas; }
    public synchronized int getCampanhasVencidas() { return campanhasVencidas; }
    public synchronized int getCampanhasPerdidas() { return campanhasPerdidas; }
    public synchronized List<String> getHistorico() { return List.copyOf(historico); }
    public synchronized Set<Equipamento> getEquipamentos() { return Set.copyOf(equipamentos); }
    public Path getArquivo() { return arquivo; }

    public static final class ResultadoMoedas {
        private final int base, chefes, objetivos, segredos, eficiencia, saldoAnterior, saldoFinal;
        private final boolean duplicada;
        public ResultadoMoedas(int base, int chefes, int objetivos, int segredos, int eficiencia,
                               int saldoAnterior, int saldoFinal, boolean duplicada) {
            this.base=base; this.chefes=chefes; this.objetivos=objetivos; this.segredos=segredos;
            this.eficiencia=eficiencia; this.saldoAnterior=saldoAnterior; this.saldoFinal=saldoFinal; this.duplicada=duplicada;
        }
        public int getBase() { return base; }
        public int getChefes() { return chefes; }
        public int getObjetivos() { return objetivos; }
        public int getSegredos() { return segredos; }
        public int getEficiencia() { return eficiencia; }
        public int getTotalRecebido() { return base + chefes + objetivos + segredos + eficiencia; }
        public int getSaldoAnterior() { return saldoAnterior; }
        public int getSaldoFinal() { return saldoFinal; }
        public boolean isDuplicada() { return duplicada; }
    }
}

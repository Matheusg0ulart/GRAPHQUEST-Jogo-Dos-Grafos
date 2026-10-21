package br.com.graphquest.modelo;

import java.util.Objects;

/**
 * Representa um Vértice (Nó) do Grafo da masmorra no GraphQuest.
 * 
 * Cada sala possui identificação única, nome, narrativa descritiva, tipo de sala,
 * coordenadas para renderização vetorial no mapa, estado de visitação e 
 * regras de interatividade (tesouro, bloqueio e chaves).
 */
public class Sala {
    private final int id;
    private final String nome;
    private final String descricao;
    private TipoSala tipo;
    
    // Coordenadas lógicas apenas para o desenho vetorial no PainelMapa (não afetam a lógica de busca)
    private final int x;
    private final int y;

    // Elementos de jogabilidade
    private final int valorTesouro;
    private boolean tesouroColetado;
    private boolean bloqueada;
    private final boolean bloqueioPermanente; // se for desmoronamento, não abre com chave
    private final String motivoBloqueio;
    private final String itemNecessario;   // Chave necessária para desbloquear, se houver
    private final String itemConcedido;    // Item entregue ao entrar na sala (ex: Chave Mística)
    private boolean visitada;

    /**
     * Construtor completo para inicialização de uma sala na masmorra.
     */
    public Sala(int id, String nome, String descricao, TipoSala tipo, int x, int y,
                int valorTesouro, boolean bloqueada, boolean bloqueioPermanente,
                String motivoBloqueio, String itemNecessario, String itemConcedido) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.tipo = tipo;
        this.x = x;
        this.y = y;
        this.valorTesouro = valorTesouro;
        this.bloqueada = bloqueada;
        this.bloqueioPermanente = bloqueioPermanente;
        this.motivoBloqueio = motivoBloqueio;
        this.itemNecessario = itemNecessario;
        this.itemConcedido = itemConcedido;
        this.tesouroColetado = false;
        this.visitada = false;
    }

    /**
     * Construtor simplificado para salas sem itens ou bloqueios complexos.
     */
    public Sala(int id, String nome, String descricao, TipoSala tipo, int x, int y) {
        this(id, nome, descricao, tipo, x, y, 0, false, false, null, null, null);
    }

    /**
     * Construtor para sala com tesouro.
     */
    public Sala(int id, String nome, String descricao, TipoSala tipo, int x, int y, int valorTesouro) {
        this(id, nome, descricao, tipo, x, y, valorTesouro, false, false, null, null, null);
    }

    /**
     * Construtor para sala com bloqueio.
     */
    public Sala(int id, String nome, String descricao, TipoSala tipo, int x, int y,
                boolean bloqueada, boolean bloqueioPermanente, String motivoBloqueio, String itemNecessario) {
        this(id, nome, descricao, tipo, x, y, 0, bloqueada, bloqueioPermanente, motivoBloqueio, itemNecessario, null);
    }

    // --- Getters e Setters de Estado ---

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public TipoSala getTipo() {
        return tipo;
    }

    public void setTipo(TipoSala tipo) {
        this.tipo = tipo;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getValorTesouro() {
        return valorTesouro;
    }

    public boolean temTesouro() {
        return valorTesouro > 0 && !tesouroColetado;
    }

    public boolean isTesouroColetado() {
        return tesouroColetado;
    }

    public void setTesouroColetado(boolean tesouroColetado) {
        this.tesouroColetado = tesouroColetado;
    }

    public boolean isBloqueada() {
        return bloqueada;
    }

    public void setBloqueada(boolean bloqueada) {
        this.bloqueada = bloqueada;
    }

    public boolean isBloqueioPermanente() {
        return bloqueioPermanente;
    }

    public String getMotivoBloqueio() {
        return motivoBloqueio;
    }

    public String getItemNecessario() {
        return itemNecessario;
    }

    public String getItemConcedido() {
        return itemConcedido;
    }

    public boolean isVisitada() {
        return visitada;
    }

    public void setVisitada(boolean visitada) {
        this.visitada = visitada;
    }

    /**
     * Tenta desbloquear a sala se ela não for um bloqueio permanente.
     * Retorna verdadeiro se o desbloqueio foi realizado.
     */
    public boolean desbloquear() {
        if (!bloqueioPermanente && bloqueada) {
            this.bloqueada = false;
            return true;
        }
        return false;
    }

    /**
     * Restaura o estado da sala para as condições iniciais do jogo.
     */
    public void resetarEstado(boolean bloqueadaInicialmente, TipoSala tipoOriginal) {
        this.visitada = false;
        this.tesouroColetado = false;
        this.bloqueada = bloqueadaInicialmente;
        this.tipo = tipoOriginal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sala sala = (Sala) o;
        return id == sala.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "[" + id + "] " + nome + (bloqueada ? " (BLOQUEADA)" : "");
    }
}

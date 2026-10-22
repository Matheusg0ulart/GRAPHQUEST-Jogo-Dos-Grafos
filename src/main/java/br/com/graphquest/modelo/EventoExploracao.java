package br.com.graphquest.modelo;

/**
 * Eventos dinâmicos e aleatórios que enriquecem a exploração da masmorra no GraphQuest.
 */
public class EventoExploracao {

    public enum TipoEvento {
        FONTE_CURA("Fonte Rejuvenescedora", "Uma fonte de água cristalina emana paz. Você bebe e restaura vida."),
        BAU_MISTERIOSO("Baú Ornamentado", "Um antigo cofre de ferro esquecido pelos guardiões."),
        ALTAR_BENCAO("Altar dos Ancestrais", "Um pedestal sagrado onde preces concedem foco e determinação."),
        MERCADOR_ERRANTE("Mercador das Sombras", "Um viajante encapuzado oferece suprimentos valiosos."),
        PASSAGEM_SECRETA("Fenda na Parede", "Você identifica tijolos soltos que revelam um atalho estratégico."),
        EMBOSCADA("Emboscada!", "Monstros à espreita saltam das sombras tentando surpreender o aventureiro.");

        private final String nome;
        private final String descricao;

        TipoEvento(String nome, String descricao) {
            this.nome = nome;
            this.descricao = descricao;
        }

        public String getNome() { return nome; }
        public String getDescricao() { return descricao; }
    }

    private final TipoEvento tipo;
    private final String titulo;
    private final String narrativa;
    private boolean resolvido;

    public EventoExploracao(TipoEvento tipo) {
        this.tipo = tipo;
        this.titulo = tipo.getNome();
        this.narrativa = tipo.getDescricao();
        this.resolvido = false;
    }

    public EventoExploracao(TipoEvento tipo, String titulo, String narrativa) {
        this.tipo = tipo;
        this.titulo = titulo;
        this.narrativa = narrativa;
        this.resolvido = false;
    }

    public TipoEvento getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getNome() { return titulo; }
    public String getNarrativa() { return narrativa; }
    public boolean isResolvido() { return resolvido; }
    public void setResolvido(boolean resolvido) { this.resolvido = resolvido; }
}

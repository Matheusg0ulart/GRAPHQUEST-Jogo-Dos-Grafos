package br.com.graphquest.jogo;

/** Estado de uma missão exibida durante a exploração. */
public final class Missao {
    public enum Estado {
        PENDENTE("Pendente"),
        EM_ANDAMENTO("Em andamento"),
        CONCLUIDA("Concluída");

        private final String rotulo;

        Estado(String rotulo) { this.rotulo = rotulo; }

        public String getRotulo() { return rotulo; }
    }

    private final String titulo;
    private final String descricao;
    private final boolean principal;
    private final int recompensa;
    private final int total;
    private int progresso;
    private Estado estado;
    private boolean recompensaEntregue;

    public Missao(String titulo, String descricao, boolean principal, int total, boolean iniciar) {
        if (total < 1) throw new IllegalArgumentException("O total da missão deve ser positivo.");
        this.titulo = titulo;
        this.descricao = descricao;
        this.principal = principal;
        this.recompensa = 0;
        this.total = total;
        this.progresso = 0;
        this.estado = iniciar ? Estado.EM_ANDAMENTO : Estado.PENDENTE;
    }

    public Missao(String titulo, String descricao, int recompensa, int total) {
        if (total < 1) throw new IllegalArgumentException("O total da missão deve ser positivo.");
        this.titulo = titulo;
        this.descricao = descricao;
        this.principal = false;
        this.recompensa = Math.max(0, recompensa);
        this.total = total;
        this.progresso = 0;
        this.estado = Estado.PENDENTE;
    }

    /** Atualiza o progresso; retorna true somente na primeira conclusão. */
    boolean atualizar(int valor) {
        Estado anterior = estado;
        progresso = Math.max(0, Math.min(total, valor));
        estado = progresso >= total ? Estado.CONCLUIDA
                : progresso > 0 || principal ? Estado.EM_ANDAMENTO : Estado.PENDENTE;
        if (estado == Estado.CONCLUIDA && anterior != Estado.CONCLUIDA && !recompensaEntregue) {
            recompensaEntregue = true;
            return true;
        }
        return false;
    }

    void concluir() { atualizar(total); }

    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public boolean isPrincipal() { return principal; }
    public int getRecompensa() { return recompensa; }
    public int getTotal() { return total; }
    public int getProgresso() { return progresso; }
    public Estado getEstado() { return estado; }
    public boolean isConcluida() { return estado == Estado.CONCLUIDA; }
    public String getProgressoFormatado() { return progresso + "/" + total; }
}

package br.com.graphquest.jogo;

/** Snapshot imutável das estatísticas registradas ao vencer a aventura. */
public final class ResumoAventura {
    private final long tempoSegundos;
    private final int pontuacao;
    private final int salasVisitadas;
    private final int totalSalas;
    private final int tesourosEncontrados;
    private final int totalTesouros;
    private final int missoesConcluidas;
    private final int totalMissoes;
    private final int movimentos;
    private final String classificacao;
    private final String criterioClassificacao;

    public ResumoAventura(long tempoSegundos, int pontuacao, int salasVisitadas, int totalSalas,
                          int tesourosEncontrados, int totalTesouros, int missoesConcluidas,
                          int totalMissoes, int movimentos, String classificacao,
                          String criterioClassificacao) {
        this.tempoSegundos = Math.max(0, tempoSegundos);
        this.pontuacao = pontuacao;
        this.salasVisitadas = salasVisitadas;
        this.totalSalas = totalSalas;
        this.tesourosEncontrados = tesourosEncontrados;
        this.totalTesouros = totalTesouros;
        this.missoesConcluidas = missoesConcluidas;
        this.totalMissoes = totalMissoes;
        this.movimentos = movimentos;
        this.classificacao = classificacao;
        this.criterioClassificacao = criterioClassificacao;
    }

    public long getTempoSegundos() { return tempoSegundos; }
    public int getPontuacao() { return pontuacao; }
    public int getSalasVisitadas() { return salasVisitadas; }
    public int getTotalSalas() { return totalSalas; }
    public int getPercentualExploracao() {
        return totalSalas == 0 ? 0 : (int) Math.round(salasVisitadas * 100.0 / totalSalas);
    }
    public int getTesourosEncontrados() { return tesourosEncontrados; }
    public int getTotalTesouros() { return totalTesouros; }
    public int getMissoesConcluidas() { return missoesConcluidas; }
    public int getTotalMissoes() { return totalMissoes; }
    public int getMovimentos() { return movimentos; }
    public String getClassificacao() { return classificacao; }
    public String getCriterioClassificacao() { return criterioClassificacao; }

    public static String formatarTempo(long segundos) {
        long minutos = segundos / 60;
        long resto = segundos % 60;
        return minutos > 0 ? String.format("%02d:%02d", minutos, resto)
                : String.format("%02d s", resto);
    }
}

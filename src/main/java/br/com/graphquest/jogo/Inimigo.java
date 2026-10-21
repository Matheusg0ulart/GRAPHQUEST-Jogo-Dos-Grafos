package br.com.graphquest.jogo;

/** Estado simples de um inimigo durante um encontro por turnos. */
public final class Inimigo {
    private final String nome;
    private final int vidaMaxima;
    private final int dano;
    private final boolean chefe;
    private int vida;

    public Inimigo(String nome, int vida, int dano, boolean chefe) {
        this.nome = nome;
        this.vidaMaxima = vida;
        this.vida = vida;
        this.dano = dano;
        this.chefe = chefe;
    }

    public String getNome() { return nome; }
    public int getVida() { return vida; }
    public int getVidaMaxima() { return vidaMaxima; }
    public int getDano() { return dano; }
    public boolean isChefe() { return chefe; }
    public boolean isDerrotado() { return vida <= 0; }
    public void receberDano(int quantidade) { vida = Math.max(0, vida - Math.max(0, quantidade)); }
}

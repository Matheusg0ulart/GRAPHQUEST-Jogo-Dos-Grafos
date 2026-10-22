package br.com.graphquest.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Gerenciador de efeitos sonoros sintetizados para o GraphQuest.
 * 
 * Utiliza exclusivamente a API padrão do Java (javax.sound.sampled) gerando
 * ondas sonoras senoidais e harmônicas em tempo de execução via PCM.
 * Não requer arquivos externos de áudio nem bibliotecas adicionais,
 * garantindo compatibilidade multiplataforma imediata sem riscos de dependências ausentes.
 */
public final class GerenciadorAudio {

    public enum EfeitoSonoro {
        ATAQUE,
        DANO_JOGADOR,
        DEFESA,
        CURA,
        CRITICO,
        MOEDA,
        BAU,
        COMPRA,
        PORTAO,
        VITORIA,
        DERROTA,
        HABILIDADE,
        CLIQUE
    }

    private static final GerenciadorAudio INSTANCIA = new GerenciadorAudio();
    private static final float TAXA_AMOSTRAGEM = 22050f;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "GraphQuest-Audio");
        t.setDaemon(true);
        return t;
    });

    private boolean ativado = true;
    private float volume = 0.7f; // 0.0f a 1.0f

    private GerenciadorAudio() {}

    public static GerenciadorAudio getInstancia() {
        return INSTANCIA;
    }

    public boolean isAtivado() {
        return ativado;
    }

    public void setAtivado(boolean ativado) {
        this.ativado = ativado;
    }

    public void alternarMudo() {
        this.ativado = !this.ativado;
    }

    public static boolean alternarAudio() {
        INSTANCIA.alternarMudo();
        return INSTANCIA.isAtivado();
    }

    public static boolean isAudioAtivado() {
        return INSTANCIA.isAtivado();
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    /**
     * Toca um efeito sonoro de forma assíncrona. Se o som estiver desligado ou
     * o dispositivo de som estiver indisponível, a execução não trava o jogo.
     */
    public void tocar(EfeitoSonoro efeito) {
        if (!ativado || efeito == null) return;
        executor.submit(() -> reproduzirSintetizado(efeito));
    }

    private void reproduzirSintetizado(EfeitoSonoro efeito) {
        byte[] dados = gerarAmostras(efeito);
        if (dados == null || dados.length == 0) return;

        try {
            AudioFormat formato = new AudioFormat(TAXA_AMOSTRAGEM, 8, 1, false, false);
            try (SourceDataLine linha = AudioSystem.getSourceDataLine(formato)) {
                linha.open(formato);
                linha.start();
                linha.write(dados, 0, dados.length);
                linha.drain();
            }
        } catch (Throwable ignored) {
            // Em ambientes sem saída de áudio física, ignora graciosamente sem travar o jogo
        }
    }

    private byte[] gerarAmostras(EfeitoSonoro efeito) {
        return switch (efeito) {
            case ATAQUE -> gerarTons(new int[]{220, 180, 140}, new int[]{40, 40, 50}, 0.8f);
            case DANO_JOGADOR -> gerarRuidoComTom(120, 90, 0.9f);
            case DEFESA -> gerarTons(new int[]{260, 390}, new int[]{60, 80}, 0.7f);
            case CURA -> gerarArpeggio(new int[]{330, 440, 550, 660}, 55, 0.65f);
            case CRITICO -> gerarTons(new int[]{240, 480, 720}, new int[]{50, 60, 90}, 0.9f);
            case MOEDA -> gerarArpeggio(new int[]{988, 1319}, 60, 0.6f);
            case BAU -> gerarArpeggio(new int[]{440, 554, 659, 880}, 75, 0.7f);
            case COMPRA -> gerarTons(new int[]{523, 659, 784}, new int[]{60, 60, 80}, 0.65f);
            case PORTAO -> gerarTons(new int[]{130, 160, 190}, new int[]{80, 80, 100}, 0.75f);
            case VITORIA -> gerarFanfarra();
            case DERROTA -> gerarLamento();
            case HABILIDADE -> gerarArpeggio(new int[]{400, 600, 800, 1200}, 45, 0.75f);
            case CLIQUE -> gerarTons(new int[]{600}, new int[]{25}, 0.4f);
        };
    }

    private byte[] gerarTons(int[] frequencias, int[] duracoesMs, float ganho) {
        int totalMs = 0;
        for (int d : duracoesMs) totalMs += d;
        int totalAmostras = (int) (TAXA_AMOSTRAGEM * totalMs / 1000f);
        byte[] buffer = new byte[totalAmostras];

        int offset = 0;
        for (int i = 0; i < frequencias.length; i++) {
            int freq = frequencias[i];
            int duracaoAmostras = (int) (TAXA_AMOSTRAGEM * duracoesMs[i] / 1000f);
            for (int j = 0; j < duracaoAmostras && (offset + j) < buffer.length; j++) {
                double t = (double) j / TAXA_AMOSTRAGEM;
                double onda = Math.sin(2.0 * Math.PI * freq * t);
                // Envelope linear de decaimento para não dar estalo
                double envelope = 1.0 - ((double) j / duracaoAmostras);
                int valor = (int) (128 + onda * 127 * ganho * volume * envelope);
                buffer[offset + j] = (byte) Math.max(0, Math.min(255, valor));
            }
            offset += duracaoAmostras;
        }
        return buffer;
    }

    private byte[] gerarArpeggio(int[] notas, int duracaoNotaMs, float ganho) {
        int[] duracoes = new int[notas.length];
        java.util.Arrays.fill(duracoes, duracaoNotaMs);
        return gerarTons(notas, duracoes, ganho);
    }

    private byte[] gerarRuidoComTom(int freq, int duracaoMs, float ganho) {
        int totalAmostras = (int) (TAXA_AMOSTRAGEM * duracaoMs / 1000f);
        byte[] buffer = new byte[totalAmostras];
        java.util.Random rnd = new java.util.Random(42);
        for (int i = 0; i < totalAmostras; i++) {
            double t = (double) i / TAXA_AMOSTRAGEM;
            double tom = Math.sin(2.0 * Math.PI * freq * t);
            double ruido = (rnd.nextDouble() * 2.0 - 1.0) * 0.4;
            double envelope = 1.0 - ((double) i / totalAmostras);
            int valor = (int) (128 + (tom * 0.7 + ruido) * 127 * ganho * volume * envelope);
            buffer[i] = (byte) Math.max(0, Math.min(255, valor));
        }
        return buffer;
    }

    private byte[] gerarFanfarra() {
        int[] notas = {523, 659, 784, 1046};
        int[] duracoes = {90, 90, 100, 260};
        return gerarTons(notas, duracoes, 0.85f);
    }

    private byte[] gerarLamento() {
        int[] notas = {392, 349, 311, 261};
        int[] duracoes = {110, 110, 130, 240};
        return gerarTons(notas, duracoes, 0.85f);
    }

    public static void tocarAtaque() { INSTANCIA.tocar(EfeitoSonoro.ATAQUE); }
    public static void tocarDano() { INSTANCIA.tocar(EfeitoSonoro.DANO_JOGADOR); }
    public static void tocarDefesa() { INSTANCIA.tocar(EfeitoSonoro.DEFESA); }
    public static void tocarPocao() { INSTANCIA.tocar(EfeitoSonoro.CURA); }
    public static void tocarCritico() { INSTANCIA.tocar(EfeitoSonoro.CRITICO); }
    public static void tocarMoeda() { INSTANCIA.tocar(EfeitoSonoro.MOEDA); }
    public static void tocarBau() { INSTANCIA.tocar(EfeitoSonoro.BAU); }
    public static void tocarCompra() { INSTANCIA.tocar(EfeitoSonoro.COMPRA); }
    public static void tocarPortao() { INSTANCIA.tocar(EfeitoSonoro.PORTAO); }
    public static void tocarVitoria() { INSTANCIA.tocar(EfeitoSonoro.VITORIA); }
    public static void tocarDerrota() { INSTANCIA.tocar(EfeitoSonoro.DERROTA); }
    public static void tocarHabilidade() { INSTANCIA.tocar(EfeitoSonoro.HABILIDADE); }
}

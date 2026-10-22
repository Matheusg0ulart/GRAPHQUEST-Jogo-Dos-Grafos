package br.com.graphquest.ui;

import br.com.graphquest.audio.GerenciadorAudio;
import br.com.graphquest.jogo.Habilidade;
import br.com.graphquest.jogo.Jogo;
import br.com.graphquest.modelo.Item;
import br.com.graphquest.modelo.TipoItem;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Diálogo da Loja e Empório do Mercador no GraphQuest.
 * 
 * Permite adquirir consumíveis, armas, armaduras, acessórios e desbloquear habilidades ativas
 * utilizando as moedas obtidas em baús, eventos e vitórias de combate.
 */
public class DialogoLoja extends JDialog {

    private final Jogo jogo;
    private final JLabel lblSaldo;
    private final JPanel painelMercadorias;

    public DialogoLoja(Window proprietario, Jogo jogo) {
        super(proprietario, "Bazar do Mercador Errante — GraphQuest", ModalityType.APPLICATION_MODAL);
        this.jogo = jogo;

        setSize(780, 580);
        setLocationRelativeTo(proprietario);
        setResizable(false);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(TemaVisual.COR_FUNDO_ESCURECIDO);

        // Cabeçalho
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(TemaVisual.COR_SUPERFICIE);
        cabecalho.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, TemaVisual.COR_BORDA),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)
        ));

        JLabel titulo = new JLabel("🪙 Empório do Mercador das Sombras");
        titulo.setFont(TemaVisual.FONTE_TITULO_GRANDE);
        titulo.setForeground(TemaVisual.COR_DOURADO);

        lblSaldo = new JLabel("Bolsa: " + jogo.getMoedas() + " ✦");
        lblSaldo.setFont(TemaVisual.FONTE_SUBTITULO);
        lblSaldo.setForeground(TemaVisual.COR_DOURADO_CLARO);

        cabecalho.add(titulo, BorderLayout.WEST);
        cabecalho.add(lblSaldo, BorderLayout.EAST);
        raiz.add(cabecalho, BorderLayout.NORTH);

        // Abas da Loja
        JTabbedPane abas = new JTabbedPane();
        abas.setBackground(TemaVisual.COR_SUPERFICIE);
        abas.setForeground(TemaVisual.COR_TEXTO_BRANCO);
        abas.setFont(TemaVisual.FONTE_NORMAL);

        painelMercadorias = new JPanel();
        painelMercadorias.setLayout(new BoxLayout(painelMercadorias, BoxLayout.Y_AXIS));
        painelMercadorias.setBackground(TemaVisual.COR_SUPERFICIE);
        painelMercadorias.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        abas.addTab("Equipamentos & Consumíveis", criarAbaEquipamentosEConsumiveis());
        abas.addTab("Habilidades Arcanas", criarAbaHabilidades());

        raiz.add(abas, BorderLayout.CENTER);

        // Rodapé
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        rodape.setBackground(TemaVisual.COR_SUPERFICIE);
        rodape.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, TemaVisual.COR_BORDA));

        JButton btnFechar = TemaVisual.criarBotao("Sair do Empório", new Color(45, 55, 72), new Color(60, 75, 95));
        btnFechar.addActionListener(e -> dispose());
        rodape.add(btnFechar);
        raiz.add(rodape, BorderLayout.SOUTH);

        setContentPane(raiz);
    }

    private JScrollPane criarAbaEquipamentosEConsumiveis() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(TemaVisual.COR_SUPERFICIE);
        painel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        List<Item> catalogo = new ArrayList<>();
        catalogo.add(new Item("pocao_maior", "Poção Maior de Vida", "Restaura 50 pontos de vida instantaneamente.", TipoItem.CONSUMIVEL, 0, 0, 0, 50, 25));
        catalogo.add(new Item("bomba_pesada", "Bomba de Fragmentação", "Causa 50 de dano garantido a um monstro.", TipoItem.CONSUMIVEL, 0, 0, 0, 0, 35));
        catalogo.add(new Item("espada_forjada", "Espada Curta Forjada", "Aço temperado que amplia o golpe físico.", TipoItem.ARMA, 10, 0, 0, 0, 60));
        catalogo.add(new Item("machado_runico", "Machado de Guerra Rúnico", "Machado pesado gravado com runas de corte.", TipoItem.ARMA, 22, 0, 0, 0, 120));
        catalogo.add(new Item("lamina_vorpal", "Lâmina Vorpal", "Lâmina mística capaz de retalhar dragões.", TipoItem.ARMA, 35, 0, 0, 0, 200));
        catalogo.add(new Item("cota_malha", "Cota de Malha Élfica", "Proteção leve que absorve impactos físicos.", TipoItem.ARMADURA, 0, 6, 0, 0, 60));
        catalogo.add(new Item("armadura_placas", "Armadura Completa de Placas", "Pesada couraça de aço que reduz danos pela metade.", TipoItem.ARMADURA, 0, 14, 0, 0, 130));
        catalogo.add(new Item("amuleto_dragao", "Amuleto de Sangue de Dragão", "Jóia que expande a vitalidade do herói.", TipoItem.ACESSORIO, 0, 0, 40, 0, 100));

        for (Item item : catalogo) {
            painel.add(criarLinhaItem(item));
            painel.add(Box.createVerticalStrut(8));
        }

        JScrollPane scroll = new JScrollPane(painel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        return scroll;
    }

    private JPanel criarLinhaItem(Item item) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(TemaVisual.COR_SUPERFICIE_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaVisual.COR_BORDA, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel lblNome = new JLabel(item.getNome() + "  (" + item.getEfeitoDescricao() + ")");
        lblNome.setFont(TemaVisual.FONTE_NEGRITO);
        lblNome.setForeground(TemaVisual.COR_TEXTO_BRANCO);

        JLabel lblDesc = new JLabel(item.getDescricao());
        lblDesc.setFont(TemaVisual.FONTE_PEQUENA);
        lblDesc.setForeground(TemaVisual.COR_TEXTO_CINZA);

        info.add(lblNome);
        info.add(Box.createVerticalStrut(2));
        info.add(lblDesc);
        card.add(info, BorderLayout.CENTER);

        // Preço e Botão de Compra
        JPanel compra = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        compra.setOpaque(false);

        JLabel lblPreco = new JLabel(item.getPreco() + " ✦");
        lblPreco.setFont(TemaVisual.FONTE_NEGRITO);
        lblPreco.setForeground(TemaVisual.COR_DOURADO);

        JButton btnComprar = TemaVisual.criarBotao("Comprar", new Color(94, 61, 27), new Color(130, 88, 35));
        btnComprar.setFont(TemaVisual.FONTE_PEQUENA);
        btnComprar.setEnabled(jogo.getMoedas() >= item.getPreco());

        btnComprar.addActionListener(e -> {
            btnComprar.setEnabled(false);
            boolean sucesso = jogo.comprarItemMercador(item);
            if (sucesso) {
                lblSaldo.setText("Bolsa: " + jogo.getMoedas() + " ✦");
                JOptionPane.showMessageDialog(this, "Você comprou: " + item.getNome() + "!", "Compra Efetuada", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Moedas insuficientes para adquirir este item.", "Saldo Insuficiente", JOptionPane.WARNING_MESSAGE);
                btnComprar.setEnabled(jogo.getMoedas() >= item.getPreco());
            }
        });

        compra.add(lblPreco);
        compra.add(btnComprar);
        card.add(compra, BorderLayout.EAST);

        return card;
    }

    private JScrollPane criarAbaHabilidades() {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(TemaVisual.COR_SUPERFICIE);
        painel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        for (Habilidade hab : Habilidade.values()) {
            painel.add(criarLinhaHabilidade(hab));
            painel.add(Box.createVerticalStrut(8));
        }

        JScrollPane scroll = new JScrollPane(painel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        return scroll;
    }

    private JPanel criarLinhaHabilidade(Habilidade hab) {
        boolean desbloqueada = jogo.getHabilidadesDesbloqueadas().contains(hab);

        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(TemaVisual.COR_SUPERFICIE_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(desbloqueada ? TemaVisual.COR_DOURADO : TemaVisual.COR_BORDA, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel lblNome = new JLabel(hab.getNome() + " (Recarga: " + hab.getTempoRecargaTurnos() + " turnos)");
        lblNome.setFont(TemaVisual.FONTE_NEGRITO);
        lblNome.setForeground(desbloqueada ? TemaVisual.COR_DOURADO_CLARO : TemaVisual.COR_TEXTO_BRANCO);

        JLabel lblDesc = new JLabel(hab.getDescricao());
        lblDesc.setFont(TemaVisual.FONTE_PEQUENA);
        lblDesc.setForeground(TemaVisual.COR_TEXTO_CINZA);

        info.add(lblNome);
        info.add(Box.createVerticalStrut(2));
        info.add(lblDesc);
        card.add(info, BorderLayout.CENTER);

        JPanel acao = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        acao.setOpaque(false);

        if (desbloqueada) {
            JLabel lblStatus = new JLabel("✓ APRENDIDA");
            lblStatus.setFont(TemaVisual.FONTE_PEQUENA);
            lblStatus.setForeground(TemaVisual.COR_ESMERALDA);
            acao.add(lblStatus);
        } else {
            JLabel lblPreco = new JLabel(hab.getCustoMoedas() + " ✦");
            lblPreco.setFont(TemaVisual.FONTE_NEGRITO);
            lblPreco.setForeground(TemaVisual.COR_DOURADO);

            JButton btnAprender = TemaVisual.criarBotao("Desbloquear", new Color(30, 64, 110), new Color(40, 85, 145));
            btnAprender.setFont(TemaVisual.FONTE_PEQUENA);
            btnAprender.setEnabled(jogo.getMoedas() >= hab.getCustoMoedas());

            btnAprender.addActionListener(e -> {
                btnAprender.setEnabled(false);
                boolean ok = jogo.desbloquearHabilidade(hab);
                if (ok) {
                    GerenciadorAudio.tocarCompra();
                    lblSaldo.setText("Bolsa: " + jogo.getMoedas() + " ✦");
                    JOptionPane.showMessageDialog(this, "Habilidade " + hab.getNome() + " desbloqueada!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    // Atualiza a linha
                    card.removeAll();
                    card.add(info, BorderLayout.CENTER);
                    JPanel novoStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
                    novoStatus.setOpaque(false);
                    JLabel l = new JLabel("✓ APRENDIDA");
                    l.setFont(TemaVisual.FONTE_PEQUENA);
                    l.setForeground(TemaVisual.COR_ESMERALDA);
                    novoStatus.add(l);
                    card.add(novoStatus, BorderLayout.EAST);
                    card.revalidate();
                    card.repaint();
                } else {
                    JOptionPane.showMessageDialog(this, "Moedas insuficientes.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    btnAprender.setEnabled(jogo.getMoedas() >= hab.getCustoMoedas());
                }
            });

            acao.add(lblPreco);
            acao.add(btnAprender);
        }

        card.add(acao, BorderLayout.EAST);
        return card;
    }
}

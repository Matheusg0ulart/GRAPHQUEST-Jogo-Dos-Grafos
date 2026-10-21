package br.com.graphquest.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Gerador nativo em Java da apresentação acadêmica PowerPoint (formato .pptx).
 * 
 * Constrói a estrutura Open Packaging Conventions (OPC) e PresentationML (ISO/IEC 29500)
 * contendo 10 slides acadêmicos ricamente formatados, com tema escuro medieval consistente,
 * diagramas conceituais, caixas de destaque e capturas de tela reais embutidas.
 */
public class GeradorApresentacaoPPTX {

    // Dimensões do slide em EMUs (16:9 Widescreen: 13.333 x 7.5 polegadas)
    private static final long SLIDE_WIDTH = 12192000L;
    private static final long SLIDE_HEIGHT = 6858000L;

    public static void main(String[] args) {
        System.out.println("Gerando apresentação PowerPoint (GraphQuest_ADO.pptx)...");

        File pastaApresentacao = new File("apresentacao");
        if (!pastaApresentacao.exists()) {
            pastaApresentacao.mkdirs();
        }

        File arquivoSaida = new File(pastaApresentacao, "GraphQuest_ADO.pptx");

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(arquivoSaida))) {
            // 1. [Content_Types].xml
            adicionarEntradaZip(zos, "[Content_Types].xml", gerarContentTypesXml());

            // 2. _rels/.rels
            adicionarEntradaZip(zos, "_rels/.rels", gerarRootRelsXml());

            // 3. ppt/presentation.xml e seus rels
            adicionarEntradaZip(zos, "ppt/presentation.xml", gerarPresentationXml());
            adicionarEntradaZip(zos, "ppt/_rels/presentation.xml.rels", gerarPresentationRelsXml());

            // 4. ppt/slideMasters e slideLayouts
            adicionarEntradaZip(zos, "ppt/slideMasters/slideMaster1.xml", gerarSlideMasterXml());
            adicionarEntradaZip(zos, "ppt/slideMasters/_rels/slideMaster1.xml.rels", gerarSlideMasterRelsXml());
            adicionarEntradaZip(zos, "ppt/slideLayouts/slideLayout1.xml", gerarSlideLayoutXml());
            adicionarEntradaZip(zos, "ppt/slideLayouts/_rels/slideLayout1.xml.rels", gerarSlideLayoutRelsXml());

            // 5. ppt/theme/theme1.xml
            adicionarEntradaZip(zos, "ppt/theme/theme1.xml", gerarThemeXml());

            // 6. Imagens Reais (Screenshots) embutidas em ppt/media/
            File imgMenu = new File("docs/screenshots/screenshot_01_menu_inicial.png");
            File imgMapaBFS = new File("docs/screenshots/screenshot_09_missoes_e_progresso.png");
            File imgBfsPasso = new File("docs/screenshots/screenshot_08_bfs_controles.png");

            if (imgMenu.exists()) adicionarArquivoZip(zos, "ppt/media/image1.png", imgMenu);
            if (imgMapaBFS.exists()) adicionarArquivoZip(zos, "ppt/media/image2.png", imgMapaBFS);
            if (imgBfsPasso.exists()) adicionarArquivoZip(zos, "ppt/media/image5.png", imgBfsPasso);

            // 7. Geração dos 10 Slides e seus respectivos .rels
            for (int i = 1; i <= 10; i++) {
                String slideXml = gerarSlideXml(i);
                adicionarEntradaZip(zos, "ppt/slides/slide" + i + ".xml", slideXml);

                String slideRelsXml = gerarSlideRelsXml(i);
                adicionarEntradaZip(zos, "ppt/slides/_rels/slide" + i + ".xml.rels", slideRelsXml);
            }

            System.out.println("✓ Apresentação PowerPoint gerada com sucesso em: " + arquivoSaida.getAbsolutePath());
            System.out.println("Tamanho do arquivo: " + arquivoSaida.length() + " bytes");
        } catch (Exception e) {
            System.err.println("Erro ao gerar arquivo .pptx: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void adicionarEntradaZip(ZipOutputStream zos, String caminho, String conteudo) throws IOException {
        ZipEntry entry = new ZipEntry(caminho);
        zos.putNextEntry(entry);
        byte[] bytes = conteudo.getBytes(StandardCharsets.UTF_8);
        zos.write(bytes);
        zos.closeEntry();
    }

    private static void adicionarArquivoZip(ZipOutputStream zos, String caminho, File arquivo) throws IOException {
        ZipEntry entry = new ZipEntry(caminho);
        zos.putNextEntry(entry);
        byte[] bytes = Files.readAllBytes(arquivo.toPath());
        zos.write(bytes);
        zos.closeEntry();
    }

    // --- Geradores de XML OpenXML padrão ---

    private static String gerarContentTypesXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                  <Default Extension="xml" ContentType="application/xml"/>
                  <Default Extension="png" ContentType="image/png"/>
                  <Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>
                  <Override PartName="/ppt/slideMasters/slideMaster1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml"/>
                  <Override PartName="/ppt/slideLayouts/slideLayout1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml"/>
                  <Override PartName="/ppt/theme/theme1.xml" ContentType="application/vnd.openxmlformats-officedocument.theme+xml"/>
                  <Override PartName="/ppt/slides/slide1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>
                  <Override PartName="/ppt/slides/slide2.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>
                  <Override PartName="/ppt/slides/slide3.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>
                  <Override PartName="/ppt/slides/slide4.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>
                  <Override PartName="/ppt/slides/slide5.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>
                  <Override PartName="/ppt/slides/slide6.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>
                  <Override PartName="/ppt/slides/slide7.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>
                  <Override PartName="/ppt/slides/slide8.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>
                  <Override PartName="/ppt/slides/slide9.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>
                  <Override PartName="/ppt/slides/slide10.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>
                </Types>
                """;
    }

    private static String gerarRootRelsXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="ppt/presentation.xml"/>
                </Relationships>
                """;
    }

    private static String gerarPresentationXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <p:presentation xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
                                xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
                                xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
                  <p:sldMasterIdLst>
                    <p:sldMasterId id="2147483648" r:id="rIdMaster1"/>
                  </p:sldMasterIdLst>
                  <p:sldIdLst>
                """);

        for (int i = 1; i <= 10; i++) {
            sb.append(String.format("    <p:sldId id=\"%d\" r:id=\"rIdSlide%d\"/>\n", 255 + i, i));
        }

        sb.append(String.format("""
                  </p:sldIdLst>
                  <p:sldSz cx="%d" cy="%d" type="screen16x9"/>
                  <p:notesSz cx="6858000" cy="9144000"/>
                </p:presentation>
                """, SLIDE_WIDTH, SLIDE_HEIGHT));
        return sb.toString();
    }

    private static String gerarPresentationRelsXml() {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rIdMaster1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="slideMasters/slideMaster1.xml"/>
                """);

        for (int i = 1; i <= 10; i++) {
            sb.append(String.format("  <Relationship Id=\"rIdSlide%d\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide\" Target=\"slides/slide%d.xml\"/>\n", i, i));
        }

        sb.append("</Relationships>");
        return sb.toString();
    }

    private static String gerarSlideMasterXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <p:sldMaster xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
                             xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
                             xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
                  <p:cSld>
                    <p:spTree>
                      <p:nvGrpSpPr>
                        <p:cNvPr id="1" name=""/>
                        <p:cNvGrpSpPr/>
                        <p:nvPr/>
                      </p:nvGrpSpPr>
                      <p:grpSpPr>
                        <a:xfrm>
                          <a:off x="0" y="0"/>
                          <a:ext cx="0" cy="0"/>
                          <a:chOff x="0" y="0"/>
                          <a:chExt cx="0" cy="0"/>
                        </a:xfrm>
                      </p:grpSpPr>
                    </p:spTree>
                  </p:cSld>
                  <p:clrMap bg1="dk1" tx1="lt1" bg2="dk2" tx2="lt2" accent1="accent1" accent2="accent2" accent3="accent3" accent4="accent4" accent5="accent5" accent6="accent6" hlink="hlink" folHlink="folHlink"/>
                  <p:sldLayoutIdLst>
                    <p:sldLayoutId id="2147483649" r:id="rIdLayout1"/>
                  </p:sldLayoutIdLst>
                </p:sldMaster>
                """;
    }

    private static String gerarSlideMasterRelsXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rIdLayout1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>
                  <Relationship Id="rIdTheme1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme" Target="../theme/theme1.xml"/>
                </Relationships>
                """;
    }

    private static String gerarSlideLayoutXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <p:sldLayout xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
                             xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
                             xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"
                             type="blank" preserve="1">
                  <p:cSld>
                    <p:spTree>
                      <p:nvGrpSpPr>
                        <p:cNvPr id="1" name=""/>
                        <p:cNvGrpSpPr/>
                        <p:nvPr/>
                      </p:nvGrpSpPr>
                      <p:grpSpPr>
                        <a:xfrm>
                          <a:off x="0" y="0"/>
                          <a:ext cx="0" cy="0"/>
                          <a:chOff x="0" y="0"/>
                          <a:chExt cx="0" cy="0"/>
                        </a:xfrm>
                      </p:grpSpPr>
                    </p:spTree>
                  </p:cSld>
                  <p:clrMapOvr>
                    <a:masterClrMapping/>
                  </p:clrMapOvr>
                </p:sldLayout>
                """;
    }

    private static String gerarSlideLayoutRelsXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rIdMaster" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="../slideMasters/slideMaster1.xml"/>
                </Relationships>
                """;
    }

    private static String gerarThemeXml() {
        return """
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <a:theme xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" name="GraphQuest Medieval Theme">
                  <a:themeElements>
                    <a:clrScheme name="MedievalDark">
                      <a:dk1><a:srgbClr val="0D1017"/></a:dk1>
                      <a:lt1><a:srgbClr val="FFFFFF"/></a:lt1>
                      <a:dk2><a:srgbClr val="161B22"/></a:dk2>
                      <a:lt2><a:srgbClr val="F3F4F6"/></a:lt2>
                      <a:accent1><a:srgbClr val="F59E0B"/></a:accent1>
                      <a:accent2><a:srgbClr val="10B981"/></a:accent2>
                      <a:accent3><a:srgbClr val="3B82F6"/></a:accent3>
                      <a:accent4><a:srgbClr val="EF4444"/></a:accent4>
                      <a:accent5><a:srgbClr val="8B5CF6"/></a:accent5>
                      <a:accent6><a:srgbClr val="FBBF24"/></a:accent6>
                      <a:hlink><a:srgbClr val="3B82F6"/></a:hlink>
                      <a:folHlink><a:srgbClr val="8B5CF6"/></a:folHlink>
                    </a:clrScheme>
                    <a:fontScheme name="ModernSans">
                      <a:majorFont><a:latin typeface="Segoe UI Semibold"/></a:majorFont>
                      <a:minorFont><a:latin typeface="Segoe UI"/></a:minorFont>
                    </a:fontScheme>
                    <a:fmtScheme name="Office">
                      <a:fillStyleLst><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:fillStyleLst>
                      <a:lnStyleLst><a:ln w="9525"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:ln></a:lnStyleLst>
                      <a:effectStyleLst><a:effectStyle><a:effectLst/></a:effectStyle></a:effectStyleLst>
                      <a:bgFillStyleLst><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:bgFillStyleLst>
                    </a:fmtScheme>
                  </a:themeElements>
                </a:theme>
                """;
    }

    private static String gerarSlideRelsXml(int slideNum) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                  <Relationship Id="rIdLayout" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>
                """);

        if (slideNum == 4) {
            sb.append("  <Relationship Id=\"rIdPic\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" Target=\"../media/image1.png\"/>\n");
        } else if (slideNum == 8) {
            sb.append("  <Relationship Id=\"rIdPic\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" Target=\"../media/image2.png\"/>\n");
        } else if (slideNum == 7) {
            sb.append("  <Relationship Id=\"rIdPic\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" Target=\"../media/image5.png\"/>\n");
        }

        sb.append("</Relationships>");
        return sb.toString();
    }

    // --- Geração do Conteúdo dos 10 Slides ---

    private static String gerarSlideXml(int slideNum) {
        StringBuilder shapes = new StringBuilder();

        // 1. Fundo do Slide
        shapes.append(criarRetanguloFundo());

        switch (slideNum) {
            case 1 -> {
                // Slide 1: Capa
                shapes.append(criarTexto(762000, 1143000, 10668000, 900000, "GRAPHQUEST", "F59E0B", 52, true));
                shapes.append(criarTexto(762000, 2050000, 10668000, 600000, "A Jornada dos Grafos — Estrutura de Dados Aplicada em Java", "FFFFFF", 24, true));
                shapes.append(criarTexto(762000, 2650000, 10668000, 400000, "Atividade Prática Acadêmica ADO • Algoritmo de Busca em Largura (BFS)", "9CA3AF", 16, false));

                // Card de Identificação Acadêmica
                shapes.append(criarCardRetangulo(762000, 3400000, 10668000, 2500000, "161B22", "30363D"));
                shapes.append(criarTexto(1143000, 3650000, 9906000, 450000, "IDENTIFICAÇÃO ACADÊMICA", "F59E0B", 18, true));
                shapes.append(criarTexto(1143000, 4150000, 9906000, 350000, "• Disciplina: Estrutura de Dados", "FFFFFF", 16, false));
                shapes.append(criarTexto(1143000, 4550000, 9906000, 350000, "• Integrantes: [ __________________________________________________ ]", "F3F4F6", 16, false));
                shapes.append(criarTexto(1143000, 4950000, 9906000, 350000, "• Turma: [ ________________________ ]", "F3F4F6", 16, false));
                shapes.append(criarTexto(1143000, 5350000, 9906000, 350000, "• Professor(a): [ __________________________________________________ ]", "F3F4F6", 16, false));
            }
            case 2 -> {
                // Slide 2: Contexto e Problema
                shapes.append(criarCabecalhoSlide("01. CONTEXTO E PROBLEMA ABORDADO", "Navegação ótima e tomada de decisão em ambientes com bifurcações e barreiras"));

                shapes.append(criarCardRetangulo(762000, 1800000, 5000000, 4400000, "161B22", "30363D"));
                shapes.append(criarTexto(1000000, 2000000, 4500000, 400000, "O DESAFIO ACADÊMICO", "F59E0B", 20, true));
                shapes.append(criarTexto(1000000, 2500000, 4500000, 3400000,
                        "• Como encontrar o caminho mais curto até a saída em um labirinto subterrâneo?\\n\\n" +
                        "• Como contornar desmoronamentos intransitáveis sem entrar em loops infinitos?\\n\\n" +
                        "• Como coletar recompensas e gerenciar bloqueios dinâmicos de forma eficiente?\\n\\n" +
                        "• Necessidade de uma estrutura de dados real que modele conexões ponto a ponto com fidelidade.", "FFFFFF", 15, false));

                shapes.append(criarCardRetangulo(6400000, 1800000, 5000000, 4400000, "161B22", "30363D"));
                shapes.append(criarTexto(6650000, 2000000, 4500000, 400000, "A SOLUÇÃO: GRAPHQUEST", "10B981", 20, true));
                shapes.append(criarTexto(6650000, 2500000, 4500000, 3400000,
                        "• Modelagem da Masmorra como um Grafo Não-Direcionado G = (V, E).\\n\\n" +
                        "• Salas = Vértices; Passagens navegáveis = Arestas.\\n\\n" +
                        "• BFS encontra o menor número de passagens; Dijkstra encontra o menor custo em corredores ponderados.\\n\\n" +
                        "• Interface gráfica desktop Java Swing com desenho vetorial Java2D didático.", "FFFFFF", 15, false));
            }
            case 3 -> {
                // Slide 3: Conceitos Fundamentais de Grafos
                shapes.append(criarCabecalhoSlide("02. CONCEITOS FUNDAMENTAIS DE GRAFOS", "A teoria matemática por trás da estrutura de dados implementada"));

                shapes.append(criarCardRetangulo(762000, 1800000, 3350000, 4400000, "161B22", "30363D"));
                shapes.append(criarTexto(950000, 2000000, 3000000, 350000, "VÉRTICES (V)", "F59E0B", 18, true));
                shapes.append(criarTexto(950000, 2450000, 3000000, 3500000,
                        "• Entidades fundamentais do grafo.\\n\\n" +
                        "• No GraphQuest, cada Vértice representa uma Câmara ou Sala da masmorra.\\n\\n" +
                        "• Cada sala armazena identificador único, nome, estado de visitação e regras de jogo (tesouro, itens, bloqueio).", "FFFFFF", 14, false));

                shapes.append(criarCardRetangulo(4400000, 1800000, 3350000, 4400000, "161B22", "30363D"));
                shapes.append(criarTexto(4600000, 2000000, 3000000, 350000, "ARESTAS (E)", "3B82F6", 18, true));
                shapes.append(criarTexto(4600000, 2450000, 3000000, 3500000,
                        "• Ligações entre pares de vértices.\\n\\n" +
                        "• No GraphQuest, cada Aresta representa um Corredor transitável.\\n\\n" +
                        "• Grafo Não-Direcionado: o corredor permite tráfego bidirecional (ida e volta livre entre salas vizinhas).", "FFFFFF", 14, false));

                shapes.append(criarCardRetangulo(8050000, 1800000, 3350000, 4400000, "161B22", "30363D"));
                shapes.append(criarTexto(8250000, 2000000, 3000000, 350000, "PESOS E CAMINHOS", "10B981", 18, true));
                shapes.append(criarTexto(8250000, 2450000, 3000000, 3500000,
                        "• Corredores podem ter pesos positivos diferentes.\\n\\n" +
                        "• BFS minimiza a quantidade de arestas; Dijkstra minimiza a soma dos pesos.\\n\\n" +
                        "• Um trajeto pode ter menos passagens e ainda custar mais que uma rota alternativa.", "FFFFFF", 14, false));
            }
            case 4 -> {
                // Slide 4: Representação do Mapa como Grafo
                shapes.append(criarCabecalhoSlide("03. REPRESENTAÇÃO DO MAPA COMO GRAFO", "Cada fase mantém 13 vértices e 19 arestas; três mapas formam a campanha"));

                shapes.append(criarCardRetangulo(762000, 1800000, 4600000, 4400000, "161B22", "30363D"));
                shapes.append(criarTexto(950000, 2000000, 4200000, 350000, "ELEMENTOS DO MAPA", "F59E0B", 18, true));
                shapes.append(criarTexto(950000, 2450000, 4200000, 3500000,
                        "• Sala Inicial (Entrada): Ponto de partida seguro.\\n\\n" +
                        "• Salas Comuns: Corredores de transição.\\n\\n" +
                        "• Salas de Tesouro: Baús com pontuação (+100, +200, +300 pts) coletáveis uma única vez.\\n\\n" +
                        "• Salas Especiais: Santuário concede a Bênção do Cristal; Biblioteca concede a Chave Mística.\\n\\n" +
                        "• Obstáculos (Salas Bloqueadas):\\n" +
                        "  - Desmoronamento permanente (Sala 6).\\n" +
                        "  - Portão destrancável com chave (Sala 10).\\n\\n" +
                        "• Missões reais: Santuário (+75), portão (+60) e baús (+100).\\n\\n" +
                        "• Portal da Sala 13 avança a fase; na etapa final, é preciso derrotar o chefe.", "FFFFFF", 13, false));

                // Imagem do Menu Inicial
                shapes.append(criarImagemSlide("rIdPic", 5600000, 1800000, 5800000, 4400000));
            }
            case 5 -> {
                // Slide 5: Tecnologias Utilizadas e Arquitetura
                shapes.append(criarCabecalhoSlide("04. TECNOLOGIAS E ARQUITETURA DO SOFTWARE", "Desenvolvimento modular, desacoplado e em conformidade estrita com o enunciado"));

                shapes.append(criarCardRetangulo(762000, 1800000, 5000000, 4400000, "161B22", "30363D"));
                shapes.append(criarTexto(1000000, 2000000, 4500000, 350000, "TECNOLOGIAS ESCOLHIDAS", "F59E0B", 18, true));
                shapes.append(criarTexto(1000000, 2450000, 4500000, 3500000,
                        "• Java 17+ LTS: Recursos nativos da linguagem sem frameworks pesados.\\n\\n" +
                        "• Java Swing: Interface gráfica desktop responsiva e de execução autônoma.\\n\\n" +
                        "• Java2D (Graphics2D): Renderização vetorial com antialiasing, gradientes e animações de pulso.\\n\\n" +
                        "• Zero Bibliotecas Externas no Core: grafo, BFS e Dijkstra usam Java padrão.\\n\\n" +
                        "• Maven + Scripts .bat: Facilidade total para compilar e rodar no IntelliJ ou Windows.", "FFFFFF", 14, false));

                shapes.append(criarCardRetangulo(6400000, 1800000, 5000000, 4400000, "161B22", "30363D"));
                shapes.append(criarTexto(6650000, 2000000, 4500000, 350000, "ORGANIZAÇÃO EM PACOTES", "10B981", 18, true));
                shapes.append(criarTexto(6650000, 2450000, 4500000, 3500000,
                        "• br.com.graphquest.modelo:\\n" +
                        "  - Sala.java (Vértice do grafo)\\n" +
                        "  - TipoSala.java (Enumeração e identidade visual)\\n\\n" +
                        "• br.com.graphquest.grafo:\\n" +
                        "  - Grafo.java (Lista, BFS e Dijkstra)\\n" +
                        "  - ResultadoBFS / ResultadoDijkstra\\n\\n" +
                        "• br.com.graphquest.jogo:\\n" +
                        "  - Jogo.java (Fases, combate e inventário)\\n" +
                        "  - Inimigo.java (Estado dos guardiões e chefe)\\n" +
                        "  - MasmorraPadrao.java (Topologia do grafo)\\n\\n" +
                        "• br.com.graphquest.ui:\\n" +
                        "  - PainelMapa.java, PainelInformacoes.java, etc.", "FFFFFF", 13, false));
            }
            case 6 -> {
                // Slide 6: Estrutura de Dados e Lista de Adjacência
                shapes.append(criarCabecalhoSlide("05. ESTRUTURA DE DADOS: LISTA DE ADJACÊNCIA", "Implementação eficiente em memória e performance de consulta"));

                shapes.append(criarCardRetangulo(762000, 1800000, 5000000, 4400000, "161B22", "30363D"));
                shapes.append(criarTexto(1000000, 2000000, 4500000, 350000, "REPRESENTAÇÃO NO CÓDIGO", "F59E0B", 18, true));
                shapes.append(criarTexto(1000000, 2450000, 4500000, 3500000,
                        "No Grafo.java, a estrutura foi implementada como:\\n\\n" +
                        "    Map<Sala, List<Sala>> listaAdjacencia;\\n\\n" +
                        "• Cada chave do Map representa um Vértice Sala.\\n\\n" +
                        "• Cada valor é uma List<Sala> contendo todos os vizinhos conectados por arestas diretas.\\n\\n" +
                        "• Inserção de aresta bidirecional:\\n" +
                        "  adicionarAresta(u, v) adiciona v na lista de u E adiciona u na lista de v.", "FFFFFF", 14, false));

                shapes.append(criarCardRetangulo(6400000, 1800000, 5000000, 4400000, "161B22", "30363D"));
                shapes.append(criarTexto(6650000, 2000000, 4500000, 350000, "VANTAGENS SOBRE MATRIZ", "3B82F6", 18, true));
                shapes.append(criarTexto(6650000, 2450000, 4500000, 3500000,
                        "1. Economia de Memória (Grafos Esparsos):\\n" +
                        "   - Uma matriz V x V gastaria O(V²), com 90% dos slots vazios.\\n" +
                        "   - A lista de adjacência consome estritamente O(V + E) espaço.\\n\\n" +
                        "2. Iteração Rápida sobre Vizinhos:\\n" +
                        "   - Para listar movimentos possíveis do jogador, consulta-se apenas a lista de vizinhos: O(grau).\\n" +
                        "   - Não é necessário varrer todos os outros nós do sistema.", "FFFFFF", 14, false));
            }
            case 7 -> {
                // Slide 7: Algoritmo BFS Passo a Passo
                shapes.append(criarCabecalhoSlide("06. DEMONSTRAÇÃO INTERATIVA DA BFS", "Eventos reais da fila, descobertas e reconstrução do caminho"));

                shapes.append(criarCardRetangulo(762000, 1800000, 4600000, 4400000, "161B22", "30363D"));
                shapes.append(criarTexto(950000, 2000000, 4200000, 350000, "ESTRUTURAS FUNDAMENTAIS", "F59E0B", 18, true));
                shapes.append(criarTexto(950000, 2450000, 4200000, 3500000,
                        "A demonstração registra os eventos emitidos pelo laço real em Grafo.buscarMenorCaminho.\\n\\n" +
                        "Queue<Sala> fila = new ArrayDeque<>();\\n" +
                        "Set<Sala> visitados = new LinkedHashSet<>();\\n" +
                        "Map<Sala, Sala> predecessores = new HashMap<>();\\n\\n" +
                        "Ao descobrir um vizinho, a BFS marca como visitado, registra o predecessor e adiciona à fila.\\n\\n" +
                        "Os controles percorrem o registro; o estado exibido é reproduzido dos eventos, sem executar uma busca paralela.", "FFFFFF", 13, false));

                // Imagem de Detalhes da BFS
                shapes.append(criarImagemSlide("rIdPic", 5600000, 1800000, 5800000, 4400000));
            }
            case 8 -> {
                // Slide 8: Progressão, exploração e escolhas de rota
                shapes.append(criarCabecalhoSlide("07. CAMPANHA, COMBATE E RECOMPENSAS", "Oito fases crescentes, economia persistente e chefes"));

                // Imagem da aplicação após descobrir o Santuário e obter a chave
                shapes.append(criarImagemSlide("rIdPic", 762000, 1800000, 6800000, 4400000));

                shapes.append(criarCardRetangulo(7800000, 1800000, 3600000, 4400000, "161B22", "30363D"));
                shapes.append(criarTexto(8000000, 2000000, 3200000, 350000, "DESTAQUES VISUAIS", "F59E0B", 18, true));
                shapes.append(criarTexto(8000000, 2450000, 3200000, 3500000,
                        "• Oito regiões crescem de 13 a 34 salas; chefes nas fases 4 e 8.\\n\\n" +
                        "• Melhorias e equipamentos alteram vida, dano, resistência e recursos iniciais.\\n\\n" +
                        "• Guardiões protegem baús; armadilhas sinalizadas causam dano uma vez por fase.\\n\\n" +
                        "• Perfil persistente guarda moedas, compras, consumíveis e resultados; recompensas não duplicam.\\n\\n" +
                        "• O Dragão das Sombras guarda o portal final; BFS continua calculando rotas no grafo.", "FFFFFF", 13, false));
            }
            case 9 -> {
                // Slide 9: Demonstração e Resultados dos Testes
                shapes.append(criarCabecalhoSlide("08. VALIDAÇÃO E TESTES AUTOMATIZADOS", "27 testes standalone cobrem grafo, BFS, campanha e combate"));

                shapes.append(criarCardRetangulo(762000, 1800000, 5000000, 4400000, "161B22", "30363D"));
                shapes.append(criarTexto(1000000, 2000000, 4500000, 350000, "CENÁRIOS TESTADOS", "F59E0B", 18, true));
                shapes.append(criarTexto(1000000, 2450000, 4500000, 3500000,
                        "✓ Teste 1: Caminho simples entre salas conectadas.\\n" +
                        "✓ Teste 2: Escolha garantida do menor caminho em rotas alternativas.\\n" +
                        "✓ Teste 3: Destino inalcançável (grafo desconexo).\\n" +
                        "✓ Teste 4: Origem igual ao destino (distância 0).\\n" +
                        "✓ Teste 5: Exclusão estrita de salas bloqueadas da rota.\\n" +
                        "✓ Teste 6: Ausência de loops infinitos em grafos cíclicos.\\n" +
                        "✓ Teste 7: Integridade e restauração no reinício da partida.\\n" +
                        "✓ Teste 8: Tesouro único e pontuação.\\n" +
                        "✓ Teste 9: Eventos reais e rota final do BFS.\\n" +
                        "✓ Teste 10: Estado após movimento inválido.\\n" +
                        "✓ Teste 11: Chave, portão e reinício.\\n" +
                        "✓ Teste 12: Condição de vitória.\\n" +
                        "✓ Teste 13: Identidade dos vértices.\\n" +
                        "✓ Teste 14: Dijkstra ponderado.\\n" +
                        "✓ Teste 15: Evento bônus de coleta única.\\n" +
                        "✓ Teste 16: Vértices nulos e grafo vazio.\\n" +
                        "✓ Teste 17: Arestas repetidas sem duplicação.\\n" +
                        "✓ Teste 18: Chave não abre bloqueio permanente.\\n" +
                        "✓ Teste 19: Recompensas únicas das missões.\\n" +
                        "✓ Teste 20: Conclusão da missão de tesouros.\\n" +
                        "✓ Teste 21: Resumo, cronômetro e classificação.\\n" +
                        "✓ Teste 22: Movimento por direção entre vizinhos.\\n" +
                        "✓ Teste 23: Rota alternativa sem a chave.\\n" +
                        "✓ Teste 24: Resumo acumulado da campanha.\\n" +
                        "✓ Teste 25: Progressão entre fases.\\n" +
                        "✓ Teste 26: Combate e consumíveis.\\n" +
                        "✓ Teste 27: Armadilha de dano único.", "FFFFFF", 11, false));

                shapes.append(criarCardRetangulo(6400000, 1800000, 5000000, 4400000, "161B22", "30363D"));
                shapes.append(criarTexto(6650000, 2000000, 4500000, 350000, "RESULTADO QUANTITATIVO", "10B981", 18, true));
                shapes.append(criarTexto(6650000, 2450000, 4500000, 3500000,
                        "• Última execução: 27 de 27 testes standalone aprovados.\\n\\n" +
                        "• Executável via testar.bat no Windows ou argumento --testes no Main.java.\\n\\n" +
                        "• Os testes JUnit 5 estão no projeto, mas Maven não estava disponível para executá-los.\\n\\n" +
                        "• A compilação Java foi concluída com sucesso.", "FFFFFF", 14, false));
            }
            case 10 -> {
                // Slide 10: Considerações Finais
                shapes.append(criarCabecalhoSlide("09. CONSIDERAÇÕES FINAIS E CONCLUSÃO", "Síntese dos aprendizados e conformidade integral com o enunciado acadêmico"));

                shapes.append(criarCardRetangulo(762000, 1800000, 5000000, 4400000, "161B22", "30363D"));
                shapes.append(criarTexto(1000000, 2000000, 4500000, 350000, "CONFORMIDADE COM O ENUNCIADO", "10B981", 18, true));
                shapes.append(criarTexto(1000000, 2450000, 4500000, 3500000,
                        "✓ Aplicação desktop completa em Java e Swing.\\n\\n" +
                        "✓ Grafo real implementado com Lista de Adjacência.\\n\\n" +
                        "✓ BFS e Dijkstra aplicados a objetivos de rota diferentes.\\n\\n" +
                        "✓ Mapa visual com conexões 100% fiéis às arestas.\\n\\n" +
                        "✓ Apresentação PowerPoint (.pptx) gerada.\\n\\n" +
                        "✓ Código-fonte ricamente comentado e documentado.", "FFFFFF", 14, false));

                shapes.append(criarCardRetangulo(6400000, 1800000, 5000000, 4400000, "161B22", "30363D"));
                shapes.append(criarTexto(6650000, 2000000, 4500000, 350000, "POSSÍVEIS MELHORIAS FUTURAS", "F59E0B", 18, true));
                shapes.append(criarTexto(6650000, 2450000, 4500000, 3500000,
                        "• Implementar geração procedural de masmorras com Prim ou Kruskal.\\n\\n" +
                        "• Busca Bidirecional: Otimização da BFS buscando simultaneamente a partir da origem e do destino.", "FFFFFF", 14, false));
            }
        }

        return montarSlideXml(shapes.toString());
    }

    private static String montarSlideXml(String shapesXml) {
        return String.format("""
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
                       xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
                       xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
                  <p:cSld>
                    <p:spTree>
                      <p:nvGrpSpPr>
                        <p:cNvPr id="1" name=""/>
                        <p:cNvGrpSpPr/>
                        <p:nvPr/>
                      </p:nvGrpSpPr>
                      <p:grpSpPr>
                        <a:xfrm>
                          <a:off x="0" y="0"/>
                          <a:ext cx="0" cy="0"/>
                          <a:chOff x="0" y="0"/>
                          <a:chExt cx="0" cy="0"/>
                        </a:xfrm>
                      </p:grpSpPr>
                      %s
                    </p:spTree>
                  </p:cSld>
                  <p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>
                </p:sld>
                """, shapesXml);
    }

    // --- Utilitários de Desenho OpenXML ---

    private static String criarRetanguloFundo() {
        return String.format("""
                <p:sp>
                  <p:nvSpPr>
                    <p:cNvPr id="2" name="Fundo"/>
                    <p:cNvSpPr><a:spLocks noGrp="1"/></p:cNvSpPr>
                    <p:nvPr/>
                  </p:nvSpPr>
                  <p:spPr>
                    <a:xfrm><a:off x="0" y="0"/><a:ext cx="%d" cy="%d"/></a:xfrm>
                    <a:prstGeom prst="rect"><a:avLst/></a:prstGeom>
                    <a:solidFill><a:srgbClr val="0D1017"/></a:solidFill>
                  </p:spPr>
                </p:sp>
                """, SLIDE_WIDTH, SLIDE_HEIGHT);
    }

    private static String criarCabecalhoSlide(String titulo, String subtitulo) {
        StringBuilder sb = new StringBuilder();
        // Barra decorativa dourada superior
        sb.append("""
                <p:sp>
                  <p:nvSpPr><p:cNvPr id="10" name="BarraDecor"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>
                  <p:spPr>
                    <a:xfrm><a:off x="762000" y="450000"/><a:ext cx="10668000" cy="30000"/></a:xfrm>
                    <a:prstGeom prst="rect"><a:avLst/></a:prstGeom>
                    <a:solidFill><a:srgbClr val="F59E0B"/></a:solidFill>
                  </p:spPr>
                </p:sp>
                """);

        sb.append(criarTexto(762000, 550000, 10668000, 550000, titulo, "F59E0B", 26, true));
        sb.append(criarTexto(762000, 1100000, 10668000, 400000, subtitulo, "9CA3AF", 15, false));
        return sb.toString();
    }

    private static String criarCardRetangulo(long x, long y, long w, long h, String corFundoHex, String corBordaHex) {
        return String.format("""
                <p:sp>
                  <p:nvSpPr><p:cNvPr id="%d" name="Card"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>
                  <p:spPr>
                    <a:xfrm><a:off x="%d" y="%d"/><a:ext cx="%d" cy="%d"/></a:xfrm>
                    <a:prstGeom prst="roundRect"><a:avLst><a:gd name="adj" fmla="val 2000"/></a:avLst></a:prstGeom>
                    <a:solidFill><a:srgbClr val="%s"/></a:solidFill>
                    <a:ln w="19050"><a:solidFill><a:srgbClr val="%s"/></a:solidFill></a:ln>
                  </p:spPr>
                </p:sp>
                """, (int) (Math.random() * 80000 + 1000), x, y, w, h, corFundoHex, corBordaHex);
    }

    private static String criarTexto(long x, long y, long w, long h, String texto, String corHex, int tamanhoPt, boolean negrito) {
        String[] linhas = texto.split("\\\\n");
        StringBuilder paragrafos = new StringBuilder();

        for (String linha : linhas) {
            paragrafos.append(String.format("""
                    <a:p>
                      <a:r>
                        <a:rPr lang="pt-BR" sz="%d" b="%d">
                          <a:solidFill><a:srgbClr val="%s"/></a:solidFill>
                          <a:latin typeface="Segoe UI"/>
                        </a:rPr>
                        <a:t>%s</a:t>
                      </a:r>
                    </a:p>
                    """, tamanhoPt * 100, negrito ? 1 : 0, corHex, escaparXml(linha)));
        }

        return String.format("""
                <p:sp>
                  <p:nvSpPr><p:cNvPr id="%d" name="Texto"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>
                  <p:spPr>
                    <a:xfrm><a:off x="%d" y="%d"/><a:ext cx="%d" cy="%d"/></a:xfrm>
                    <a:prstGeom prst="rect"><a:avLst/></a:prstGeom>
                    <a:noFill/>
                  </p:spPr>
                  <p:txBody>
                    <a:bodyPr lIns="0" tIns="0" rIns="0" bIns="0"/>
                    <a:lstStyle/>
                    %s
                  </p:txBody>
                </p:sp>
                """, (int) (Math.random() * 80000 + 2000), x, y, w, h, paragrafos);
    }

    private static String criarImagemSlide(String rId, long x, long y, long w, long h) {
        return String.format("""
                <p:pic>
                  <p:nvPicPr>
                    <p:cNvPr id="%d" name="Screenshot"/>
                    <p:cNvPicPr><a:picLocks noChangeAspect="1"/></p:cNvPicPr>
                    <p:nvPr/>
                  </p:nvPicPr>
                  <p:blipFill>
                    <a:blip r:embed="%s"/>
                    <a:stretch><a:fillRect/></a:stretch>
                  </p:blipFill>
                  <p:spPr>
                    <a:xfrm><a:off x="%d" y="%d"/><a:ext cx="%d" cy="%d"/></a:xfrm>
                    <a:prstGeom prst="rect"><a:avLst/></a:prstGeom>
                    <a:ln w="19050"><a:solidFill><a:srgbClr val="30363D"/></a:solidFill></a:ln>
                  </p:spPr>
                </p:pic>
                """, (int) (Math.random() * 80000 + 5000), rId, x, y, w, h);
    }

    private static String escaparXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}

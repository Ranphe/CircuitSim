import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Map;

public class PanelCircuito extends JPanel {
    public static final int TAMANO_FUENTE_TEXTO = 32;
    private static final int TAMANO_NODO = 9;
    private static final int GROSOR_ARISTA = 3;

    private double xOffset = 0;
    private double yOffset = 0;
    private double escala = 1.0;
    private int tamanoGrid = 60;

    private Map<Nodo, Point> posicionesNodos;
    private Point offsetComponente;
    private Point puntoArrastre;
    private Nodo nodoEnMovimiento = null;

    private final Circuito circuito;
    private final int numNodos;

    public PanelCircuito(Circuito circuito) {
        setBackground(Color.WHITE);
        this.circuito = circuito;
        if (circuito == null) {
            this.numNodos = 0;
            this.posicionesNodos = Collections.emptyMap();
            return;
        }
        this.numNodos = circuito.getNumNodosTotales();
        calcularPosicionesIniciales();
        agregarListeners();
    }

    private void agregarListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point puntoGrafo = convertirAPuntoPlano(e.getPoint());
                nodoEnMovimiento = null;
                for (Nodo nodo : circuito.getNodos()) {
                    Point posicionNodo = posicionesNodos.get(nodo);
                    if (posicionNodo != null && puntoGrafo.distance(posicionNodo) <= TAMANO_NODO / 2) {
                        nodoEnMovimiento = nodo;
                        offsetComponente = new Point(puntoGrafo.x - posicionNodo.x, puntoGrafo.y - posicionNodo.y);
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                        break;
                    }
                }
                if (nodoEnMovimiento == null) {
                    puntoArrastre = e.getPoint();
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                nodoEnMovimiento = null;
                setCursor(Cursor.getDefaultCursor());
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (nodoEnMovimiento != null) {
                    Point puntoGrafo = convertirAPuntoPlano(e.getPoint());
                    int x = snap(puntoGrafo.x - offsetComponente.x);
                    int y = snap(puntoGrafo.y - offsetComponente.y);
                    posicionesNodos.put(nodoEnMovimiento, new Point(x, y));
                    repaint();
                } else if (puntoArrastre != null) {
                    int dx = e.getX() - puntoArrastre.x;
                    int dy = e.getY() - puntoArrastre.y;
                    xOffset += dx;
                    yOffset += dy;
                    puntoArrastre = e.getPoint();
                    repaint();
                }
            }
        });
        addMouseWheelListener(e -> {
            Point p = e.getPoint();
            double xMouse = (p.x - xOffset) / escala;
            double yMouse = (p.y - yOffset) / escala;
            double factorZoom = (e.getWheelRotation() < 0) ? 1.1 : 1 / 1.1;
            escala *= factorZoom;
            xOffset = p.x - xMouse * escala;
            yOffset = p.y - yMouse * escala;
            repaint();
        });
    }

    private Point convertirAPuntoPlano(Point punto) {
        // Convierte coordenadas de pantalla a coordenadas del plano, ajustando por desplazamiento y escala.
        int x = (int) ((punto.x - xOffset) / escala);
        int y = (int) ((punto.y - yOffset) / escala);

        return new Point(x, y);
    }

    private int snap(int value) {
        // Redondea el valor a la cuadrícula más cercana para alinear elementos visualmente.
        return Math.round(value / (float) tamanoGrid) * tamanoGrid;
    }

    public void dibujarCable(Graphics2D g2, Point inicioPlano, Point finPlano) {
        // Si ambos puntos coinciden, no dibujar nada
        if (inicioPlano.equals(finPlano)) return;

        // Guardar el estado actual del contexto gráfico (color, trazo y transformación)
        Color colorOriginal = g2.getColor();
        Stroke trazoOriginal = g2.getStroke();
        AffineTransform transformacionOriginal = g2.getTransform();

        // Suavizar líneas
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Calcular diferencia y distancia
        double dx = finPlano.x - inicioPlano.x;
        double dy = finPlano.y - inicioPlano.y;
        double distancia = inicioPlano.distance(finPlano);

        // Factor para acortar la línea y que no solape el nodo
        double factor = (TAMANO_NODO / 2.0) / distancia;

        // Coordenadas ajustadas
        double xi = inicioPlano.x + dx * factor;
        double yi = inicioPlano.y + dy * factor;
        double xf = finPlano.x   - dx * factor;
        double yf = finPlano.y   - dy * factor;

        // Aplicar nuevo estilo de trazo y color
        g2.setStroke(new BasicStroke(GROSOR_ARISTA, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Color.BLACK);

        // Dibujar la línea
        Line2D.Double linea = new Line2D.Double(xi, yi, xf, yf);
        g2.draw(linea);

        // Restaurar el estado original del contexto gráfico
        g2.setColor(colorOriginal);
        g2.setStroke(trazoOriginal);
        g2.setTransform(transformacionOriginal);
    }

    public void dibujarResistencia(Graphics2D g2, Point inicioPlano, Point finPlano, ValorElectrico valorResistencia) {
        // Si ambos puntos coinciden, no hay resistencia que dibujar
        if (inicioPlano.equals(finPlano)) return;

        // Guardar el estado actual del contexto gráfico (color, trazo y transformación)
        Color colorOriginal = g2.getColor();
        Stroke trazoOriginal = g2.getStroke();
        AffineTransform transformacionOriginal = g2.getTransform();

        // Parámetros del zigzag
        final int ANCHO_ZIGZAG = 60;
        final int NUM_PICOS = 6;
        final int ALTURA_PICO = 30;

        // Activar suavizado de líneas y texto
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Calcular vector y distancia total
        double dx = finPlano.x - inicioPlano.x;
        double dy = finPlano.y - inicioPlano.y;
        double distancia = inicioPlano.distance(finPlano);

        // Factor para no solapar con los nodos
        double factorNodo = (TAMANO_NODO / 2.0) / distancia;
        double x0 = inicioPlano.x + dx * factorNodo;
        double y0 = inicioPlano.y + dy * factorNodo;
        double x1 = finPlano.x   - dx * factorNodo;
        double y1 = finPlano.y   - dy * factorNodo;

        // Punto medio y gap para zigzag
        double xm = (x0 + x1) / 2;
        double ym = (y0 + y1) / 2;
        double factorGap = (ANCHO_ZIGZAG / 2.0) / distancia;
        double xg0 = xm - dx * factorGap;
        double yg0 = ym - dy * factorGap;
        double xg1 = xm + dx * factorGap;
        double yg1 = ym + dy * factorGap;

        // Dibujar los brazos rectos de la resistencia
        g2.setStroke(new BasicStroke(GROSOR_ARISTA, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Color.BLACK);
        g2.draw(new Line2D.Double(x0, y0, xg0, yg0));
        g2.draw(new Line2D.Double(xg1, yg1, x1, y1));

        // Dibujar zigzag
        double ux = (xg1 - xg0) / ANCHO_ZIGZAG;
        double uy = (yg1 - yg0) / ANCHO_ZIGZAG;

        // Vector perpendicular unitario
        double px = -uy, py = ux;
        double xa = xg0, ya = yg0;
        boolean haciaArriba = true;

        // Dibuja cada pico del zigzag alternando la dirección, usando vectores para formar los vértices de la resistencia.
        for (int pico = 1; pico <= NUM_PICOS; pico++) {
            // Calcula el punto final del tramo, el vértice del pico y su desplazamiento perpendicular para formar el zigzag.
            double t = pico * (ANCHO_ZIGZAG / (double) NUM_PICOS);
            double xb = xg0 + ux * t;
            double yb = yg0 + uy * t;
            double desplaz = ALTURA_PICO * (haciaArriba ? -1 : 1);
            double xp = (xa + xb) / 2 + px * desplaz;
            double yp = (ya + yb) / 2 + py * desplaz;

            // Dibuja dos segmentos: desde el punto anterior al pico y del pico al siguiente punto del zigzag.
            g2.draw(new Line2D.Double(xa, ya, xp, yp));
            g2.draw(new Line2D.Double(xp, yp, xb, yb));

            // Actualiza el punto de inicio para el siguiente tramo y alterna la dirección del próximo pico.
            xa = xb;  ya = yb;
            haciaArriba = !haciaArriba;
        }

        // Dibujar etiqueta con valor formateado, centrada y rotada
        g2.translate(xm, ym);
        double angulo = Math.atan2(dy, dx);
        if (Math.abs(dy) < 1e-3) angulo = 0;
        g2.rotate(angulo);

        // Prepara la fuente y calcula el ancho del texto formateado para centrarlo sobre la resistencia.
        g2.setFont(new Font("Arial", Font.PLAIN, TAMANO_FUENTE_TEXTO));
        FontMetrics fm = g2.getFontMetrics();
        int anchoTexto = fm.stringWidth(valorResistencia.toString());

        // Se dibuja un poco por encima del zigzag
        g2.setColor(Color.BLACK);
        g2.drawString(valorResistencia.toString(), -anchoTexto / 2, -ALTURA_PICO - 10);

        // Restaurar el estado original del contexto gráfico
        g2.setColor(colorOriginal);
        g2.setStroke(trazoOriginal);
        g2.setTransform(transformacionOriginal);
    }

    public void dibujarFuente(Graphics2D g2, Point inicioPlano, Point finPlano, ValorElectrico valorFuente, ValorElectrico corriente) {
        // Si ambos puntos coinciden, nada que dibujar
        if (inicioPlano.equals(finPlano)) return;

        // Guardar el estado actual del contexto gráfico (color, trazo y transformación)
        Color colorOriginal = g2.getColor();
        Stroke trazoOriginal = g2.getStroke();
        AffineTransform transformacionOriginal = g2.getTransform();

        // Activar antialiasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Parámetros del círculo
        final int DIAMETRO_CIRCULO = 60;
        double radio = DIAMETRO_CIRCULO / 2.0;

        // Calcular vector y distancia total
        double dx = finPlano.x - inicioPlano.x;
        double dy = finPlano.y - inicioPlano.y;
        double distancia = inicioPlano.distance(finPlano);

        // Factor para no solapar con el centro de los nodos
        double factorNodo = (TAMANO_NODO / 2.0) / distancia;
        double x0 = inicioPlano.x + dx * factorNodo;
        double y0 = inicioPlano.y + dy * factorNodo;
        double x1 = finPlano.x - dx * factorNodo;
        double y1 = finPlano.y - dy * factorNodo;

        // Centro del círculo
        double xc = (x0 + x1) / 2.0;
        double yc = (y0 + y1) / 2.0;

        // Dibujar líneas de conexión al círculo
        g2.setStroke(new BasicStroke(GROSOR_ARISTA, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(Color.BLACK);
        double factorCirculo = radio / distancia;
        double xa = xc - dx * factorCirculo;
        double ya = yc - dy * factorCirculo;
        double xb = xc + dx * factorCirculo;
        double yb = yc + dy * factorCirculo;
        g2.draw(new Line2D.Double(x0, y0, xa, ya));
        g2.draw(new Line2D.Double(xb, yb, x1, y1));

        // Dibujar el círculo que representa la fuente
        double xCir = xc - radio;
        double yCir = yc - radio;
        g2.draw(new Ellipse2D.Double(xCir, yCir, DIAMETRO_CIRCULO, DIAMETRO_CIRCULO));

        // Preparar sistema de coordenadas rotado y centrado para etiquetas de valor y corriente
        g2.translate(xc, yc);
        double angulo = -Math.atan2(dy, dx);
        if (Math.abs(dy) < 1e-3) angulo = 0;
        g2.rotate(angulo);

        // Configura la fuente y calcula el alto del texto formateado para posicionarlo centrado sobre la fuente.
        g2.setFont(new Font("Arial", Font.PLAIN, TAMANO_FUENTE_TEXTO));
        FontMetrics fm = g2.getFontMetrics();
        int altoTexto = fm.getAscent();

        // Dibujar texto valor fuente por encima del círculo
        int anchoTextoValor = fm.stringWidth(valorFuente.toString());
        g2.setColor(Color.BLACK);
        g2.drawString(valorFuente.toString(), -anchoTextoValor / 2, (int)(-radio - 10));

        // Dibujar texto corriente total debajo del círculo
        int anchoTextoCorriente = fm.stringWidth(corriente.toString());
        g2.setColor(Color.BLUE);
        if (Math.abs(dy) < 1e-3) {
            g2.drawString(corriente.toString(), -anchoTextoCorriente / 2, (int)(radio + altoTexto + 10));
        } else {
            g2.rotate(Math.PI);
            g2.drawString(corriente.toString(), -anchoTextoCorriente / 2, (int)(-radio - 10));
        }

        // Restaurar el estado original del contexto gráfico
        g2.setColor(colorOriginal);
        g2.setStroke(trazoOriginal);
        g2.setTransform(transformacionOriginal);
    }

    private void calcularPosicionesIniciales() {
        SwingUtilities.invokeLater(() -> {
            // Calcula las posiciones iniciales de los nodos del circuito según el algoritmo de distribución espacial.
            posicionesNodos = LayoutCircuito.calcularPosicionesIniciales(circuito.getNodos(), circuito.getMapaIndicesNodosCompletos(), circuito.getMapaAdyacencia());

            // Calcula los límites extremos del circuito para determinar su tamaño total en el panel.
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
            for (Point p : posicionesNodos.values()) {
                if (p == null) continue;
                minX = Math.min(minX, p.x);
                minY = Math.min(minY, p.y);
                maxX = Math.max(maxX, p.x);
                maxY = Math.max(maxY, p.y);
            }

            // Calcula el centro del circuito y el centro del panel para alinear visualmente el contenido.
            int centroCircuitoX = (minX + maxX) / 2;
            int centroCircuitoY = (minY + maxY) / 2;
            int centroPanelX = getWidth() / 2;
            int centroPanelY = getHeight() / 2;

            // Ajusta el desplazamiento para centrar el circuito en el panel según la escala actual.
            xOffset = centroPanelX - centroCircuitoX * escala;
            yOffset = centroPanelY - centroCircuitoY * escala;
        });
    }

    public BufferedImage capturarImagen() {
        // Crea una imagen del tamaño del panel, la dibuja con el contenido actual y libera los recursos gráficos.
        BufferedImage imagen = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagen.createGraphics();
        paint(g2d);
        g2d.dispose();

        return imagen;
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Llama al metodo base, activa suavizado de bordes y prepara el contexto gráfico para dibujar.
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Muestra un mensaje centrado si no hay circuito cargado.
        if (circuito == null || numNodos == 0 || posicionesNodos == null) {
            String msg = "Cargue un archivo para comenzar";
            g2.setColor(Color.GRAY);
            g2.setFont(new Font("Arial", Font.PLAIN, TAMANO_FUENTE_TEXTO));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
            return;
        }

        // Aplicar pan y zoom
        g2.translate(xOffset, yOffset);
        g2.scale(escala, escala);

        // Dibujar cuadrícula
        g2.setColor(new Color(235, 235, 235));
        int anchoVisible = (int) (getWidth() / escala);
        int altoVisible = (int) (getHeight() / escala);
        int xInicio = (int) (-xOffset / escala);
        int yInicio = (int) (-yOffset / escala);
        for (int x = xInicio - (xInicio % tamanoGrid); x < xInicio + anchoVisible; x += tamanoGrid) {
            g2.drawLine(x, yInicio, x, yInicio + altoVisible);
        }
        for (int y = yInicio - (yInicio % tamanoGrid); y < yInicio + altoVisible; y += tamanoGrid) {
            g2.drawLine(xInicio, y, xInicio + anchoVisible, y);
        }

        // Dibujar nodos y etiquetas
        g2.setFont(new Font("Arial", Font.PLAIN, TAMANO_FUENTE_TEXTO));
        for (Nodo nodo : circuito.getNodos()) {
            Point posicionNodo = posicionesNodos.get(nodo);
            if (posicionNodo == null) continue;
            g2.setColor(Color.BLACK);
            g2.fillOval(posicionNodo.x - TAMANO_NODO / 2, posicionNodo.y - TAMANO_NODO / 2, TAMANO_NODO, TAMANO_NODO);
            g2.setColor(Color.BLUE);
            FontMetrics fm2 = g2.getFontMetrics();
            g2.drawString(nodo.getValor().toString(), posicionNodo.x - fm2.stringWidth(nodo.getValor().toString()) / 2, posicionNodo.y - TAMANO_NODO);
        }

        // Dibujar componentes
        for (Componente componente : circuito.getComponentes()) {
            char tipo = componente.getElemento();
            Point pA = posicionesNodos.get(componente.getNodoA());
            Point pB = posicionesNodos.get(componente.getNodoB());
            if (pA == null || pB == null) continue;
            switch (tipo) {
                case 'C' -> dibujarCable(g2, pA, pB);
                case 'R' -> dibujarResistencia(g2, pA, pB, componente.getValor());
                case 'F' -> dibujarFuente(g2, pA, pB, componente.getValor(), componente.getCorriente());
                default -> throw new IllegalArgumentException("Componente no reconocido: '" + tipo + "'");
            }
        }
    }
}
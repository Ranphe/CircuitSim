import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main extends JFrame {
    public final float TAMANO_FUENTE_TEXTO = 16.0f;
    private PanelCircuito panelCircuito;

    private Main() {
        setTitle("Circuitos");
        setSize(1728, 972);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        panelCircuito = new PanelCircuito(null);
        add(panelCircuito);
        setJMenuBar(crearBarraMenu());
    }

    private JMenuBar crearBarraMenu() {
        JMenuBar barraMenu = new JMenuBar();
        JMenu menuOpciones = new JMenu("Opciones");
        JMenuItem itemCargar = new JMenuItem("Cargar");
        JMenuItem itemCaptura = new JMenuItem("Guardar");
        Font fuente = barraMenu.getFont().deriveFont(TAMANO_FUENTE_TEXTO);
        menuOpciones.setFont(fuente);
        itemCargar.setFont(fuente);
        itemCaptura.setFont(fuente);

        itemCargar.addActionListener(e -> {
            JFileChooser explorador = new JFileChooser();
            explorador.setFileFilter(new FileNameExtensionFilter("Archivos de texto", "txt"));
            int seleccion = explorador.showOpenDialog(Main.this);
            if (seleccion == JFileChooser.APPROVE_OPTION) {
                File archivo = explorador.getSelectedFile();
                try {
                    Circuito circuito = CircuitoParser.desdeArchivo(archivo);
                    double[][] matrizMNA = MNA.generarMatriz(circuito);
                    double[] solucion = MNA.resolverMatriz(matrizMNA);
                    MNA.asignarVoltajesNodos(circuito, solucion);
                    MNA.asignarCorrientesFuentes(circuito, solucion);

                    // DEBUG
                    Prueba.unitaria(circuito, matrizMNA, solucion);

                    cargarCircuito(circuito);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(
                            Main.this,
                            "Error al leer el archivo:\n" + ex.getMessage(),
                            "Error de lectura",
                            JOptionPane.ERROR_MESSAGE
                    );
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(
                            Main.this,
                            "No fue posible leer el archivo:\n" + ex.getMessage(),
                            "Formato inválido",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        itemCaptura.addActionListener(e -> {
            JFileChooser guardador = new JFileChooser();
            guardador.setDialogTitle("Guardar captura como imagen");
            guardador.setFileFilter(new FileNameExtensionFilter("Archivo PNG", "png"));
            int resultado = guardador.showSaveDialog(Main.this);
            if (resultado == JFileChooser.APPROVE_OPTION) {
                File archivo = guardador.getSelectedFile();
                if (!archivo.getName().toLowerCase().endsWith(".png")) {
                    archivo = new File(archivo.getParentFile(), archivo.getName() + ".png");
                }
                try {
                    BufferedImage imagen = panelCircuito.capturarImagen();
                    ImageIO.write(imagen, "png", archivo);
                    JOptionPane.showMessageDialog(Main.this, "Captura guardada exitosamente.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(Main.this, "Error al guardar la imagen:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        menuOpciones.add(itemCargar);
        menuOpciones.add(itemCaptura);
        barraMenu.add(menuOpciones);
        return barraMenu;
    }

    public void cargarCircuito(Circuito circuito) {
        if (panelCircuito != null) {
            getContentPane().remove(panelCircuito);
        }
        panelCircuito = new PanelCircuito(circuito);
        getContentPane().add(panelCircuito, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}
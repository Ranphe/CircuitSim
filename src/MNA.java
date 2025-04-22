import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MNA {

    public static double[][] generarMatriz(Circuito circuito) {
        List<Componente> componentes = circuito.getComponentes();
        Map<Nodo, Integer> indices = circuito.getMapaIndicesNodosCanonicos();
        int numNodos = circuito.getNumNodosCanonicos() - 1; // menos tierra
        int numFuentesVoltaje = circuito.getNumFuentesVoltaje();

        // Matriz aumentada MNA: nodos sin tierra + fuentes de voltaje, con columna extra para vector.
        int tamano = numNodos + numFuentesVoltaje;
        double[][] matrizMNA = new double[tamano][tamano + 1];

        int indiceNodosTierra = numNodos; // indice de los nodos de tierra
        int filaFuente = numNodos; // primera fila donde se colocarán las fuentes de voltaje

        for (Componente componente : componentes) {
            char elemento = componente.getElemento();
            if (elemento == 'C') continue;

            // Obtener nodos, sus índices canónicos y el valor eléctrico del componente.
            Nodo nodoA = componente.getNodoA();
            Nodo nodoB = componente.getNodoB();
            int i = indices.get(nodoA);
            int j = indices.get(nodoB);
            double valor = componente.getValor();

            // Procesamiento de componentes según su tipo
            if (elemento == 'R') {
                double conductancia = 1.0 / valor;
                if (i != indiceNodosTierra) matrizMNA[i][i] += conductancia;
                if (j != indiceNodosTierra) matrizMNA[j][j] += conductancia;
                if (i != indiceNodosTierra && j != indiceNodosTierra) {
                    matrizMNA[i][j] -= conductancia;
                    matrizMNA[j][i] -= conductancia;
                }
            } else if( elemento == 'F') {
                if (i != indiceNodosTierra) {
                    matrizMNA[filaFuente][i] = 1.0;
                    matrizMNA[i][filaFuente] = 1.0;
                }
                if (j != indiceNodosTierra) {
                    matrizMNA[filaFuente][j] = -1.0;
                    matrizMNA[j][filaFuente] = -1.0;
                }
                matrizMNA[filaFuente][tamano] = valor;
                filaFuente++;
            } else if (elemento == 'I') {
                if (i != indiceNodosTierra) matrizMNA[i][tamano] -= valor;
                if (j != indiceNodosTierra) matrizMNA[j][tamano] += valor;
            }
        }

        return matrizMNA;
    }

    public static double[] resolverMatriz(double[][] matrizMNAOriginal) {
        // Validación de la matriz
        if (matrizMNAOriginal == null || matrizMNAOriginal.length == 0) throw new IllegalArgumentException("Dimensiones inválidas: la matriz no puede ser nula o tener 0 filas.");
        int numFilas = matrizMNAOriginal.length;
        if (matrizMNAOriginal[0] == null || matrizMNAOriginal[0].length == 0) throw new IllegalArgumentException("Dimensiones inválidas: la matriz debe tener al menos una columna.");
        int numColumnas = matrizMNAOriginal[0].length;
        if (numColumnas != numFilas + 1) throw new IllegalArgumentException("Dimensiones inválidas: se espera una matriz aumentada de tamaño n x (n+1).");
        int ultimaColumna = numColumnas - 1;

        // Crear una copia de la matriz
        double[][] matrizMNA = Arrays.stream(matrizMNAOriginal).map(fila -> Arrays.copyOf(fila, fila.length)).toArray(double[][]::new);

        // Gauss-Jordan con pivote absoluto
        for (int i = 0; i < numFilas; i++) {
            // Buscar fila de pivote con valor máximo absoluto en columna i
            int filaPivote = i;
            double valorMaximo = Math.abs(matrizMNA[i][i]);
            for (int fila = i + 1; fila < numFilas; fila++) {
                double valorAbs = Math.abs(matrizMNA[fila][i]);
                if (valorAbs > valorMaximo) {
                    valorMaximo = valorAbs;
                    filaPivote = fila;
                }
            }

            // Comprobar que tenga solución
            final double EPSILON = 1e-12;
            if (valorMaximo < EPSILON) throw new IllegalArgumentException("Sistema incompatible o sin solución única: pivote cero en columna " + i);

            // Intercambiar filas si es necesario
            if (filaPivote != i) intercambiarFilas(matrizMNA, i, filaPivote);

            // Normalizar fila i para que pivote valga 1
            double pivote = matrizMNA[i][i];
            for (int columna = i; columna <= ultimaColumna; columna++) {
                matrizMNA[i][columna] /= pivote;
            }

            // Eliminar las otras filas para hacer cero en columna i
            for (int fila = 0; fila < numFilas; fila++) {
                if (fila != i) {
                    double factor = matrizMNA[fila][i];
                    for (int columna = i; columna <= ultimaColumna; columna++) {
                        matrizMNA[fila][columna] -= factor * matrizMNA[i][columna];
                    }
                }
            }
        }

        // Armar solución
        double[] soluciones = new double[numFilas];
        for (int i = 0; i < numFilas; i++) {
            soluciones[i] = matrizMNA[i][ultimaColumna];
        }

        return soluciones;
    }

    public static void asignarVoltajesNodos(Circuito circuito, double[] soluciones) {
        Map<Nodo, Integer> indices = circuito.getMapaIndicesNodosCanonicos();

        // Asignar voltajes a los nodos según la solución, usando 0.0 para nodos de tierra
        int numNodosSinTierra = circuito.getNumNodosCanonicos() - 1;
        for(Nodo nodo : circuito.getNodos()) {
            int indice = indices.getOrDefault(nodo, -1);
            if (indice >= 0 && indice < numNodosSinTierra) {
                nodo.setValor(soluciones[indice]);
            } else {
                nodo.setValor(0.0);
            }
        }
    }

    private static void intercambiarFilas(double[][] matrizMNA, int fila1, int fila2) {
        // Reubicar la fila con el pivote óptimo en la posición actual mediante intercambio de filas
        double[] temp = matrizMNA[fila1];
        matrizMNA[fila1] = matrizMNA[fila2];
        matrizMNA[fila2] = temp;
    }
}
import java.util.Map;

public class SolucionadorCircuito {

    public static void resolver(Circuito circuito) {
        double[][] matriz = MNA.generarMatriz(circuito);
        double[] solucion = MNA.resolverMatriz(matriz);
        asignarVoltajesNodos(circuito, solucion);
        asignarVoltajesComponentes(circuito);
        asignarCorrientesComponentes(circuito, solucion);
    }

    private static void asignarVoltajesNodos(Circuito circuito, double[] soluciones) {
        Map<Nodo, Integer> indices = circuito.getMapaIndicesNodosCanonicos();

        // Asignar voltajes a los nodos según la solución, usando 0.0 para nodos de tierra
        int numNodosSinTierra = circuito.getNumNodosCanonicos() - 1;
        for(Nodo nodo : circuito.getNodos()) {
            int indice = indices.getOrDefault(nodo, -1);
            if (indice >= 0 && indice < numNodosSinTierra) {
                nodo.getValor().setNumero(soluciones[indice]);
            } else {
                nodo.getValor().setNumero(0.0);
            }
        }
    }

    private static void asignarVoltajesComponentes(Circuito circuito) {
        // Calcula y asigna el voltaje de cada resistencia como la diferencia entre sus nodos, conservando el prefijo de unidad.
        for (Componente componente : circuito.getComponentes()) {
            if (componente.getElemento() == 'R') {
                double valorNodoA = componente.getNodoA().getValor().getNumero();
                double valorNodoB = componente.getNodoB().getValor().getNumero();
                componente.getVoltaje().setNumero(valorNodoA - valorNodoB);
                componente.getVoltaje().setPrefijo(componente.getValor().getPrefijo());
            }
        }
    }

    private static void asignarCorrientesComponentes(Circuito circuito, double[] soluciones) {
        // Determina el índice inicial de las fuentes en la solución (después de los nodos sin tierra) y prepara el contador de fuentes.
        int numNodosSinTierra = circuito.getNumNodosCanonicos() - 1;
        int contaforFuentes = 0;

        // Asigna la corriente a resistencias usando la ley de Ohm y a fuentes extrayéndola del vector solución.
        for (Componente componente : circuito.getComponentes()) {
            if (componente.getElemento() == 'R') {
                double voltajeComponente = componente.getVoltaje().getNumero();
                double resistenciaComponente = componente.getValor().getNumero();
                componente.getCorriente().setNumero(voltajeComponente / resistenciaComponente);
            } else if (componente.getElemento() == 'F') {
                double corrienteFuente = soluciones[numNodosSinTierra + contaforFuentes] * -1.0;
                componente.getCorriente().setNumero(corrienteFuente);
                contaforFuentes++;
            }
        }
    }
}

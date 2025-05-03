public class Prueba {
    public static void unitaria(Circuito circuito) {
        double[][] matrizMNA = MNA.generarMatriz(circuito);
        double[] solucion = MNA.resolverMatriz(matrizMNA);

        System.out.println("\n===============================");
        System.out.println("       CIRCUITO CARGADO");
        System.out.println("===============================");
        for (Componente componente : circuito.getComponentes()) {
            String tipo = String.valueOf(componente.getElemento());
            int nodoA = circuito.getNodos().indexOf(componente.getNodoA());
            int nodoB = circuito.getNodos().indexOf(componente.getNodoB());

            if (tipo.equals("C")) {
                System.out.println(tipo + ' ' + nodoA + ' ' + nodoB + ' ' + (int) componente.getValor().getNumero());
            } else {
                System.out.println(tipo + ' ' + nodoA + ' ' + nodoB + ' ' + componente.getValor());
            }
        }

        System.out.println("\n===============================");
        System.out.println("         MATRIZ MNA");
        System.out.println("===============================");
        for (double[] fila : matrizMNA) {
            for (double valor : fila) {
                System.out.printf("%7.3f", valor);
            }
            System.out.println();
        }

        System.out.println("\n===============================");
        System.out.println("     SOLUCIÓN DEL SISTEMA");
        System.out.println("===============================");
        for (int i = 0; i < solucion.length; i++) {
            System.out.printf("x[%d] = %9.6f\n", i, solucion[i]);
        }

        System.out.println("\n===============================");
        System.out.println("      VOLTAJES EN NODOS");
        System.out.println("===============================");
        for (int i = 0; i < circuito.getNodos().size(); i++) {
            Nodo nodo = circuito.getNodos().get(i);
            System.out.println("Nodo " + i + ": " + nodo.getValor());
        }

        System.out.println("\n===============================");
        System.out.println("   VOLTAJES Y CORRIENTES EN RESISTENCIAS");
        System.out.println("===============================");
        for (Componente componente : circuito.getComponentes()) {
            if (componente.getElemento() == 'R') {
                String etiqueta = String.format("R (%s)", componente.getValor());
                System.out.printf("%-10s | Voltaje: %-10s | Corriente: %s\n", etiqueta, componente.getVoltaje(), componente.getCorriente());
            }
        }

        System.out.println("\n===============================");
        System.out.println("     CORRIENTES DE FUENTES");
        System.out.println("===============================");
        for (Componente componente : circuito.getComponentes()) {
            if (componente.getElemento() == 'F') System.out.printf("Fuente (%s): %s\n", componente.getValor(), componente.getCorriente());
        }

        System.out.println("\n===============================");
        System.out.println(" DISTANCIA DESDE CADA NODO HASTA TIERRA, SIGUIENDO CAÍDA DE VOLTAJE");
        System.out.println("===============================");

        for (Nodo nodo : circuito.getNodos()) {
            int distancia = LayoutCircuito.calcularDistanciaVoltaje(circuito.getNodos(), circuito.getMapaIndicesNodosCompletos(), circuito.getMapaAdyacencia(), nodo);
            int indice = circuito.getNodos().indexOf(nodo);
            System.out.printf("Nodo %d | Voltaje: %-11s | Distancia a tierra: %d\n", indice, nodo.getValor(), distancia);
        }
    }
}
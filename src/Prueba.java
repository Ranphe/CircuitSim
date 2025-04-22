public class Prueba {
    public static void unitaria(Circuito circuito, double[][] matriz, double[] solucion) {
        System.out.println("\n===============================");
        System.out.println("   CIRCUITO CARGADO");
        System.out.println("===============================");
        for (Componente componente : circuito.getComponentes()) {
            String tipo = String.valueOf(componente.getElemento());
            int nodoA = circuito.getNodos().indexOf(componente.getNodoA());
            int nodoB = circuito.getNodos().indexOf(componente.getNodoB());
            double valor = componente.getValor();
            String prefijo = componente.getPrefijo();
            char sufijo = componente.getSufijo();

            if (tipo.equals("C")) {
                System.out.println(tipo + ' ' + nodoA + ' ' + nodoB + ' ' + (int) valor);
            } else {
                String etiqueta = ValorElectrico.formatearValor(valor, prefijo, sufijo);
                System.out.println(tipo + ' ' + nodoA + ' ' + nodoB + ' ' + etiqueta);
            }
        }

        System.out.println("\n===============================");
        System.out.println("         MATRIZ MNA");
        System.out.println("===============================");
        for (double[] fila : matriz) {
            for (double valor : fila) {
                System.out.printf("%10.3f", valor);
            }
            System.out.println();
        }

        System.out.println("\n===============================");
        System.out.println("     SOLUCIÓN DEL SISTEMA");
        System.out.println("===============================");
        for (int i = 0; i < solucion.length; i++) {
            System.out.printf("x[%d] = %6.3f\n", i, solucion[i]);
        }

        System.out.println("\n===============================");
        System.out.println("      VOLTAJES EN NODOS");
        System.out.println("===============================");
        for (int i = 0; i < circuito.getNodos().size(); i++) {
            Nodo nodo = circuito.getNodos().get(i);
            String etiqueta = ValorElectrico.formatearValor(nodo.getValor(), nodo.getPrefijo(), nodo.getSufijo());
            System.out.println("Nodo " + i + ": " + etiqueta);
        }

        System.out.println("\n===============================");
        System.out.println(" DISTANCIA DESDE CADA NODO HASTA NODO FINAL");
        System.out.println("===============================");

        for (Nodo nodo : circuito.getNodos()) {
            int distancia = LayoutCircuito.calcularDistanciaVoltaje(circuito.getNodos(), circuito.getMapaIndicesNodosCompletos(), circuito.getMapaAdyacencia(), nodo);
            String etiqueta = ValorElectrico.formatearValor(nodo.getValor(), nodo.getPrefijo(), nodo.getSufijo());
            int indice = circuito.getNodos().indexOf(nodo);
            System.out.printf("Nodo %-2d | Voltaje: %-10s | Distancia a nodo final: %d\n", indice, etiqueta, distancia);
        }
    }
}
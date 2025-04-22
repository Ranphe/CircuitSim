import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CircuitoParser {

    public static Circuito desdeArchivo(File archivo) throws IOException {
        // Preparación de documento
        List<String> lineas = Files.readAllLines(archivo.toPath(), StandardCharsets.UTF_8);
        List<Componente> listaComponentes = new ArrayList<>();
        Map<String, Nodo> mapaNodos = new HashMap<>();
        List<Nodo> listaNodos = new LinkedList<>();
        String prefijoResistencias = "?";

        for (String lineaOriginal : lineas) {
            // Preparación de renglones por partes
            String linea = lineaOriginal.trim();
            if (linea.isEmpty()) continue;
            String[] partes = linea.split("\\s+");
            if (partes.length < 4) throw new IllegalArgumentException("Línea con formato incorrecto (menos de 4 campos): '" + lineaOriginal + "'");

            // Procesamiento parte 0
            char elemento = partes[0].charAt(0);
            Set<Character> tiposValidos = Set.of('R', 'C', 'F', 'I');
            if (!tiposValidos.contains(elemento)) throw new IllegalArgumentException("Tipo de componente desconocido '" + elemento + "' en línea: '" + lineaOriginal + "'");

            // Procesamiento parte 1
            String nombreNodo1 = partes[1];
            Nodo nodo1 = mapaNodos.get(nombreNodo1);
            if (nodo1 == null) {
                nodo1 = new Nodo();
                mapaNodos.put(nombreNodo1, nodo1);
                insertarOrdenado(nombreNodo1, nodo1, mapaNodos, listaNodos);
            }

            // Procesamiento parte 2
            String nombreNodo2 = partes[2];
            Nodo nodo2 = mapaNodos.get(nombreNodo2);
            if (nodo2 == null) {
                nodo2 = new Nodo();
                mapaNodos.put(nombreNodo2, nodo2);
                insertarOrdenado(nombreNodo2, nodo2, mapaNodos, listaNodos);
            }

            // Procesamiento parte 3
            String valorStr = partes[3];
            if (elemento == 'R') {
                String prefijoResistencia = ValorElectrico.extraerPrefijo(valorStr);
                if (prefijoResistencias.equals("?")) {
                    prefijoResistencias = prefijoResistencia;
                } else if (!prefijoResistencia.equals(prefijoResistencias)) {
                    throw new IllegalArgumentException("Todas las resistencias deben compartir la misma unidad: " + prefijoResistencias + "Ω vs " + prefijoResistencia + "Ω");
                }
            } else if (prefijoResistencias.equals("?")) {
                throw new IllegalArgumentException("Debe aparecer al menos una resistencia antes de una fuente para definir el prefijo de referencia.");
            }

            // Armado de objeto Componente y almacenamiento en listas
            Componente componente;
            if (elemento != 'C') {
                double valor = ValorElectrico.procesarValor(elemento, valorStr, prefijoResistencias);
                char sufijo = ValorElectrico.extraerSufijo(valorStr);
                componente = new Componente(elemento, nodo1, nodo2, valor, prefijoResistencias, sufijo);
            } else {
                componente = new Componente(elemento, nodo1, nodo2);
            }
            listaComponentes.add(componente);
        }

        // Armar mapa de índices de nodos totales
        Map<Nodo, Integer> mapaIndicesNodosTotales = generarMapaIndicesNodosTotales(listaNodos);

        // Armar mapa de indices de nodos canónicos
        Map<Nodo, Integer> mapaIndicesNodosCanonicos = generarMapaIndicesNodosCanonicos(mapaNodos);

        // Armar lista de adyacencia
        Map<Nodo, List<Nodo>> mapaAdyacencia = generarmapaAdyacencia(listaComponentes, listaNodos);

        // Asignar prefijo y sufijo a todos los nodos
        for (Nodo nodo : listaNodos) {
            nodo.setPrefijo(prefijoResistencias);
            nodo.setSufijo('V');
        }

        return new Circuito(listaComponentes, listaNodos, mapaIndicesNodosTotales, mapaIndicesNodosCanonicos, mapaAdyacencia);
    }

    private static void insertarOrdenado(String nombre, Nodo nodo, Map<String, Nodo> mapaNodos, List<Nodo> listaNodos) {
        // Iterador para insertar el nodo en la posición correcta
        ListIterator<Nodo> it = listaNodos.listIterator();

        // Recorrido secuencial para encontrar la posición
        while (it.hasNext()) {
            Nodo nodoActual = it.next();
            String nombreActual = obtenerNombreNodo(nodoActual, mapaNodos);
            if (compararNombresNodos(nombre, nombreActual) < 0) {
                it.previous();
                it.add(nodo);
                return;
            }
        }

        listaNodos.add(nodo);
    }

    private static String obtenerNombreNodo(Nodo nodo, Map<String, Nodo> mapaNodos) {
        // Recorrido para identificar la clave (nombre) correspondiente al nodo
        for (Map.Entry<String, Nodo> entrada : mapaNodos.entrySet()) {
            if (entrada.getValue() == nodo) {
                return entrada.getKey();
            }
        }

        return null;
    }

    private static int compararNombresNodos(String nombreNodo1, String nombreNodo2) {
        // Extraer y convertir a entero la parte numérica de ambos nombres de nodo para compararlos numéricamente
        int numeroNodo1 = Integer.parseInt(ValorElectrico.extraerValor(nombreNodo1));
        int numeroNodo2 = Integer.parseInt(ValorElectrico.extraerValor(nombreNodo2));

        // Priorizar nodos distintos de tierra, colocando los nodos con valor 0 al final del ordenamiento
        if (numeroNodo1 == 0 && numeroNodo2 != 0) return 1;
        if (numeroNodo2 == 0 && numeroNodo1 != 0) return -1;

        // Comparar los valores numéricos y retornar si son diferentes
        int cmp = Integer.compare(numeroNodo1, numeroNodo2);
        if (cmp != 0) return cmp;

        // Extraer los sufijos alfabéticos de los nombres de nodo para compararlos si los números son iguales
        char sufijoNodo1 = ValorElectrico.extraerSufijo(nombreNodo1);
        char sufijoNodo2 = ValorElectrico.extraerSufijo(nombreNodo2);

        return Character.compare(sufijoNodo1, sufijoNodo2);
    }

    private static Map<Nodo, Integer> generarMapaIndicesNodosTotales(List<Nodo> listaNodos) {
        Map<Nodo, Integer> mapa = new HashMap<>();

        // Asignar a cada nodo su índice correspondiente en la lista completa de nodos
        for (int i = 0; i < listaNodos.size(); i++) {
            mapa.put(listaNodos.get(i), i);
        }

        return mapa;
    }

    private static Map<Nodo, Integer> generarMapaIndicesNodosCanonicos(Map<String, Nodo> mapaNodos) {
        Map<Nodo, Integer> mapaIndicesNodosCanonicos = new HashMap<>();

        // Asignar índices, nodos de tierra temporalmente en -1
        for (Map.Entry<String, Nodo> entrada : mapaNodos.entrySet()) {
            Nodo nodo = entrada.getValue();
            String nombreNodo = entrada.getKey();
            int indiceNodo = Integer.parseInt(ValorElectrico.extraerValor(nombreNodo)) - 1;
            mapaIndicesNodosCanonicos.put(nodo, indiceNodo);
        }

        // Calcular número de nodos canónicos excluyendo nodos de tierra
        int maxIndice = mapaIndicesNodosCanonicos.values().stream().filter(indice -> indice != -1).max(Integer::compare).orElse(-1);
        int indiceTierra = maxIndice + 1;

        // Reemplazar índices de nodos de tierra por índice final
        for (Map.Entry<Nodo, Integer> entrada : mapaIndicesNodosCanonicos.entrySet()) {
            if (entrada.getValue() == -1) entrada.setValue(indiceTierra);
        }

        return mapaIndicesNodosCanonicos;
    }

    private static Map<Nodo, List<Nodo>> generarmapaAdyacencia(List<Componente> componentes, List<Nodo> listaNodos) {
        Map<Nodo, List<Nodo>> mapaAdyacencia = new HashMap<>();

        // Iterar sobre los componentes agregando conexiones bidireccionales entre nodos unidos por componentes
        for (Componente componente : componentes) {
            Nodo nodoA = componente.getNodoA();
            Nodo nodoB = componente.getNodoB();
            mapaAdyacencia.computeIfAbsent(nodoA, clave -> new LinkedList<>()).add(nodoB);
            mapaAdyacencia.computeIfAbsent(nodoB, clave -> new LinkedList<>()).add(nodoA);
        }

        // Ordenar las listas de vecinos por el orden global de nodos
        for (List<Nodo> vecinos : mapaAdyacencia.values()) {
            vecinos.sort(Comparator.comparingInt(listaNodos::indexOf));
        }

        return mapaAdyacencia;
    }
}
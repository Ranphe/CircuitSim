import java.awt.Point;
import java.util.*;
import java.util.stream.Collectors;

public class LayoutCircuito {

    public static Map<Nodo, Point> calcularPosicionesIniciales(List<Nodo> listaNodos, Map<Nodo, Integer> mapaIndicesNodosCompletos, Map<Nodo, List<Nodo>> mapaAdyacencia) {
        // Inicializa estructuras de control y define el nodo origen para comenzar el recorrido BFS.
        Map<Nodo, Point> mapaCoordenadas = new HashMap<>();
        Set<Point> posicionesOcupadas = new HashSet<>();
        Set<Nodo> nodosVisitados = new HashSet<>();
        Queue<Nodo> colaNodos = new LinkedList<>();
        Nodo nodoOrigen = listaNodos.get(0);

        // Inicializar BFS con nodo origen en (0,0)
        Point origen = new Point(0, 0);
        mapaCoordenadas.put(nodoOrigen, origen);
        posicionesOcupadas.add(origen);
        nodosVisitados.add(nodoOrigen);
        colaNodos.add(nodoOrigen);

        // Procesa cada nodo en orden BFS, ubicando a sus hijos según la distancia al nodo tierra y asignándoles posiciones disponibles.
        while (!colaNodos.isEmpty()) {
            // Extrae el siguiente nodo a procesar y obtiene su posición actual en el plano.
            Nodo nodoActual = colaNodos.poll();
            Point puntoActual = mapaCoordenadas.get(nodoActual);

            // Obtener hijos no visitados
            List<Nodo> nodosHijos = mapaAdyacencia.getOrDefault(nodoActual, List.of()).stream().filter(n -> !nodosVisitados.contains(n)).collect(Collectors.toList());

            // Ordenar por distancia creciente al nodo tierra
            nodosHijos.sort(Comparator.comparingInt(n -> {int d = calcularDistanciaVoltaje(listaNodos, mapaIndicesNodosCompletos, mapaAdyacencia, n);
                return d < 0 ? Integer.MAX_VALUE : d;
            }));

            // Asigna posición al único hijo o distribuye hasta tres hijos con distintas direcciones iniciales según su prioridad.
            if (nodosHijos.size() == 1) {
                // Único hijo: inicia búsqueda en dirección derecha (índice 0)
                asignarPosicionConDireccion(nodosHijos.get(0), puntoActual, 0, mapaCoordenadas, posicionesOcupadas, nodosVisitados, colaNodos);
            } else {
                // Varios hijos (hasta 3): asignar direcciones iniciales diferenciadas
                for (int i = 0; i < nodosHijos.size() && i < 3; i++) {
                    int direccionInicial = switch (i) {
                        case 0 -> 3; // primero: abajo
                        case 1 -> 0; // segundo: derecha
                        case 2 -> 1; // tercero: arriba
                        default -> 2; // default: izquierda
                    };
                    asignarPosicionConDireccion(nodosHijos.get(i), puntoActual, direccionInicial, mapaCoordenadas, posicionesOcupadas, nodosVisitados, colaNodos);
                }
            }
        }

        return mapaCoordenadas;
    }

    private static void asignarPosicionConDireccion(Nodo nodo, Point puntoPadre, int direccionInicial, Map<Nodo, Point> mapaCoordenadas, Set<Point> posicionesOcupadas, Set<Nodo> nodosVisitados, Queue<Nodo> colaNodos) {
        // Define la distancia fija entre nodos en el plano cartesiano.
        final int DISTANCIA_PX = 300;

        // Direcciones unitarias en sentido antihorario: derecha, arriba, izquierda, abajo
        final List<Point> DIRECCIONES = List.of(
                new Point(DISTANCIA_PX, 0), // 0: derecha
                new Point(0, -DISTANCIA_PX), // 1: arriba
                new Point(-DISTANCIA_PX, 0), // 2: izquierda
                new Point(0, DISTANCIA_PX) // 3: abajo
        );

        // Recorre las direcciones en orden antihorario desde la inicial y asigna la primera posición libre disponible al nodo.
        for (int i = 0; i < DIRECCIONES.size(); i++) {
            Point delta = DIRECCIONES.get((direccionInicial + i) % DIRECCIONES.size());
            Point candidata = new Point(puntoPadre.x + delta.x, puntoPadre.y + delta.y);
            if (posicionesOcupadas.add(candidata)) {
                mapaCoordenadas.put(nodo, candidata);
                nodosVisitados.add(nodo);
                colaNodos.add(nodo);
                break;
            }
        }
    }

    public static int calcularDistanciaVoltaje(List<Nodo> listaNodos, Map<Nodo, Integer> mapaIndicesNodosCompletos, Map<Nodo, List<Nodo>> mapaAdyacencia, Nodo origen) {
        // Verifica que la lista de nodos no sea nula ni esté vacía antes de iniciar el cálculo.
        if (listaNodos == null || listaNodos.isEmpty()) throw new IllegalArgumentException("La lista de nodos no puede ser nula o vacía.");

        // Obtiene el nodo tierra, que es el último en la lista de nodos.
        Nodo nodoDestino = listaNodos.get(listaNodos.size() - 1);

        // Inicializa estructuras para el recorrido BFS: cola de procesamiento, mapa de distancias y conjunto de nodos visitados.
        Map<Nodo, Integer> distancias = new HashMap<>();
        Queue<Nodo> colaNodos = new LinkedList<>();
        Set<Nodo> nodosVisitados = new HashSet<>();

        // Agrega el nodo origen a la cola, marca su distancia como 0 y lo registra como visitado.
        colaNodos.add(origen);
        distancias.put(origen, 0);
        nodosVisitados.add(origen);

        // Realiza recorrido BFS considerando solo vecinos válidos según voltaje y dirección, y retorna la distancia al nodo tierra si se alcanza.
        while (!colaNodos.isEmpty()) {
            // Extrae el nodo actual de la cola y obtiene su distancia, voltaje e índice en el circuito.
            Nodo nodoActual = colaNodos.poll();
            int distanciaActual = distancias.get(nodoActual);
            double voltajeActual = nodoActual.getValor().getNumero();
            int indiceNodoActual = mapaIndicesNodosCompletos.get(nodoActual);

            // Si se alcanza el nodo tierra, retorna la distancia acumulada desde el origen.
            if (nodoActual == nodoDestino) return distanciaActual;

            // Recorre los vecinos del nodo actual y expande solo aquellos que cumplen con la dirección del voltaje y no han sido visitados.
            for (Nodo nodoVecino : mapaAdyacencia.getOrDefault(nodoActual, List.of())) {
                // Omite la conexión directa entre origen y tierra, y descarta vecinos ya visitados.
                boolean esOrigen = nodoActual == listaNodos.get(0);
                boolean esDestino = nodoVecino == nodoDestino;
                if (esOrigen && esDestino) continue;
                if (nodosVisitados.contains(nodoVecino)) continue;

                // Obtiene el voltaje y el índice del nodo vecino para evaluar si cumple con las condiciones de avance.
                double voltajeNodoVecino = nodoVecino.getValor().getNumero();
                int indiceNodoVecino = mapaIndicesNodosCompletos.getOrDefault(nodoVecino, -1);

                // Si el voltaje no aumenta y la dirección es válida, actualiza la distancia y encola al vecino para continuar el recorrido.
                if (voltajeNodoVecino <= voltajeActual && indiceNodoVecino >= indiceNodoActual) {
                    distancias.put(nodoVecino, distanciaActual + 1);
                    colaNodos.add(nodoVecino);
                    nodosVisitados.add(nodoVecino);
                }
            }
        }
        return -1; // Retorna -1 si no se encontró un camino válido hasta el nodo tierra.
    }
}

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Circuito {
    private final List<Componente> componentes;
    private final List<Nodo> nodos;
    private final Map<Nodo, Integer> mapaIndicesNodosCompletos;
    private final Map<Nodo, Integer> mapaIndicesNodosCanonicos;
    private final Map<Nodo, List<Nodo>> mapaAdyacencia;
    private final int numComponentes;
    private final int numNodosTotales;
    private final int numNodosCanonicos;
    private final int numFuentesVoltaje;

    public Circuito(List<Componente> componentes, List<Nodo> nodos, Map<Nodo, Integer> mapaIndicesNodosCompletos, Map<Nodo, Integer> mapaIndicesNodosCanonicos, Map<Nodo, List<Nodo>> mapaAdyacencia) {
        this.componentes = componentes;
        this.nodos = nodos;
        this.mapaIndicesNodosCompletos = mapaIndicesNodosCompletos;
        this.mapaIndicesNodosCanonicos = mapaIndicesNodosCanonicos;
        this.mapaAdyacencia = mapaAdyacencia;
        this.numComponentes = calcularNumComponentes();
        this.numNodosTotales = calcularNumNodosTotales();
        this.numNodosCanonicos = calcularNumNodosCanonicos();
        this.numFuentesVoltaje = calcularNumFuentes();
    }

    public List<Componente> getComponentes() {
        return componentes;
    }

    public List<Nodo> getNodos() {
        return nodos;
    }

    public Map<Nodo, Integer> getMapaIndicesNodosCompletos() {
        return mapaIndicesNodosCompletos;
    }

    public Map<Nodo, Integer> getMapaIndicesNodosCanonicos() {
        return mapaIndicesNodosCanonicos;
    }

    public Map<Nodo, List<Nodo>> getMapaAdyacencia() {
        return mapaAdyacencia;
    }

    public int getNumComponentes() {
        return numComponentes;
    }

    public int getNumNodosTotales() {
        return numNodosTotales;
    }

    public int getNumNodosCanonicos() {
        return numNodosCanonicos;
    }

    public int getNumFuentesVoltaje() {
        return numFuentesVoltaje;
    }

    private int calcularNumComponentes() {
        return (int) componentes.stream().filter(componente -> componente.getElemento() != 'C').count();
    }

    private int calcularNumNodosTotales() {
        return nodos.size();
    }

    private int calcularNumNodosCanonicos() {
        return new HashSet<>(mapaIndicesNodosCanonicos.values()).size();
    }

    private int calcularNumFuentes() {
        return (int) componentes.stream().filter(componente -> componente.getElemento() == 'F').count();
    }
}
public class Componente {
    private char elemento;
    private Nodo nodoA;
    private Nodo nodoB;
    private double valor;
    private String prefijo;
    private char sufijo;

    public Componente(char elemento, Nodo nodoA, Nodo nodoB, double valor, String prefijo, char sufijo) {
        this.elemento = elemento;
        this.nodoA = nodoA;
        this.nodoB = nodoB;
        this.valor = valor;
        this.prefijo = prefijo;
        this.sufijo = sufijo;
    }

    public Componente(char elemento, Nodo nodoA, Nodo nodoB) {
        this(elemento, nodoA, nodoB, 0.0, "", '\0');
    }

    public char getElemento() {
        return elemento;
    }

    public Nodo getNodoA() {
        return nodoA;
    }

    public Nodo getNodoB() {
        return nodoB;
    }

    public double getValor() {
        return valor;
    }

    public String getPrefijo() {
        return prefijo;
    }

    public char getSufijo() {
        return sufijo;
    }
}
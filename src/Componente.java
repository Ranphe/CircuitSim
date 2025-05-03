public class Componente {
    private char elemento;
    private Nodo nodoA;
    private Nodo nodoB;
    private ValorElectrico valor;
    private ValorElectrico voltaje;
    private ValorElectrico corriente;

    public Componente(char elemento, Nodo nodoA, Nodo nodoB, ValorElectrico valor) {
        this.elemento = elemento;
        this.nodoA = nodoA;
        this.nodoB = nodoB;
        this.valor = valor;
        this.voltaje = new ValorElectrico();
        this.voltaje.setSufijo('V');
        this.corriente = new ValorElectrico();
        this.corriente.setSufijo('A');
    }

    public Componente(char elemento, Nodo nodoA, Nodo nodoB) {
        this(elemento, nodoA, nodoB, new ValorElectrico());
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

    public ValorElectrico getValor() {
        return valor;
    }

    public ValorElectrico getVoltaje() {
        return voltaje;
    }

    public ValorElectrico getCorriente() {
        return corriente;
    }
}
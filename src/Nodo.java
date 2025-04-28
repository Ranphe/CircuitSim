public class Nodo {
    private ValorElectrico valor;

    public Nodo(ValorElectrico valor) {
        this.valor = valor;
    }

    public Nodo() {
        this(new ValorElectrico());
    }

    public ValorElectrico getValor() {
        return valor;
    }
}
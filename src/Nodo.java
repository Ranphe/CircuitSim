public class Nodo {
    private double valor;
    private String prefijo;
    private char sufijo;

    public Nodo(double valor, String prefijo, char sufijo) {
        this.valor = valor;
        this.prefijo = prefijo;
        this.sufijo = sufijo;
    }

    public Nodo() {
        this(0.0, "", '\0');
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

    public void setValor(double valor) {
        this.valor = valor;
    }

    public void setPrefijo(String prefijo) {
        this.prefijo = prefijo;
    }

    public void setSufijo(char sufijo) {
        this.sufijo = sufijo;
    }
}
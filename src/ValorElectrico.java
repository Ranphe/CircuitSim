public class ValorElectrico {
    private double numero;
    private String prefijo;
    private char sufijo;

    public ValorElectrico(double numero, String prefijo, char sufijo) {
        this.numero = numero;
        this.prefijo = prefijo;
        this.sufijo = sufijo;
    }

    public ValorElectrico() {
        this(0.0, "", '\0');
    }

    public double getNumero() {
        return numero;
    }

    public String getPrefijo() {
        return prefijo;
    }

    public char getSufijo() {
        return sufijo;
    }

    public void setNumero(double numero) {
        this.numero = numero;
    }

    public void setPrefijo(String prefijo) {
        this.prefijo = prefijo;
    }

    public void setSufijo(char sufijo) {
        this.sufijo = sufijo;
    }

    @Override
    public String toString() {
        return FormatoElectrico.formatearValor(numero, prefijo, sufijo);
    }
}
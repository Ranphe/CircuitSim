import java.text.DecimalFormat;
import java.util.*;

public class FormatoElectrico {

    private static final Map<Character, Character> SUFIJOS = Map.of(
            'R', 'Ω',
            'F', 'V',
            'I', 'A'
    );

    private static final Map<String, Double> FACTOR = Map.of(
            "G", 1e9,
            "M", 1e6,
            "K", 1e3,
            "", 1.0,
            "m", 1e-3,
            "µ", 1e-6,
            "n", 1e-9,
            "p", 1e-12
    );

    public static double procesarValor(char elemento, String valorStr, String prefijoResistencias) {
        // Validación de sufijo
        char sufijoEsperado = SUFIJOS.getOrDefault(elemento, '?');
        if (!valorStr.endsWith(String.valueOf(sufijoEsperado))) throw new IllegalArgumentException("Unidad no compatible en: '" + valorStr + "', se esperaba '" + sufijoEsperado + "'.");

        // Validación de prefijos
        String prefijo = extraerPrefijo(valorStr);
        boolean prefijoInvalido = !FACTOR.containsKey(prefijo);
        boolean refInvalida = !FACTOR.containsKey(prefijoResistencias);
        if (prefijoInvalido || refInvalida) throw new IllegalArgumentException("Prefijo no reconocido en: " + valorStr + '.');

        // Validación de número
        double valor = Double.parseDouble(extraerValor(valorStr));
        if (valor == 0.0) throw new IllegalArgumentException("El valor no puede ser 0 para el componente '" + elemento + "' en: " + valorStr);

        // Retorno de valor unificado con el prefijo de resistencias
        if (prefijo != prefijoResistencias && (elemento == 'F' || elemento == 'I')) {
            double factorResistencias = FACTOR.get(prefijoResistencias);
            double factorFuente = FACTOR.get(prefijo);
            return valor * factorFuente / factorResistencias;
        } else {
            return valor;
        }
    }

    public static String formatearValor(double valor, String prefijo, char sufijo) {
        // Validación de sufijo
        if (!SUFIJOS.containsValue(sufijo)) throw new IllegalArgumentException("Sufijo no reconocido: " + sufijo + '.');

        // Validación de prefijo
        if (!FACTOR.containsKey(prefijo)) throw new IllegalArgumentException("Prefijo no reconocido: " + prefijo + '.');

        // Valores 0.0
        if (valor == 0.0) return "0 " + sufijo;

        // Obtener valor real y absoluto
        double valorReal = valor * FACTOR.get(prefijo);
        double valorAbs = Math.abs(valorReal);

        // Ordenar los prefijos por factor descendente
        List<Map.Entry<String, Double>> entradasOrdenadas = FACTOR.entrySet().stream().sorted(Map.Entry.<String,Double>comparingByValue().reversed()).toList();

        // Elegir prefijo visual que deje valor entre 1 y 1000
        prefijo = "";
        double factor = 1.0;
        for (Map.Entry<String, Double> entrada : entradasOrdenadas) {
            double factorMapa = entrada.getValue();
            double valorEscalado = valorAbs / factorMapa;
            if (valorEscalado >= 1.0 && valorEscalado < 1000.0) {
                prefijo = entrada.getKey();
                factor = factorMapa;
                break;
            }
        }

        // Formatear valor real con prefijo y sufijo elegidos
        DecimalFormat formatoDecimal = new DecimalFormat("0.###");
        return formatoDecimal.format(valorReal / factor) + " " + prefijo + sufijo;
    }

    public static String extraerValor(String valorStr) {
        // Recorrer la cadena hasta identificar el final de la parte numérica (dígitos y punto decimal)
        int i = 0;
        while (i < valorStr.length() && (Character.isDigit(valorStr.charAt(i)) || valorStr.charAt(i) == '.')) {
            i++;
        }

        // Validar que exista al menos un dígito numérico al inicio de la cadena
        if (i == 0) throw new IllegalArgumentException("No se encontró ningún valor numérico válido en: " + valorStr);

        return valorStr.substring(0, i);
    }

    public static char extraerSufijo (String valorStr) {
        // Obtener el último carácter de la cadena como posible sufijo
        char sufijo = valorStr.charAt(valorStr.length() - 1);

        return (!Character.isDigit(sufijo)) ? sufijo : '\0';
    }

    public static String extraerPrefijo(String valorStr) {
        // Si la cadena es demasiado corta, no puede contener un prefijo válido
        if (valorStr.length() < 2) return "";

        // Separar la parte anterior al sufijo y avanzar hasta el primer carácter no numérico para detectar el prefijo
        String prefijo = valorStr.substring(0, valorStr.length() - 1);
        int i = 0;
        while (i < prefijo.length() && (Character.isDigit(prefijo.charAt(i)) || prefijo.charAt(i) == '.')) {
            i++;
        }

        return prefijo.substring(i);
    }
}
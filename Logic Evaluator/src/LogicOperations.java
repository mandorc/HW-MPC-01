/*
 * The MIT License
 *
 * Copyright 2025 armando.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 *
 * @author armando
 */
import java.util.*;

public final class LogicOperations {

    private LogicOperations() {
    }

    public static Map<String, Boolean> buscarAsignacionSatisfactoria(String expresion) {

        List<String> variables = obtenerVariables(expresion);
        int n = variables.size();

        if (n == 0) {
            return evaluar(expresion, Collections.emptyMap()) ? new LinkedHashMap<>() : null;
        }

        int total = 1 << n;

        for (int mascara = 0; mascara < total; mascara++) {
            Map<String, Boolean> asignacion = new LinkedHashMap<>();

            for (int i = 0; i < n; i++) {
                asignacion.put(variables.get(i), ((mascara >> i) & 1) == 1);
            }

            if (evaluar(expresion, asignacion)) {
                return asignacion; // primera que haga verdadera
            }

        }
        return null;
    }

    public static boolean evaluar(String expresion, Map<String, Boolean> valores) {
        Analizador analizador = new Analizador(expresion, valores);
        boolean resultado = analizador.analizarO();
        analizador.saltarEspacios();

        if (!analizador.fin()) {
            throw new IllegalArgumentException("Error");
        }

        return resultado;
    }

    public static List<String> obtenerVariables(String expresion) {
        LinkedHashSet<String> conjunto = new LinkedHashSet<>();

        if (expresion != null) {
            String cadena = expresion;

            for (int indice = 0; indice < cadena.length(); indice++) {
                char caracter = cadena.charAt(indice);

                if (Character.isLetter(caracter)) {
                    int indice2 = indice + 1;

                    while (indice2 < cadena.length() && Character.isLetter(cadena.charAt(indice2))) {
                        indice2++;
                    }

                    conjunto.add(cadena.substring(indice, indice2));
                    indice = indice2 - 1;
                }
            }
        }

        List<String> lista = new ArrayList<>(conjunto);
        lista.sort(String.CASE_INSENSITIVE_ORDER);
        return lista;
    }

    public static String formatearAsignacion(Map<String, Boolean> asignacion) {

        if (asignacion == null) {
            return "(sin solución)";
        }

        List<String> claves = new ArrayList<>(asignacion.keySet());
        claves.sort(String.CASE_INSENSITIVE_ORDER);
        StringBuilder construccion = new StringBuilder();

        for (String k : claves) {
            construccion.append(k)
                    .append("=")
                    .append(Boolean.TRUE.equals(asignacion.get(k)) ? "V" : "F")
                    .append("; ");
        }

        return construccion.toString().trim();

    }

    private static final class Analizador {

        private final String cadena;
        private final Map<String, Boolean> valores;
        private int indice = 0;

        Analizador(String cadena, Map<String, Boolean> valores) {
            this.cadena = cadena == null ? "" : cadena;
            this.valores = valores == null ? Collections.emptyMap() : valores;
        }

        boolean fin() {
            return indice >= cadena.length();
        }

        void saltarEspacios() {
            while (indice < cadena.length() && Character.isWhitespace(cadena.charAt(indice))) {
                indice++;
            }
        }

        char caracter() {
            return cadena.charAt(indice);
        }

        String verResto() {
            return cadena.substring(indice);
        }

        boolean analizarO() {
            boolean v = analizarY();
            saltarEspacios();
            while (!fin() && (caracter() == '∨' || caracter() == '|')) {
                indice++; // ∨ o |
                boolean r = analizarY();
                v = v | r;
                saltarEspacios();
            }
            return v;
        }

        boolean analizarY() {
            boolean v = analizarNo();
            saltarEspacios();
            while (!fin() && (caracter() == '∧' || caracter() == '&')) {
                indice++; // ∧ o &
                boolean r = analizarNo();
                v = v & r;
                saltarEspacios();
            }
            return v;
        }

        boolean analizarNo() {
            saltarEspacios();
            if (!fin() && (caracter() == '¬' || caracter() == '!')) {
                indice++;
                boolean r = analizarNo();
                return !r;
            }
            return analizarAtomo();
        }

        // ATOMO := ( expresion ) | VAR
        boolean analizarAtomo() {
            saltarEspacios();
            if (fin()) {
                throw new IllegalArgumentException("Falta operando al final");
            }
            char c = caracter();
            if (c == '(') {
                indice++; // (
                boolean v = analizarO();
                saltarEspacios();
                if (fin() || caracter() != ')') {
                    throw new IllegalArgumentException("falta ')'");
                }
                indice++; // )
                return v;
            }
            if (Character.isLetter(c)) {
                int j = indice + 1;
                while (j < cadena.length() && Character.isLetter(cadena.charAt(j))) {
                    j++;
                }
                String variable = cadena.substring(indice, j);
                indice = j;
                return leer(variable);
            }
            throw new IllegalArgumentException("no valido");
        }

        boolean leer(String variable) {

            if (valores.containsKey(variable)) {
                return Boolean.TRUE.equals(valores.get(variable));
            }
            if (valores.containsKey(variable.toLowerCase())) {
                return Boolean.TRUE.equals(valores.get(variable.toLowerCase()));
            }
            if (valores.containsKey(variable.toUpperCase())) {
                return Boolean.TRUE.equals(valores.get(variable.toUpperCase()));
            }
            return false;
        }
    }
}

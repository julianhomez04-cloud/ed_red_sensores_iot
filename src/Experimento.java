/* ============================================================
   Experimento de la Semana 2: deja en consola la evidencia de
   las Fases 1, 2.1, 3 y 4 con el archivo real.

   Uso (desde la raiz del repositorio):
     java -cp out Experimento data/lecturas_ampliadas.csv
     java -cp out Experimento data/lecturas_ampliadas.csv --escala

   Aplica las MISMAS reglas de IngestaSensores (5 campos, numeros
   validos y LecturaSensor.esValida()). Se lee aqui, y no con
   IngestaSensores, para poder cargar el mismo archivo con
   distintas estrategias de crecimiento sin tocar ese archivo.
   ============================================================ */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

public class Experimento {

    private static final int CAPACIDAD_INICIAL = 10;
    private static final int CAMPOS_ESPERADOS = 5;

    /** Resumen de una carga. */
    private static class Resumen {
        int filas;
        int almacenadas;
        int descartadasFormato;
        int descartadasRango;
        int rechazadasPorCapacidad;
    }

    public static void main(String[] args) throws IOException {
        String ruta = args.length > 0 ? args[0] : "data/lecturas_ampliadas.csv";
        boolean conEscala = args.length > 1 && args[1].equals("--escala");

        fase1Diagnostico(ruta);
        RepositorioLecturas repositorio = fase2Crecimiento(ruta);
        fase3Busqueda(repositorio);
        fase4Matriz(ruta);
        if (conEscala) {
            experimentoEscala();
        }
    }

    // ---------------------------------------------------------------
    // FASE 1: reproducir el problema con la capacidad fija original
    // ---------------------------------------------------------------
    private static void fase1Diagnostico(String ruta) throws IOException {
        titulo("FASE 1 - Diagnostico (arreglo de capacidad fija = " + CAPACIDAD_INICIAL + ")");

        RepositorioLecturas repositorio = new RepositorioLecturas(
                CAPACIDAD_INICIAL, RepositorioLecturas.EstrategiaCrecimiento.FIJA);
        System.out.println("Filas descartadas por la validacion:");
        Resumen r = cargar(ruta, repositorio, null, true);

        System.out.println();
        System.out.println("=== INGESTA ===");
        System.out.printf("%-28s%5d%n", "Filas leidas:", r.filas);
        System.out.printf("%-28s%5d%n", "Lecturas almacenadas:", r.almacenadas);
        System.out.printf("%-28s%5d%n", "Descartadas por formato:", r.descartadasFormato);
        System.out.printf("%-28s%5d%n", "Descartadas por rango:", r.descartadasRango);
        System.out.printf("%-28s%5d%n", "Rechazadas por capacidad:", r.rechazadasPorCapacidad);
        System.out.println();
        System.out.println("Cuenta esperada: " + r.filas + " - " + r.descartadasFormato + " - "
                + r.descartadasRango + " = " + (r.filas - r.descartadasFormato - r.descartadasRango)
                + " lecturas validas");
        System.out.println("Almacenadas: " + repositorio.tamano()
                + " -> lecturas perdidas por el techo del arreglo: " + r.rechazadasPorCapacidad);
    }

    // ---------------------------------------------------------------
    // FASE 2 / 2.1: romper el techo y medir
    // ---------------------------------------------------------------
    private static RepositorioLecturas fase2Crecimiento(String ruta) throws IOException {
        titulo("FASE 2 / 2.1 - Redimensionar y medir (capacidad inicial = " + CAPACIDAD_INICIAL + ")");

        RepositorioLecturas.EstrategiaCrecimiento[] estrategias = {
                RepositorioLecturas.EstrategiaCrecimiento.UNO_A_UNO,
                RepositorioLecturas.EstrategiaCrecimiento.DUPLICAR
        };

        System.out.printf(Locale.US, "%-12s %12s %10s %20s %8s%n",
                "Estrategia", "Almacenadas", "Capacidad", "Redimensionamientos", "Copias");

        RepositorioLecturas elegido = null;
        for (RepositorioLecturas.EstrategiaCrecimiento estrategia : estrategias) {
            RepositorioLecturas repositorio = new RepositorioLecturas(CAPACIDAD_INICIAL, estrategia);
            cargar(ruta, repositorio, null, false);

            System.out.printf(Locale.US, "%-12s %12d %10d %20d %8d%n",
                    estrategia, repositorio.tamano(), repositorio.capacidad(),
                    repositorio.getRedimensionamientos(), repositorio.getCopiasRealizadas());

            if (estrategia == RepositorioLecturas.EstrategiaCrecimiento.DUPLICAR) {
                elegido = repositorio;
            }
        }
        System.out.println();
        System.out.println("Lecturas almacenadas (duplicando): " + elegido.tamano());
        System.out.println("PM2.5 promedio del repositorio: " + formato(elegido.promedioPm25()));
        return elegido;
    }

    // ---------------------------------------------------------------
    // FASE 3: buscar
    // ---------------------------------------------------------------
    private static void fase3Busqueda(RepositorioLecturas repositorio) {
        titulo("FASE 3 - Contrato: buscar");

        String[] consultas = {"EST-004", "EST-999"};
        for (String id : consultas) {
            LecturaSensor encontrada = repositorio.buscarPorEstacion(id);
            System.out.println("buscarPorEstacion(\"" + id + "\") -> "
                    + (encontrada == null ? "null" : encontrada)
                    + "   [comparaciones: " + repositorio.getComparacionesUltimaBusqueda() + "]");
        }
    }

    // ---------------------------------------------------------------
    // FASE 4: matriz estacion x hora y el cero mentiroso
    // ---------------------------------------------------------------
    private static void fase4Matriz(String ruta) throws IOException {
        titulo("FASE 4 - Matriz estacion x hora");

        RepositorioLecturas repositorio = new RepositorioLecturas();
        AnalizadorMatriz matriz = new AnalizadorMatriz();
        cargar(ruta, repositorio, matriz, false);

        System.out.println("Estaciones: " + matriz.numeroEstaciones()
                + " | celdas sin dato: " + matriz.celdasSinDato()
                + " de " + (matriz.numeroEstaciones() * AnalizadorMatriz.NUM_HORAS)
                + " | lecturas sobrescritas: " + matriz.getLecturasSobrescritas()
                + " | lecturas fuera de la matriz: " + matriz.getLecturasNoRegistradas());
        System.out.println();

        matriz.imprimirMatriz();

        System.out.println();
        System.out.println("Promedio de PM2.5 por hora");
        System.out.printf(Locale.US, "%-7s %11s %10s %14s%n",
                "Hora", "Reportaron", "Correcto", "Dividiendo /9");
        for (int hora = 0; hora < AnalizadorMatriz.NUM_HORAS; hora++) {
            System.out.printf(Locale.US, "%02d:00   %11d %10s %14s%n", hora,
                    matriz.estacionesQueReportaron(hora),
                    formato(matriz.promedioDeHora(hora)),
                    formato(matriz.promedioDeHoraDividiendoEntreTodas(hora)));
        }

        System.out.println();
        System.out.println("Promedio de PM2.5 por estacion");
        System.out.printf(Locale.US, "%-9s %14s %10s %14s%n",
                "Estacion", "Horas con dato", "Correcto", "Dividiendo /24");
        for (int fila = 0; fila < matriz.numeroEstaciones(); fila++) {
            System.out.printf(Locale.US, "%-9s %14d %10s %14s%n",
                    matriz.idEstacion(fila),
                    matriz.horasConDato(fila),
                    formato(matriz.promedioDeEstacion(fila)),
                    formato(matriz.promedioDeEstacionDividiendoEntre24(fila)));
        }

        System.out.println();
        int hora = matriz.horaMasContaminada();
        int fila = matriz.estacionMasContaminada();
        System.out.println("Hora mas contaminada: "
                + (hora < 0 ? "sin datos" : String.format("%02d:00 (%s)", hora, formato(matriz.promedioDeHora(hora)))));
        System.out.println("Estacion mas contaminada: "
                + (fila < 0 ? "sin datos" : matriz.idEstacion(fila) + " (" + formato(matriz.promedioDeEstacion(fila)) + ")"));
    }

    // ---------------------------------------------------------------
    // Experimento opcional: que pasa con 8.000 estaciones (pregunta 5)
    // ---------------------------------------------------------------
    private static void experimentoEscala() {
        titulo("EXPERIMENTO - 8.000 estaciones x 24 horas");

        final int estaciones = 8000;
        RepositorioLecturas repositorio = new RepositorioLecturas();
        for (int e = 1; e <= estaciones; e++) {
            for (int h = 0; h < AnalizadorMatriz.NUM_HORAS; h++) {
                repositorio.agregar(new LecturaSensor(String.format("EST-%04d", e),
                        String.format("2026-03-02 %02d:00", h), 20.0, 60.0, 15.0));
            }
        }

        long inicio = System.nanoTime();
        repositorio.buscarPorEstacion("EST-9999");   // peor caso: no existe
        long microsegundos = (System.nanoTime() - inicio) / 1000;

        System.out.println("Lecturas almacenadas: " + repositorio.tamano());
        System.out.println("Peor caso de buscarPorEstacion: "
                + repositorio.getComparacionesUltimaBusqueda() + " comparaciones (~"
                + microsegundos + " microsegundos en este equipo)");
        System.out.println("Si se consulta cada estacion una vez (peor caso): "
                + ((long) estaciones * repositorio.tamano()) + " comparaciones");
    }

    // ---------------------------------------------------------------
    // Lectura del archivo (mismas reglas que IngestaSensores)
    // ---------------------------------------------------------------

    /**
     * Carga el archivo en el repositorio (y, si no es null, en la matriz).
     * @param mostrarDescartes si es true, imprime cada fila descartada con su motivo
     */
    private static Resumen cargar(String ruta, RepositorioLecturas repositorio,
                                  AnalizadorMatriz matriz, boolean mostrarDescartes) throws IOException {
        Resumen resumen = new Resumen();

        try (BufferedReader lector = new BufferedReader(new FileReader(ruta))) {
            lector.readLine();   // encabezado
            String linea;
            int numeroLinea = 1;

            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                if (linea.isBlank()) {
                    continue;
                }
                resumen.filas++;

                String[] campos = linea.split(",");
                if (campos.length != CAMPOS_ESPERADOS) {
                    resumen.descartadasFormato++;
                    descartar(mostrarDescartes, numeroLinea, "formato (cantidad de campos)", linea);
                    continue;
                }

                LecturaSensor lectura;
                try {
                    lectura = new LecturaSensor(campos[0], campos[1],
                            Double.parseDouble(campos[2]),
                            Double.parseDouble(campos[3]),
                            Double.parseDouble(campos[4]));
                } catch (NumberFormatException e) {
                    resumen.descartadasFormato++;
                    descartar(mostrarDescartes, numeroLinea, "formato (valor no numerico)", linea);
                    continue;
                }

                if (!lectura.esValida()) {
                    resumen.descartadasRango++;
                    descartar(mostrarDescartes, numeroLinea, "rango", linea);
                    continue;
                }

                if (repositorio.agregar(lectura)) {
                    resumen.almacenadas++;
                } else {
                    resumen.rechazadasPorCapacidad++;
                }
                if (matriz != null) {
                    matriz.registrar(lectura);
                }
            }
        }
        return resumen;
    }

    private static void descartar(boolean mostrar, int numeroLinea, String motivo, String linea) {
        if (mostrar) {
            System.out.println("  Fila " + numeroLinea + " descartada por " + motivo + ": " + linea);
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("==================================================");
        System.out.println(texto);
        System.out.println("==================================================");
    }

    /** Muestra NaN como "sin dato" para no confundirlo con un 0. */
    private static String formato(double valor) {
        return Double.isNaN(valor) ? "sin dato" : String.format(Locale.US, "%.2f", valor);
    }
}

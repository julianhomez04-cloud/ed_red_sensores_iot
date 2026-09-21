/* ============================================================
   PLATAFORMA DE MONITOREO AMBIENTAL URBANO
   AnalizadorMatriz - VERSION 1.0 COMPLETA (Semana 2)

   Matriz de 9 estaciones x 24 horas para responder: a que hora
   se contamina mas la ciudad y cual estacion sostiene los peores
   niveles.

   filas    = estaciones (0..8  ->  EST-001..EST-009)
   columnas = horas      (0..23)

   DECISION DE DISENO: la matriz es Double[][] (objeto), no
   double[][] (primitivo). Una celda null significa "la estacion
   no reporto" y 0.0 significa "la estacion midio cero". Con
   double[][] las dos cosas se ven igual y los promedios mienten.
   El acceso a las celdas esta encapsulado en hayDato()/valorEn()
   para no tropezar con NullPointerException.
   ============================================================ */

public class AnalizadorMatriz {

    private static final int NUM_ESTACIONES = 9;
    public static final int NUM_HORAS = 24;
    private static final int LARGO_PREFIJO_ID = 4;   // "EST-"

    private final int numeroEstaciones;
    private final Double[][] pm25PorEstacionHora;    // null = sin dato
    private int lecturasSobrescritas;
    private int lecturasNoRegistradas;

    public AnalizadorMatriz() {
        this(NUM_ESTACIONES);
    }

    public AnalizadorMatriz(int numeroEstaciones) {
        if (numeroEstaciones < 1) {
            throw new IllegalArgumentException("Debe haber al menos una estacion");
        }
        this.numeroEstaciones = numeroEstaciones;
        this.pm25PorEstacionHora = new Double[numeroEstaciones][NUM_HORAS];
    }

    /**
     * Convierte "EST-004" en el indice de fila 3.
     * @return la fila, o -1 si el identificador no tiene el formato esperado.
     */
    private int indiceDeEstacion(String idSensor) {
        if (idSensor == null || idSensor.length() <= LARGO_PREFIJO_ID) {
            return -1;
        }
        try {
            return Integer.parseInt(idSensor.substring(LARGO_PREFIJO_ID)) - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** @return la hora (0-23) de la lectura, o -1 si el timestamp no se puede leer. */
    private int horaDe(LecturaSensor lectura) {
        try {
            return lectura.getHora();
        } catch (StringIndexOutOfBoundsException | NumberFormatException e) {
            return -1;
        }
    }

    // ---------- CONSTRUCCION Y CONSULTA DE CELDAS ----------

    /**
     * Ubica una lectura en su celda correspondiente.
     * Si la celda ya tenia dato, se sobrescribe y se cuenta en getLecturasSobrescritas().
     * @return true si se registro; false si la estacion o la hora no caben en la matriz
     *         (queda contada en getLecturasNoRegistradas(): nada desaparece en silencio).
     */
    public boolean registrar(LecturaSensor lectura) {
        if (lectura == null) {
            lecturasNoRegistradas++;
            return false;
        }
        int fila = indiceDeEstacion(lectura.getIdSensor());
        int columna = horaDe(lectura);
        if (!estacionValida(fila) || !horaValida(columna)) {
            lecturasNoRegistradas++;
            return false;
        }
        if (pm25PorEstacionHora[fila][columna] != null) {
            lecturasSobrescritas++;
        }
        pm25PorEstacionHora[fila][columna] = lectura.getPm25();
        return true;
    }

    public boolean hayDato(int fila, int hora) {
        return estacionValida(fila) && horaValida(hora) && pm25PorEstacionHora[fila][hora] != null;
    }

    /** @return el valor de la celda, o null si no hubo dato. */
    public Double valorEn(int fila, int hora) {
        return hayDato(fila, hora) ? pm25PorEstacionHora[fila][hora] : null;
    }

    public int numeroEstaciones() { return numeroEstaciones; }

    /** @return "EST-001" para la fila 0, etc. */
    public String idEstacion(int fila) {
        validarEstacion(fila);
        return String.format("EST-%03d", fila + 1);
    }

    public int getLecturasSobrescritas() { return lecturasSobrescritas; }

    public int getLecturasNoRegistradas() { return lecturasNoRegistradas; }

    public int estacionesQueReportaron(int hora) {
        validarHora(hora);
        int reportes = 0;
        for (int fila = 0; fila < numeroEstaciones; fila++) {
            if (hayDato(fila, hora)) {
                reportes++;
            }
        }
        return reportes;
    }

    public int horasConDato(int fila) {
        validarEstacion(fila);
        int reportes = 0;
        for (int hora = 0; hora < NUM_HORAS; hora++) {
            if (hayDato(fila, hora)) {
                reportes++;
            }
        }
        return reportes;
    }

    public int celdasSinDato() {
        int vacias = 0;
        for (int fila = 0; fila < numeroEstaciones; fila++) {
            vacias += NUM_HORAS - horasConDato(fila);
        }
        return vacias;
    }

    // ---------- PROMEDIOS (siempre sobre los datos que EXISTEN) ----------

    /**
     * Promedio de PM2.5 de una hora del dia entre las estaciones que reportaron.
     * Divide entre las estaciones que reportaron, NO entre 9.
     * @return el promedio, o NaN si ninguna estacion reporto esa hora.
     */
    public double promedioDeHora(int hora) {
        validarHora(hora);
        double suma = 0;
        int cantidadReportes = 0;
        for (int fila = 0; fila < numeroEstaciones; fila++) {
            if (hayDato(fila, hora)) {
                suma = suma + pm25PorEstacionHora[fila][hora];
                cantidadReportes++;
            }
        }
        return dividir(suma, cantidadReportes);
    }

    /**
     * Promedio de PM2.5 de una estacion a lo largo del dia.
     * Divide entre las horas en que la estacion SI reporto, NO entre 24.
     * @return el promedio, o NaN si la estacion nunca reporto.
     */
    public double promedioDeEstacion(int fila) {
        validarEstacion(fila);
        double suma = 0;
        int cantidadReportes = 0;
        for (int hora = 0; hora < NUM_HORAS; hora++) {
            if (hayDato(fila, hora)) {
                suma = suma + pm25PorEstacionHora[fila][hora];
                cantidadReportes++;
            }
        }
        return dividir(suma, cantidadReportes);
    }

    /**
     * Hora del dia con mayor contaminacion promedio en la ciudad (ignora horas sin datos).
     * @return la hora (0-23), o -1 si no hay ningun dato.
     */
    public int horaMasContaminada() {
        int mejorHora = -1;
        double mayorPromedio = Double.NEGATIVE_INFINITY;
        for (int hora = 0; hora < NUM_HORAS; hora++) {
            double promedio = promedioDeHora(hora);
            if (!Double.isNaN(promedio) && promedio > mayorPromedio) {
                mayorPromedio = promedio;
                mejorHora = hora;
            }
        }
        return mejorHora;
    }

    /** @return la fila de la estacion con mayor promedio propio, o -1 si no hay datos. */
    public int estacionMasContaminada() {
        int mejorFila = -1;
        double mayorPromedio = Double.NEGATIVE_INFINITY;
        for (int fila = 0; fila < numeroEstaciones; fila++) {
            double promedio = promedioDeEstacion(fila);
            if (!Double.isNaN(promedio) && promedio > mayorPromedio) {
                mayorPromedio = promedio;
                mejorFila = fila;
            }
        }
        return mejorFila;
    }

    // ---------- VERSIONES INCORRECTAS, SOLO PARA MOSTRAR EL ERROR (evidencia) ----------

    /** Trata la ausencia como 0 y divide entre TODAS las estaciones. No usar en analisis reales. */
    public double promedioDeHoraDividiendoEntreTodas(int hora) {
        validarHora(hora);
        double suma = 0;
        for (int fila = 0; fila < numeroEstaciones; fila++) {
            if (hayDato(fila, hora)) {
                suma = suma + pm25PorEstacionHora[fila][hora];
            }
        }
        return suma / numeroEstaciones;
    }

    /** Trata la ausencia como 0 y divide siempre entre 24. No usar en analisis reales. */
    public double promedioDeEstacionDividiendoEntre24(int fila) {
        validarEstacion(fila);
        double suma = 0;
        for (int hora = 0; hora < NUM_HORAS; hora++) {
            if (hayDato(fila, hora)) {
                suma = suma + pm25PorEstacionHora[fila][hora];
            }
        }
        return suma / NUM_HORAS;
    }

    // ---------- SALIDA ----------

    /**
     * Imprime la matriz completa. Las celdas sin dato se muestran como "--",
     * nunca como 0.0.
     */
    public void imprimirMatriz() {
        System.out.print("EST\\HORA");
        for (int h = 0; h < NUM_HORAS; h++) {
            System.out.printf("%7s", String.format("%02d", h));
        }
        System.out.println();
        for (int f = 0; f < numeroEstaciones; f++) {
            System.out.printf("EST-%03d ", f + 1);
            for (int h = 0; h < NUM_HORAS; h++) {
                Double valor = pm25PorEstacionHora[f][h];
                if (valor == null) {
                    System.out.printf("%7s", "--");
                } else {
                    System.out.printf(java.util.Locale.US, "%7.1f", valor);
                }
            }
            System.out.println();
        }
    }

    // ---------- UTILIDADES INTERNAS ----------

    private static double dividir(double suma, int cantidad) {
        return cantidad == 0 ? Double.NaN : suma / cantidad;
    }

    private boolean horaValida(int hora) {
        return hora >= 0 && hora < NUM_HORAS;
    }

    private boolean estacionValida(int fila) {
        return fila >= 0 && fila < numeroEstaciones;
    }

    private void validarHora(int hora) {
        if (!horaValida(hora)) {
            throw new IllegalArgumentException("Hora fuera de rango (0-23): " + hora);
        }
    }

    private void validarEstacion(int fila) {
        if (!estacionValida(fila)) {
            throw new IllegalArgumentException("Estacion fuera de rango: " + fila);
        }
    }
}

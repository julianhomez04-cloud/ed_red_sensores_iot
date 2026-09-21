/* ============================================================
   PLATAFORMA DE MONITOREO AMBIENTAL URBANO
   TAD RepositorioLecturas - VERSION 1.0 COMPLETA (Semana 2)

   Contrato (lo que el resto del programa puede usar):
     agregar, obtener, buscarPorEstacion, actualizar, eliminar,
     tamano, promedioPm25.

   Detalle interno (puede cambiar sin afectar a quien lo usa):
     el arreglo, la capacidad, redimensionar() y los contadores.
   ============================================================ */

public class RepositorioLecturas {

    /** Que hace el repositorio cuando el arreglo se llena. */
    public enum EstrategiaCrecimiento {
        /** No crece: agregar() devuelve false. Reproduce la version 0.1 (solo para el diagnostico de la Fase 1). */
        FIJA,
        /** Crece de a una posicion: 10 -> 11 -> 12 ... (solo para comparar en la Fase 2.1). */
        UNO_A_UNO,
        /** Duplica la capacidad: 10 -> 20 -> 40 ... (estrategia por defecto). */
        DUPLICAR
    }

    private static final int CAPACIDAD_INICIAL = 10;

    private LecturaSensor[] lecturas;
    private int cantidad;
    private final EstrategiaCrecimiento estrategia;

    // Instrumentacion de la Fase 2.1 ("no estimes, mide")
    private int copiasRealizadas;
    private int redimensionamientos;
    private int desplazamientosRealizados;
    private int comparacionesUltimaBusqueda;

    public RepositorioLecturas() {
        this(CAPACIDAD_INICIAL, EstrategiaCrecimiento.DUPLICAR);
    }

    public RepositorioLecturas(int capacidadInicial) {
        this(capacidadInicial, EstrategiaCrecimiento.DUPLICAR);
    }

    public RepositorioLecturas(int capacidadInicial, EstrategiaCrecimiento estrategia) {
        if (capacidadInicial < 1) {
            throw new IllegalArgumentException("La capacidad inicial debe ser al menos 1");
        }
        if (estrategia == null) {
            throw new IllegalArgumentException("La estrategia de crecimiento no puede ser null");
        }
        this.lecturas = new LecturaSensor[capacidadInicial];
        this.cantidad = 0;
        this.estrategia = estrategia;
    }

    // ---------- OPERACIONES DEL CONTRATO ----------

    /**
     * Agrega una lectura al final del repositorio.
     * @return true si quedo almacenada; false si la lectura es null o si la
     *         estrategia es FIJA y no habia espacio.
     */
    public boolean agregar(LecturaSensor lectura) {
        if (lectura == null) {
            return false;
        }
        if (cantidad >= lecturas.length) {
            if (estrategia == EstrategiaCrecimiento.FIJA) {
                return false;
            }
            redimensionar();
        }
        lecturas[cantidad] = lectura;
        cantidad++;
        return true;
    }

    /**
     * Devuelve la lectura que esta en la posicion indicada.
     * @return la lectura, o null si la posicion no es valida (negativa o >= tamano()).
     */
    public LecturaSensor obtener(int posicion) {
        if (!posicionValida(posicion)) {
            return null;
        }
        return lecturas[posicion];
    }

    /**
     * Cantidad de lecturas almacenadas actualmente (NO la capacidad del arreglo).
     */
    public int tamano() {
        return cantidad;
    }

    /**
     * Elimina la lectura de la posicion indicada compactando el arreglo:
     * los elementos posteriores se mueven una posicion a la izquierda.
     * @return true si se elimino; false si la posicion no es valida.
     */
    public boolean eliminar(int posicion) {
        if (!posicionValida(posicion)) {
            return false;
        }
        for (int i = posicion; i < cantidad - 1; i++) {
            lecturas[i] = lecturas[i + 1];
            desplazamientosRealizados++;
        }
        lecturas[cantidad - 1] = null;   // evita dejar una referencia duplicada al final
        cantidad--;
        return true;
    }

    /**
     * Busca la primera lectura de una estacion.
     * @return la lectura, o null si no existe (o si idSensor es null).
     */
    public LecturaSensor buscarPorEstacion(String idSensor) {
        int posicion = buscarPosicionPorEstacion(idSensor);
        return posicion < 0 ? null : lecturas[posicion];
    }

    /**
     * Igual que buscarPorEstacion(), pero devuelve la posicion (util para actualizar/eliminar).
     * @return la posicion de la primera coincidencia, o -1 si no existe.
     */
    public int buscarPosicionPorEstacion(String idSensor) {
        comparacionesUltimaBusqueda = 0;
        if (idSensor == null) {
            return -1;
        }
        for (int i = 0; i < cantidad; i++) {
            comparacionesUltimaBusqueda++;
            if (lecturas[i].getIdSensor().equals(idSensor)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Reemplaza la lectura de una posicion por otra.
     * @return true si se reemplazo; false si la posicion no es valida o la lectura es null.
     */
    public boolean actualizar(int posicion, LecturaSensor nueva) {
        if (!posicionValida(posicion) || nueva == null) {
            return false;
        }
        lecturas[posicion] = nueva;
        return true;
    }

    /**
     * Promedio de PM2.5 de todas las lecturas almacenadas.
     * Recorre solo las posiciones ocupadas (0..cantidad-1); como eliminar()
     * compacta, nunca se topa con un hueco.
     * @return el promedio, o NaN si el repositorio esta vacio (no existe promedio de nada).
     */
    public double promedioPm25() {
        if (cantidad == 0) {
            return Double.NaN;
        }
        double suma = 0;
        for (int i = 0; i < cantidad; i++) {
            suma = suma + lecturas[i].getPm25();
        }
        return suma / cantidad;
    }

    // ---------- INFORMACION DE DIAGNOSTICO (no es parte del contrato) ----------

    public boolean estaVacio() { return cantidad == 0; }

    public int capacidad() { return lecturas.length; }

    public EstrategiaCrecimiento getEstrategia() { return estrategia; }

    public int getCopiasRealizadas() { return copiasRealizadas; }

    public int getRedimensionamientos() { return redimensionamientos; }

    public int getDesplazamientosRealizados() { return desplazamientosRealizados; }

    public int getComparacionesUltimaBusqueda() { return comparacionesUltimaBusqueda; }

    // ---------- DETALLES INTERNOS ----------

    private boolean posicionValida(int posicion) {
        return posicion >= 0 && posicion < cantidad;
    }

    /**
     * Crea un arreglo mas grande, copia los elementos y reemplaza la referencia.
     * Se llama desde agregar() cuando el arreglo esta lleno.
     */
    private void redimensionar() {
        int nuevaCapacidad = (estrategia == EstrategiaCrecimiento.DUPLICAR)
                ? lecturas.length * 2
                : lecturas.length + 1;

        LecturaSensor[] nuevo = new LecturaSensor[nuevaCapacidad];
        for (int i = 0; i < cantidad; i++) {
            copiasRealizadas++;
            nuevo[i] = lecturas[i];
        }
        lecturas = nuevo;
        redimensionamientos++;
    }

    /** Solo para pruebas: mira una casilla del arreglo sin validar contra cantidad. */
    LecturaSensor casillaCruda(int indice) {
        if (indice < 0 || indice >= lecturas.length) {
            return null;
        }
        return lecturas[indice];
    }
}

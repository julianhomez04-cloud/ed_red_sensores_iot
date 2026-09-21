/* ============================================================
   Pruebas de la Semana 2: los 6 casos de la guia, sin librerias.
   Uso:  java PruebasRepositorio     (termina con codigo 1 si algo falla)
   ============================================================ */

public class PruebasRepositorio {

    private static int aprobadas = 0;
    private static int falladas = 0;

    public static void main(String[] args) {
        caso1Agregar();
        caso2Capacidad();
        caso3Buscar();
        caso4Actualizar();
        caso5Eliminar();
        caso6Matriz();

        System.out.println();
        System.out.println("Resultado: " + aprobadas + " pruebas OK, " + falladas + " fallidas");
        System.exit(falladas == 0 ? 0 : 1);
    }

    private static void caso1Agregar() {
        System.out.println("Caso 1 - Agregar");
        RepositorioLecturas repo = new RepositorioLecturas();
        verificar("repositorio nuevo esta vacio", repo.tamano() == 0 && repo.estaVacio());
        verificar("agregar lectura valida devuelve true", repo.agregar(lectura("EST-001", 8, 30.0)));
        verificar("tamano() pasa a 1", repo.tamano() == 1);
        verificar("agregar(null) devuelve false", !repo.agregar(null));
        verificar("tamano() sigue en 1 tras agregar(null)", repo.tamano() == 1);
    }

    private static void caso2Capacidad() {
        System.out.println("Caso 2 - Capacidad");
        RepositorioLecturas repo = new RepositorioLecturas(2);
        boolean todasAgregadas = true;
        for (int i = 0; i < 25; i++) {
            todasAgregadas &= repo.agregar(lectura("EST-001", i % 24, i));
        }
        verificar("las 25 lecturas se agregan superando la capacidad inicial de 2", todasAgregadas);
        verificar("tamano() = 25", repo.tamano() == 25);
        verificar("la capacidad crecio (>= 25)", repo.capacidad() >= 25);
        boolean sinPerdidas = true;
        for (int i = 0; i < 25; i++) {
            sinPerdidas &= repo.obtener(i) != null && repo.obtener(i).getPm25() == i;
        }
        verificar("no se perdio ni se desordeno ninguna lectura", sinPerdidas);
        verificar("obtener(-1) y obtener(25) devuelven null",
                repo.obtener(-1) == null && repo.obtener(25) == null);

        RepositorioLecturas fijo = new RepositorioLecturas(2, RepositorioLecturas.EstrategiaCrecimiento.FIJA);
        fijo.agregar(lectura("EST-001", 0, 1));
        fijo.agregar(lectura("EST-001", 1, 2));
        verificar("con estrategia FIJA la tercera lectura se rechaza (false)",
                !fijo.agregar(lectura("EST-001", 2, 3)) && fijo.tamano() == 2);

        // Conteos deterministas: 201 lecturas y capacidad inicial 10
        RepositorioLecturas unoAUno = new RepositorioLecturas(10, RepositorioLecturas.EstrategiaCrecimiento.UNO_A_UNO);
        RepositorioLecturas duplicando = new RepositorioLecturas(10, RepositorioLecturas.EstrategiaCrecimiento.DUPLICAR);
        for (int i = 0; i < 201; i++) {
            unoAUno.agregar(lectura("EST-001", i % 24, i));
            duplicando.agregar(lectura("EST-001", i % 24, i));
        }
        verificar("UNO_A_UNO con 201 lecturas: 191 redimensionamientos y 20055 copias",
                unoAUno.getRedimensionamientos() == 191 && unoAUno.getCopiasRealizadas() == 20055);
        verificar("DUPLICAR con 201 lecturas: 5 redimensionamientos, 310 copias, capacidad 320",
                duplicando.getRedimensionamientos() == 5 && duplicando.getCopiasRealizadas() == 310
                        && duplicando.capacidad() == 320);
        verificar("ambas estrategias almacenan 201", unoAUno.tamano() == 201 && duplicando.tamano() == 201);
    }

    private static void caso3Buscar() {
        System.out.println("Caso 3 - Buscar");
        RepositorioLecturas repo = new RepositorioLecturas(3);
        for (int e = 1; e <= 5; e++) {
            repo.agregar(lectura(String.format("EST-%03d", e), 8, e * 10.0));
        }
        LecturaSensor encontrada = repo.buscarPorEstacion("EST-004");
        verificar("EST-004 devuelve una lectura de esa estacion",
                encontrada != null && encontrada.getIdSensor().equals("EST-004"));
        verificar("EST-999 devuelve null", repo.buscarPorEstacion("EST-999") == null);
        verificar("buscar con null devuelve null", repo.buscarPorEstacion(null) == null);
        verificar("buscar en repositorio vacio devuelve null",
                new RepositorioLecturas().buscarPorEstacion("EST-001") == null);

        repo.agregar(lectura("EST-004", 9, 99.0));
        verificar("con repetidas devuelve la PRIMERA coincidencia",
                repo.buscarPorEstacion("EST-004").getPm25() == 40.0);
        repo.buscarPorEstacion("EST-999");
        verificar("el peor caso recorre todas las lecturas (6 comparaciones)",
                repo.getComparacionesUltimaBusqueda() == 6);
    }

    private static void caso4Actualizar() {
        System.out.println("Caso 4 - Actualizar");
        RepositorioLecturas repo = new RepositorioLecturas(4);
        repo.agregar(lectura("EST-001", 0, 10));
        repo.agregar(lectura("EST-002", 0, 20));
        LecturaSensor nueva = lectura("EST-002", 0, 25);

        verificar("actualizar posicion valida devuelve true", repo.actualizar(1, nueva));
        verificar("la lectura cambio", repo.obtener(1) == nueva && repo.obtener(1).getPm25() == 25);
        verificar("actualizar(-1) devuelve false", !repo.actualizar(-1, nueva));
        verificar("actualizar(999) devuelve false", !repo.actualizar(999, nueva));
        verificar("actualizar(tamano()) devuelve false (posicion vacia)", !repo.actualizar(repo.tamano(), nueva));
        verificar("actualizar con null devuelve false", !repo.actualizar(0, null));
        verificar("tamano() no cambio", repo.tamano() == 2);
    }

    private static void caso5Eliminar() {
        System.out.println("Caso 5 - Eliminar");
        RepositorioLecturas repo = new RepositorioLecturas(10);
        double[] valores = {10, 20, 30, 40, 60};
        for (int i = 0; i < valores.length; i++) {
            repo.agregar(lectura("EST-001", i, valores[i]));
        }
        double promedioAntes = repo.promedioPm25();

        verificar("eliminar posicion valida (2) devuelve true", repo.eliminar(2));
        verificar("tamano() baja a 4", repo.tamano() == 4);
        verificar("los elementos se compactaron (la posicion 2 ahora tiene lo que estaba en 3)",
                repo.obtener(2).getPm25() == 40 && repo.obtener(3).getPm25() == 60);
        verificar("obtener(4) devuelve null", repo.obtener(4) == null);
        verificar("no quedo referencia duplicada en la ultima casilla usada", repo.casillaCruda(4) == null);
        verificar("promedioPm25() no se cae y cambia de 32.0 a 32.5",
                Math.abs(promedioAntes - 32.0) < 1e-9 && Math.abs(repo.promedioPm25() - 32.5) < 1e-9);

        verificar("eliminar(-1) devuelve false", !repo.eliminar(-1));
        verificar("eliminar(99) devuelve false", !repo.eliminar(99));
        verificar("eliminar en repositorio vacio devuelve false", !new RepositorioLecturas().eliminar(0));

        verificar("eliminar la primera funciona",
                repo.eliminar(0) && repo.obtener(0).getPm25() == 20 && repo.tamano() == 3);
        verificar("eliminar la ultima funciona",
                repo.eliminar(2) && repo.tamano() == 2 && repo.casillaCruda(2) == null);

        RepositorioLecturas vacio = new RepositorioLecturas();
        verificar("promedioPm25() de un repositorio vacio es NaN (no 0 ni excepcion)",
                Double.isNaN(vacio.promedioPm25()));
    }

    private static void caso6Matriz() {
        System.out.println("Caso 6 - Matriz (ausencia != 0.0)");
        AnalizadorMatriz matriz = new AnalizadorMatriz(3);
        matriz.registrar(lectura("EST-001", 0, 10));
        matriz.registrar(lectura("EST-002", 0, 20));      // EST-003 no reporto a la hora 0
        matriz.registrar(lectura("EST-001", 1, 10));
        matriz.registrar(lectura("EST-003", 1, 0.0));     // EST-003 midio CERO real a la hora 1

        verificar("celda sin reporte: hayDato = false y valor null",
                !matriz.hayDato(2, 0) && matriz.valorEn(2, 0) == null);
        verificar("cero real: hayDato = true y valor 0.0",
                matriz.hayDato(2, 1) && matriz.valorEn(2, 1) == 0.0);
        verificar("hora 0: reportaron 2 estaciones", matriz.estacionesQueReportaron(0) == 2);
        verificar("hora 0: promedio correcto = 15.0 (suma / 2)", matriz.promedioDeHora(0) == 15.0);
        verificar("hora 0: dividir entre todas daria 10.0 (incorrecto)",
                matriz.promedioDeHoraDividiendoEntreTodas(0) == 10.0);
        verificar("hora 1: el cero real SI cuenta (promedio 5.0)", matriz.promedioDeHora(1) == 5.0);
        verificar("hora sin ningun reporte devuelve NaN (no 0)", Double.isNaN(matriz.promedioDeHora(2)));
        verificar("EST-003: promedio 0.0 (un solo dato, que es cero)", matriz.promedioDeEstacion(2) == 0.0);
        verificar("EST-001: promedio 10.0 en 2 horas con dato",
                matriz.promedioDeEstacion(0) == 10.0 && matriz.horasConDato(0) == 2);
        verificar("EST-001 dividiendo entre 24 daria 0.83 (incorrecto)",
                Math.abs(matriz.promedioDeEstacionDividiendoEntre24(0) - (20.0 / 24)) < 1e-9);
        verificar("hora mas contaminada = 0 (promedio 15.0)", matriz.horaMasContaminada() == 0);
        verificar("estacion mas contaminada = EST-002 (fila 1)", matriz.estacionMasContaminada() == 1);
        verificar("matriz sin datos: horaMasContaminada = -1",
                new AnalizadorMatriz(1).horaMasContaminada() == -1);

        verificar("EST-999, hora 24 y un id mal formado no caben: registrar devuelve false",
                !matriz.registrar(lectura("EST-999", 0, 1))
                        && !matriz.registrar(lectura("EST-001", 24, 1))
                        && !matriz.registrar(lectura("XYZ", 0, 1)));
        verificar("esas 3 lecturas quedan contadas como no registradas (no desaparecen en silencio)",
                matriz.getLecturasNoRegistradas() == 3);

        AnalizadorMatriz duplicada = new AnalizadorMatriz(1);
        duplicada.registrar(lectura("EST-001", 5, 9.2));
        duplicada.registrar(lectura("EST-001", 5, 9.2));
        verificar("una lectura repetida sobrescribe la celda y se cuenta",
                duplicada.getLecturasSobrescritas() == 1 && duplicada.valorEn(0, 5) == 9.2);
    }

    // ---------------------------------------------------------------

    private static LecturaSensor lectura(String id, int hora, double pm25) {
        return new LecturaSensor(id, String.format("2026-03-02 %02d:00", hora), 20.0, 60.0, pm25);
    }

    private static void verificar(String descripcion, boolean condicion) {
        if (condicion) {
            aprobadas++;
            System.out.println("  [OK]    " + descripcion);
        } else {
            falladas++;
            System.out.println("  [FALLA] " + descripcion);
        }
    }
}

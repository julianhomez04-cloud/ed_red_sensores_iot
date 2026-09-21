# Decisiones de Diseno - Bitacora Tecnica

Formato: cada entrada con fecha, decision, alternativas consideradas, justificacion.

## S1 - De codigo fragil a confiable - 2026-08-26

### Decision 1: Crear clase LecturaSensor
- **Alternativas:** mantener 5 variables sueltas vs crear clase.
- **Elegida:** clase con atributos privados, constantes de rangos.
- **Justificacion:** encapsulamiento (numeral 1.2), reutilizable 12 semanas, firma de metodos pasa de 5 params a 1. Evita error de orden de parametros.

### Decision 2: Estrategia ante fila invalida
- **Alternativas:** A) descartar solo campo malo B) descartar fila completa.
- **Elegida:** B) fila completa.
- **Justificacion:** principio conservador para reporte oficial. Si un canal falla (-999), no confiamos en sincronia de los otros dos. Preferimos perdida de datos a contaminacion silenciosa. Registrado en descartes.csv para auditoria.

### Decision 3: Manejo de excepciones
- **Alternativas:** catch generico Exception vs especificos.
- **Elegida:** catch especifico NumberFormatException y ArrayIndexOutOfBoundsException + validacion fisica.
- **Justificacion:** catch vacio o generico esconde causa. Necesitamos trazabilidad: cuantos, por que motivo.

### Decision 4: Constantes vs numeros magicos
- **Elegida:** TEMP_MIN=-40, TEMP_MAX=60, HUM_MIN=0, HUM_MAX=100, PM_MIN=0, CODIGO_DESCONECTADO=-999
- **Justificacion:** si cambian umbrales de la norma ambiental, se cambia en un solo lugar.

## S2 - Donde viven los datos - 2026-09-19

### Decision 1: Como crece el repositorio
- **Alternativas:** A) subir la capacidad fija (por ejemplo a 500) B) crecer de a una posicion C) duplicar la capacidad.
- **Elegida:** C) duplicar, con `redimensionar()` llamado desde `agregar()` cuando el arreglo se llena.
- **Justificacion:** se midio con las 201 lecturas validas y capacidad inicial 10. Crecer de a uno: 191 redimensionamientos y 20055 copias. Duplicar: 5 redimensionamientos y 310 copias (capacidad final 320). El costo de duplicar es memoria reservada sin usar (320 - 201 = 119 casillas). La opcion A solo aplaza el problema: con 8.000 estaciones x 24 horas (192.000 lecturas) el techo vuelve a aparecer. Evidencia: `docs/evidencia-s02.txt`.

### Decision 2: Como se elimina una lectura
- **Alternativas:** A) compactar (mover los elementos a la izquierda) B) marcar el hueco con `boolean[] ocupada`.
- **Elegida:** A) compactar, anulando `lecturas[cantidad - 1]` y haciendo `cantidad--`.
- **Justificacion:** conserva el orden cronologico, no deja huecos, `tamano()` siempre dice la verdad y todos los recorridos (`i < cantidad`) siguen siendo simples. En este sistema se lee y se busca mucho mas de lo que se elimina. Costo: hasta `cantidad - 1` desplazamientos por eliminacion (200 en el peor caso con 201 lecturas). Marcar obligaria a que cada recorrido (busqueda, promedios, matriz) supiera saltar huecos.

### Decision 3: Como se representa la ausencia de datos en la matriz
- **Alternativas:** A) `double[][]` con un `boolean[][]` paralelo B) valor centinela (-1) C) `Double[][]` con `null`.
- **Elegida:** C) `Double[][]`: `null` = la estacion no reporto, `0.0` = la estacion midio cero.
- **Justificacion:** con `double[][]` sin mas, las 16 celdas vacias del dia (entre ellas EST-003 de 09:00 a 12:00) se leen como 0.0 y el promedio de EST-003 baja de 17.86 a 14.14, ocultando que es la estacion mas contaminada. La opcion A funciona pero obliga a mantener dos estructuras sincronizadas. La opcion B puede colarse en un promedio si alguien olvida filtrar, o ser algun dia un valor legitimo. El riesgo de C (NullPointerException) se controla encapsulando el acceso en `hayDato()` y `valorEn()`.

### Decision 4: Promedios sobre los datos que existen
- **Elegida:** `promedioDeHora()` divide entre las estaciones que reportaron (no entre 9) y `promedioDeEstacion()` entre las horas con dato (no entre 24). Si no hay ningun dato devuelven `NaN`, no 0.
- **Justificacion:** dividir entre 9 da 9.93 a las 09:00 cuando el valor correcto es 11.18 (8 estaciones reportaron). Devolver 0 cuando no hay datos reintroduciria el mismo error, asi que se prefiere `NaN` y `horaMasContaminada()` ignora esas horas.

### Decision 5: Nada se pierde en silencio
- **Elegida:** `registrar()` devuelve `false` y cuenta `getLecturasNoRegistradas()` cuando una lectura no cabe en la matriz; las celdas repetidas se cuentan en `getLecturasSobrescritas()`.
- **Observacion:** el archivo trae la fila EST-002 15:00 repetida (identica, 9.2). El repositorio guarda las 201 lecturas, incluida la repetida (por eso `promedioPm25()` la cuenta dos veces), mientras que la matriz tiene 200 celdas con dato y 1 sobrescritura. Detectar repetidos queda pendiente para la semana de `HashSet`.

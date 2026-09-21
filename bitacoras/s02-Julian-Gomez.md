# Bitacora individual - Semana 02

> \*\*BORRADOR.\*\* Solo falta completar los hashes de los commits (sección 9), que tendrás cuando subas el trabajo a GitHub. Lee todo y cambia cualquier frase que no describa lo que tú de verdad pensaste o hiciste: la bitácora es individual y el profesor puede preguntarte por cualquier parte.

## 1\. Datos de la actividad

* **Estudiante:** Julian Gomez
* **Equipo:** trabajo individual (sin equipo)
* **Semana:** 2
* **Fecha del laboratorio:** 2026-09-20
* **Fecha del taller:** 2026-09-21
* **Tema principal:** TAD, arreglos unidimensionales, redimensionamiento y matrices (repositorio de lecturas y matriz estación × hora)
* **Pregunta de la semana:** Los datos ya llegan limpios. ¿Dónde viven ahora y qué podemos preguntarles?

## 2\. Prediccion antes de ejecutar

No escribí una predicción antes de ejecutar el programa: lo ejecuté sin pensar antes qué resultado esperaba. Lo dejo dicho con honestidad y respondo lo que sí puedo responder.

1. **Que creo que va a ocurrir?**
No lo pensé antes de ejecutar. No tengo una predicción para comparar.
2. **Que parte del programa o del algoritmo puede fallar?**
No la definí antes. Ahora, después de ver los resultados, sé que lo que fallaba era el arreglo de capacidad fija (`cantidad == lecturas.length`, que dejaba de guardar lecturas al llegar a 10) y los promedios que dividían entre todas las estaciones o entre las 24 horas aunque algunas no hubieran reportado.
3. **Como comprobare mi prediccion?**
No la definí antes. Lo que sí hice fue ejecutar las pruebas, la ingesta y el experimento en mi computador y medir el tiempo del experimento tres veces. Para la próxima semana voy a escribir la predicción **antes** de ejecutar.

## 3\. Evidencia del laboratorio

### Resultado observado

Con el código original (arreglo de capacidad fija 10) el programa mostraba **10 lecturas almacenadas**, sin ninguna excepción, cuando el archivo tiene 211 filas y 201 son válidas (211 − 2 descartadas por formato − 8 por rango). Las 191 restantes se perdían en silencio.

Con `redimensionar()` implementado, `IngestaSensores` muestra **201 lecturas almacenadas**, 2 descartadas por formato y 8 por rango. El perfil horario corregido da, entre otros: 08:00 → 30.99, 09:00 → 11.18, 10:00 → 12.15, 11:00 → 11.34, 12:00 → 11.83, 13:00 → 12.53. La hora más contaminada es las 18:00 (34.01) y la estación más contaminada es EST-003 (17.86). Todo está en `docs/evidencia-s02.txt` y se reproduce con los comandos del README.

### Diferencia entre la prediccion y el resultado

Como no escribí una predicción antes de ejecutar, no puedo comparar lo esperado con lo observado de forma honesta. Lo que muestra el resultado es que el programa original guardaba 10 de las 201 lecturas válidas sin avisar y sin ninguna excepción. Lo que más me costó fue entender por qué el arreglo se llena y por qué hay que agrandarlo (ver la sección 5).

### Error o comportamiento inesperado

* **Que ocurrio?** El repositorio "corría sin caerse" y entregaba resultados incorrectos: (a) guardaba solo 10 de 201 lecturas; (b) el promedio horario de las 09:00 a las 12:00 salía más bajo de lo real (9.93 en lugar de 11.18 a las 09:00); (c) el promedio de EST-003 parecía normal (14.14) cuando en realidad es el más alto (17.86).
* **Por que ocurrio?** (a) El arreglo tiene capacidad fija y `agregar()` devolvía `false` sin que nadie lo revisara. (b) y (c) EST-003 no reportó de 09:00 a 12:00 (y EST-002 tampoco a las 12:00): en un `double\[]\[]` esas celdas valen 0.0 y el promedio dividía entre 9 estaciones o 24 horas aunque menos hubieran reportado.
* **Como lo corregimos o que falta corregir?** Se implementó `redimensionar()` (duplicando), se cambió la matriz a `Double\[]\[]` (`null` = sin dato) y los promedios dividen entre los datos que existen. Falta: el archivo trae la fila EST-002 15:00 repetida (idéntica); el repositorio la guarda dos veces. Detectar repetidos queda para la semana de `HashSet`.
* **Error del entorno (al ejecutar en mi computador):** `java -version` mostraba Java 8 (solo el entorno de ejecución) y `javac` no se reconocía, así que no se podía compilar. Causa: faltaba el JDK (el compilador) y el proyecto necesita Java 11 o superior. Solución: instalé Temurin JDK 21 y comprobé que `java -version` y `javac -version` mostraban la versión 21.

## 4\. Explicacion en lenguaje llano

> \*\*Borrador — reescríbelo con tus palabras.\*\* Un arreglo es como una fila de casillas numeradas que se compra con un tamaño fijo. Cuando se llenan, no se pueden agregar casillas: hay que comprar una fila más grande y pasar todo lo que había. Y si una casilla está vacía porque nadie midió, no se puede escribir "0", porque cero también es una medida real; hay que dejarla marcada como "sin dato".

### Ejemplo o analogia

Un casillero de 10 lockers en un colegio. Cada locker es una posición del arreglo y cada mochila es una lectura. Cuando llega la mochila 11 no se puede pegar un locker al lado: se busca un casillero de 20 y se traslada todo. Si un locker está vacío y en el registro aparece "0 mochilas", nadie sabe si está vacío o si alguien guardó algo de peso cero. La analogía deja de ser exacta en que trasladar 10 mochilas cuesta lo mismo que copiar 10 referencias en Java, pero Java copia en microsegundos; el costo real aparece cuando son millones de lecturas.

## 5\. El vacio que encontre

* **Mi duda concreta es:** por qué el arreglo se llena y hay que agrandarlo: ¿por qué en Java no se puede simplemente agregar más casillas a un arreglo que ya existe?
* **Lo que ya puedo explicar es:** un arreglo se crea con un tamaño fijo. Cuando `cantidad` llega a `lecturas.length` ya no quedan casillas libres, y en el código original `agregar()` devolvía `false` y esa lectura se perdía.
* **Para resolver la duda consulte:** el código de `redimensionar()`, el trazado de la sección 6 (10 → 20 → 40 → 80 → 160 → 320), el experimento que cuenta las copias y la explicación paso a paso que me dio una IA (Claude).
* **Ahora lo entiendo asi:** un arreglo en Java no se puede estirar. Cuando se llena hay que crear uno más grande (por ejemplo del doble), copiar todo lo que había y seguir agregando en el nuevo. Duplicar hace que se copie muy poco en total: 310 copias frente a 20.055 si creciera de a una posición.

## 6\. Trazado de la solucion

**Caso 1 — cargar las 201 lecturas válidas con capacidad inicial 10 (duplicando):**

|Paso|Estado de la estructura|Decision o resultado|
|-|-|-|
|1|`length = 10`, `cantidad = 10` (lecturas 1 a 10)|Llega la lectura 11: `cantidad >= lecturas.length`, hay que crecer.|
|2|`redimensionar()`: arreglo nuevo de 20, se copian 10 elementos|`copias = 10`, `redimensionamientos = 1`.|
|3|`lecturas\[10] = lectura 11`, `cantidad = 11`|La lectura 11 ya quedó guardada.|
|4|Lo mismo al llegar a 21, 41, 81 y 161 lecturas|Capacidades 40, 80, 160 y 320; copias acumuladas 30, 70, 150 y 310.|
|5|Final: `cantidad = 201`, `length = 320`|5 redimensionamientos, 310 copias, 119 casillas libres.|

**Caso 2 — promedio de las 09:00:**

|Paso|Estado de los datos|Decision o resultado|
|-|-|-|
|1|Fila 09 de la matriz: EST-001 10.5, EST-002 10.2, **EST-003 sin dato**, EST-004 11.7, EST-005 10.9, EST-006 12.3, EST-007 10.4, EST-008 11.0, EST-009 12.4|`hayDato()` es `false` solo para EST-003.|
|2|Suma de los datos que existen = 89.4; estaciones que reportaron = 8|Se cuenta cuántas reportaron, no cuántas hay.|
|3|89.4 / 9 = 9.93 (incorrecto) vs 89.4 / 8 = 11.175|El promedio correcto es 11.18.|

## 7\. Decision de diseño

**Decisión A — cómo representar que una estación no reportó**

* **Problema que debiamos resolver:** distinguir "PM2.5 = 0.0" de "la estación no reportó" en la matriz estación × hora.
* **Estructura, algoritmo o estrategia elegida:** `Double\[]\[]` con `null` para "sin dato", y acceso por `hayDato()`/`valorEn()`.
* **Alternativa descartada:** `boolean\[]\[]` paralela (y un valor centinela como −1).
* **Por que elegimos la primera:** una sola estructura que no se puede desincronizar y que dice explícitamente qué es cada celda. Costo: los objetos `Double` ocupan más memoria y hay riesgo de `NullPointerException`, que se controla encapsulando el acceso.
* **Que evidencia respalda la decision:** EST-003 pasa de 14.14 (dividiendo entre 24) a 17.86 (sobre sus 19 horas con dato) y resulta ser la más contaminada; las pruebas del caso 6 verifican que un cero real cuenta y una ausencia no.

**Decisión B — cómo crece el repositorio**

* **Problema que debiamos resolver:** el arreglo de capacidad 10 perdía 191 lecturas en silencio.
* **Estructura, algoritmo o estrategia elegida:** duplicar la capacidad en `redimensionar()`.
* **Alternativa descartada:** crecer de a una posición (y subir la capacidad fija a 500).
* **Por que elegimos la primera:** 5 redimensionamientos y 310 copias frente a 191 y 20.055; el costo es memoria libre (119 casillas).
* **Que evidencia respalda la decision:** los contadores del repositorio (`getCopiasRealizadas()`, `getRedimensionamientos()`) y `docs/evidencia-s02.txt`.

## 8\. Aporte al proyecto

* **Archivo(s) o modulo(s) trabajado(s):** `src/RepositorioLecturas.java`, `src/AnalizadorMatriz.java`, `src/PruebasRepositorio.java`, `src/Experimento.java`, `docs/contrato-repositorio.md`, `docs/decisiones.md`, `docs/evidencia-s02.txt`.
* **Cambio realizado:** repositorio completo (agregar, obtener, buscar, actualizar, eliminar por compactación, redimensionamiento) y matriz que distingue ausencia de cero, con promedios, hora y estación más contaminadas.
* **Como se conecta con la capa anterior:** `IngestaSensores` valida cada fila (formato y rango) y solo las lecturas válidas llegan a `repositorio.agregar()` y `analizador.registrar()`.
* **Que queda pendiente para la siguiente semana:** la búsqueda recorre el arreglo: `buscarPorEstacion("EST-004")` hizo 64 comparaciones y una estación inexistente hace 201. Con 8.000 estaciones serían 192.000 por consulta; hay que buscar de forma más eficiente. También queda detectar lecturas repetidas.

## 9\. Commits realizados

Un commit es cada vez que guardé cambios en GitHub. Cada uno tiene un mensaje y un código corto (hash). Estos son los commits que muestran mi aporte:

|Commit|Mensaje|Que demuestra|
|-|-|-|
|✍ `\[hash 1]`|`S02: agrega codigo del repositorio, la matriz y las pruebas`|Carpeta `src`: `RepositorioLecturas`, `AnalizadorMatriz`, pruebas y experimento|
|✍ `\[hash 2]`|`S02: agrega documentacion y evidencia`|Carpetas `docs` y `data` y el `README.md`: contrato, decisiones y evidencia de ejecución|

## 10\. Reexplicacion final

> \*\*Borrador — reescríbelo con tus palabras.\*\* Los datos viven en un repositorio que promete operaciones (guardar, buscar, cambiar, borrar, contar) y esconde que por dentro es un arreglo. Como un arreglo tiene techo, lo hice crecer duplicándose: 310 copias en lugar de 20.055. Y como una celda vacía no es un cero, guardé las ausencias como `null` para que EST-003 no pareciera normal cuando en realidad es la estación más contaminada.

## 11\. Reflexion individual

1. **Lo que ahora puedo hacer y antes no podia:**
Instalar un JDK, compilar y ejecutar un programa Java desde la terminal (`javac` y `java`) y medir el tiempo de un experimento. Antes tenía solo Java 8 sin compilador y no podía compilar. También puedo explicar por qué un arreglo tiene que crecer y por qué duplicar su tamaño cuesta muchas menos copias que crecer de a una posición.
2. **El error o supuesto que mas me enseno:**
Que un programa puede "funcionar" sin dar ningún error y aun así entregar resultados incorrectos: el programa original mostraba 10 lecturas almacenadas y no avisaba que había perdido 191. Lo mismo pasa con los promedios: un dato que falta no es un cero.
3. **La pregunta que llevaria a la proxima clase:**
Si buscar una estación entre 192.000 lecturas puede costar 192.000 comparaciones, ¿cómo se puede buscar sin recorrer todo el arreglo?
4. **Que parte del trabajo fue realmente mia:**
Instalé el JDK 21, compilé y ejecuté todo en mi computador, medí los tiempos del experimento (9.419, 9.722 y 9.637 microsegundos), leí el código y traté de entenderlo, y escribí o cambié partes del código. También revisé los resultados ejecutándolos yo mismo y trabajé lo que no entendía (por qué el arreglo debe crecer, sección 5).

   **Uso de IA:** usé Claude y Gemini como herramientas de apoyo. Claude me dio el código de partida de `RepositorioLecturas` y `AnalizadorMatriz`, las pruebas, el experimento y borradores de la documentación, y me explicó los pasos cuando tuve dudas. La IA no ejecutó ni midió nada por mí: eso lo hice yo en mi computador.

## 12\. Preguntas de la guia de la Semana 2

> Son las cinco preguntas que pide la guía. Las cifras salen de `lecturas\_ampliadas.csv`; reescribe con tus palabras.

**1. ¿Qué es un TAD? (máx. 5 líneas, sin las palabras "abstracto", "interfaz" ni "implementación")**

Es una descripción de lo que se le puede hacer a un conjunto de datos —guardar, buscar, cambiar, borrar, contar— junto con las reglas de cada operación, sin decir cómo se guardan por dentro. Quien lo usa solo conoce esas operaciones y sus promesas. Por eso, quien lo construye puede cambiar el arreglo por una lista enlazada u otra estructura sin que el resto del programa se entere.

**2. Copias al cargar las 211 filas (201 válidas, capacidad inicial 10)**

|Estrategia|Redimensionamientos|Copias|Capacidad final|
|-|-:|-:|-:|
|Crecer de uno en uno|191|20.055|201|
|Duplicar|5|310|320|

Duplicar hizo unas 65 veces menos copias (20.055 / 310 ≈ 64,7). Crecer de uno en uno vuelve a copiar casi todo el arreglo en cada inserción, así que el trabajo total crece de forma cuadrática; duplicando cada elemento se copia pocas veces y el total queda por debajo de 2n (310 < 402). El precio es memoria reservada sin usar: 119 casillas vacías de 320. La conclusión sale de la medición, no de la intuición.

**3. Estrategia para `eliminar()`: compactar**

Mantiene el orden cronológico y no deja huecos: `tamano()` es siempre la cantidad real y todos los recorridos (`i < cantidad`) siguen siendo simples. Se lee y se busca mucho más de lo que se elimina, y con 201 lecturas el peor caso son 200 desplazamientos. Marcar huecos obligaría a mantener un `boolean\[] ocupada` y a que la búsqueda, los promedios y la matriz supieran saltarlos. Cambiaría de opinión con muchísimas eliminaciones sobre millones de lecturas (ahí convendría una lista enlazada). También se anula `lecturas\[cantidad - 1]` para no dejar una referencia duplicada.

**4. El cero fantasma**

Usé `Double\[]\[]`: `null` = no reportó y `0.0` = midió cero. Descarté (a) la `boolean\[]\[]` paralela, porque son dos estructuras que hay que mantener sincronizadas; (b) un centinela como −1, porque puede colarse en un promedio si alguien olvida filtrarlo o ser algún día un valor legítimo; y (c) `if (valor != 0)`, porque elimina las mediciones reales de cero. El riesgo de `Double` (`NullPointerException`) lo controlo con `hayDato()`/`valorEn()`, y devuelvo `NaN` (no 0) cuando no hay ningún dato.

**5. ¿Sirve `buscarPorEstacion()` con 8.000 estaciones?**

Con 8.000 estaciones × 24 horas hay 192.000 lecturas. Una búsqueda en el peor caso (estación inexistente) hace **192.000 comparaciones**, unas 955 veces más que las 201 de hoy. En el experimento (`--escala`) tardó unos 9,6 ms en mi computador (9.419, 9.722 y 9.637 microsegundos en tres ejecuciones). Para una consulta aislada todavía es tolerable. Pero si se consulta cada estación una vez (8.000 búsquedas) son 8.000 × 192.000 = **1.536.000.000 comparaciones**, y ahí deja de ser adecuada. Es la pregunta de la Semana 3. La matriz, en cambio, accede a una celda por índice en un solo paso.

## Lista de verificacion antes de entregar

* \[x] Escribi la prediccion antes de consultar el resultado.
* \[x] Inclui evidencia concreta del laboratorio.
* \[x] Explique un concepto sin depender de jerga.
* \[x] Registre un vacio, una duda o un error real.
* \[x] Trace al menos un caso paso a paso.
* \[x] Justifique una decision del proyecto y una alternativa descartada.
* \[x] Registre mis commits y mi aporte individual.
* \[x] Deje claro que queda pendiente.
* \[x] Renombre el archivo con el formato `sXX-nombre.md`.


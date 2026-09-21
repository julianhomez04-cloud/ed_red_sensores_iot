# Proyecto Integrador Red de Sensores IoT
>Proyecto base del curso de estructura de datos.

## Modulo de ingesta

`src/IngestaSensores.java` lee el archivo `data/lecturas.csv` y muestra:

- Cada lectura con su estacion, fecha, temperatura, humedad y PM2.5.
- La cantidad de registros procesados.
- El promedio de temperatura, humedad y PM2.5.
- La estacion con el valor mas alto de PM2.5.

### Conceptos principales

- `BufferedReader` lee el archivo una linea a la vez.
- `split(",")` separa las columnas del CSV.
- `Double.parseDouble` convierte texto numerico a `double`.
- Los acumuladores suman los valores para calcular promedios.
- La condicion `if` compara cada PM2.5 con el maximo encontrado.

### Ejecucion

Desde la carpeta `src`, con un JDK instalado:

```text
javac IngestaSensores.java
java IngestaSensores
```

El programa espera encontrar `lecturas.csv` en la carpeta desde la que se
ejecuta. Por ejemplo, copia el CSV a `src` o ejecuta el programa desde `data`
ajustando la ruta del archivo en el codigo.

> Nota: el CSV de ejemplo contiene valores vacios y textos como `ERR`. El
> programa actual intenta convertir todos los valores de medicion a numero,
> por lo que esas filas pueden producir un error durante la ejecucion.

## Semana 2 - Donde viven los datos

Componentes nuevos sobre la ingesta:

- `src/RepositorioLecturas.java`: TAD de almacenamiento respaldado por un arreglo que crece (agregar, obtener, buscarPorEstacion, actualizar, eliminar, tamano).
- `src/AnalizadorMatriz.java`: matriz estacion x hora de PM2.5 (`Double[][]`, `null` = sin dato).
- `src/PruebasRepositorio.java`: los 6 casos de prueba de la guia.
- `src/Experimento.java`: diagnostico, medicion de copias y analisis de la matriz con el archivo real.
- `docs/contrato-repositorio.md`, `docs/decisiones.md` (S2) y `docs/evidencia-s02.txt`.

### Ejecucion (desde la raiz del repositorio, con un JDK instalado)

```text
javac -d out src/*.java
java -cp out PruebasRepositorio
java -cp out Experimento data/lecturas_ampliadas.csv
java -cp out Experimento data/lecturas_ampliadas.csv --escala
cd data
java -cp ../out IngestaSensores
```

`IngestaSensores` busca `lecturas_ampliadas.csv` en la carpeta desde la que se ejecuta, por eso se corre desde `data`. La carpeta `out/` esta en `.gitignore`.

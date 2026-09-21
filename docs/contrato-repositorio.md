# Hoja del contrato — `RepositorioLecturas` (Fase 0)

Documento de la Fase 0: se define qué promete el TAD **antes** de decidir cómo se guarda por dentro.

## Operaciones del contrato (públicas)

| Operación | Parámetros | Devuelve | Comportamiento esperado | Situación inválida |
|---|---|---|---|---|
| `agregar` | `LecturaSensor lectura` | `boolean` | Guarda la lectura al final. Si no hay espacio, el repositorio crece y la guarda. | Lectura `null` → `false`, no se guarda. |
| `obtener` | `int posicion` | `LecturaSensor` | Devuelve la lectura de esa posición. | Posición negativa, `>= tamano()` o repositorio vacío → `null`. |
| `buscarPorEstacion` | `String idSensor` | `LecturaSensor` | Devuelve la **primera** lectura de esa estación. | `idSensor` `null`, estación inexistente o repositorio vacío → `null`. |
| `actualizar` | `int posicion`, `LecturaSensor nueva` | `boolean` | Reemplaza la lectura de esa posición. | Posición inválida o `nueva` `null` → `false`, sin cambios. |
| `eliminar` | `int posicion` | `boolean` | Quita la lectura, compacta y `tamano()` baja en 1. | Posición inválida o repositorio vacío → `false`, sin cambios. |
| `tamano` | — | `int` | Número **real** de lecturas (no la capacidad del arreglo). | — |
| `promedioPm25` | — | `double` | Promedio de PM2.5 de las lecturas almacenadas. | Repositorio vacío → `NaN` (no existe promedio de nada). |

## Detalles internos (privados; se pueden cambiar sin afectar a quien usa el repositorio)

- `lecturas` (el arreglo) y `cantidad`.
- `redimensionar()` y `posicionValida()`.
- La estrategia de crecimiento y los contadores de medición.

## Extras públicos que NO son parte del contrato

`buscarPosicionPorEstacion()`, `capacidad()`, `estaVacio()`, `getEstrategia()`, `getCopiasRealizadas()`, `getRedimensionamientos()`, `getDesplazamientosRealizados()` y `getComparacionesUltimaBusqueda()`. Son ayudas de diagnóstico y medición; el resto del programa no debería depender de ellas.

## Regla de encapsulamiento

Los atributos son `private` y el arreglo nunca se expone: nadie puede dejar el repositorio en un estado inconsistente (por ejemplo, `cantidad = 999999`).

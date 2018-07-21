---
language: "Spanish"
title: "Tarea 3 - Diseño Avanzado de Algoritmos"
author: "Francisco Casas Barrientos"
urlcolor: blue
---

# Supuestos

- Se supone que sólo los puntajes positivos (que equivalen a cercanía) han de considerarse ya que una distancia negativa no debería servir para ser un vecino más cercano más que el no contar con la información de dicho arco.
- En caso de empate al momento de considerar vecinos más cercanos, todos los vecinos que empatan son considerados, como efecto puede haber más de K vecinos más cercanos, pero pareció tener más sentido que romper empates arbitrariamente.
- Se supone que la muestra ponderada es para alcanzar una cantidad máxima de corepoints en cada partición y que la ponderación es una ponderación de la probabilidad de cada nodo de ser escogido, una vez elegido un nodo se saca de su clúster y sea actualizan las ponderaciones (de otra forma, dada la fórmula, sería claro que los clústers más grandes terminarían con menos puntos seleccionados en promedio).

# Implementación

El código se implementó separándolo en 3 archivos, `main.scala`, `ops.scala` y `unionfind.scala`.

* `unionfind.scala` simplemente proveé la estructura de datos union-find para realizar clústering, junto con funciones auxiliares para obtener los grupos de puntos.
* `ops.scala` tiene funciones que realizan las operaciones principales del algoritmo:
    * `nearestNeighboors`: computa los vecinos más cercanos de cada punto a partir de los arcos que miden la distancia, adicionalmente puede recibir el parámetro `nodes` para sólo considerar un subconjunto de los nodos.
    * `corePoints`: A partir de los vecinos más cercanos identifica los corepoints, recibiendo `minpts` como la densidad mínima.
    * `clustering`: Se encarga de realizar clústering de los nodos si su SNN es mayor que eps.
    * `selectPonderated`: Se encarga de elegir una muestra aleatoria ponderada de nodos que se encuentran separados por clúster.
* `main.scala` contiene el código principal, usando directamente Spark.

Spark se usa para particionar los datos y correr la primera fase del algoritmo en cada partición, para distribuír aleatoriamente los datos se utiliza `repartition` y la selección de los corepoints en cada una de las particiones se realiza en la función que se pasa como argumento a `mapPartitions`, finalmente se recolectan los corepoints usando `collect` para ser finalmente computados en el nodo maestro.

# Instalación

## Setup de Java y spark

En Ubuntu, instalar java 8:
```bash
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java8-installer
```

Descargar Spark (versión spark-2.3.1) de [aquí](https://spark.apache.org/downloads.html).

**Suponiendo** que se descomprime en `~/Videos`...

Agregar siguientes líneas al archivo `~/.profile`:
```bash
PATH="$HOME/Videos/spark-2.3.1-bin-hadoop2.7/bin:$PATH"
JAVA_HOME="/usr/lib/jvm/java-8-oracle/"
```
Actualizar variables en la terminal (o reiniciar para hacerlo permanente en todas):
```bash
source ~/.profile
```
Correr spark como standalone, [instrucciones tomadas de aquí](https://spark.apache.org/docs/latest/spark-standalone.html).
```bash
~/Videos/spark-2.3.1-bin-hadoop2.7/sbin/start-master.sh
~/Videos/spark-2.3.1-bin-hadoop2.7/sbin/start-slave <master_url>
```
Donde `<master_url>` es la URL que sale en `http://localhost:8080/` tras iniciar el master.

## NOTA:

Recordar settear correctamente la variable `spark_home` y `master_url` en `src/main/scala/main.scala`.


## Compilación

La compilación se realiza utilizando `sbt`, lo que se puede hacer utilizando el comando:

```bash
sudo apt-get install sbt
```

Para compilar:

```bash
make compile
```

Si `sbt` da en particular el error `Server access Error: java.lang.RuntimeException: Unexpected error: java.security.InvalidAlgorithmParameterException: the trustAnchors parameter must be non-empty`, revisar [siguiente respuesta en stackoverflow](https://stackoverflow.com/a/50103533/3827), tal como indica la [documentacion de sbt](https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Linux.html).

## Ejecutar

Finalmente se puede hacer:

```bash
make run
```

Las etiquetas finales quedan en `results/labels` una vez ejecutado el código.

### NOTA:

Recordar que para que funcione debe estar corriendo el proceso **master** y al menos un **slave**.

# Resultados

- Se notó que particionando mucho (8 particiones) es díficil que cada partición entregue más de un clúster, pues un `eps=2` resulta en que todos los nodos terminan en el mismo clúster y un `eps=3` resulta en que no hay corepoints finales. Se bajó a 4 particiones para tener resultados más aceptables.

- Se probó con varias combinaciones de parámetros y el algoritmo resultó en 0 o 1 clúster de puntos. Se pudo modificar este comportamiento agregando un `eps` mayor a la hora de realizar el clústering de los corepoints finales, específicamente la variable `finalcore_eps` en `src/main/scala/main.scala`. Esto hace variar la cantidad de clúster dependiendo del comportamiento aleatorio del algoritmo.

Con los parámetros actuales:

| Parameter       | Value   |
| :-------------- | ------: |
| `n_partitions`  |       4 |
| `k`             |      10 |
| `eps`           |       4 |
| `finalcore_eps` |       6 |
| `minpts`        |       2 |

Se realizaron 3 experimentos, que resultaron en 5,9 y 11 clústers, de los siguientes tamaños:

| Cluster | `labels01` | `labels02` | `labels03` |
| :------ | ---------: | ---------: | ---------: |
|       0 |        231 |        220 |         76 |
|       1 |          5 |          5 |          8 |
|       2 |          4 |          5 |        106 |
|       3 |          2 |          3 |          7 |
|       4 |          6 |          1 |          9 |
|       5 |         -- |          2 |          4 |
|       6 |         -- |          1 |          2 |
|       7 |         -- |          4 |          3 |
|       8 |         -- |          1 |          6 |
|       9 |         -- |         -- |          3 |
|      10 |         -- |         -- |          1 |

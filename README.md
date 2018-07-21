# daa3
Tarea 3 DAA.

# Supuestos

- Se supone que sólo las distancias positivas (que equivalena cercanía) han de considerarse ya que una distancia negativa no debería servir para ser un vecino más cercano más que el no contar con información sobre el arco.
- En caso de empate al momento de considerar vecinos más cercanos, todos los vecinos que empatan son considerados, como efecto puede haber más de K vecinos más cercanos, pero pareció tener más sentido que romper empates arbitrariamente.

# Instalación

## Setup de spark

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

Para compilar:

`make compile`

Si `sbt` da en particular el error `Server access Error: java.lang.RuntimeException: Unexpected error: java.security.InvalidAlgorithmParameterException: the trustAnchors parameter must be non-empty`, revisar [siguiente respuesta en stackoverflow](https://stackoverflow.com/a/50103533/3827), tal como indica la [documentacion de sbt](https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Linux.html).

## Ejecutar

Finalmente se puede hacer:

```bash
make run
```

### NOTA:

Recordar que para que funcione debe estar corriendo el proceso master y al menos un slave.

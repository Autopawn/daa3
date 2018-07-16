# daa3
Tarea 3 DAA.

# Instalación

## Setup de spark

En Ubuntu, instalar java 8:
```bash
sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java8-installer
```

Descargar Spark (versión spark-2.3.1) de [aquí](https://spark.apache.org/downloads.html).

Suponiendo que se descomprime en `~/Videos`...

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

## Compilación

Para compilar:

Descargar e instalar Scala:
```bash
sudo apt remove scala-library scala
wget www.scala-lang.org/files/archive/scala-2.12.6.deb
sudo dpkg -i scala-2.12.6.deb
```

## Ejecutar

Finalmente se puede hacer:

```bash
make compile
make run
```

**NOTA**: Recordar que para que funcione debe estar corriendo el proceso master y al menos un slave.

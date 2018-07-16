SPARK_CORE = spark-core_2.11-2.3.1.jar

compile:
	# Compile with scala:
	scalac -classpath "$(SPARK_CORE)" src/test.scala
	# Create jar:
	jar -cvf test.jar Test*.class
run:
	spark-submit --class Test --master local test.jar

	#spark-core_2.10-1.3.0.jar/usr/local/spark/lib/spark-assembly-1.4.0-hadoop2.6.0.jar

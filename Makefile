compile:
	sbt package
run:
	rm -r results || true
	mkdir -p results
	spark-submit --class "Clustering" --master local[2] target/scala-2.11/clustering_2.11-1.0.jar
doc:
	pandoc README.md -o informe.pdf

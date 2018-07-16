import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark._

object Test {
    def main(args: Array[String]) {
        val master_url = "spark://graya:7077"
        val spark_home = "/home/fcasas/Videos/spark-2.3.1-bin-hadoop2.7"
        val app_name = "Test"
        val sc = new SparkContext(master_url,app_name,spark_home)

        val datafile = sc.textFile("bitcoinalpha.csv")

        datafile.saveAsTextFile("results")

        System.out.println("OK");
    }
}

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark._

import collection.mutable.HashMap
import collection.mutable.HashSet

object Clustering {

    def main(args: Array[String]) {
        val n_partitions: Int = 8
        val k = 10
        val eps = 3
        val minpts = 3 // Minimum density to be core point.
        val max_corept_part = 5 // Maximum core points per partition

        val master_url = "spark://graya:7077"
        val spark_home = "/home/fcasas/Videos/spark-2.3.1-bin-hadoop2.7"
        val app_name = "Clustering"
        val sc = new SparkContext(master_url,app_name,spark_home)

        val df = sc.textFile("bitcoinalpha.csv")
        val df_parsed = df.map(lin => {
            val vals = lin.split(",")
            (vals(0).toInt,vals(1).toInt,vals(2).toInt)
        })
        val df_repart = df_parsed.repartition(n_partitions)
        val df_coreps = df_repart.mapPartitions(ks => {
            // List of (a,b,score):
            var dists : List[(Int,Int,Int)] = Nil
            for(k <- ks){
                dists = k :: dists
            }
            // Nearest neighboors for each node:
            val nneighs : HashMap[Int,HashSet[Int]] = Ops.nearestNeighboors(k, dists, null)
            // Core points and their computed SNN
            val (cpts,snn) : (List[Int],HashMap[(Int,Int),Int]) =
                Ops.corePoints(eps, minpts, nneighs)
            // Clustering
            val clusters : Array[List[Int]] = Ops.clustering(eps,cpts,snn)
            // Select core points ponderated by their pertenency to a cluster
            val sel_cpts : List[Int] = Ops.selectPonderated(clusters,max_corept_part)
            sel_cpts.toIterator
        })

        // Collect the corepoints and distances again, here in the master:
        val dists : Array[(Int,Int,Int)] = df_parsed.collect()
        val corepts : Array[Int] = df_coreps.collect()
        // Nearest neighboors only between corepoints:
        val c_set : HashSet[Int] = HashSet() ++ corepts
        val c_nneighs : HashMap[Int,HashSet[Int]] = Ops.nearestNeighboors(k,dists.toList,c_set)
        // Corecore points and their computed SNN
        val (c_cpts,c_snn) : (List[Int],HashMap[(Int,Int),Int]) =
            Ops.corePoints(eps, minpts, c_nneighs)
        // Clustering
        val c_clusters : Array[List[Int]] = Ops.clustering(eps,c_cpts,c_snn)





        // // Send every core point to the same partition:
        // val df_merged = df_coreps.coalesce(1)
        // val df_final = df_merged.mapPartitions(cpts => {
        //     val abomination = df_parsed.collect()
        //
        //     // val nneighs : HashMap[Int,HashSet[Int]] = Ops.nearestNeighboors(k, dists)
        //     abomination.toIterator
        // })

        // df_final.saveAsTextFile("results")

        System.out.println("OK");
    }
}

// def

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark._

import java.io.File
import java.io.PrintWriter

import collection.mutable.HashMap
import collection.mutable.HashSet

object Clustering {

    def main(args: Array[String]) {
        val n_partitions = 4
        val max_corept_part = 10 // Maximum core points per partition

        val k = 10
        val eps = 4
        val minpts = 2 // Minimum density to be core point.

        val finalcore_eps = 6

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
            val nneighs : HashMap[Int,HashSet[Int]] = Ops.nearestNeighboors(k,dists,null)
            // Core points and their computed SNN
            val (cpts,snn) : (HashSet[Int],HashMap[(Int,Int),Int]) =
                Ops.corePoints(eps,minpts,nneighs)
            // Clustering
            val uf : UnionFind = Ops.clustering(eps,cpts,snn)
            val clusters : Array[List[Int]] = uf.retrieve()
            // Select core points ponderated by their pertenency to a cluster
            val sel_cpts : List[Int] = Ops.selectPonderated(clusters,max_corept_part)
            // Print number of core points and clusters:
            println("------------- CORE: "+sel_cpts.length+" / "+cpts.size)
            println("------------- CLUSTERS: "+clusters.length)
            //
            sel_cpts.toIterator
        })

        // Collect the corepoints and computee distances again, here in the master:
        val corepts : Array[Int] = df_coreps.collect()
        println("------------- TOTAL COREPTS: "+corepts.length)
        val dists : List[(Int,Int,Int)] = df_parsed.collect().toList
        // Nearest neighboors only between corepoints:
        val c_set : HashSet[Int] = HashSet() ++ corepts
        val c_nneighs : HashMap[Int,HashSet[Int]] = Ops.nearestNeighboors(k,dists,c_set)
        // final core points and their computed SNN
        val (c_cpts,c_snn) : (HashSet[Int],HashMap[(Int,Int),Int]) =
            Ops.corePoints(eps,minpts,c_nneighs)
        println("------------- TOTAL CORECOREPTS: "+c_cpts.size)
        // Clustering of final core points
        val c_clusters : UnionFind = Ops.clustering(finalcore_eps,c_cpts,c_snn)
        println("------------- TOTAL CLUSTERS: "+c_clusters.nGroups())
        // Nearest neighboors for all nodes (to compute the SNN with the corepoints):
        val nneighs : HashMap[Int,HashSet[Int]] = Ops.nearestNeighboors(k,dists,null)
        // Join each node to its nearest final corepoint
        for(i <- nneighs.keys){
            var nearest : Int = -1
            if(c_cpts contains i){
                nearest = i
            } else {
                var max_simil : Int = eps
                for(ccp <- c_cpts){
                    val common = (nneighs(i) & nneighs(ccp)).size
                    if(common>=max_simil) nearest = ccp
                }
            }
            if(nearest>=0){
                c_clusters.add(i)
                c_clusters.union(i,nearest)
            }
        }
        // Get the final labels for each node, save them on a file
        val pw = new PrintWriter(new File("results/labels"))
        val labels : HashMap[Int,Int] = c_clusters.retrieveLabels()
        for(n <- labels.keys){
            pw.write(n+","+labels(n)+"\n")
        }
        pw.close()

        println("OK");
    }
}

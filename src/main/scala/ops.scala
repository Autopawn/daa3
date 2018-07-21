import collection.mutable.HashMap
import collection.mutable.HashSet
import scala.util.Random

object Ops {

    def nearestNeighboors(k: Int, dists: List[(Int,Int,Int)], nodes: HashSet[Int]) : HashMap[Int,HashSet[Int]] = {
        // For each node, create an array of lists of neighboors, one list for each distance.
        var hm : HashMap[Int,Array[List[Int]]] = new HashMap[Int,Array[List[Int]]]
        dists.foreach( tpl => {
            val (a,b,dist) = tpl
            val concern : Boolean = nodes==null || ((nodes contains a) && (nodes contains b)) 
            if(dist>=0 && concern){
                if(! (hm contains a)){
                    hm(a) = Array.fill[List[Int]](11)(Nil) // There are only 11 possible distances.
                }
                // Add b as neighboor on distance 10-dist
                hm(a)(dist) = b :: hm(a)(dist)
            }
        })
        // For each node, create the final list of neighboors:
        hm.map( kv => {
            val (a,arr) = kv
            var neighs : HashSet[Int] = new HashSet[Int]
            var remain : Int = k
            for(i <- 10 to 0 by -1){
                if(remain>0){
                    val len : Int = arr(i).length
                    arr(i).foreach(v => neighs += v)
                    remain -= len
                }
            }
            (a,neighs)
        })
    }

    def corePoints(eps: Int, minpts: Int, nneighs: HashMap[Int,HashSet[Int]]) : (List[Int],HashMap[(Int,Int),Int]) = {
        // Compute corepoints:
        var coreps : List[Int] = Nil
        for(a <- nneighs.keys){
            var density : Int = 0
            for(b <- nneighs.keys){
                val common = (nneighs(a) & nneighs(b)).size
                if(common>=eps) density += 1
            }
            if(density>=minpts) coreps = a :: coreps
        }
        // Compute SNN between corepoints:
        var snn : HashMap[(Int,Int),Int] = new HashMap[(Int,Int),Int]
        for(a <- coreps){
            for(b <- coreps){
                val common = (nneighs(a) & nneighs(b)).size
                snn((a,b)) = common
            }
        }
        (coreps,snn)
    }

    def clustering(eps: Int, cores: List[Int], snn: HashMap[(Int,Int),Int]) : Array[List[Int]] = {
        // Create a new group for each core point
        var uf : UnionFind = new UnionFind
        for(cp <- cores) uf.add(cp)
        // Join all the corepoints that have a snn of at least eps
        for(p <- snn.keys){
            if(snn(p) <= eps){
                val (a,b) = p
                uf.union(a,b)
            }
        }
        // Get all the groups
        val groups : Array[List[Int]] = uf.retrieve()
        groups
    }

    def selectPonderated(cps: Array[List[Int]], nsel : Int) : List[Int] = {
        // Get total number of nodes:
        var totaln : Int = 0
        for(part <- cps){
            totaln += part.size
        }
        // Get total ponderation
        var totalp : Double = 0.0
        for(part <- cps){
            totalp += (1.0-part.size/totaln.toDouble)/2.0
        }
        // Pick nsel points:
        var selected : List[Int] = Nil
        if(nsel>=totaln){
            // NOTE: can be done faster with segment trees
            var roulette : Double = Random.nextDouble*totalp
            // First pick the set:
            for(i <- 0 to cps.size-1){
                if(roulette>0 && cps(i).size>0){
                    roulette -= (1.0-cps(i).size/totaln.toDouble)/2.0
                    if(roulette<=0){
                        // Update total ponderation:
                        totalp -= (1.0-cps(i).size/totaln.toDouble)/2.0
                        // Pick random element from cps(i)
                        val p :Int = Random.nextInt%cps(i).size
                        val x : Int = (cps(i).drop(p)).head
                        // Save point and set
                        selected = x :: selected
                        // Update the set, remove element
                        cps(i) = List.concat(cps(i).take(p),cps(i).drop(p+1))
                        // Recover total ponderation
                        if(cps(i).size>0){
                            totalp += (1.0-cps(i).size/totaln.toDouble)/2.0
                        }
                    }
                }
            }
        }else{
            // Just create a list with everything:
            for(i <- 0 to cps.size-1){
                for(x <- cps(i)){
                    selected = x :: selected
                }
            }
        }
        selected
    }

}


/*

Form clusters from the core points.
If two core points are within a radius Eps of each other then they are placed in the same cluster.

Discard all noise points. All non-core points that are not within a radius of Eps of a core point are discarded.

Assign all non-noise, non-core points to clusters. We can do this by assigning such points to the nearest core point.

*/

import collection.mutable.HashMap
import collection.mutable.HashSet

class UnionFind() {
    private var parent : HashMap[Int,Int] = new HashMap[Int,Int]
    private var rank : HashMap[Int,Int] = new HashMap[Int,Int]
    private var n_groups : Int = 0

    def add(p : Int) : Boolean = {
        if (this.parent contains p) false else {
            this.parent(p) = p
            this.rank(p) = 0
            this.n_groups += 1
            true
        }
    }

    def nGroups() : Int = {
        n_groups
    }

    def find(aa: Int) : Int = {
        if(this.parent(aa)==aa) aa else {
            this.parent(aa) = this.find(this.parent(aa))
            this.parent(aa)
        }
    }

    def union(aa: Int, bb: Int) = {
        var ga : Int = this.find(aa);
        var gb : Int = this.find(bb);
        if(ga!=gb){
            this.n_groups -= 1;
            if(this.rank(gb) < this.rank(ga)){
                this.parent(gb) = ga;
            }else{
                if(this.rank(gb) == this.rank(ga)) this.rank(gb) += 1;
                this.parent(ga) = gb;
            }
        }
    }

    def retrieveLabels() : HashMap[Int,Int] = {
        var hm : HashMap[Int,Int] = new HashMap[Int,Int]
        val retri : Array[List[Int]] = this.retrieve()
        for(i <- 0 to retri.length-1){
            for(x <- retri(i)){
                hm(x) = i
            }
        }
        hm
    }

    def retrieve() : Array[List[Int]] = {
        var nxt_idx : Int = 0
        var trnf : HashMap[Int,Int] = new HashMap[Int,Int]
        var parts : Array[List[Int]] = new Array[List[Int]](this.n_groups)
        for(p <- this.rank.keys){
            val gr : Int = this.find(p)
            if(!(trnf contains gr)){
                trnf(gr) = nxt_idx
                parts(nxt_idx) = Nil
                nxt_idx += 1
            }
            parts(trnf(gr)) = p :: parts(trnf(gr))
        }
        parts
    }


}

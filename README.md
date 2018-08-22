# spark-pscan
A fast community detection algorithm for large scale graph.

spark implementation of 

>Zhao, W., Martha, V., & Xu, X. (2013, March). **PSCAN: a parallel Structural clustering algorithm for big networks in MapReduce**. In Advanced Information Networking and Applications (AINA), 2013 IEEE 27th International Conference on (pp. 862-869). IEEE


which is the parallel version of:

>X.Xu, N.Yuruk, Z. Feng, T. Schweiger. **SCAN: a structural clustering algorithm for networks**,Proceedings of the 13th ACM SIGKDD international conference on Knowledge discovery and data mining, pp. 824-833, 2007.

## Usage

```
val graph:Graph[Int, Int] = GraphLoader.edgeListFile(sc, "path_to_graph_file")

val components:Graph[VertexId, Int] = PSCAN.pscan(graph, epsilon = 0.2)

println("num communities: " + components.vertices.map{case (vId,cId)=>cId}.distinct.count)
println("nodes of every communities:")
components.vertices.map(v=>(v._2, v._1)).groupByKey().collect
    .foreach(x=>println("%d: %s".format(x._1, x._2.mkString(" "))))
```


import org.apache.spark.graphx._
import scala.reflect.ClassTag

object PSCAN{
      def pscan[VD: ClassTag, ED: ClassTag](graph: Graph[VD, ED], epsilon: Double = 0.5): Graph[VertexId, ED] = {
          // set vertex property to Set of neighborhood vertex ids
          val withNeighboursVertices:VertexRDD[Set[VertexId]] = graph.mapVertices((_, _) => Set[VertexId]())
                  .aggregateMessages[Set[VertexId]](
              sendMsg = edgeContext => {
                  edgeContext.sendToSrc(Set(edgeContext.dstId))
                  edgeContext.sendToDst(Set(edgeContext.srcId))
              },
              mergeMsg = (s1, s2) => {
                  s1 ++ s2
              }
          ).mapValues((vid, neighbours)=> neighbours + vid)
          
          val neighbours: Graph[Set[VertexId], ED] = graph.outerJoinVertices(withNeighboursVertices)(
              (_, _, newValue) => newValue.getOrElse(Set(0L))
          )
          
          // compute similarities of all connected vertexs pairs
          val edgesWithSimilarity:Graph[Set[VertexId], Double] = neighbours.mapTriplets(edge => {
              val sizeOfIntersection = edge.srcAttr.intersect(edge.dstAttr).size
              val denominator = Math.sqrt(edge.srcAttr.size * edge.dstAttr.size)
              sizeOfIntersection / denominator
          })
          
          // remove edges whose similarity is smaller than eposilon
          val cutOffGraph:Graph[Set[VertexId], Double] = edgesWithSimilarity.filter[Set[VertexId], Double](
              preprocess = g => g,
              epred = edge => {
                  edge.attr >= epsilon
              })

          // every connected component is a cluster
          val componentsGraph:Graph[VertexId, Double] = cutOffGraph.connectedComponents()

          // return the origin graph with vertex property set to cluster identifier
          graph.outerJoinVertices(componentsGraph.vertices)((vId, oldData, newData) => {
              newData.getOrElse(-1)
          })
      }
}

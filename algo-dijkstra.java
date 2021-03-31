/*
 * Copyright (C) 2013 - 2020 Oracle and/or its affiliates. All rights reserved.
 */
package oracle.pgx.algorithms;

import oracle.pgx.algorithm.EdgeProperty;
import oracle.pgx.algorithm.PgxEdge;
import oracle.pgx.algorithm.PgxGraph;
import oracle.pgx.algorithm.PgxMap;
import oracle.pgx.algorithm.PgxVertex;
import oracle.pgx.algorithm.VertexProperty;
import oracle.pgx.algorithm.annotations.GraphAlgorithm;
import oracle.pgx.algorithm.annotations.Out;

@GraphAlgorithm
public class Dijkstra {
  public boolean dijkstra(PgxGraph g, EdgeProperty<Double> weight, PgxVertex root, PgxVertex dest,
      @Out VertexProperty<PgxVertex> parent, @Out VertexProperty<PgxEdge> parentEdge) {
    if (g.getNumVertices() == 0) {
      return false;
    }

    VertexProperty<Boolean> reached = VertexProperty.create();

    // sequentially initialize, otherwise compiler flags this algorithm as
    //parallel in nature
    g.getVertices().forSequential(n -> {
      parent.set(n, PgxVertex.NONE);
      parentEdge.set(n, PgxEdge.NONE);
      reached.set(n, false);
    });

    //-------------------------------
    // look up the vertex
    //-------------------------------
    PgxMap<PgxVertex, Double> reachable = PgxMap.create();
    reachable.set(root, 0d);

    //-------------------------------
    // look up the vertex
    //-------------------------------
    boolean found = false;
    boolean failed = false;

    while (!found && !failed) {
      if (reachable.size() == 0) {
        failed = true;
      } else {
        PgxVertex next = reachable.getKeyForMinValue();
        if (next == dest) {
          found = true;
        } else {
          reached.set(next, true);
          double dist = reachable.get(next);
          reachable.remove(next);
          next.getNeighbors().filter(v -> !reached.get(v)).forSequential(v -> {
            PgxEdge e = v.edge();
            if (!reachable.containsKey(v)) {
              reachable.set(v, dist + weight.get(e));
              parent.set(v, next);
              parentEdge.set(v, e);
            } else if (reachable.get(v) > dist + weight.get(e)) {
              reachable.set(v, dist + weight.get(e));
              parent.set(v, next);
              parentEdge.set(v, e);
            }
          });
        }
      }
    }

    // return false if not reachable
    return !failed;
  }
}

package org.Main;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.HashSet;

public class BidirectionalDijkstra {
    
    public static QueryResult bidirectionalDijkstra(Graph graph, long source, long target) {
        Map<Long, List<Edge>> adjList = graph.getAdjacencyList();

        Map<Long, Long> distL = new HashMap<>();
        Map<Long, Long> distR = new HashMap<>();
        Map<Long, Boolean> settledL = new HashMap<>();
        Map<Long, Boolean> settledR = new HashMap<>();
        PriorityQueue<Node> queueL = new PriorityQueue<>(Comparator.comparingLong(a -> a.distance));
        PriorityQueue<Node> queueR = new PriorityQueue<>(Comparator.comparingLong(a -> a.distance));

        long relaxedEdgesL = 0, relaxedEdgesR = 0;

        // Initialize all nodes' distances and settled status
        for (long v : adjList.keySet()) {
            distL.put(v, Long.MAX_VALUE);
            distR.put(v, Long.MAX_VALUE);
            settledL.put(v, false);
            settledR.put(v, false);
        }

        distL.put(source, 0L);
        distR.put(target, 0L);
        queueL.add(new Node(source, 0L));
        queueR.add(new Node(target, 0L));

        long shortestPath = Long.MAX_VALUE;

        while (!queueL.isEmpty() || !queueR.isEmpty()) {
            long forwardKey = queueL.isEmpty() ? Long.MAX_VALUE : queueL.peek().distance;
            long backwardKey = queueR.isEmpty() ? Long.MAX_VALUE : queueR.peek().distance;
            if (Math.min(forwardKey, backwardKey) >= shortestPath) {
                break;
            }

            boolean processL = !queueL.isEmpty() &&
                    (queueR.isEmpty() || queueL.peek().distance <= queueR.peek().distance);

            PriorityQueue<Node> queue = processL ? queueL : queueR;
            Map<Long, Long> distThis = processL ? distL : distR;
            Map<Long, Long> distOther = processL ? distR : distL;
            Map<Long, Boolean> settledThis = processL ? settledL : settledR;

            Node current = queue.poll();
            long u = current.vertex;

            if (settledThis.getOrDefault(u, false)) {
                continue;
            }
            settledThis.put(u, true);

            if (adjList.containsKey(u)) {
                for (Edge edge : adjList.get(u)) {
                    long v = edge.getTo();
                    long newDist = distThis.get(u) + edge.getCost();

                    // Relaxation logic
                    if (newDist < distThis.getOrDefault(v, Long.MAX_VALUE)) {
                        distThis.put(v, newDist);
                        queue.add(new Node(v, newDist));
                        if (processL) relaxedEdgesL++;
                        else relaxedEdgesR++;
                    }

                    // Check for potential path meeting in the middle
                    if (distOther.containsKey(v)) {
                        shortestPath = Math.min(shortestPath, distThis.get(v) + distOther.get(v));
                    }
                }
            }
        }

        return new QueryResult(shortestPath == Long.MAX_VALUE ? -1 : shortestPath, relaxedEdgesL + relaxedEdgesR);
    }
}

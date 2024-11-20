package org.Main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class ContractionHierarchy {
    private Graph graph;
    private List<Vertex> vertexOrder;
    private Set<Vertex> contractedVertices;
    private Map<Vertex, Integer> rankMap;
    private List<Edge> allEdges;
    private int lazyUpdateCount = 0;
    private int updateThreshold = 50;
    private int totalShortcutsAdded = 0;

    public ContractionHierarchy(Graph graph) {
        this.graph = graph;
        this.vertexOrder = new ArrayList<>();
        this.contractedVertices = new HashSet<>();
        this.rankMap = new HashMap<>();
        this.allEdges = new ArrayList<>();
    }

    public void preprocess() {
        long startTime = System.nanoTime();

        // Priority queue with caching for priorities
        PriorityQueue<Vertex> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(this::getNodePriority));
        Set<Vertex> dirtyVertices = new HashSet<>();

        // Add all vertices to the priority queue initially
        for (Vertex v : graph.getVertices().values()) {
            if (!contractedVertices.contains(v)) {
                priorityQueue.add(v);
            }
        }

        // Track cached priorities to avoid recalculation
        Map<Vertex, Integer> priorityCache = new HashMap<>();
        for (Vertex v : graph.getVertices().values()) {
            priorityCache.put(v, getNodePriority(v));
        }

        int totalVertices = graph.getVertices().size();  // Total vertices for progress tracking
        System.out.println("Total vertices in graph: " + totalVertices);

        int contractionCounter = 0;

        while (!priorityQueue.isEmpty() && contractedVertices.size() < totalVertices) {
            Vertex v = priorityQueue.poll();

            // Skip contracted vertices (redundant check as it should already be in priorityQueue)
            if (contractedVertices.contains(v)) continue;

            // Contract the vertex
            vertexOrder.add(v);
            contractedVertices.add(v);
            rankMap.put(v, vertexOrder.size());

            // Add shortcuts for the contracted vertex's neighbors
            int shortcutsAdded = contractVertex(v);
            totalShortcutsAdded += shortcutsAdded;

            // Debug prints to check priorities and contracted vertices
            contractionCounter++;

            // Lazy updates for neighbors
            for (Edge e : v.getEdges()) {
                Vertex neighbor = graph.getVertexById(e.getTo());
                if (!contractedVertices.contains(neighbor)) {
                    dirtyVertices.add(neighbor);
                }
            }

            // Rebuild priority queue periodically if necessary
            if (dirtyVertices.size() > updateThreshold) {
                for (Vertex vertex : dirtyVertices) {
                    if (!contractedVertices.contains(vertex)) {
                        priorityQueue.add(vertex);
                        priorityCache.put(vertex, getNodePriority(vertex));
                    }
                }
                dirtyVertices.clear();
            }
        }

        long endTime = System.nanoTime();
        System.out.println("Preprocessing complete. Total vertices contracted: " + vertexOrder.size());
        System.out.println("Total shortcuts added: " + totalShortcutsAdded);
        System.out.println("Preprocessing time: " + (endTime - startTime) / 1_000_000 + " ms");
    }

    private int getNodePriority(Vertex v) {
        int edgeDiff = getEdgeDifference(v);
        int deletedNeighbors = getDeletedNeighbors(v);
        return (int) (edgeDiff + 0.75 * deletedNeighbors);
    }

    private int getEdgeDifference(Vertex v) {
        Set<Edge> activeEdges = v.getActiveEdges();
        int contractedNeighbors = (int) v.getEdges().stream()
                .filter(e -> contractedVertices.contains(graph.getVertexById(e.getTo())))
                .count();
        return activeEdges.size() + contractedNeighbors;
    }

    private int getDeletedNeighbors(Vertex v) {
        return (int) v.getEdges().stream()
                .filter(e -> contractedVertices.contains(graph.getVertexById(e.getTo())))
                .count();
    }

    private int contractVertex(Vertex v) {
        int shortcutsAdded = 0;
    
        Map<Vertex, Integer> neighbors = new HashMap<>();
        for (Edge edge : v.getEdges()) {
            Vertex neighbor = graph.getVertexById(edge.getTo());
            if (!contractedVertices.contains(neighbor)) {
                neighbors.put(neighbor, edge.getCost());
            }
        }
    
        // Add shortcuts between every pair of uncontracted neighbors
        for (Vertex u : neighbors.keySet()) {
            for (Vertex w : neighbors.keySet()) {
                if (!u.equals(w)) {
                    int shortcutCost = neighbors.get(u) + neighbors.get(w);
    
                    // Check if an edge already exists between u and w and if the shortcut would be beneficial
                    if (!graph.hasEdge(u.getId(), w.getId()) || graph.getEdgeCost(u.getId(), w.getId()) > shortcutCost) {
                        graph.addEdge(u.getId(), w.getId(), shortcutCost);
                        allEdges.add(new Edge(u.getId(), w.getId(), shortcutCost));
                        shortcutsAdded++;
                    }
                }
            }
        }
    
        return shortcutsAdded;
    }
    

    private boolean isUniqueShortestPath(Vertex u, Vertex v, Vertex w) {
        QueryResult uvResult = Dijkstra.dijkstra(graph, u.getId(), v.getId());
        QueryResult vwResult = Dijkstra.dijkstra(graph, v.getId(), w.getId());

        if (uvResult.getShortestPath() == -1 || vwResult.getShortestPath() == -1) {
            return false;
        }

        long expectedDistance = uvResult.getShortestPath() + vwResult.getShortestPath();
        QueryResult uwResult = Dijkstra.dijkstra(graph, u.getId(), w.getId());

        return uwResult.getShortestPath() >= expectedDistance;
    }

    public void exportAugmentedGraph(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(graph.getVertices().size() + " " + (graph.getEdges().size() + allEdges.size()));
            writer.newLine();

            for (Vertex vertex : graph.getVertices().values()) {
                int rank = rankMap.getOrDefault(vertex, -1);
                writer.write(vertex.getId() + " " + rank);
                writer.newLine();
            }

            for (Edge edge : graph.getEdges()) {
                if (!contractedVertices.contains(graph.getVertexById(edge.getTo()))) {
                    writer.write(edge.getFrom() + " " + edge.getTo() + " " + edge.getCost() + " -1");
                    writer.newLine();
                }
            }

            for (Edge shortcutEdge : allEdges) {
                writer.write(shortcutEdge.getFrom() + " " + shortcutEdge.getTo() + " " + shortcutEdge.getCost() + " " + shortcutEdge.getFrom());
                writer.newLine();
            }

            System.out.println("Augmented graph exported successfully.");
        } catch (IOException e) {
            System.err.println("Error writing augmented graph to file: " + e.getMessage());
        }
    }
}
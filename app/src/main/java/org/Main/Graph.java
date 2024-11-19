package org.Main;

import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Graph {
    private Map<Long, Vertex> vertices;
    private Map<Long, List<Edge>> adjacencyList;

    public Graph() {
        vertices = new HashMap<>();
        adjacencyList = new HashMap<>();
    }

    // Add a vertex to the graph
    public void addVertex(long id, double longitude, double latitude) {
        vertices.put(id, new Vertex(id, longitude, latitude));
        adjacencyList.putIfAbsent(id, new ArrayList<>());
    }

    // Add an edge between two vertices
    public void addEdge(long from, long to, int cost) {
        Vertex v1 = vertices.get(from);
        Vertex v2 = vertices.get(to);
        if (v1 != null && v2 != null) {
            // Add edge to adjacency list
            Edge edge = new Edge(from, to, cost);
            adjacencyList.get(from).add(edge);
            adjacencyList.get(to).add(edge);
            v1.addEdge(edge);
            v2.addEdge(edge);
        }
    }

        public List<Edge> getEdges() {
        Set<Edge> edgeSet = new HashSet<>();
        
        // Collect all edges from adjacency list
        for (List<Edge> edges : adjacencyList.values()) {
            edgeSet.addAll(edges);
        }
        
        return new ArrayList<>(edgeSet);
    }

    // Get all vertices in the graph
    public Map<Long, Vertex> getVertices() {
        return vertices;
    }

    
    // Get adjacency list (all edges of each vertex)
    public Map<Long, List<Edge>> getAdjacencyList() {
        return adjacencyList;
    }

    // Get a vertex by its ID
    public Vertex getVertexById(long id) {
        return vertices.get(id);
    }

    // Remove a vertex from the graph
    public void removeVertex(long id) {
        vertices.remove(id);
        adjacencyList.remove(id);
    }

    // Add a vertex directly
    public void addVertex(Vertex vertex) {
        vertices.put(vertex.getId(), vertex);
    }

    // Read graph from input stream
    public static Graph readGraphFromInput(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String[] firstLine = reader.readLine().split(" ");
        int n = Integer.parseInt(firstLine[0]);
        int m = Integer.parseInt(firstLine[1]);

        Graph graph = new Graph();

        // Read vertices
        for (int i = 0; i < n; i++) {
            String[] vertexLine = reader.readLine().split(" ");
            long id = Long.parseLong(vertexLine[0]);
            double lon = Double.parseDouble(vertexLine[1]);
            double lat = Double.parseDouble(vertexLine[2]);
            graph.addVertex(id, lon, lat);
        }

        // Read edges
        for (int i = 0; i < m; i++) {
            String[] edgeLine = reader.readLine().split(" ");
            long from = Long.parseLong(edgeLine[0]);
            long to = Long.parseLong(edgeLine[1]);
            int cost = Integer.parseInt(edgeLine[2]);
            graph.addEdge(from, to, cost);
        }

        return graph;
    }
}
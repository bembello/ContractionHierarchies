package org.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.Main.Edge;
import org.Main.Graph;
import org.Main.Vertex;
import org.junit.Test;

public class BuildGraphTest {

    @Test
    public void testAddVertex() {
        Graph graph = new Graph();
        graph.addVertex(1, 10.0, 20.0);
        Vertex vertex = graph.getVertexById(1);
        
        assertNotNull(vertex);
        assertEquals(1, vertex.getId());
        assertEquals(10.0, vertex.getLongitude(), 0.001);
        assertEquals(20.0, vertex.getLatitude(), 0.001);
    }

    @Test
    public void testUndirectedGraph() {
        Graph graph = new Graph();
        graph.addVertex(1, 10.0, 20.0);
        graph.addVertex(2, 15.0, 25.0);

        // Add edge between 1 and 2 (undirected graph)
        graph.addEdge(1, 2, 5);

        // Check the adjacency list for vertex 1 (should have an edge to 2)
        List<Edge> edges1 = graph.getAdjacencyList().get(1L);
        assertNotNull(edges1);
        assertEquals(1, edges1.size()); // Should be 1 edge from 1 to 2
        Edge edge1 = edges1.get(0);
        assertEquals(2, edge1.getTo());
        assertEquals(5, edge1.getCost());

        // Check the adjacency list for vertex 2 (should have an edge to 1)
        List<Edge> edges2 = graph.getAdjacencyList().get(2L);
        assertNotNull(edges2);
        assertEquals(1, edges2.size()); // Should be 1 edge from 2 to 1 (undirected)
        Edge edge2 = edges2.get(0);
        assertEquals(1, edge2.getTo());
        assertEquals(5, edge2.getCost());
    }

    @Test
    public void testAddEdge() {
        Graph graph = new Graph();
        graph.addVertex(1, 10.0, 20.0);
        graph.addVertex(2, 15.0, 25.0);
        graph.addEdge(1, 2, 5);

        // Check adjacency list for vertex 1
        List<Edge> edges1 = graph.getAdjacencyList().get(1L);
        assertNotNull(edges1);
        assertEquals(1, edges1.size()); // Should be 1 edge from 1 to 2
        Edge edge1 = edges1.get(0);
        assertEquals(1, edge1.getFrom());
        assertEquals(2, edge1.getTo());
        assertEquals(5, edge1.getCost());

        // Check adjacency list for vertex 2
        List<Edge> edges2 = graph.getAdjacencyList().get(2L);
        assertNotNull(edges2);
        assertEquals(1, edges2.size()); // Should be 1 edge from 2 to 1 (undirected)
        Edge edge2 = edges2.get(0);
        assertEquals(2, edge2.getFrom());
        assertEquals(1, edge2.getTo());
        assertEquals(5, edge2.getCost());
    }

    @Test
    public void testHasEdge() {
        Graph graph = new Graph();
        graph.addVertex(1, 10.0, 20.0);
        graph.addVertex(2, 15.0, 25.0);
        graph.addEdge(1, 2, 5);

        assertTrue(graph.hasEdge(1, 2));  // Should return true for 1 -> 2
        assertTrue(graph.hasEdge(2, 1));  // Should return true for 2 -> 1 (undirected graph)
    }

    @Test
    public void testGetEdgeCost() {
        Graph graph = new Graph();
        graph.addVertex(1, 10.0, 20.0);
        graph.addVertex(2, 15.0, 25.0);
        graph.addEdge(1, 2, 5);

        int cost = graph.getEdgeCost(1, 2);
        assertEquals(5, cost);
        
        // Check non-existing edge
        cost = graph.getEdgeCost(2, 3);  // Edge 2 -> 3 does not exist
        assertEquals(Integer.MAX_VALUE, cost);
        
        // Check reverse edge in an undirected graph
        cost = graph.getEdgeCost(2, 1);  // Edge 2 -> 1 should exist
        assertEquals(5, cost);
    }

    @Test
    public void testReadGraphFromInput() throws Exception {
        String graphData = "3 2\n" + 
                           "1 10.0 20.0\n" + 
                           "2 15.0 25.0\n" + 
                           "3 20.0 30.0\n" + 
                           "1 2 5\n" + 
                           "2 3 10\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(graphData.getBytes());
        Graph graph = Graph.readGraphFromInput(inputStream);
    
        // Check vertices
        assertEquals(3, graph.getVertices().size());
    
        // Check edges for vertex 1
        List<Edge> edges1 = graph.getAdjacencyList().get(1L);
        assertNotNull(edges1);
        assertEquals(1, edges1.size());
        Edge edge1 = edges1.get(0);
        assertEquals(2, edge1.getTo());
        assertEquals(5, edge1.getCost());
    
        // Check edges for vertex 2
        List<Edge> edges2 = graph.getAdjacencyList().get(2L);
        assertNotNull(edges2);
        assertEquals(1, edges2.size());
        Edge edge2 = edges2.get(0);
        assertEquals(1, edge2.getTo());  // Reverse edge in undirected graph
        assertEquals(5, edge2.getCost());
    }
    
    @Test
    public void testRemoveVertex() {
        Graph graph = new Graph();
        graph.addVertex(1, 10.0, 20.0);
        graph.addVertex(2, 15.0, 25.0);
        graph.addEdge(1, 2, 5);

        graph.removeVertex(1);

        // Vertex 1 should be removed
        assertNull(graph.getVertexById(1));
        assertNull(graph.getAdjacencyList().get(1L));

        // Vertex 2 should still exist
        assertNotNull(graph.getVertexById(2));
    }
}

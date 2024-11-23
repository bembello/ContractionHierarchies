package org.example;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.Main.ContractionHierarchy;
import org.Main.Graph;
import org.Main.Vertex;
import org.junit.Before;
import org.junit.Test;

public class ContractionHierarchyTest {

    private Graph graph;
    private ContractionHierarchy contractionHierarchy;

    @Before
    public void setUp() {
        // Initialize a simple graph with vertices and edges
        graph = new Graph();
        graph.addVertex(new Vertex(1, 0.0, 0.0));  // Vertex 1
        graph.addVertex(new Vertex(2, 1.0, 1.0));  // Vertex 2
        graph.addVertex(new Vertex(3, 2.0, 2.0));  // Vertex 3
        graph.addEdge(1, 2, 10);  // Edge from 1 to 2
        graph.addEdge(1, 3, 20);  // Edge from 1 to 3
        graph.addEdge(2, 3, 5);   // Edge from 2 to 3

        contractionHierarchy = new ContractionHierarchy(graph);
    }

    @Test
    public void testGetNeighbors() {
        Vertex v1 = graph.getVertexById(1);
        Vertex v2 = graph.getVertexById(2);
        Vertex v3 = graph.getVertexById(3);

        // Test the neighbors of vertex 1
        Set<Vertex> neighborsV1 = contractionHierarchy.getNeighbors(v1);
        assertTrue(neighborsV1.contains(v2));
        assertTrue(neighborsV1.contains(v3));

        // Test the neighbors of vertex 2
        Set<Vertex> neighborsV2 = contractionHierarchy.getNeighbors(v2);
        assertTrue(neighborsV2.contains(v1));
        assertTrue(neighborsV2.contains(v3));

        // Test the neighbors of vertex 3
        Set<Vertex> neighborsV3 = contractionHierarchy.getNeighbors(v3);
        assertTrue(neighborsV3.contains(v1));
        assertTrue(neighborsV3.contains(v2));
    }
}

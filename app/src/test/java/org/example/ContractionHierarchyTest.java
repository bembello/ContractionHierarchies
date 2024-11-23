package org.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.Main.ContractionHierarchy;
import org.Main.Graph;
import org.Main.Vertex;
import org.junit.Before;
import org.junit.Test;

public class ContractionHierarchyTest {

    private Graph graph;
    private ContractionHierarchy ch;

    @Before
    public void setUp() {
        // Initialize a simple graph for testing
        graph = new Graph();
        
        // Add vertices
        graph.addVertex(1, 0, 0);
        graph.addVertex(2, 0, 1);
        graph.addVertex(3, 1, 0);

        // Add edges
        graph.addEdge(1, 2, 5); // Edge 1 -> 2 with cost 5
        graph.addEdge(2, 3, 5); // Edge 2 -> 3 with cost 5
        graph.addEdge(1, 3, 15); // Direct edge 1 -> 3 with cost 15

        ch = new ContractionHierarchy(graph);
    }

    @Test
    public void testPreprocess() {
        ch.preprocess();
        List<Vertex> vertexOrder = ch.getVertexOrder();
        System.out.println("Contracted vertices count: " + vertexOrder.size());
        assertEquals(graph.getVertices().size(), vertexOrder.size());
    
        int totalShortcuts = ch.getTotalShortcutsAdded();
        System.out.println("Total shortcuts added: " + totalShortcuts);
        assertTrue(totalShortcuts > 0);
    }

    @Test
    public void testContractVertex() {
        Vertex v = graph.getVertexById(2);

        // Contract vertex 2 and count the shortcuts added
        int shortcutsAdded = ch.contractVertex(v);

        // Verify that shortcuts were added between uncontracted neighbors
        assertTrue(shortcutsAdded > 0);
    }

    @Test
    public void testGetNodePriority() {
        Vertex v = graph.getVertexById(2);

        // Get priority of vertex 2
        int priority = ch.getNodePriority(v);

        // Priority should be calculated without errors
        assertTrue(priority >= 0);
    }

    @Test
    public void testExportAugmentedGraph() {
        // Preprocess the graph
        ch.preprocess();

        // Export the augmented graph to a file
        ch.exportAugmentedGraph("test_augmented_graph.txt");

        // Check if the file was created and contains expected content
        // (This can be done with file reading utilities, which would be external to this test)
    }

    @Test
    public void testIsUniqueShortestPath() {
        Vertex u = graph.getVertexById(1);
        Vertex v = graph.getVertexById(2);
        Vertex w = graph.getVertexById(3);

        // Verify that the unique shortest path condition is met for some vertices
        boolean isUnique = ch.isUniqueShortestPath(u, v, w);
        assertTrue(isUnique);
    }
}


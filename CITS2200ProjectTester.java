import java.io.*;
import java.util.*;

public class CITS2200ProjectTester {
    public static void loadGraph(CITS2200Project project, String path) {
        // The graph is in the following format:
        // Every pair of consecutive lines represent a directed edge.
        // The edge goes from the URL in the first line to the URL in the second line.
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            while (reader.ready()) {
                String from = reader.readLine();
                String to = reader.readLine();
                System.out.println("Adding edge from " + from + " to " + to);
                project.addEdge(from, to);
            }
        } catch (Exception e) {
            System.out.println("There was a problem:");
            System.out.println(e.toString());
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("usage: CITS2200ProjectTester GRAPH_FILE");
            System.exit(1);
        }

        // Change this to be the path to the graph file.
        String pathToGraphFile = args[0];
        // Create an instance of your implementation.
        CITS2200Project proj = new CITS2200Project();
        // Load the graph into the project.
        loadGraph(proj, pathToGraphFile);

        // Print out the graph mappings
        for (Integer vertex : proj.urlMapping.keySet()) {
            System.out.println(vertex + " " + proj.urlMapping.get(vertex));
        }

        // Print out the adjacency lists
        System.out.println("Adjacency Lists:");
        for (Integer vertex : proj.adjacencyList.keySet()) {
            System.out.println(vertex + ": " + proj.adjacencyList.get(vertex).toString());
        }

        // Print out the shortest paths for each vertex. An entry of -1
        // indicates that there is no path from the source vertex to the destination
        System.out.println("Shortest Paths:");
        for (Integer vertex : proj.adjacencyList.keySet()) {
            System.out.println(vertex + ": " + new ArrayList<Integer>() {{
                int[] paths = proj.getShortestPaths(vertex);
                for (int i = 0; i < paths.length; ++i) {
                    add(paths[i]);
                }
            }}.toString());
        }

        System.out.println("Center nodes:");
        System.out.println(new ArrayList<String>() {{
            String[] centers = proj.getCenters();
            for (int i = 0; i < centers.length; ++i) {
                add(centers[i]);
            }
        }}.toString());

        System.out.println("Strongly connected components:");
        System.out.println(proj.getIntegerStronglyConnectedComponents().toString());
    }
}
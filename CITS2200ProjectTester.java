import java.io.*;
import java.util.*;

import java.lang.Math;

public class CITS2200ProjectImplementationTester {
    public static void loadGraph(CITS2200ProjectImplementation project, String path) {
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

    public static class PerformanceAnalysis {
        public static interface Task {
            public void run(CITS2200ProjectImplementation proj);
        }

        public static void run(CITS2200ProjectImplementation proj, Task task, String descriptor) {
            CITS2200ProjectImplementation.opcount = 0;
            long startTime = System.nanoTime();
            task.run(proj);
            long finishTime = System.nanoTime();

            System.out.println(descriptor + "\t" + CITS2200ProjectImplementation.opcount + "\t" + ((finishTime - startTime) / 1000));
        }
    }

    public static void performTestsOnProject(CITS2200ProjectImplementation proj) {
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

        System.out.println("Most interesting hamiltonian path:");
        System.out.println(Arrays.asList(proj.getHamiltonianPath()).toString());
    }

    public static void loadRandomGraph(CITS2200ProjectImplementation proj, int size, double density) {
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j) {
                if (Math.random() < density) {
                    proj.addEdge(i, j);
                }
            }
        }
    }

    public static void loadIncreasingGraph(CITS2200ProjectImplementation proj, int size) {
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < (i + 1); ++j) {
                proj.addEdge(i, j);
            }
        }
    }

    public static void loadFullyConncetedGraph(CITS2200ProjectImplementation proj, int size) {
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j) {
                proj.addEdge(i, j);
            }
        }
    }

    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("usage: CITS2200ProjectImplementationTester [GRAPH_FILE]");
            System.exit(1);
        }

        if (args.length == 1) {
            System.out.println("Performing tests with " + args[0]);
            // Change this to be the path to the graph file.
            String pathToGraphFile = args[0];
            // Create an instance of your implementation.
            CITS2200ProjectImplementation proj = new CITS2200ProjectImplementation();
            // Load the graph into the project.
            loadGraph(proj, pathToGraphFile);
            performTestsOnProject(proj);
        } else {
            // Run a self-test suite to check the performance of the
            // algorithms under various scenarios
            double[] densities = new double[] { 0.6 };
            int maxSize = 80;
            int hamiltonianSize = 20;

            // Test shortest path performance
            System.out.println("--");
            System.out.println("Shortest paths with random graphs");
            for (int size = 2; size < maxSize; ++size) {
                for (int j = 0; j < densities.length; ++j) {
                    CITS2200ProjectImplementation proj = new CITS2200ProjectImplementation();
                    loadRandomGraph(proj, size, densities[j]);

                    PerformanceAnalysis.run(proj, new PerformanceAnalysis.Task() {
                        public void run(CITS2200ProjectImplementation proj) {
                            for (int i = 0; i < maxSize; ++i) {
                                if (proj.adjacencyList.get(i) != null)
                                    proj.getShortestPaths(i);
                            }
                        }
                    }, "" + size);
                }
            }

            System.out.println("--");
            System.out.println("Shortest paths with increasing graphs");
            for (int size = 2; size < maxSize; ++size) {
                CITS2200ProjectImplementation proj = new CITS2200ProjectImplementation();
                loadIncreasingGraph(proj, size);

                PerformanceAnalysis.run(proj, new PerformanceAnalysis.Task() {
                    public void run(CITS2200ProjectImplementation proj) {
                        for (int i = 0; i < maxSize; ++i) {
                            if (proj.adjacencyList.get(i) != null)
                                proj.getShortestPaths(i);
                        }
                    }
                }, "" + size);
            }

            System.out.println("--");
            System.out.println("Shortest paths with fully connected graph");
            for (int size = 2; size < maxSize; ++size) {
                CITS2200ProjectImplementation proj = new CITS2200ProjectImplementation();
                loadFullyConncetedGraph(proj, size);

                PerformanceAnalysis.run(proj, new PerformanceAnalysis.Task() {
                    public void run(CITS2200ProjectImplementation proj) {
                        for (int i = 0; i < maxSize; ++i) {
                            if (proj.adjacencyList.get(i) != null)
                                proj.getShortestPaths(i);
                        }
                    }
                }, "" + size);
            }

            // Graph centers
            System.out.println("--");
            System.out.println("Graph centers with random graphs");
            for (int size = 2; size < maxSize; ++size) {
                for (int j = 0; j < densities.length; ++j) {
                    CITS2200ProjectImplementation proj = new CITS2200ProjectImplementation();
                    loadRandomGraph(proj, size, densities[j]);

                    PerformanceAnalysis.run(proj, new PerformanceAnalysis.Task() {
                        public void run(CITS2200ProjectImplementation proj) {
                            proj.getCenters();
                        }
                    }, "" + size);
                }
            }

            System.out.println("--");
            System.out.println("Graph centers with increasing graphs");
            for (int size = 2; size < maxSize; ++size) {
                CITS2200ProjectImplementation proj = new CITS2200ProjectImplementation();
                loadIncreasingGraph(proj, size);

                PerformanceAnalysis.run(proj, new PerformanceAnalysis.Task() {
                    public void run(CITS2200ProjectImplementation proj) {
                        proj.getCenters();
                    }
                }, "" + size);
            }

            System.out.println("--");
            System.out.println("Graph centers with fully connected graph");
            for (int size = 2; size < maxSize; ++size) {
                CITS2200ProjectImplementation proj = new CITS2200ProjectImplementation();
                loadFullyConncetedGraph(proj, size);

                PerformanceAnalysis.run(proj, new PerformanceAnalysis.Task() {
                    public void run(CITS2200ProjectImplementation proj) {
                        proj.getCenters();
                    }
                }, "" + size);
            }

            // Strongly connected components
            System.out.println("--");
            System.out.println("Strongly connected components with random graphs");
            for (int size = 2; size < maxSize; ++size) {
                for (int j = 0; j < densities.length; ++j) {
                    CITS2200ProjectImplementation proj = new CITS2200ProjectImplementation();
                    loadRandomGraph(proj, size, densities[j]);

                    PerformanceAnalysis.run(proj, new PerformanceAnalysis.Task() {
                        public void run(CITS2200ProjectImplementation proj) {
                            proj.getIntegerStronglyConnectedComponents();
                        }
                    }, "" + size);
                }
            }

            System.out.println("--");
            System.out.println("Strongly connected components with increasing graphs");
            for (int size = 2; size < maxSize; ++size) {
                CITS2200ProjectImplementation proj = new CITS2200ProjectImplementation();
                loadIncreasingGraph(proj, size);

                PerformanceAnalysis.run(proj, new PerformanceAnalysis.Task() {
                    public void run(CITS2200ProjectImplementation proj) {
                        proj.getIntegerStronglyConnectedComponents();
                    }
                }, "" + size);
            }

            System.out.println("--");
            System.out.println("Strongly connected components with fully connected graph");
            for (int size = 2; size < maxSize; ++size) {
                CITS2200ProjectImplementation proj = new CITS2200ProjectImplementation();
                loadFullyConncetedGraph(proj, size);

                PerformanceAnalysis.run(proj, new PerformanceAnalysis.Task() {
                    public void run(CITS2200ProjectImplementation proj) {
                        proj.getIntegerStronglyConnectedComponents();
                    }
                }, "" + size);
            }

            // Hamiltonian path - DP
            System.out.println("--");
            System.out.println("DP Hamiltonian Path with random graphs");
            for (int size = 2; size < hamiltonianSize; ++size) {
                for (int j = 0; j < densities.length; ++j) {
                    CITS2200ProjectImplementation proj = new CITS2200ProjectImplementation();
                    loadRandomGraph(proj, size, densities[j]);

                    PerformanceAnalysis.run(proj, new PerformanceAnalysis.Task() {
                        public void run(CITS2200ProjectImplementation proj) {
                            proj.getHamiltonianPathUsingDynamicProgramming();
                        }
                    }, "" + size);
                }
            }

            System.out.println("DP Hamiltonian Path with increasing graphs");
            for (int size = 2; size < hamiltonianSize; ++size) {
                CITS2200ProjectImplementation proj = new CITS2200ProjectImplementation();
                loadIncreasingGraph(proj, size);

                PerformanceAnalysis.run(proj, new PerformanceAnalysis.Task() {
                    public void run(CITS2200ProjectImplementation proj) {
                        proj.getHamiltonianPathUsingDynamicProgramming();
                    }
                }, "" + size);
            }

            System.out.println("--");
            System.out.println("DP Hamiltonian Path with fully connected graph");
            for (int size = 2; size < hamiltonianSize; ++size) {
                CITS2200ProjectImplementation proj = new CITS2200ProjectImplementation();
                loadFullyConncetedGraph(proj, size);

                PerformanceAnalysis.run(proj, new PerformanceAnalysis.Task() {
                    public void run(CITS2200ProjectImplementation proj) {
                        proj.getHamiltonianPathUsingDynamicProgramming();
                    }
                }, "" + size);
            }

            // Hamiltonian path - DFS
            System.out.println("--");
            System.out.println("DFS Hamiltonian Path with random graphs");
            for (int size = 2; size < hamiltonianSize; ++size) {
                for (int j = 0; j < densities.length; ++j) {
                    CITS2200ProjectImplementation proj = new CITS2200ProjectImplementation();
                    loadRandomGraph(proj, size, densities[j]);

                    PerformanceAnalysis.run(proj, new PerformanceAnalysis.Task() {
                        public void run(CITS2200ProjectImplementation proj) {
                            proj.getHamiltonianPathUsingDFSFibres();
                        }
                    }, "" + size);
                }
            }

            System.out.println("--");
            System.out.println("DFS Hamiltonian Path with increasing graphs");
            for (int size = 2; size < hamiltonianSize; ++size) {
                CITS2200ProjectImplementation proj = new CITS2200ProjectImplementation();
                loadIncreasingGraph(proj, size);

                PerformanceAnalysis.run(proj, new PerformanceAnalysis.Task() {
                    public void run(CITS2200ProjectImplementation proj) {
                        proj.getHamiltonianPathUsingDFSFibres();
                    }
                }, "" + size);
            }

            System.out.println("--");
            System.out.println("DFS Hamiltonian Path with fully connected graph");
            for (int size = 2; size < hamiltonianSize; ++size) {
                CITS2200ProjectImplementation proj = new CITS2200ProjectImplementation();
                loadFullyConncetedGraph(proj, size);

                PerformanceAnalysis.run(proj, new PerformanceAnalysis.Task() {
                    public void run(CITS2200ProjectImplementation proj) {
                        proj.getHamiltonianPathUsingDFSFibres();
                    }
                }, "" + size);
            }
        }
    }
}
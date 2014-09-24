package thesis.snapshots.communities.structure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;

import thesis.snapshots.communities.utils.Bag;

public class RandomGraph
{
	Graph graph;
	private int WEIGTH = 5;
	
	public RandomGraph(int vertices, int edges)
	{
		this.graph = new Graph(vertices, edges);
	}
	
	public void createGephiGraph(String name) 
	{
		try {
			//File file = new File("src/thesis/snapshots/communities/structure/random.gml");
			File file = new File("src/thesis/snapshots/communities/structure/"+name+".gml");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file);
			
			BufferedWriter bw = new BufferedWriter(fw);
			StringBuilder sb = new StringBuilder();			
	        String NEWLINE = System.getProperty("line.separator");

			String content = "graph"+NEWLINE+"["+NEWLINE+"directed 0"+NEWLINE;
			sb.append(content);
			for(int i = 0; i < graph.V(); i++)
			{
				content = "node"+NEWLINE+"["+NEWLINE+"id "+i+NEWLINE+
						"source "+"\""+"github"+"\""+NEWLINE+"]"+NEWLINE;
				sb.append(content);
			}
			
			int countEdges = 0;
			for (int i = 0; i < graph.V(); i++) 
			{
				for (int j : graph.adj(i)) 
				{
					content = "edge"+NEWLINE+"["+NEWLINE+"source "+i+NEWLINE+"target "+j+NEWLINE+"]"+NEWLINE;
					sb.append(content);
					countEdges++;
				}
			}		
			
			content="]"+NEWLINE;
			sb.append(content);
			
			bw.write(sb.toString());
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}	
}



class Graph {
    private final int V;
    private int E;
    private Bag<Integer>[] adj;
    
   /**
     * Create an empty graph with V vertices.
     * @throws java.lang.IllegalArgumentException if V < 0
     */
    public Graph(int V) {
        if (V < 0) throw new IllegalArgumentException("Number of vertices must be nonnegative");
        this.V = V;
        this.E = 0;
        adj = (Bag<Integer>[]) new Bag[V];
        for (int v = 0; v < V; v++) {
            adj[v] = new Bag<Integer>();
        }
    }

   /**
     * Create a random graph with V vertices and E edges.
     * Expected running time is proportional to V + E.
     * @throws java.lang.IllegalArgumentException if either V < 0 or E < 0
     */
    public Graph(int V, int E) {
        this(V);
        if (E < 0) throw new IllegalArgumentException("Number of edges must be nonnegative");
        for (int i = 0; i < E; i++) {
        	int v, w;
        	do{
	            v = (int) (Math.random() * V);
	            w = (int) (Math.random() * V);
        	}
        	while(v == w);
            addEdge(v, w);
        }
    }

   /**
     * Return the number of vertices in the graph.
     */
    public int V() { return V; }

   /**
     * Return the number of edges in the graph.
     */
    public int E() { return E; }


   /**
     * Add the undirected edge v-w to graph.
     * @throws java.lang.IndexOutOfBoundsException unless both 0 <= v < V and 0 <= w < V
     */
    public void addEdge(int v, int w) {
        if (v < 0 || v >= V) throw new IndexOutOfBoundsException();
        if (w < 0 || w >= V) throw new IndexOutOfBoundsException();
        E++;
        adj[v].add(w);
        adj[w].add(v);
    }


   /**
     * Return the list of neighbors of vertex v as in Iterable.
     * @throws java.lang.IndexOutOfBoundsException unless 0 <= v < V
     */
    public Iterable<Integer> adj(int v) {
        if (v < 0 || v >= V) throw new IndexOutOfBoundsException();
        return adj[v];
    }


   /**
     * Return a string representation of the graph.
     */
    public String toString() {
        StringBuilder s = new StringBuilder();
        String NEWLINE = System.getProperty("line.separator");
        s.append(V + " vertices, " + E + " edges " + NEWLINE);
        for (int v = 0; v < V; v++) {
            s.append(v + ": ");
            for (int w : adj[v]) {
                s.append(w + " ");
            }
            s.append(NEWLINE);
        }
        return s.toString();
    }
}



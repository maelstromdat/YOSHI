using System.Collections.Generic;
using System.Linq;

namespace YOSHI
{
    /// <summary>
    /// Graph class represents an undirected graph using adjacency list representation
    /// Source: https://stackoverflow.com/questions/10032940/iterative-connected-components-algorithm
    /// </summary>
    public class Graph<T>
    {
        public Dictionary<T, HashSet<T>> nodesNeighbors;
        public IEnumerable<T> Nodes
        {
            get { return nodesNeighbors.Keys; }
        }

        public Graph()
        {
            this.nodesNeighbors = new Dictionary<T, HashSet<T>>();
        }

        /// <summary>
        /// Adds the given node to the graph.
        /// </summary>
        /// <param name="node">The node to add to the graph.</param>
        public void AddNode(T node)
        {
            this.nodesNeighbors.Add(node, new HashSet<T>());
        }

        /// <summary>
        /// Adds the given collection of nodes to the graph.
        /// </summary>
        /// <param name="nodes">The collection of nodes to add to the graph.</param>
        public void AddNodes(IEnumerable<T> nodes)
        {
            foreach (T n in nodes)
            {
                this.AddNode(n);
            }
        }

        /// <summary>
        /// Adds an undirected edge to the graph between the given nodes. If the graph does not contain the given nodes,
        /// it will add them.
        /// </summary>
        /// <param name="node1">One node of the edge.</param>
        /// <param name="node2">The other node of the edge.</param>
        public void AddEdge(T node1, T node2)
        {
            if (!this.ContainsNode(node1))
            {
                this.AddNode(node1);
            }

            if (!this.ContainsNode(node2))
            {
                this.AddNode(node2);
            }

            this.nodesNeighbors[node1].Add(node2);
            this.nodesNeighbors[node2].Add(node1);
        }

        /// <summary>
        /// Adds an undirected edge to the graph between the given nodes. If the graph does not contain the given nodes,
        /// it will add them.
        /// </summary>
        /// <param name="edges">A collection of tuples to be added as edges.</param>
        public void AddEdges(IEnumerable<(T, T)> edges)
        {
            foreach ((T node1, T node2) in edges)
            {
                if (!this.ContainsNode(node1))
                {
                    this.AddNode(node1);
                }

                if (!this.ContainsNode(node2))
                {
                    this.AddNode(node2);
                }

                this.nodesNeighbors[node1].Add(node2);
                this.nodesNeighbors[node2].Add(node1);
            }

        }

        /// <summary>
        /// Checks whether the graph contains a certain node.
        /// </summary>
        /// <param name="node">The node for which we want to know whether it is contained in the graph.</param>
        /// <returns>True if node occurs in the graph, false otherwise.</returns>
        public bool ContainsNode(T node)
        {
            return this.nodesNeighbors.ContainsKey(node);
        }

        /// <summary>
        /// Gets the collection of neighbors of a given node.
        /// </summary>
        /// <param name="node">The node that we want the neighbors from.</param>
        /// <returns>A collection of neighbor nodes of the given node.</returns>
        public IEnumerable<T> GetNeighbors(T node)
        {
            return nodesNeighbors[node];
        }

        /// <summary>
        /// A depth first search algorithm implementation.
        /// </summary>
        /// <param name="nodeStart">The node to start the depth first search from.</param>
        /// <returns>A collection of nodes found from the given starting node.</returns>
        public IEnumerable<T> DepthFirstSearch(T nodeStart)
        {
            Stack<T> stack = new Stack<T>();
            HashSet<T> visitedNodes = new HashSet<T>();
            stack.Push(nodeStart);
            while (stack.Count > 0)
            {
                T curr = stack.Pop();
                if (!visitedNodes.Contains(curr))
                {
                    visitedNodes.Add(curr);
                    yield return curr;
                    foreach (T next in this.GetNeighbors(curr))
                    {
                        if (!visitedNodes.Contains(next))
                        {
                            stack.Push(next);
                        }
                    }
                }
            }
        }

        /// <summary>
        /// A method that returns all connected components in a graph.
        /// </summary>
        /// <returns>A collection of the connected components in a graph.</returns>
        public IEnumerable<HashSet<T>> GetConnectedComponents()
        {
            HashSet<T> visitedNodes = new HashSet<T>();
            List<HashSet<T>> components = new List<HashSet<T>>();

            foreach (T node in this.Nodes)
            {
                if (!visitedNodes.Contains(node))
                {
                    HashSet<T> subGraph = this.DepthFirstSearch(node).ToHashSet();
                    components.Add(subGraph);
                    visitedNodes.UnionWith(subGraph);
                }
            }
            return components;
        }
    }
}

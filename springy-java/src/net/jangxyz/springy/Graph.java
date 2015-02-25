package net.jangxyz.springy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class Graph {
	private int nextNodeId = 0;
	private int nextEdgeId = 0;
	
	public LinkedList<Node> nodes;
	public List<Edge> edges;
	public Map<String, Node> nodeSet = new HashMap<String, Node>();
	public Map<String, Map<String, List<Edge>> > adjacency = new HashMap<String, Map<String, List<Edge>> >();
	
	public class Node {
		public String id;
		public Map<String,String> data;
		
		Node(String id, Map<String,String>data) {
			this.id = id;
			this.data = data; 
		}
		Node(int id, Map<String,String>data) {
			this(Integer.toString(id), data);
		}
	}
	
	public class Edge {
		public String id;
		public Node source;
		public Node target;
		public Map<String,String> data;
		
		Edge(int id, Node source, Node target, Map<String, String> data) {
			this.id = Integer.toString(id);
			this.source = source;
			this.target = target;
			this.data = data;
		}
		
	}

	public Graph() {
		this.nodes = new LinkedList<Node>();
		this.edges = new LinkedList<Edge>();
	}
	public Graph(int nodesCount, int edgesCount) {
		this();
	}
	
	//
	public Node getNode(String nodeId) {
		return this.nodeSet.get(nodeId);
	}
	public Node addNode(Node node) {
		if (!(this.nodeSet.containsKey(node.id))) {
			this.nodes.add(node);
		}

		this.nodeSet.put(node.id, node);

		return node;		
	}
    public void addNodes(Iterable<String> names) {
        for (String name: names) {
        	Map<String,String> nodeData = new HashMap<String, String>();
        	nodeData.put("label", name);
            Node newNode = new Node(name, nodeData);
            this.addNode(newNode);
        }
    }

	public Node newNode(Map<String,String> data) {
		Node node = new Node(this.nextNodeId++, data);
		this.addNode(node);
		return node;
	}
	
	//
	public Edge addEdge(Edge edge) {		
		// add edge if not exist yet
		boolean exists = false;
		for (Edge e: edges) {
			if (edge.id == e.id) { 
				exists = true;
				break;
			}
		}
		if (!exists) {
			this.edges.add(edge);
		}

		// initialize [edge.source]
		if (!this.adjacency.containsKey(edge.source.id)) {
			this.adjacency.put(edge.source.id, new HashMap<String, List<Edge>>());
		}
		Map<String, List<Edge>> sourceAdjacencies = this.adjacency.get(edge.source.id);
		// initialize [edge.source][edge.target]
		if (!sourceAdjacencies.containsKey(edge.target.id)) {
			sourceAdjacencies.put(edge.target.id, new LinkedList<Edge>());
		}

		// add to adjacencies
		exists = false;
		for (Edge e: sourceAdjacencies.get(edge.target.id)) {			
			if (edge.id == e.id) { 
				exists = true;
				break;
			}
		}
		if (!exists) {
			sourceAdjacencies.get(edge.target.id).add(edge);
		}

		return edge;		
	}
	public void addEdges(Iterable<String[]> edgeInfos) {
    	for (String[] edgeInfo: edgeInfos) {
    		Node source = this.nodeSet.get(edgeInfo[0]);
    		Node target = this.nodeSet.get(edgeInfo[1]);
    		
    		Map<String,String> edgeData = new HashMap<String,String>();
    		edgeData.put("label", edgeInfo[2]);
    		
    		if (source != null & target != null) {
                this.newEdge(source, target, edgeData);
    		}
    	}
    }

	
	public Edge newEdge(Node source, Node target, Map<String, String> edgeData) {
		Edge edge = new Edge(this.nextEdgeId++, source, target, edgeData);
		this.addEdge(edge);
		return edge;

	}
	
	public List<Edge> getEdges(Node source, Node target) {
    	Map<String, List<Edge>> sourceAdjacencies = this.adjacency.get(source.id);
    	if (sourceAdjacencies == null) {
    		return new ArrayList<Edge>();
    	}
    	
    	List<Edge> sourceTargetEdges = sourceAdjacencies.get(target.id);
    	if (sourceTargetEdges == null) {
    		return new ArrayList<Edge>();
    	}

        return sourceTargetEdges;
    }

}

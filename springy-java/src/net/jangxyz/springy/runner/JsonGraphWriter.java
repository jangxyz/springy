package net.jangxyz.springy.runner;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import net.jangxyz.springy.Graph;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonGraphWriter {
	private Graph graph;

    /*
       {
		    "nodes": [
				{ "id": "0", "data": { "label": "0" } }, 
				{ "id": "2", "data": { "label": "2" } }, 
				{ "id": "6", "data": { "label": "6" } }
			],
		    "edges": [{
		        "id": "0",
		        "source": { "id": "2", "data": { "label": "2" } },
		        "data": { "label": "2 --> 6" },
		        "target": { "id": "6", "data": { "label": "6" } }
		    }]
		}
     */
	
	JsonGraphWriter(Graph graph) {
		this.graph = graph;
	}

	
	JSONObject nodeToJson(Graph.Node node) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("id", node.id);
		json.put("data", nodeDataToJson(node.data));

		return json;
	}

	private JSONObject nodeDataToJson(Map<String, String> nodeData)
			throws JSONException {
		JSONObject nodeDataJson = new JSONObject();
		for (Map.Entry<String, String> entry: nodeData.entrySet()) {
			nodeDataJson.put(entry.getKey(), entry.getValue());
		}
		return nodeDataJson;
	}

	JSONObject edgeToJson(Graph.Edge edge) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("id", edge.id);
		json.put("source", nodeToJson(edge.source));
		json.put("target", nodeToJson(edge.target));
		json.put("data", nodeDataToJson(edge.data));
		return json;
	}

	void write(Writer out) throws JSONException, IOException {	
		JSONObject json = new JSONObject();
		
        // put nodes
		JSONArray nodesJson = new JSONArray();
		for (Graph.Node node: this.graph.nodes) {
			nodesJson.put(nodeToJson(node));
		}
        json.put("nodes", nodesJson);
        
        // put edges
		JSONArray edgesJson = new JSONArray();
		for (Graph.Edge edge: this.graph.edges) {
			edgesJson.put(edgeToJson(edge));
		}
        json.put("edges", edgesJson);

        // write
        out.write(json.toString(4));
	}
}

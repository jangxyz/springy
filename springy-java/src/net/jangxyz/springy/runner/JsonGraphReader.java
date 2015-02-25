package net.jangxyz.springy.runner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import net.jangxyz.springy.Graph;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonGraphReader {
	private Reader reader;
	JsonGraphReader(Reader reader) {
		this.reader = reader;
	}
	JsonGraphReader(String filename) {
		this(buildReaderFromFilename(filename));
	}
	static Reader buildReaderFromFilename(String filename) {
		Reader reader = null;
		try {
			if (filename.endsWith(".gz")) {
				InputStream instream = new GZIPInputStream(new FileInputStream(filename));
				reader = new BufferedReader(new InputStreamReader(instream));
			} else {
				reader = new FileReader(filename);			
			}
		} catch (Exception e) {
			throw new RuntimeException("cannot read file: "+ filename);
		}
		return reader;
	}
	Graph read() throws JSONException {
		// read from json
	    JSONTokener tokener = new JSONTokener(this.reader);
	    JSONObject obj = new JSONObject(tokener);

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
	   
	    //
	    Graph graph = new Graph();

	    // add nodes	    

	    JSONArray nodes = obj.getJSONArray("nodes");
	    if (nodes != null) {
		    List<String> nodeNames = new ArrayList<String>(nodes.length());
		    for(int i = 0 ; i < nodes.length() ; i++) {
		    	JSONObject node = nodes.getJSONObject(i);
		    	String nodeId = node.getString("id");
		    	if (nodeId == null) {
			    	nodeId =  Integer.toString(node.getInt("id"));		    		
		    	}
		    	if (nodeId != null) {
		    		nodeNames.add(nodeId);
		    	} else {
		    		System.out.println("error");
		    	}
		    }
		    graph.addNodes(nodeNames);
	    }
	    
	    // add edges
	    JSONArray edges = obj.getJSONArray("edges");
	    if (edges != null) {
	    	List<String[]> edgeTuples = new ArrayList<String[]>(edges.length());
	    	for (int i = 0; i < edges.length(); i++) {
	    		JSONObject edge = edges.getJSONObject(i);
	    		
	    		String sourceId = edge.getJSONObject("source").getString("id");
	    		String targetId = edge.getJSONObject("target").getString("id");
	    		String edgeLabel = edge.getJSONObject("data").getString("label");
	    		
	    		String[] edgeInfo = {sourceId, targetId, edgeLabel}; 
	    		edgeTuples.add(edgeInfo);
	    	}
	    	
	        graph.addEdges(edgeTuples);
	    }

	    return graph;
	}
}

package net.jangxyz.springy.runner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import net.jangxyz.springy.BarnesHutLayout;
import net.jangxyz.springy.ForceDirectedLayout;
import net.jangxyz.springy.ForceDirectedLayout.Point;
import net.jangxyz.springy.ForceDirectedLayout.Spring;
import net.jangxyz.springy.Graph;
import net.jangxyz.springy.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/*
 * 
 * Read layout file and build a ForceDirectedLayout object
 * 
 */

public class JsonLayoutReader {
	private Reader reader;
	private Graph graph;	

	JsonLayoutReader(Graph graph, Reader reader) {
		this.graph = graph;
		this.reader = reader;
	}

	JsonLayoutReader(Reader reader) {
		this.reader = reader;
	}
	
	JsonLayoutReader(Graph graph, String filename) {
		this(graph, buildReaderFromFilename(filename));
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


    Point buildPoint(JSONObject pointData) throws JSONException {
    	JSONObject p = pointData.getJSONObject("p");
        Vector position = new Vector(p.getDouble("x"), p.getDouble("y"));
        double mass = pointData.getDouble("m");
        ForceDirectedLayout.Point point = new ForceDirectedLayout.Point(position, mass);

        JSONObject v = pointData.getJSONObject("v");
        Vector velocity = new Vector(v.getDouble("x"), v.getDouble("y"));
        point.v = velocity;

        return point;
    }

    ForceDirectedLayout read() throws JSONException {
    	return this.read(new HashMap<String, Float>());
    }
    	
	ForceDirectedLayout read(Map<String, Float> options) throws JSONException {
	    JSONTokener tokener = new JSONTokener(this.reader);
	    JSONObject edgeData = new JSONObject(tokener);

	    // set node points
	    Map<String,ForceDirectedLayout.Point> nodePoints = new HashMap<String,Point>();
	    Map<String,ForceDirectedLayout.Point> nodePointsIndexByPM = new HashMap<String,Point>();
	    readNodePoints(edgeData, nodePoints, nodePointsIndexByPM);

	    // set edge springs
	    Map<String,Spring> edgeSprings = new HashMap<String,Spring>();
	    readEdgeSprings(edgeData, edgeSprings, nodePointsIndexByPM);

	    //
	    double stiffness = edgeData.getDouble("stiffness");
	    double repulsion = edgeData.getDouble("repulsion");
	    double damping   = edgeData.getDouble("damping");
	    double unitTime  = options.containsKey("unitTime") ? 
	    		options.get("unitTime") :
	    		edgeData.getDouble("unitTime");
	    double minEnergyThreshold = 0.01;

	    ForceDirectedLayout layout = buildLayout(stiffness, repulsion, damping,
				unitTime, minEnergyThreshold, nodePoints, edgeSprings, options);
	    
	    return layout;
	}

	private void readNodePoints(JSONObject edgeData, Map<String, Point> nodePoints, Map<String, Point> nodePointsIndexByPM)
			throws JSONException 
	{
		JSONArray nodePointsDataArray = edgeData.optJSONArray("nodePoints");
	    if (nodePointsDataArray  != null) {
		    for (int i = 0; i < nodePointsDataArray.length(); i++) {
		    	JSONObject pointData = nodePointsDataArray.getJSONObject(i);
		    	Point point = this.buildPoint(pointData);
		    	
		    	nodePoints.put(Integer.toString(i), point);
		    	nodePointsIndexByPM.put(point.toString(), point);
		    }
	    } else {
		    JSONObject nodePointsDataObject = edgeData.getJSONObject("nodePoints");
		    for (String key: JSONObject.getNames(nodePointsDataObject)) {
		    	JSONObject pointData = nodePointsDataObject.getJSONObject(key);
		    	Point point = this.buildPoint(pointData);
		    	
		    	nodePoints.put(key, point);
		    	nodePointsIndexByPM.put(point.toString(), point);
		    }
	    }
	}

	private void readEdgeSprings(JSONObject edgeData, Map<String, Spring> edgeSprings, Map<String, Point> nodePointsIndexByPM)
			throws JSONException 
	{
		JSONObject edgeSpringsData = edgeData.getJSONObject("edgeSprings");
	    for (int i = 0; i < edgeSpringsData.length(); i++) {
	    	String edgeId = Integer.toString(i);

	    	if (edgeSpringsData.isNull(edgeId)) {
	    		continue;
	    	}
	    	
	    	JSONObject _springData = edgeSpringsData.getJSONObject(edgeId);
	    	
	    	double length = _springData.getDouble("length");
	    	double k = _springData.getDouble("k");

	        String point1Key = this.buildPoint(_springData.getJSONObject("point1")).toString();
	        String point2Key = this.buildPoint(_springData.getJSONObject("point2")).toString();
	        ForceDirectedLayout.Point point1 = nodePointsIndexByPM.get(point1Key);
	        ForceDirectedLayout.Point point2 = nodePointsIndexByPM.get(point2Key);

	        ForceDirectedLayout.Spring spring = new ForceDirectedLayout.Spring(point1, point2, length, k);
	        edgeSprings.put(edgeId, spring);
	    }
	}

	ForceDirectedLayout buildLayout(double stiffness, double repulsion,
			double damping, double unitTime, double minEnergyThreshold,
			Map<String, ForceDirectedLayout.Point> nodePoints,
			Map<String, ForceDirectedLayout.Spring> edgeSprings, Map<String, Float> options) {
		ForceDirectedLayout layout;

		boolean isBarnesHutLayout = options.containsKey("barnes-hut-layout") ? 
	    		options.get("barnes-hut-layout") == 1.0 : false;

		if (isBarnesHutLayout) {
		    float theta = options.containsKey("theta") ? 
		    	options.get("theta") : 0.5f;
			layout = new BarnesHutLayout(this.graph, stiffness, repulsion, damping, minEnergyThreshold, unitTime, theta);						
		} else {
			layout = new ForceDirectedLayout(this.graph, stiffness, repulsion, damping, minEnergyThreshold, unitTime);			
		}
	    layout.nodePoints = nodePoints;
	    layout.edgeSprings = edgeSprings;
		return layout;
	}
}
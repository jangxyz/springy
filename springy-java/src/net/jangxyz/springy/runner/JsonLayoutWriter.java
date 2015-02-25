package net.jangxyz.springy.runner;

import java.io.IOException;
import java.io.Writer;

import net.jangxyz.springy.ForceDirectedLayout;
import net.jangxyz.springy.Graph;
import net.jangxyz.springy.Vector;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonLayoutWriter {
	private ForceDirectedLayout layout;

	JsonLayoutWriter(ForceDirectedLayout layout) {
		this.layout = layout;
	}

	JSONObject vectorToJson(Vector vector) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("x", vector.x);
		json.put("y", vector.y);
		return json;
	}
	
	JSONObject pointToJson(ForceDirectedLayout.Point point) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("m", point.m);
		json.put("p", this.vectorToJson(point.p));
		json.put("v", this.vectorToJson(point.v));
		json.put("a", this.vectorToJson(point.a));
		return json;
	}

	JSONObject springToJson(ForceDirectedLayout.Spring spring) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("k", spring.k);
		json.put("length", spring.length);
		json.put("point1", this.pointToJson(spring.point1));
		json.put("point2", this.pointToJson(spring.point2));
		return json;
	}

	void write(Writer out) throws JSONException, IOException {	
		JSONObject json = new JSONObject();
        json.put("stiffness", this.layout.stiffness);
        json.put("repulsion", this.layout.repulsion);
        json.put("damping", this.layout.damping);
        json.put("unitTime", this.layout.unitTime);
        json.put("energy", this.layout.totalEnergy());
        json.put("energy", this.layout.totalEnergy());
        // put nodePoints
		JSONObject nodePointsJson = new JSONObject();
		for (Graph.Node node: this.layout.graph.nodes) {
			ForceDirectedLayout.Point point = this.layout.point(node);
			nodePointsJson.put(node.id, pointToJson(point));
		}
        json.put("nodePoints", nodePointsJson);
        
        // put edgeSprings
		JSONObject edgeSpringsJson = new JSONObject();
		for (Graph.Edge edge: this.layout.graph.edges) {
			ForceDirectedLayout.Spring spring = this.layout.spring(edge);
			edgeSpringsJson.put(edge.id, springToJson(spring));
		}
        json.put("edgeSprings", edgeSpringsJson);

        // write
        out.write(json.toString(4));
	}
}

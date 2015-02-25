package net.jangxyz.springy.runner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import net.jangxyz.springy.ForceDirectedLayout;
import net.jangxyz.springy.ForceDirectedLayout.Point;
import net.jangxyz.springy.Graph;
import net.jangxyz.springy.Vector;

import org.json.JSONException;

/*
 * 
 * Analyze basic statistics about a layout of a graph
 * 
 */
public class AnalyzeLayoutMain {
	
	static Graph importGraph(String filename) throws IOException, JSONException {
		Reader reader;
		if (filename.endsWith(".gz")) {
			InputStream instream = new GZIPInputStream(new FileInputStream(filename));
			reader = new BufferedReader(new InputStreamReader(instream));
		} else {
			reader = new FileReader(filename);			
		}		
		return new JsonGraphReader(reader).read();
	}
	static ForceDirectedLayout importLayout(String layoutFilename, String graphFilename) throws JSONException, IOException {
		Graph graph = importGraph(graphFilename);
		Reader reader;
		if (layoutFilename.endsWith(".gz")) {
			InputStream instream = new GZIPInputStream(new FileInputStream(layoutFilename));
			reader = new BufferedReader(new InputStreamReader(instream));
		} else {
			reader = new FileReader(layoutFilename);			
		}
		
	    Map<String, Float> layoutOptions = new HashMap<String, Float>();
		return new JsonLayoutReader(graph, reader).read(layoutOptions);
	}

	/**
	 * @param args
	 *
	 * 		Usage: java AnalyzeLayoutMain GRAPH_FILENAME LAYOUT_FILENAME
	 * 
	 * 		- graph MATCH: 700 nodes, 350 edges
	 * 
	 * 		layout
	 *      - stiffness: 400
	 *      - damping: 0.3
	 *      - repulsion: 400
	 *      - unitTime: 0.02
	 * @throws Exception 
	 * @throws IOException 
	 * 
	 */	
	public static void main(String[] _args) throws Exception {
		//
		ForceDirectedLayout layout;
		
		if (_args.length < 2) {
			System.out.println("Usage: AnalyzeLayoutMain GRAPH_FILENAME LAYOUT_FILENAME");
			return;
		}
		
		//
		List<String> argList = new ArrayList<String>(Arrays.asList(_args));
		
		//
		String graphFilename = argList.get(0);
		String layoutFilename = argList.get(1);
		
	    layout = importLayout(layoutFilename, graphFilename);
	    layout.tick(layout.unitTime);
	    
	    //
	    System.out.println("Result\n");
	    System.out.format("graph: %d nodes, %d edges\n", layout.graph.nodes.size(), layout.graph.edges.size());
	    System.out.println("layout");
	    System.out.format("  - stiffness %f\n", layout.stiffness);
	    System.out.format("  - damping %f\n", layout.damping);
	    System.out.format("  - repulsion %f\n", layout.repulsion);
	    System.out.format("  - unit time %f\n", layout.unitTime);
	    
	    System.out.format("  - total energy %s\n", layout.totalEnergy());
	    System.out.format("  - force %s\n", formatVector(layout, new PointValueGetter() {
	    	@Override
			public Vector get(Point point) {
	    		return point.a;
	    	}
	    }));
	    System.out.format("  - velocity %s\n", formatVector(layout, new PointValueGetter() {
	    	@Override
			public Vector get(Point point) {
	    		return point.v;
	    	}
	    }));
	    System.out.format("  - position %s\n", formatVector(layout, new PointValueGetter() {
	    	@Override
			public Vector get(Point point) {
	    		return point.p;
	    	}
	    }));
	}

	static String formatVector(ForceDirectedLayout layout, PointValueGetter getter) {
		Double[] distance2Diff = new Double[layout.nodePoints.size()];
		
		// collect magnitude
		int i = 0;
		Iterator<Point> points1 = layout.eachPoints().iterator();
		while (points1.hasNext()) {
			Point point1 = points1.next();

			Vector v1 = getter.get(point1);
			distance2Diff[i] = v1.magnitude2();
			
			i += 1;
		}
		
		// 
		Double minDiff2 = distance2Diff[0];
		Double maxDiff2 = distance2Diff[0];
		Double medDiff2 = 0.0;
		Double q1Diff2 = 0.0;
		Double q3Diff2 = 0.0;
		Double sumDiff = 0.0;
		
		// sum
		for (double diff2: distance2Diff) {
			if (diff2 < minDiff2) { minDiff2 = diff2; }
			if (diff2 > maxDiff2) { maxDiff2 = diff2; }
			sumDiff += Math.sqrt(diff2);
		}
		
		// median
		Arrays.sort(distance2Diff);
		medDiff2 = distance2Diff[distance2Diff.length/2];
		q1Diff2 = distance2Diff[distance2Diff.length/4*1];
		q3Diff2 = distance2Diff[distance2Diff.length/4*3];
		
		// format string
		String[] descriptions = {
			String.format("(min, max): (%f, %f)", Math.sqrt(minDiff2), Math.sqrt(maxDiff2)),
			String.format("[1q,2q,3q]: [%f, %f, %f]", Math.sqrt(q1Diff2), Math.sqrt(medDiff2), Math.sqrt(q3Diff2)),
			String.format("(sum, avg): (%f, %f)", sumDiff, sumDiff/distance2Diff.length)
		};
		StringBuffer sb = new StringBuffer();
		for (String d: descriptions) {
			sb.append("    - ");
			sb.append(d);
			sb.append("\n");
		}
		
		return String.format("\n    %s", sb.toString().trim());
	}

}

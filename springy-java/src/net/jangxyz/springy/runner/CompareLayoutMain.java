package net.jangxyz.springy.runner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.jangxyz.springy.ForceDirectedLayout;
import net.jangxyz.springy.ForceDirectedLayout.Point;
import net.jangxyz.springy.Graph;
import net.jangxyz.springy.Vector;

import org.json.JSONException;

public class CompareLayoutMain {
	
	static Graph importGraph(String filename) throws IOException, JSONException {
		return new JsonGraphReader(filename).read();
	}
	static ForceDirectedLayout importLayout(String filename, String graphFilename) throws JSONException, IOException {
		Graph graph = importGraph(graphFilename);
	    Map<String, Float> layoutOptions = new HashMap<String, Float>();
		return new JsonLayoutReader(graph, filename).read(layoutOptions);
	}

	/**
	 * @param args
	 *
	 * 		Usage: java CompareLayoutMain GRAPH_FILENAME LAYOUT1_FILENAME LAYOUT2_FILENAME
	 * 
	 * 		- graph MATCH: 700 nodes, 350 edges
	 * 
	 * 		layout
	 *      - stiffness MATCH: 400
	 *      - damping MATCH: 0.3
	 *      - repulsion MATCH: 400
	 *      - unitTime MATCH: 0.02
	 *      - position MISMATCH:
	 *          - min of distance difference
	 *          - max of distance difference
	 *          - med. of distance difference
	 *          - sum of distance difference
	 *          - avg of distance difference
	 *      - total energy MISMATCH: 
	 *          388772.7419827046 VS 388772.7419827046 
	 * @throws Exception 
	 * @throws IOException 
	 * 
	 */	
	public static void main(String[] _args) throws Exception {
		//
		ForceDirectedLayout refLayout;
		
		if (_args.length < 3) {
			System.out.println("Usage: CompareLayoutMain GRAPH_FILENAME LAYOUT1_FILENAME LAYOUT2_FILENAME");
			return;
		}
		
		//
		List<String> argList = new ArrayList<String>(Arrays.asList(_args));
		
		//
		String graphFilename = argList.get(0);
		String layoutFilename1 = argList.get(1);
		List<String> layoutFilenames = argList.subList(2, argList.size());
	
		// 
	    refLayout = importLayout(layoutFilename1, graphFilename);
	    refLayout.tick(refLayout.unitTime);
	    
	    // rest layouts
	    boolean isFirst = true;
	    for (String layoutFilename: layoutFilenames) {
	    	ForceDirectedLayout targetLayout = importLayout(layoutFilename, graphFilename);
	    	targetLayout.tick(targetLayout.unitTime);
		    
		    //
		    compareLayouts(refLayout, targetLayout);
//		    compareLayouts2(refLayout, layoutFilename, targetLayout, isFirst);
		    
		    if (isFirst) {
		    	isFirst = false;
		    }
	    }
	}
	
	static void compareLayouts(ForceDirectedLayout layout1, ForceDirectedLayout layout2) {
		System.out.println("Result\n");
	    System.out.format("graph MATCH: %d nodes, %d edges\n", layout1.graph.nodes.size(), layout1.graph.edges.size());
	    System.out.println("layout");
	    System.out.format("  - stiffness %s\n", formatMatchValue(layout1.stiffness, layout2.stiffness));
	    System.out.format("  - damping %s\n", formatMatchValue(layout1.damping, layout2.damping));
	    System.out.format("  - repulsion %s\n", formatMatchValue(layout1.repulsion, layout2.repulsion));
	    System.out.format("  - unit time %s\n", formatMatchValue(layout1.unitTime, layout2.unitTime));
	    
	    System.out.format("  - total energy %s\n", formatMatchValue(layout1.totalEnergy(), layout2.totalEnergy()));
	    System.out.format("  - force %s\n", formatMatchVector(layout1, layout2, new PointValueGetter() {
	    	@Override
			public Vector get(Point point) {
	    		return point.a;
	    	}
	    }));
	    System.out.format("  - velocity %s\n", formatMatchVector(layout1, layout2, new PointValueGetter() {
	    	@Override
			public Vector get(Point point) {
	    		return point.v;
	    	}
	    }));
	    System.out.format("  - position %s\n", formatMatchVector(layout1, layout2, new PointValueGetter() {
	    	@Override
			public Vector get(Point point) {
	    		return point.p;
	    	}
	    }));
	}
	
	static String join(Object[] strings, String sep) {
		StringBuffer sb = new StringBuffer();
		
		for(int i = 0; i < strings.length; i++) {
			sb.append(strings[i].toString());
			if (i != strings.length - 1) {
				sb.append(sep);
			}
		}
		
		return sb.toString();
	}
	
	
	static String formatMatchValue(Double value1, Double value2) {
		String matched, description;
		
		if (value1.equals(value2)) {
			matched = "MATCH";
			description = value1.toString();
		} else {
			matched = "MISMATCH";
			description = String.format("%s VS %s", value1, value2);
		}
		
		return String.format("%s: %s", matched, description);		
	}

	interface PointValueGetter {
		Vector get(Point point);
	}

	static Double[] compareVectorDistance2Diff(ForceDirectedLayout layout1, ForceDirectedLayout layout2, PointValueGetter getter) {
		Double[] distance2Diff = new Double[layout1.nodePoints.size()];
		
		// collect diff2
		int i = 0;
		Iterator<Point> points1 = layout1.eachPoints().iterator();
		Iterator<Point> points2 = layout2.eachPoints().iterator();
		while (points1.hasNext()) {
			Point point1 = points1.next();
			Point point2 = points2.next();

			Vector v1 = getter.get(point1);
			Vector v2 = getter.get(point2);
			Vector diff = v1.subtract(v2);
			distance2Diff[i] = diff.magnitude2();
			
			i += 1;
		}

		Arrays.sort(distance2Diff);

		return distance2Diff;
	}
	
	static class VectorComparison {
		Double[] distance2Diff;
		//
		Double diff2Sum;
		Double diff2Min;
		Double diff2Max;
		Double diff2Med;
		Double diff2Q1;
		Double diff2Q3;
		//
		double diffMin;
		double diffMax;
		double diffMed;
		double diffQ1;
		double diffQ3;
		double diffSum;
		double diffAvg;

		VectorComparison(ForceDirectedLayout layout1, ForceDirectedLayout layout2, PointValueGetter getter) {
			this.distance2Diff = compareVectorDistance2Diff(layout1, layout2, getter);
			
			// diff2
			this.diff2Min = distance2Diff[0];
			this.diff2Max = distance2Diff[distance2Diff.length - 1];
			this.diff2Med = distance2Diff[distance2Diff.length/2];
			this.diff2Q1 = distance2Diff[distance2Diff.length/4*1];
			this.diff2Q3 = distance2Diff[distance2Diff.length/4*3];		
			
			// sum
			this.diff2Sum = 0.0;
			for (double diff2: distance2Diff) {
				this.diff2Sum += diff2;
			}

			// diff1
			this.diffMin = Math.sqrt(this.diff2Min);
			this.diffMax = Math.sqrt(this.diff2Max);
			this.diffMed = Math.sqrt(this.diff2Med);
			this.diffQ1  = Math.sqrt(this.diff2Q1);
			this.diffQ3  = Math.sqrt(this.diff2Q3);
			this.diffSum = Math.sqrt(this.diff2Sum);
			this.diffAvg = Math.sqrt(this.diff2Sum)/this.distance2Diff.length;
		}
		
	}
	
	static String formatMatchVector(ForceDirectedLayout layout1, ForceDirectedLayout layout2, PointValueGetter getter) {
		VectorComparison comp = new VectorComparison(layout1, layout2, getter);

		if (comp.diff2Sum == 0) {
			return String.format("MATCH: all %d nodes are exactly same.", layout1.nodePoints.size());
		} 
		
		// format string
		String[] descriptions = {
				String.format("(min, max): (%f, %f)", comp.diffMin, comp.diffMax),
				String.format("[1q,2q,3q]: [%f, %f, %f]", comp.diffQ1, comp.diffMed, comp.diffQ3),
				String.format("(sum, avg): (%f, %f)", comp.diffSum, comp.diffAvg)
			};
		StringBuffer sb = new StringBuffer();
		for (String d: descriptions) {
			sb.append("    - ");
			sb.append(d);
			sb.append("\n");
		}
		
		return String.format("MISMATCH:\n    %s", sb.toString().trim());
	}


}

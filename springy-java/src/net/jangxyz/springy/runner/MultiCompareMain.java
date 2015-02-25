package net.jangxyz.springy.runner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jangxyz.springy.ForceDirectedLayout;
import net.jangxyz.springy.ForceDirectedLayout.Point;
import net.jangxyz.springy.Graph;
import net.jangxyz.springy.Vector;

import org.json.JSONException;

/*
 * 
 * Compares multiple layouts, given a reference layout
 * 
 * Could be used for:
 *   - in a single graph, track layout diff along the ticks
 * 
 */
public class MultiCompareMain {
	
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
	 * 		Usage: java MultiCompareMain GRAPH_FILENAME REF_LAYOUT_FILENAME TARGET_LAYOUT1_FILENAME TARGET_LAYOUT2_FILENAME ...
	 * 
	 * @throws Exception 
	 * 
	 */	
	public static void main(String[] _args) throws Exception {
		//
		ForceDirectedLayout refLayout;
		
		if (_args.length < 3) {
			System.out.println("Usage: MultiCompareMain GRAPH_FILENAME REF_LAYOUT_FILENAME TARGET_LAYOUT1_FILENAME ...");
			return;
		}
		
		//
		List<String> argList = new ArrayList<String>(Arrays.asList(_args));
		
		//
		String graphFilename = argList.get(0);
		String refLayoutFilename = argList.get(1);
		List<String> layoutFilenames = argList.subList(2, argList.size());
	
		// 
	    refLayout = importLayout(refLayoutFilename, graphFilename);
	    refLayout.tick(refLayout.unitTime);
	    
	    // rest layouts
	    boolean isFirst = true;
	    for (String layoutFilename: layoutFilenames) {
	    	ForceDirectedLayout targetLayout = importLayout(layoutFilename, graphFilename);
	    	targetLayout.tick(targetLayout.unitTime);
		    
		    //
	    	if (layoutFilename.equals(refLayoutFilename)) {
			    compareLayout(refLayout, layoutFilename, targetLayout, isFirst);	    		
	    	} else {
			    compareLayout(refLayout, layoutFilename, targetLayout, isFirst);	    		
	    	}
		    
		    if (isFirst) {
		    	isFirst = false;
		    }
	    }
	}

	static void compareLayout(ForceDirectedLayout refLayout, String targetLayoutFilename, ForceDirectedLayout targetLayout, boolean printHeaders) {
		if (printHeaders) {
			System.out.format("%s\t%s\n", "filename", 
					join(new String[] {
							"total energy", 
							"force min", "force q1", "force median", "force q3", "force max", "force sum", "force avg", 
							"velocity min", "velocity q1", "velocity median", "velocity q3", "velocity max", "velocity sum", "velocity avg", 
							"pos error min", "pos error q1", "pos error median", "pos error q3", "pos error max", "pos error sum", "pos error avg" 
					}, "\t")
			);
		}

		//
		Double[] values = computeLayoutValues(targetLayout, refLayout);
		System.out.format("%s\t%s\n", targetLayoutFilename, join(values, "\t"));
	}
	
	static Double[] computeLayoutValues(ForceDirectedLayout targetLayout, ForceDirectedLayout refLayout) {
		double energyValue = targetLayout.totalEnergy();
		VectorAnalysis forcesValue = new VectorAnalysis(targetLayout, new PointValueGetter() {
			@Override
			public Vector get(Point point) {
				return point.a;
			}
		});
		VectorAnalysis velocitiesValue = new VectorAnalysis(targetLayout, new PointValueGetter() {
	    	@Override
			public Vector get(Point point) {
	    		return point.v;
	    	}
	    });
		VectorAnalysis positionsDiff = new VectorAnalysis(targetLayout, new PointValueGetter() {
	    	@Override
			public Vector get(Point point) {
	    		return point.p;
	    	}
	    }).compareTo(refLayout);

		Double[] values = {
			energyValue,
			forcesValue.minValue,     forcesValue.q1Value,     forcesValue.medianValue,     forcesValue.q3Value,     forcesValue.maxValue,     forcesValue.sumValue,     forcesValue.avgValue,
			velocitiesValue.minValue, velocitiesValue.q1Value, velocitiesValue.medianValue, velocitiesValue.q3Value, velocitiesValue.maxValue, velocitiesValue.sumValue, velocitiesValue.avgValue,  
			positionsDiff.minValue,   positionsDiff.q1Value,   positionsDiff.medianValue,   positionsDiff.q3Value,   positionsDiff.maxValue,   positionsDiff.sumValue,   positionsDiff.avgValue  
		};
		return values;
	}

}

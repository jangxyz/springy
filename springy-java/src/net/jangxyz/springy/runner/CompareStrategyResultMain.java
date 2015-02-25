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
 * Compare two strategies' results, by comparing their end positions.
 * Reference is set with the initial and the end layouts of the reference strategy
 * (NOTE the initial layout is the same for the target strategy as well)
 * 
 * Having a reference set, now compare it with the target reference.
 * This way, you can have difference in a percentage value.
 * 
 */
public class CompareStrategyResultMain {

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
	 * 		Usage: java CompareStrategyResultMain GRAPH_FILENAME INIT_LAYOUT_FILENAME REF_LAYOUT_FILENAME TARGET_LAYOUT_FILENAME
	 * 
	 * @throws Exception 
	 * 
	 */	
	public static void main(String[] _args) throws Exception {
		if (_args.length < 4) {
			System.out.println("Usage: CompareStrategyResultMain GRAPH_FILENAME INIT_LAYOUT_FILENAME REF_LAYOUT_FILENAME TARGET_LAYOUT_FILENAME");
			return;
		}
		
		//
		List<String> argList = new ArrayList<String>(Arrays.asList(_args));
		
		//
		String graphFilename = argList.get(0);
		String initLayoutFilename = argList.get(1);
		String refLayoutFilename = argList.get(2);
		String targetLayoutFilename = argList.get(3);

		// 
		System.out.println("init layout file     : " + initLayoutFilename);
		ForceDirectedLayout initLayout = importLayout(initLayoutFilename, graphFilename);
	    initLayout.tick(initLayout.unitTime);

		// 
		System.out.println("reference layout file: " + refLayoutFilename);
	    ForceDirectedLayout refLayout = importLayout(refLayoutFilename, graphFilename);
	    refLayout.tick(refLayout.unitTime);
	    
		System.out.println("target layout file   : " + targetLayoutFilename);
	    ForceDirectedLayout targetLayout = importLayout(targetLayoutFilename, graphFilename);
	    targetLayout.tick(targetLayout.unitTime);

	    // compare layout
		VectorAnalysis initDiff  = computeLayoutValues(initLayout, refLayout);
		VectorAnalysis targetDiff = computeLayoutValues(targetLayout, refLayout);
		double errorRate = targetDiff.sumValue / initDiff.sumValue;

		// try tick 1
		initLayout.tick(initLayout.unitTime);
		VectorAnalysis init2Diff = computeLayoutValues(initLayout, refLayout);
		
		System.out.format("initial position error: sum %f, avg %f, error 100.0 %% \n", initDiff.sumValue, initDiff.avgValue);
		System.out.format("init +1 position error: sum %f, avg %f, error %f (%.2f %%) \n", init2Diff.sumValue, init2Diff.avgValue, init2Diff.sumValue / initDiff.sumValue, init2Diff.sumValue / initDiff.sumValue * 100.0);
		System.out.format("target  position error: sum %f, avg %f, error %f (%.2f %%) \n", targetDiff.sumValue, targetDiff.avgValue, errorRate, errorRate * 100.0);
		System.out.format("\t-[|]- %f\t%f\t%f\t%f\t%f\n", targetDiff.minValue, targetDiff.q1Value, targetDiff.medianValue, targetDiff.q3Value, targetDiff.maxValue);
		
	}
	
	static VectorAnalysis computeLayoutValues(ForceDirectedLayout targetLayout, ForceDirectedLayout refLayout) {
		return new VectorAnalysis(targetLayout, new PointValueGetter() {
	    	@Override
			public Vector get(Point point) {
	    		return point.p;
	    	}
	    }).compareTo(refLayout);
	}

}
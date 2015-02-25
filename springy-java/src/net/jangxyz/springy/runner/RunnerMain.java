package net.jangxyz.springy.runner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.jangxyz.springy.BarnesHutLayout;
import net.jangxyz.springy.ForceDirectedLayout;
import net.jangxyz.springy.Graph;
import net.jangxyz.springy.Graph.Edge;

import org.json.JSONException;


public class RunnerMain {
	
	static String testResultPath;// = "data/" + m.format("YYYYMMDD-HHmmSS") + '_' + [nodesNum, edgesNum, layout.stiffness, layout.repulsion, layout.damping, layout.unitTime].join('_');
	static boolean save = true;

	static Graph createTestGraph(int nodesNum, int edgesNum) {
	    Graph graph = new Graph(nodesNum, edgesNum);

	    // add 100 nodes
	    List<Graph.Node> nodes = new ArrayList<Graph.Node>(nodesNum);
	    List<String> edgeCandidate = new ArrayList<String>(edgesNum);
	    for (int i=0; i<nodesNum; i++) {
	    	Map<String, String> options = new HashMap<String,String>();
	    	options.put("label", Integer.toString(i));
	        Graph.Node node = graph.newNode(options);
	        //
	        nodes.add(node);
	        edgeCandidate.add(node.id);
	    }

//        // add random nodes
//	    for (int i=0; i<edgesNum; i++) {
//			Graph.Node node1 = selectRandomNode(graph, edgeCandidate);
//			Graph.Node node2 = selectRandomNode(graph, edgeCandidate);
//			createEdge(graph, node1, node2);
//			//
//			edgeCandidate.add(node1.id);
//			edgeCandidate.add(node2.id);
//	    }
	    
//	    // add complete edges
//	    for (int i=0; i<nodesNum; i++) {
//	    	Graph.Node node1 = nodes.get(i);
//		    for (int j=i+1; j<nodesNum; j++) {
//		    	Graph.Node node2 = nodes.get(j);
//				createEdge(graph, node1, node2);
//		    }
//	    	System.out.println(i);
//	    }
	    
	    // add ring edges	    	
	    for (int i=0; i<nodesNum; i++) {
	    	Graph.Node node1 = nodes.get(i);
	    	Graph.Node node2 = nodes.get((i+1)%nodesNum);
	    	createEdge(graph, node1, node2);
	    }

	    return graph;
	}

	static Graph.Node selectRandomNode(Graph graph, List<String> edgeCandidate) {
		int randomIndex = (int)(Math.random() * edgeCandidate.size());
		String candId = edgeCandidate.get(randomIndex);
		Graph.Node node = graph.getNode(candId);
		return node;
	}
	
	static Edge createEdge(Graph graph, Graph.Node node1, Graph.Node node2) {
		Map<String, String> edgeData = new HashMap<String, String>();
		edgeData.put("label", node1.id + " --> " + node2.id);
		return graph.newEdge(node1, node2, edgeData);
	}

	
	static ForceDirectedLayout createGraphLayout(Graph graph, Map<String, Float> options) {
		float stiffness      = options.containsKey("stiffness") ? options.get("stiffness") : 400.0f;
		float repulsion      = options.containsKey("repulsion") ? options.get("repulsion") :  400.0f;
		float damping        = options.containsKey("damping") ? options.get("damping") :   0.5f;
		float unitTime       = options.containsKey("unitTime") ? options.get("unitTime") :  0.03f;
		float energyTreshold = options.containsKey("energyThreshold") ? options.get("energyTreshold") :  0.01f;

	    // layout
		boolean isBarnesHutLayout = options.containsKey("barnes-hut-layout") ? options.get("barnes-hut-layout") == 1.0f :  false;
		ForceDirectedLayout layout;
		if (isBarnesHutLayout) {
			System.out.println("BarnesHutLayout");
			float theta = options.containsKey("theta") ? options.get("theta") :  0.05f;
			layout = new BarnesHutLayout(graph, stiffness, repulsion, damping, energyTreshold, unitTime, theta);						
		} else {
			System.out.println("ForceDirectedLayout");
			layout = new ForceDirectedLayout(graph, stiffness, repulsion, damping, energyTreshold, unitTime);			
		}

	    return layout;
	}

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

	static void exportGraph(Graph graph, Writer writer) throws JSONException, IOException {
		new JsonGraphWriter(graph).write(writer);
		writer.close();
	}
	static void exportGraph(Graph graph, String filename) throws IOException, JSONException {
		Writer out;
		if (filename.endsWith(".gz")) {
	        GZIPOutputStream outstream = new GZIPOutputStream(new FileOutputStream(filename));
	        out = new OutputStreamWriter(outstream, "UTF-8");
	        out = new BufferedWriter(out);
		} else {
			out = new BufferedWriter(new PrintWriter(filename, "UTF-8"));
		}
//		out = new BufferedWriter(new OutputStreamWriter(System.out));			
		
		exportGraph(graph, out);
	}
	
	static ForceDirectedLayout importLayout(String filename, Graph graph, Map<String, Float> options) throws JSONException, IOException {
		Reader reader;
		if (filename.endsWith(".gz")) {
			InputStream instream = new GZIPInputStream(new FileInputStream(filename));
			reader = new BufferedReader(new InputStreamReader(instream));
		} else {
			reader = new FileReader(filename);			
		}
		
		return new JsonLayoutReader(graph, reader).read(options);
	}

	static void exportLayout(ForceDirectedLayout layout, Writer writer) throws JSONException, IOException {
		new JsonLayoutWriter(layout).write(writer);
		writer.close();
	}
	static void exportLayout(ForceDirectedLayout layout, String filename) throws IOException, JSONException {
		Writer out;
		if (filename.endsWith(".gz")) {
	        GZIPOutputStream outstream = new GZIPOutputStream(new FileOutputStream(filename));
	        out = new OutputStreamWriter(outstream, "UTF-8");
	        out = new BufferedWriter(out);
		} else {
			out = new PrintWriter(filename, "UTF-8");
			out = new BufferedWriter(out);
		}
//		out = new BufferedWriter(new OutputStreamWriter(System.out));			
		
		exportLayout(layout, out);
	}

    static boolean checkPrint(int i) {
//    	if ( i < 1000) return true; // DEBUG
        if (i <= 300) { return true; } // DEBUG
        
        if (i == 1)   { return true; }
//        if (i <= 100) { return true; }
//        if (i > 30  && i <=  100 && i % 10 == 0) { return true; }
//        if (i > 100 && i <= 1000 && i % 50 == 0) { return true; }
        if (i % 100 == 0)              { return true; }
        
        return false;
    }
    static boolean checkSaveAt(int i) {
//    	if (true) return true; // DEBUG
        if (i <= 300 || i % 10 == 0) { return true; } // DEBUG
        
        if (i < 10) { return true; } 	// 1,2,3,4,5,6,7,8,9 
        if (i == 10) { return true; } 	// 10
        if (i < 1000 && i % 100 == 0) { return true; } // 100, 200, 300, 400, 500, 600, 700, 800, 900
        if (i % 1000 == 0) { return true; } // 1000, 2000, 3000, 4000, ...
//        if (i % 1000 == 300) { return true; }
//        if (i % 1000 == 700) { return true; }
        
        return false;
    }
    

	static long printLayoutState(final ForceDirectedLayout layout, int count, long prevRunTime) {
		
		
		//
		String description = String.format("- %d energy: %f", count, layout.totalEnergy());
		description += String.format(" / %s /", layout.appendDescription());
		
		description += String.format(" [%.2fx%.2f]", layout.getBoundingBox().getWidth(), layout.getBoundingBox().getHeight());

//	    Double minVelocity2 = null;
//	    Double maxVelocity2 = null;
//		for (ForceDirectedLayout.Point point: layout.eachPoints()) {
//			double vMag2 = point.v.magnitude2();
//			if (minVelocity2 == null) {
//				minVelocity2 = vMag2;				
//			}
//			if (vMag2 < minVelocity2) {
//				minVelocity2 = vMag2;
//			}
//			if (maxVelocity2 == null) {
//				maxVelocity2 = vMag2;				
//			}
//			if (vMag2 > maxVelocity2) {
//				maxVelocity2 = vMag2;
//			}			
//		}
//		String velocityStr = String.format("v:(%f, %f)", Math.sqrt(minVelocity2), Math.sqrt(maxVelocity2));
//		description += String.format(" / %s ", velocityStr);
		
		long thisTime = System.nanoTime();
		double elapsedTime = (thisTime - prevRunTime)/1e9;
		description += String.format(" (%f s)", elapsedTime);
		
		System.out.println(description);

		
		return thisTime;
	}

	static void runTest(final ForceDirectedLayout layout) {		
		layout.start(new ForceDirectedLayout.Callbacks() {
			int count = 0;

			long startTime = System.nanoTime();    
			long prevRunTime = startTime;
			
			public void onRenderStart() {
				if (!save) {
					return;
				}
				
				// save graph
			    try {
		        	String graphFilename = String.format("%s/graph.json.gz", testResultPath);
					exportGraph(layout.graph, graphFilename);
				} catch (Exception e1) {
					System.err.println("failed exporting graph.");
					e1.printStackTrace();
				}
				
				// save initial layout
		        try {
					String layoutFilename = String.format("%s/layout_%d.json.gz", testResultPath, count);
					exportLayout(layout, layoutFilename);
				} catch (Exception e) {
					System.err.println("failed exporting layout.");
					e.printStackTrace();
				}
				
//				layout.stop(); // DEBUG
			}
			public void render() {
			    this.count += 1;

			    if (checkPrint(count)) {
			        long thisTime = printLayoutState(layout, count, prevRunTime);
			        prevRunTime = thisTime;
			    }
			    if (save && checkSaveAt(count)) {
			        try {
						exportLayout(layout, String.format("%s/layout_%d.json.gz", testResultPath, count));
					} catch (Exception e) {
						System.err.println("failed exporting layout.");
						e.printStackTrace();
					}
			    }

			}
			public void onRenderStop() {
				// print layout
		        printLayoutState(layout, count, prevRunTime);

		        if (save) {
				    // write layout		
					Writer out = null;
					try {
						String filename = String.format("%s/layout_%d.json", testResultPath, count);
						out = new BufferedWriter(new PrintWriter(filename, "UTF-8"));
						exportLayout(layout, out);
						System.out.println("saved under: " + testResultPath);
					} catch (Exception e) {
						System.err.println("failed exporting layout.");
						e.printStackTrace();
					} finally {
						if (out != null) {
							try {
								out.close();
							} catch (IOException e) {
							}
						}
					}		        	
		        }

				// time
			    long end = System.nanoTime();
			    System.out.printf("done. %d ticks, %f seconds.\n", count, (end - startTime)/1e9);
			}
		});
	}
	
	public static String ffmt(double d)	{
	    if (d == (int)d) {
	        return String.format("%d", (int)d);
	    }
	    return String.format("%f", d).replaceAll("0*$", "");
	}
	
	/**
	 * @param args
	 * @throws JSONException 
	 * @throws Exception 
	 */
	public static void main(String[] _args) throws Exception {
		int nodesNum;
		int edgesNum;
		
		//
		Graph graph;
		ForceDirectedLayout layout;

		//
		List<String> argList = new ArrayList<String>(Arrays.asList(_args));
		
		// unit time
		Float unitTimeArg = null;
		int argIndex = argList.indexOf("--unittime");
		if (argIndex != -1) {
			unitTimeArg = Float.parseFloat(argList.remove(argIndex+1));
			argList.remove(argIndex);
		}
		// layout
		boolean isBarnesHutLayout = false;
		argIndex = argList.indexOf("--layout");
		if (argIndex != -1) {
			String layoutClassName = argList.remove(argIndex+1);
			if (layoutClassName.equals("BarnesHutLayout")) {
				isBarnesHutLayout = true;
			}
			argList.remove(argIndex);
		}
		// theta for BarnesHutLayout
		float theta = 0.5f;
		argIndex = argList.indexOf("--theta");
		if (argIndex != -1) {
			theta = Float.parseFloat(argList.remove(argIndex+1));
			argList.remove(argIndex);
		}
		// save
		save = true;
		argIndex = argList.indexOf("--no-save");
		if (argIndex != -1) {
			save = false;
			argList.remove(argIndex);
		}
		
		//
		String args1 = argList.size() >= 1 ? argList.get(0) : "2";
		String args2 = argList.size() >= 2 ? argList.get(1) : "1";
		
		System.out.println("arg1: " + args1 + " "  + new File(args1).exists());
		System.out.println("arg2: " + args2 + " " + new File(args2).exists());
		
	    Map<String, Float> layoutOptions = new HashMap<String, Float>();
	    if (unitTimeArg != null) {
		    layoutOptions.put("unitTime", unitTimeArg);
	    } else {
		    layoutOptions.put("unitTime", 0.01f);
	    }
	    if (isBarnesHutLayout) {
		    layoutOptions.put("barnes-hut-layout", 1.0f);		    	
	    }
	    layoutOptions.put("theta", theta);


		// read from file
		if (new File(args1).exists() && new File(args2).exists()) {
		    String graphFilename  = args1;
		    String layoutFilename = args2;

		    // import graph
			graph = importGraph(graphFilename);
			
			// import layout
		    layout = importLayout(layoutFilename, graph, layoutOptions);
		}
		// generate
		else {
			nodesNum = Integer.parseInt(args1);
			edgesNum = Integer.parseInt(args2);
			//
			graph = createTestGraph(nodesNum, edgesNum);
			//
		    layoutOptions.put("stiffness", 400f);
		    layoutOptions.put("repulsion", 400f);
		    layoutOptions.put("damping", 0.3f);
		    layoutOptions.put("energyTreshold", 0.01f);
		    layout = createGraphLayout(graph, layoutOptions);	    
		}
		nodesNum = graph.nodes.size();
		edgesNum = graph.edges.size();
		
		System.out.format("graph: %s, nodes %d, edges %d\n", graph, nodesNum, edgesNum);
		System.out.println("layout: " + layout);

		// prepare directory
		if (save) {
			Date now = new Date();
		    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-hhmmss");
		    testResultPath = String.format("data/%s_%d_%d_%s_%s_%s_%s_java", dateFormat.format(now), 
		    		nodesNum, edgesNum, 
		    		ffmt(layout.stiffness), ffmt(layout.repulsion), ffmt(layout.damping), ffmt(layout.unitTime));
		    File testPath = new File(testResultPath);
		    if (!testPath.exists()) {
		    	testPath.mkdirs();
		    }
		    testResultPath = testPath.getCanonicalPath();			
		}
	    
	    //
		runTest(layout);
	}

}

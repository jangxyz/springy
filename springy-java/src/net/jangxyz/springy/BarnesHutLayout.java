package net.jangxyz.springy;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;

import mikechambers.quadtree.Bound;
import mikechambers.quadtree.Qnode;
import mikechambers.quadtree.QuadTree;

public class BarnesHutLayout extends ForceDirectedLayout {
    //
    //
    //
	private float THETA = 0.05f;
	
    QuadTree<Point> quadtree;
	private int _forceAppliedCount;

	public BarnesHutLayout(Graph graph, double stiffness, double repulsion, double damping, double energyThreshold, double unitTime, float theta) {
		this(graph, stiffness, repulsion, damping, energyThreshold, unitTime);
		this.THETA = theta;
	}
	
	public BarnesHutLayout(Graph graph, double stiffness, double repulsion, double damping, double energyThreshold, double unitTime) {
		super(graph, stiffness, repulsion, damping, energyThreshold, unitTime);
	}
	public BarnesHutLayout(Graph graph, float stiffness, float repulsion, float damping, float energyThreshold, float unitTime) {
		this(graph, (double)stiffness, (double)repulsion, (double)damping, (double)energyThreshold, (double)unitTime);
	}
	
	public String toString() {
		return String.format("<BarnesHutLayout %d %d %.1f %.1f %.1f %.2f %.2f>", nodePoints.size(), edgeSprings.size(), stiffness, repulsion, damping, unitTime, THETA);
	}
	
	//
	// strategy
	//	
	QuadTree<Point> initializeQuadTree() {
		// this initializes point position
		Double minX = null, 
			   maxX = null,
			   minY = null, 
			   maxY = null;
		for(Point point: this.eachPoints()) {
			minX = minX == null ? point.p.x : Math.min(minX, point.p.x);
			maxX = maxX == null ? point.p.x : Math.max(maxX, point.p.x);
			minY = minY == null ? point.p.y : Math.min(minY, point.p.y);
			maxY = maxY == null ? point.p.y : Math.max(maxY, point.p.y);			
		}

		BoundingBox bb = this.getBoundingBox();
//		System.out.println("initial bounding box: " + bb);
		Integer maxDepth = 10;
		int maxChildren = 1;
		QuadTree<Point> quadtree = new QuadTree<Point>(new Bound(bb.bottomleft.x,bb.bottomleft.y, bb.getWidth(),bb.getHeight()), maxDepth, maxChildren);

		for(Graph.Node node: this.eachNodes()) {
			Point point = this.point(node);
			quadtree.insert(point);
		}

		return quadtree;
	}
	
    void applyCoulombsLaw() {
    	this._forceAppliedCount = 0;
    	
    	this.quadtree = this.initializeQuadTree();
//    	System.out.println("--------------------------------------------------------------------------------");
//    	Bound treeBound = this.quadtree.getRootNode().getBound();
//		System.out.format("tree bound : %s\n", treeBound);

    	ListIterator<Point> i1 = this.pointListIterator();
    	while(i1.hasNext()) {
    		Point p1 = i1.next();
//    		System.out.format("* p: (%5.2f,%5.2f)\n", p1.p.x, p1.p.y);
    		
    		Vector totalForceAppliedToPoint = new Vector(0,0);
    		QuadtreeIterator quadIter = new QuadtreeIterator(quadtree);
    		while(quadIter.hasNext()) {
    			Qnode qnode = quadIter.next();
                boolean hasSubnodes = qnode.hasSubnodes();
  
                // leaf node: apply force for each children nodes
                if (!hasSubnodes) {

            		Vector forceApplied = new Vector(0,0);
                	for(Positionable pos2: qnode.getChildren()) {
                		Point p2 = (Point)pos2;
                		if (p1.equals(p2)) {
                            continue;
                        }
                        Vector f = ForceDirectedLayout.applyCoulombsLawSingle(p1, p2, this.repulsion);
                        this._forceAppliedCount += 1;                        
                        forceApplied = forceApplied.add(f);
                        totalForceAppliedToPoint = totalForceAppliedToPoint.add(f); 
                    }
                	
            		if (qnode.getChildren().size() > 0) {
//                        System.out.format("#%d ", qnode.id);
//                		System.out.format("reached leaf node with %d points, ", qnode.getChildren().size());
//                		System.out.format("force %.2f %s", forceApplied.magnitude(), forceApplied.toString(2));     
//                		System.out.format("\n");

            		} else {
//                        System.out.format("#%d ", qnode.id);
//                		System.out.format("reached empty leaf. ");    
//                		System.out.format("\n");
            		}
                }
                // internal node
                else {
                	// compute rate s/d: 
                	// 	the further the distance (larger d) and the smaller the node (smaller s), the smaller the ratio
                    double s = nodeWidth(qnode);
                    double d = distanceToNodeCenter(p1, qnode);
                    double ratio = s/d;
                    
//                    System.out.format("#%d ", qnode.id);
//              		Bound b = qnode.getBound();
//					System.out.format("bound : (%5.2f,%5.2f: %.0fx%.0f) / ", b.getX(), b.getY(), b.getWidth(), b.getHeight());
//            		System.out.format("center: (%5.2f,%5.2f) ", qnode.centerPos().getX(), qnode.centerPos().getY());
//            		System.out.format("%.3f ", ratio);

                    // treat it as a single body. no need to examine further children nodes
                    if (ratio < this.THETA) {
                    	Bound centerPos = qnode.centerPos();
                        Vector centerVector = new Vector(centerPos.getX(), centerPos.getY());
                        final Double totalMass = (Double)qnode.getData();
                        Point centerPoint = new ForceDirectedLayout.Point(centerVector, totalMass);

                        Vector f = ForceDirectedLayout.computeCoulombsForce(p1, centerPoint, this.repulsion);
                        f = f.multiply(totalMass);
                        p1.applyForce(f);
                        this._forceAppliedCount += 1;
                        totalForceAppliedToPoint = totalForceAppliedToPoint.add(f); 

                        // stop iterating children nodes
                        quadIter.stopChildrenNodes();

//                        System.out.format("#%d ", qnode.id);
//                        Bound b = qnode.getBound();
//                        System.out.format("bound : (%5.2f,%5.2f: %.0fx%.0f) / ", b.getX(), b.getY(), b.getWidth(), b.getHeight());
//                        System.out.format("center: (%5.2f,%5.2f) ", qnode.centerPos().getX(), qnode.centerPos().getY());
//                        System.out.format("%.3f ", ratio);
//                        System.out.format("force %.2f %s", f.magnitude(), f.toString(2));     
//                        System.out.format(" stop. ");
//                        System.out.format("\n");

                    }
                    // continue running on next sub nodes
                    else {
                    	
                    }
                    
//            		System.out.format("\n");
                }
    		}
//    		System.out.format("total force applied to point: %.2f %s\n", totalForceAppliedToPoint.magnitude(), totalForceAppliedToPoint.toString(2));
    	}    	
    }
    
    @Override
	public String appendDescription() {
    	int N = this.graph.nodes.size();
    	int ref = N * (N-1) / 2;
		return String.format("count: %d (%.1f %% of %d)", this._forceAppliedCount, this._forceAppliedCount * 100.0 / ref, ref);
	}

    
    // compute s/d 
    //   s: width of the region represented by the internal node
    //   d: distance between the body and the nodeÕs center-of-mass
    double computeDistanceRatio(Qnode qnode, Point p) {
        double s = nodeWidth(qnode);
        double d = distanceToNodeCenter(p, qnode);
        return s/d;
    }
	double distanceToNodeCenter(Point point, Qnode qnode) {
		Bound centerPos = qnode.centerPos();
        Vector centerPosVector = new Vector(centerPos.getX(), centerPos.getY());
        Vector distance = point.p.subtract(centerPosVector);
		return distance.magnitude();
	}
	double nodeWidth(Qnode qnode) {
		return qnode.getBound().getWidth();
	}

    
    // iterator

    // spring iterator
    static class QuadtreeIterator implements Iterator<Qnode> {
		QuadTree<Point> quadtree;
		Queue<Qnode> nextQueue;
		Queue<Qnode> preQueue;

		public QuadtreeIterator(QuadTree<Point> quadtree) {
    		this.quadtree = quadtree;

    		this.nextQueue = new LinkedList<Qnode>();
    		this.preQueue = new LinkedList<Qnode>();
    		
    		this.nextQueue.add(quadtree.getRootNode());
		}

		@Override
        public boolean hasNext() {
			return this.nextQueue.size() > 0 || this.preQueue.size() > 0;
        }

    	@Override
        public Qnode next() {
    		// move qnodes from preQueue to nextQueue
    		nextQueue.addAll(preQueue);
    		preQueue.clear();
    		
    		// pop from next queue
    		Qnode qnode = nextQueue.remove();

    		// add subnodes to preQueue
            List<Qnode> subnodes = qnode.getSubnodes();
            preQueue.addAll(subnodes);
    		
			return qnode;
        }

		public boolean stopChildrenNodes() {
			int size = preQueue.size();

			// clear preQueue
			preQueue.clear();
			
			return size > 0;
		}


    	@Override
        public void remove() {
            throw new RuntimeException("remove not supported");
        }
    }

    public Iterable<Qnode> eachQnodes() {
    	final BarnesHutLayout layout = this;
        return new Iterable<Qnode>() {
    		@Override
    		public Iterator<Qnode> iterator() {
    			return new QuadtreeIterator(layout.quadtree);
    		}
        };        
    }

    //
    // run
    //
	public void tick(double unitTime) {
		this.initForce();
		this.applyCoulombsLaw();
        this.applyHookesLaw();
        this.attractToCenter();
        this.updateVelocity(unitTime);
        this.updatePosition(unitTime);		
	}
	
	public void start(Callbacks callbacks) {
        if (this._started == true) {
            return;
        }
        _started = true;
        _stop = false;
        
        //
        callbacks.onRenderStart();
        
        //
        while (!_stop) {
            // tick
            this.tick(this.unitTime);
            
            //
            callbacks.render();
            
            // check stop
            if (_stop || totalEnergy() < this.minEnergyThreshold) {
                break;
            }
        }
        _started = false;
        callbacks.onRenderStop();
    }

}

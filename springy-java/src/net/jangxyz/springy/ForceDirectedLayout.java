package net.jangxyz.springy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import mikechambers.quadtree.Qnode;


public class ForceDirectedLayout {
	public Graph graph;
	public double stiffness;
	public double repulsion;
	public double damping;
	public double minEnergyThreshold;
	public double unitTime;
	
	public Map<String,Point> nodePoints = new HashMap<String, Point>(); // keep track of points associated with nodes
	public Map<String,Spring> edgeSprings = new HashMap<String, Spring>(); // keep track of springs associated with edges

    boolean _started = false;    // prevents running again
    boolean _stop = true;        // signals stop
    
    // cache
	protected Double _totalEnergy;

    public static class Point implements Positionable {
    	public Vector p;
    	public Vector v;
    	public Vector a;
    	public double m;
        
        public Point(Vector p, double m) {
            this.p = p;
            this.m = m;
            this.v = new Vector(0, 0);
            this.a = new Vector(0, 0);
        }
        
        public boolean equals(Point p) {
        	return this.p.equals(p.p) && this.v.equals(p.v) && this.a.equals(p.a) && this.m == m; 
        }
        
        void applyForce(Vector force) {
            this.a = this.a.add(force.divide(this.m));
        }
        
        public String toString() {
	        return String.format("[(%f,%f), (%f,%f)]", p.x, p.y, v.x, v.y);
        }

		@Override
		public double getX() {
			return p.x;
		}

		@Override
		public double getY() {
			return p.y;
		}
		
		@Override
		public void onAdd(Qnode qnode) {
			Double data = (Double)qnode.getData();
			if (data == null) {
				data = new Double(0.0);
			}
			data += this.m;
			qnode.setData((Object)data);
		}
    }
    
    public static class Spring {
    	public Point point1;
    	public Point point2;
    	public double length;
    	public double k;
    	
    	public Spring(Point point1, Point point2, double length, double k) {
            this.point1 = point1;
            this.point2 = point2;
            this.length = length; // spring length at rest
            this.k = k; // spring constant (See Hooke's law) .. how stiff the spring is
        }

    }
    
    // Hooke's Law
    static void applyHookesLaw(Spring spring) {
        if (spring.point1 == spring.point2) {
            return;
        }
        
        Vector d = spring.point2.p.subtract( spring.point1.p );   // direction of spring
        Vector direction = d.normalise();
        double displacement = spring.length - d.magnitude();
        
        // apply force to each end point
        Vector force = direction.multiply(spring.k * displacement * 0.5);
        spring.point1.applyForce(force.multiply(-1));
        spring.point2.applyForce(force);
    }
    // Coulomb's Law
    static void applyCoulombsLaw1(Point point1, Point point2, double repulsion) {
        Vector d = point1.p.subtract(point2.p);
        Vector direction = d.normalise();
        double distance = d.magnitude() + 0.1; // avoid massive forces at small distances (and divide by zero)
        
        // apply force to each end point
        Vector force = (direction.multiply(repulsion)).divide(distance * distance * 0.5);
        point1.applyForce(force);
        point2.applyForce(force.multiply(-1));
    }
	protected static Vector computeCoulombsForce(Point point1, Point point2,
			double repulsion) {
		Vector d = point1.p.subtract(point2.p);
        Vector direction = d.normalise();
        double distance2 = d.magnitude2() + 0.01;
        
        Vector force = (direction.multiply(repulsion)).divide(distance2/* *  0.5*/);
		return force;
	}
    static void applyCoulombsLaw2(Point point1, Point point2, double repulsion) {
        Vector force = computeCoulombsForce(point1, point2, repulsion);
        // apply force to each end point
        point1.applyForce(force);
        point2.applyForce(force.multiply(-1));
    }
    static Vector applyCoulombsLawSingle(Point targetPoint, Point sourcePoint, double repulsion) {
        Vector force = computeCoulombsForce(targetPoint, sourcePoint, repulsion);
        targetPoint.applyForce(force);
        return force;
    }

    static void updateVelocity(Point point, double damping, double timestep) {
        point.v = (point.v.add(point.a.multiply(timestep))).multiply(damping);
    }
    static void updatePosition(Point point, double timestep) {
        // Same question as above; along with updateVelocity, is this all of
        // your integration code?
        point.p = point.p.add( (point.v.multiply(timestep)) );
    }
    static double kineticEnergy(List<Point> points) {
        double energy = 0.0;
        for (Point point: points) {
            energy += ForceDirectedLayout.kineticEnergy2(point);
        }
        return energy/2;
    }
    static double kineticEnergy2(Point point) {
        return point.m * point.v.magnitude2();
    }


    //
    //
    //
    
	public ForceDirectedLayout(Graph graph, double stiffness, double repulsion, double damping, double energyThreshold, double unitTime) {
		this.graph              = graph;
		this.stiffness          = stiffness;
		this.repulsion          = repulsion;
		this.damping            = damping;
		this.minEnergyThreshold = energyThreshold;
		this.unitTime           = unitTime;		
	}
	public ForceDirectedLayout(Graph graph, float stiffness, float repulsion, float damping, float energyThreshold, float unitTime) {
		this(graph, (double)stiffness, (double)repulsion, (double)damping, (double)energyThreshold, (double)unitTime);
	}
	
	public String toString() {
		return String.format("<ForceDirectedLayout %d %d %f %f %.1f %.2f>", nodePoints.size(), edgeSprings.size(), stiffness, repulsion, damping, unitTime);
	}
	
	//
	// iterators
	//
	public Point point(Graph.Node node) {
    	Point point = nodePoints.get(node.id);
    	if (point != null) {
    		return point;
    	}

    	// create random point
        double mass = node.data.containsKey("mass") ? Double.parseDouble(node.data.get("mass")) : 1.0; 
        point = new Point(Vector.random(), mass);
        nodePoints.put(node.id, point);
        return point;
    }
    public Spring spring(Graph.Edge edge) {
    	Spring spring = this.edgeSprings.get(edge.id);
        if (spring != null) {
            return spring;
        }

        // find spring for any other sibling edges
        Spring existingSpring = null;
        List<Graph.Edge> fromEdges = graph.getEdges(edge.source, edge.target);
        for (Graph.Edge e: fromEdges) {
            existingSpring = this.edgeSprings.get(e.id);
            if (existingSpring != null) {
                break;
            }
        }
        // try in reverse direction
        if (existingSpring == null) {
        	List<Graph.Edge>  toEdges = graph.getEdges(edge.target, edge.source);
            for (Graph.Edge e: toEdges) {
                existingSpring = this.edgeSprings.get(e.id);
                if (existingSpring != null) {
                    break;
                }
            }
        }
        // use the sibling edge to create new spring
        if (existingSpring != null) {
            return new Spring(existingSpring.point1, existingSpring.point2, 0.0, 0.0);
        }

        // create new spring
        String lengthStr = edge.data.get("length");
        double length = lengthStr != null ? Double.parseDouble(lengthStr) : 1.0;
        Spring newSpring = new Spring(this.point(edge.source), this.point(edge.target),
            length, stiffness);
        edgeSprings.put(edge.id, newSpring);
        return newSpring;
    }

    // point iterator
    static class PointListIterator implements ListIterator<Point> {
    	ListIterator<Graph.Node> it;
		ForceDirectedLayout layout;

		public PointListIterator(LinkedList<Graph.Node> nodes, ForceDirectedLayout layout) {
    		this.layout = layout;
    		this.it = nodes.listIterator();
		}

		public PointListIterator(LinkedList<Graph.Node> nodes,
				ForceDirectedLayout layout, int nextIndex) {
    		this.layout = layout;
    		this.it = nodes.listIterator(nextIndex);
		}

		@Override
        public boolean hasNext() {
			return this.it.hasNext();
        }

		@Override
		public boolean hasPrevious() {
			return this.it.hasPrevious();
		}

		@Override
		public int nextIndex() {
			return this.it.nextIndex();
		}

		@Override
		public int previousIndex() {
			return this.it.previousIndex();
		}

    	@Override
        public Point next() {
    		Graph.Node node = this.it.next();
			return this.layout.point(node);
        }

		@Override
		public Point previous() {
    		Graph.Node node = this.it.previous();
			return this.layout.point(node);
		}

		@Override
		public void set(Point arg0) {
            throw new RuntimeException("set not supported");
		}

    	@Override
        public void remove() {
            throw new RuntimeException("remove not supported");
        }

		@Override
		public void add(Point arg0) {
            throw new RuntimeException("add not supported");
			
		}
    }

    ListIterator<Point> pointListIterator() {
    	return new PointListIterator(this.graph.nodes, this);
    }    
    ListIterator<Point> pointListIterator(int nextIndex) {
    	return new PointListIterator(this.graph.nodes, this, nextIndex);
	}
    
    public Iterable<Point> eachPoints() {
    	final ForceDirectedLayout layout = this;
        return new Iterable<Point>() {
    		@Override
    		public Iterator<Point> iterator() {
    			return new PointListIterator(layout.graph.nodes, layout);
    		}
        };        
    }

    public Iterable<Graph.Node> eachNodes() {
    	final ForceDirectedLayout layout = this;
        return new Iterable<Graph.Node>() {
    		@Override
    		public Iterator<Graph.Node> iterator() {
    			return layout.graph.nodes.listIterator();
    		}
        };        
    }

    
    // spring iterator
    static class SpringIterator implements Iterator<Spring> {
    	Iterator<Graph.Edge> it;
		ForceDirectedLayout layout;

		public SpringIterator(List<Graph.Edge> edges, ForceDirectedLayout layout) {
    		this.layout = layout;
    		this.it = edges.iterator();
		}

		@Override
        public boolean hasNext() {
			return this.it.hasNext();
        }

    	@Override
        public Spring next() {
    		Graph.Edge edge = this.it.next();
			return this.layout.spring(edge);
        }

    	@Override
        public void remove() {
            throw new RuntimeException("remove not supported");
        }
    }


    
    public Iterable<Spring> eachSprings() {
		final ForceDirectedLayout layout = this;
		final Iterator<Graph.Edge> it = this.graph.edges.iterator();
    	return new Iterable<Spring>() {
			@Override
			public Iterator<Spring> iterator() {
				return new Iterator<Spring>() {
					@Override
		            public boolean hasNext() {
		    			return it.hasNext();
		            }

					@Override
		            public Spring next() {
		    			return layout.spring(it.next());
		            }

					@Override
		            public void remove() {
		                throw new RuntimeException("remove not supported");
		            }
				};
			}
    	};
    }
    

    public class BoundingBox {
    	Vector bottomleft;
    	Vector topright;
    	BoundingBox(Vector bottomleft, Vector topright) {
    		this.bottomleft = bottomleft;
    		this.topright = topright;
    	}
    	
    	public double getWidth() {
    		return this.topright.x - this.bottomleft.x;
    	}
    	public double getHeight() {
    		return this.topright.y - this.bottomleft.y;
    	}
    	
    	@Override
    	public String toString() {
    		return String.format("(%.2f,%.2f: %.2fx%.2f)", this.bottomleft.x,this.bottomleft.y,getWidth(),getHeight());
    	}
    }
    
    public BoundingBox getBoundingBox() {
        Vector bottomleft = new Vector(-2,-2);
        Vector topright = new Vector(2,2);

		for(Point point: this.eachPoints()) {
            if (point.p.x < bottomleft.x) {
                bottomleft.x = point.p.x;
            }
            if (point.p.y < bottomleft.y) {
                bottomleft.y = point.p.y;
            }
            if (point.p.x > topright.x) {
                topright.x = point.p.x;
            }
            if (point.p.y > topright.y) {
                topright.y = point.p.y;
            }
        }

		// apply padding
        Vector padding = topright.subtract(bottomleft).multiply(0.07); // ~5% padding
        bottomleft = bottomleft.subtract(padding);
        topright = topright.add(padding);

        return new BoundingBox(bottomleft, topright);
    }


	
	//
    void initForce() {
    	for(Point point: this.eachPoints()) {
            point.a = Vector.ZERO;
    	}    	
    }
    void applyHookesLaw() {
    	for(Spring spring: this.eachSprings()) {
            ForceDirectedLayout.applyHookesLaw(spring);    		
    	}
    }
    void applyCoulombsLaw() {
    	ListIterator<Point> i1 = this.pointListIterator();
    	while(i1.hasNext()) {
    		Point p1 = i1.next();
        	ListIterator<Point> i2 = this.pointListIterator(i1.nextIndex());
//    		ListIterator<Point> i2 = this.pointListIterator();
        	
//    		System.out.format("* p: (%5.2f,%5.2f)\n", p1.p.x, p1.p.y);
    		Vector totalForceAppliedToPoint = new Vector(0,0);

        	while (i2.hasNext()) {
        		Point p2 = i2.next();
        		if (p1.equals(p2)) {
                    continue;
                }
        		
            	Vector forceApplied = new Vector(0,0);

        		//
                //ForceDirectedLayout.applyCoulombsLaw2(p1, p2, this.repulsion);
                Vector force = ForceDirectedLayout.computeCoulombsForce(p1, p2, repulsion);
                p1.applyForce(force);
                p2.applyForce(force.multiply(-1));
                //
                forceApplied = forceApplied.add(force);
                totalForceAppliedToPoint = totalForceAppliedToPoint.add(force);

//        		System.out.format("  force %.2f %s\n", forceApplied.magnitude(), forceApplied.toString(2));
        	}
//    		System.out.format("total force applied to point: %.2f %s\n", totalForceAppliedToPoint.magnitude(), totalForceAppliedToPoint.toString(2));
//    		System.out.format("\n");
    	}
    }
	void attractToCenter() {
    	for(Point point: this.eachPoints()) {
            Vector reverseDir = point.p.multiply(-1.0);
            point.applyForce(reverseDir.multiply(repulsion / 50.0));
        }
    }
    
    void updateVelocity(double timestep) {
    	for(Point point: this.eachPoints()) {
            ForceDirectedLayout.updateVelocity(point, damping, timestep);
    	}
        this._totalEnergy = null;
    }

    void updatePosition(double timestep) {
    	for(Point point: this.eachPoints()) {
    		ForceDirectedLayout.updatePosition(point, timestep);
    	}
    }

    public double totalEnergy() {
    	return this.totalEnergy(false);
    }
    public double totalEnergy(boolean useCache) {
    	if (useCache && this._totalEnergy != null) {
        	return this._totalEnergy;    	
    	}
    	
    	// compute total energy
        double energy = 0.0;
        for (Point point: this.eachPoints()) {
            energy += ForceDirectedLayout.kineticEnergy2(point);
        }
        this._totalEnergy = energy/2;
    	
    	return this._totalEnergy;    	
    }

    
    //
    // run
    //
	public interface Callbacks {
		void render();		
		void onRenderStart();
		void onRenderStop();
	}
	
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
	public void stop() {
		this._stop = true;
	}
	public String appendDescription() {
		return "";
	}
    
}

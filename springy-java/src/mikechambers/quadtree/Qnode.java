package mikechambers.quadtree;

import java.util.ArrayList;
import java.util.List;

import net.jangxyz.springy.Positionable;


public class Qnode {
    final static int TOP_LEFT = 0;
    final static int TOP_RIGHT = 1;
    final static int BOTTOM_LEFT = 2;
    final static int BOTTOM_RIGHT = 3;

    // subnodes
    ArrayList<Qnode> nodes = new ArrayList<Qnode>(4);
//    ArrayList<Qnode> nodes;

    // children contained directly in the node
    ArrayList<Positionable> children;
    Bound _bounds;

    // read only
    int _depth = 0;

    int _maxChildren = 4;
    Integer _maxDepth = 4;

    Object data = null;
    
    public int id;
    static int QNODE_NEXT_ID = 0;
    
    Qnode(Bound bounds, int depth, Integer maxDepth, int maxChildren) {
    	id = QNODE_NEXT_ID++;
    	
        this._bounds = bounds;
        this.children = new ArrayList<Positionable>(maxChildren);

        this._maxChildren = maxChildren;
        this._maxDepth = maxDepth;
        this._depth = depth;
    }


    // insert item to node
    void insert(Positionable item) {
        if (!this.nodes.isEmpty()) {
            int index = this._findIndex(item);
            this.nodes.get(index).insert(item);
            return;
        }

        // save item
//    	System.out.format("#%d insert: (%f, %f)\n", id, item.getX(), item.getY());
        this.children.add(item);
        item.onAdd(this);

        // maximum depth not reached, but maximum children items for node reached
        if ((this._maxDepth != null && this._depth < this._maxDepth) && this.children.size() > this._maxChildren) {
            this.subdivide();
//        	System.out.format("#%d	subdivide %s: [%s, %s, %s, %s]\n", id, this._bounds, this.nodes.get(0).getBound(), this.nodes.get(1).getBound(), this.nodes.get(2).getBound(), this.nodes.get(3).getBound());

            // move children items to subnodes
            for(Positionable childItem: this.children){
            	this.insert(childItem);
            }
            this.children.clear();
        }
    }
    
    public Object getData() {
    	return data;
    }
    public void setData(Object newData) {
    	this.data = newData;
    }

    public boolean hasChildren() {
        return this.nodes.size() > 0;
    }
    
    //
    @SuppressWarnings("unchecked")
	public List<Positionable> getChildren() {
        return (List<Positionable>) this.children.clone();
    }

	@SuppressWarnings("unchecked")
	public List<Qnode> getSubnodes() {
        return (List<Qnode>) this.nodes.clone();
    }

	public boolean hasSubnodes() {
		return this.nodes.size() > 0;
	};

	
	public Bound getBound() {
		return this._bounds;
	}


    public Bound centerPos() {
        return new Bound(this._bounds.x + this._bounds.width / 2,
        					this._bounds.y + this._bounds.height / 2, 0,0);
    }

    // retrieve items that is in same node
    ArrayList<Positionable> retrieve (Positionable item) {
        if (!this.nodes.isEmpty()) {
            int index = this._findIndex(item);
            return this.nodes.get(index).retrieve(item);
        }

        return this.children;
    }

    int _findIndex(Positionable item) {
        Bound b = this._bounds;
        boolean left = (item.getX() > b.x + b.width / 2) ? false : true;
        boolean top = (item.getY() > b.y + b.height / 2) ? false : true;

        // top-left
        int index = Qnode.TOP_LEFT;
        if (left) { // left side
            // bottom-left
            if (!top) {
                index = Qnode.BOTTOM_LEFT;
            }
        } 
        else { // right side
            // top right
            if (top) {
                index = Qnode.TOP_RIGHT;
            } 
            // bottom right
            else {
                index = Qnode.BOTTOM_RIGHT;
            }
        }

        return index;
    }


    void subdivide() {
        int depth = this._depth + 1;

        double bx = this._bounds.x;
        double by = this._bounds.y;

        // floor the values
        double b_w_h = this._bounds.width / 2;
        double b_h_h = this._bounds.height / 2;
        double bx_b_w_h = bx + b_w_h;
        double by_b_h_h = by + b_h_h;
                
//        this.nodes.set(Qnode.TOP_LEFT,     new Qnode(new Bound(bx, by, b_w_h, b_h_h),             depth, this._maxDepth, this._maxChildren));
//        this.nodes.set(Qnode.TOP_RIGHT,    new Qnode(new Bound(bx_b_w_h, by, b_w_h, b_h_h),       depth, this._maxDepth, this._maxChildren));
//        this.nodes.set(Qnode.BOTTOM_LEFT,  new Qnode(new Bound(bx, by_b_h_h, b_w_h, b_h_h),       depth, this._maxDepth, this._maxChildren));
//        this.nodes.set(Qnode.BOTTOM_RIGHT, new Qnode(new Bound(bx_b_w_h, by_b_h_h, b_w_h, b_h_h), depth, this._maxDepth, this._maxChildren));
        
        // order
        this.nodes.add(new Qnode(new Bound(bx, by, b_w_h, b_h_h),             depth, this._maxDepth, this._maxChildren));
        this.nodes.add(new Qnode(new Bound(bx_b_w_h, by, b_w_h, b_h_h),       depth, this._maxDepth, this._maxChildren));
        this.nodes.add(new Qnode(new Bound(bx, by_b_h_h, b_w_h, b_h_h),       depth, this._maxDepth, this._maxChildren));
        this.nodes.add(new Qnode(new Bound(bx_b_w_h, by_b_h_h, b_w_h, b_h_h), depth, this._maxDepth, this._maxChildren));
    };

    void clear() {
    	this.children.clear();

        for(Qnode node: this.nodes) {
        	node.clear();
        }
        this.nodes.clear();
    }

}

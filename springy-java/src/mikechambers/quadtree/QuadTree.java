package mikechambers.quadtree;

import java.util.ArrayList;

import net.jangxyz.springy.Positionable;

public class QuadTree<T> {

    /**
    * The root node of the QuadTree which covers the entire area being segmented.
    * @property root
    * @type Qnode
    **/
    Qnode root = null;

    /**
    * QuadTree data structure.
    * @class QuadTree
    * @constructor
    * @param {Object} An object representing the bounds of the top level of the QuadTree. The object 
    * should contain the following properties : x, y, width, height
    * @param {Boolean} pointQuad Whether the QuadTree will contain points (true), or items with bounds 
    * (width / height)(false). Default value is false.
    * @param {Number} maxDepth The maximum number of levels that the quadtree will create. Default is 4.
    * @param {Number} maxChildren The maximum number of children that a node can contain before it is split into sub-nodes.
    **/
    public QuadTree(Bound bounds, Integer maxDepth, int maxChildren) {
    	Qnode.QNODE_NEXT_ID = 0;
        Qnode node = new Qnode(bounds, 0, maxDepth, maxChildren);
        this.root = node;
    }

    public Qnode getRootNode() {
    	return this.root;
    }

    /**
    * Inserts an item into the QuadTree.
    * @method insert
    * @param {Object|Array} item The item or Array of items to be inserted into the QuadTree. The item should expose x, y 
    * properties that represents its position in 2D space.
    **/
    public void insert(Positionable item) {
    	this.root.insert(item);
    }

    /**
    * Clears all nodes and children from the QuadTree
    * @method clear
    **/
    void clear() {
        this.root.clear();
    }

    /**
    * Retrieves all items / points in the same node as the specified item / point. If the specified item
    * overlaps the bounds of a node, then all children in both nodes will be returned.
    * @method retrieve
    * @param {Object} item An object representing a 2D coordinate point (with x, y properties), or a shape
    * with dimensions (x, y, width, height) properties.
    **/
    @SuppressWarnings("unchecked")
	ArrayList<Positionable> retrieve (Positionable item) {
        return (ArrayList<Positionable>) this.root.retrieve(item).clone();
    }

}

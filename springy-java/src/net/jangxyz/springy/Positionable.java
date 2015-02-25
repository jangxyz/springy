package net.jangxyz.springy;

import mikechambers.quadtree.Qnode;

public interface Positionable {
	double getX();
	double getY();
	
	void onAdd(Qnode qnode);
}

package net.jangxyz.springy.runner;

import net.jangxyz.springy.ForceDirectedLayout;
import net.jangxyz.springy.Vector;

public interface PointValueGetter {
	Vector get(ForceDirectedLayout.Point point);
}

package net.jangxyz.springy.runner;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.jangxyz.springy.ForceDirectedLayout;
import net.jangxyz.springy.ForceDirectedLayout.Point;
import net.jangxyz.springy.Vector;

public class VectorAnalysis {
	double[] distance2;
	//
	double minValue;
	double maxValue;
	double medianValue;
	double q1Value;
	double q3Value;
	double sumValue;
	double avgValue;
	ForceDirectedLayout layout;
	PointValueGetter getter;

	VectorAnalysis(ForceDirectedLayout layout, PointValueGetter getter, double[] distance2) {
		this.layout = layout;
		this.getter = getter;
		//
		setDistance2(distance2);
	}

	VectorAnalysis(ForceDirectedLayout layout, PointValueGetter getter) {
		this.layout = layout;
		this.getter = getter;

		// get distance2
		List<Vector> vectors = collectVector(layout, getter);
		double[] distance2Diff = computeMagnitude2(vectors);
		Arrays.sort(distance2Diff);

		//
		setDistance2(distance2Diff);
	}

	void setDistance2(double[] distance2) {
		this.distance2 = distance2;

		// diff2
		double diff2Min = distance2[0];
		double diff2Max = distance2[distance2.length - 1];
		double diff2Med = distance2[distance2.length/2];
		double diff2Q1 = distance2[distance2.length/4*1];
		double diff2Q3 = distance2[distance2.length/4*3];		

		// sum
		double diffSum = 0.0;
		for (double diff2: distance2) {
			diffSum += Math.sqrt(diff2);
		}

		// diff1
		this.minValue = Math.sqrt(diff2Min);
		this.maxValue = Math.sqrt(diff2Max);
		this.medianValue = Math.sqrt(diff2Med);
		this.q1Value  = Math.sqrt(diff2Q1);
		this.q3Value  = Math.sqrt(diff2Q3);
		this.sumValue = diffSum;
		this.avgValue = diffSum/distance2.length;
	}

	VectorAnalysis compareTo(ForceDirectedLayout reference) {
		// get diff distance2
		List<Vector> vectors = collectDiffVector(reference, this.layout, this.getter);
		double[] distance2Diff = computeMagnitude2(vectors);
		Arrays.sort(distance2Diff);

		return new VectorAnalysis(reference, this.getter, distance2Diff);
	}


	static List<Vector> collectDiffVector(ForceDirectedLayout layout1, ForceDirectedLayout layout2, PointValueGetter getter) {
		List<Vector> vectors = new LinkedList<Vector>();
		Iterator<Point> points1 = layout1.eachPoints().iterator();
		Iterator<Point> points2 = layout2.eachPoints().iterator();
		while (points1.hasNext()) {
			Point point1 = points1.next();
			Point point2 = points2.next();

			Vector v1 = getter.get(point1);
			Vector v2 = getter.get(point2);
			Vector diff = v1.subtract(v2);
			vectors.add(diff);
		}
		return vectors;
	}
	

	static double[] computeMagnitude2(List<Vector> vectors) {
		double[] distance2Diff = new double[vectors.size()];
		int i = 0;
		for(Vector v1: vectors) {
			distance2Diff[i] = v1.magnitude2();
			i += 1;
		}
		return distance2Diff;
	}
	

	static List<Vector> collectVector(ForceDirectedLayout layout1, PointValueGetter getter) {
		List<Vector> vectors = new LinkedList<Vector>();
		Iterator<Point> points1 = layout1.eachPoints().iterator();
		while (points1.hasNext()) {
			Point point1 = points1.next();
			Vector v1 = getter.get(point1);
			vectors.add(v1);
		}
		return vectors;
	}
}
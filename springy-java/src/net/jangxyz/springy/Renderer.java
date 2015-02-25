package net.jangxyz.springy;


public class Renderer {
	public interface Callback {
		void clear();		
		void drawEdge(Graph.Edge edge, Vector point1, Vector point2);
		void drawNode(Graph.Node node, Vector point);
		void onRenderStart();
		void onRenderStop();
	}

	ForceDirectedLayout layout;
	Renderer.Callback callback;
	
	Renderer(ForceDirectedLayout layout, Renderer.Callback callback) {
		this.layout = layout;
		this.callback = callback;
	}
}

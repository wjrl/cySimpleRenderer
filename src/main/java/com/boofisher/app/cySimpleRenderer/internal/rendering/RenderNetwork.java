package com.boofisher.app.cySimpleRenderer.internal.rendering;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

import org.apache.log4j.Logger;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;

import com.boofisher.app.cySimpleRenderer.internal.data.GraphicsData;
import com.boofisher.app.cySimpleRenderer.internal.tools.NetworkToolkit;
import com.boofisher.app.cySimpleRenderer.internal.tools.PairIdentifier;

/********************************************************************************
 * Class will be responsible for rendering the network's nodes and edges
 * */

public class RenderNetwork implements GraphicsProcedure {
	final Logger logger = Logger.getLogger(CyUserLog.NAME);
	GraphicsData graphicsData;
	boolean isMain;
	
	
	@Override
	public void initialize(GraphicsData graphicsData) {
		this.graphicsData = graphicsData;		
	}
	
	
	
	@Override
	public void execute(GraphicsData graphicsData) {
			
		int width = graphicsData.getContainer().getWidth();
		int height = graphicsData.getContainer().getHeight();

		int midWidth = width/2;
		int midHeight = height/2;				
		
		CyNetworkView networkView = graphicsData.getNetworkView();
		JComponent component = graphicsData.getContainer();
		Graphics2D imageGraphics = (Graphics2D)graphicsData.getMyGraphics();
		
		//TODO make rendering more efficient
		if( component == null || component.getWidth() != width || height != component.getHeight() || true){
			//logger.warn("Rendering main ? " + graphicsData.isMain());
			//logger.warn("width = " + width + " height = " + height + " zoomFact = " + graphicsData.getZoomFactor());
			int zoomFactor = graphicsData.getZoomFactor();
															
			//paint background
			imageGraphics.setColor((Color) networkView.getVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT));
			imageGraphics.fillRect(0,0, width, height);	
						
			drawEdges(imageGraphics, networkView, midWidth, midHeight, zoomFactor);
			drawNodes(imageGraphics, networkView, midWidth, midHeight, zoomFactor);
			BufferedImage bImage = component.getGraphicsConfiguration().createCompatibleImage(width, height);
			graphicsData.setBufferedImage(bImage);			
		} else if(graphicsData.isMain()){
			//do nothing network has already been drawn so clear rendered image
			//paint background
			Graphics2D g2d = graphicsData.getBufferedImage().createGraphics();
			g2d.setColor((Color) networkView.getVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT));
			g2d.fillRect(0,0, width, height);	
		}			
			
	}
	
	public void drawEdges(Graphics2D g2, CyNetworkView networkView, int midWidth, int midHeight, int zoomFactor){
		// A set containing all pairs of nodes that have had an edge drawn between them
		Set<PairIdentifier> drawnPairs = new HashSet<PairIdentifier>();
		CyNode source, target;		
		
		Shape shape = null;
		boolean rectMade = false;
		
		double x1=0, x2=0, y1=0, y2=0;
		for (View<CyEdge> edgeView : networkView.getEdgeViews()) {
			source = edgeView.getModel().getSource();
			target = edgeView.getModel().getTarget();
			
			//set color and stroke
			float edgeWidth = edgeView.getVisualProperty(BasicVisualLexicon.EDGE_WIDTH).intValue();
			edgeWidth = (zoomFactor > 0) ? (edgeWidth /(int)(zoomFactor)) : edgeWidth;
			g2.setColor(chooseColor(null, edgeView, graphicsData));
			g2.setStroke( new BasicStroke( edgeWidth ));
			
			if(!rectMade){
				shape = getShape(networkView, edgeView.getModel().getTarget(), midWidth, midHeight, zoomFactor);
				rectMade = true;
				
				//set stroke and color													
				graphicsData.setMyShape(shape);
			}					
			
			PairIdentifier pairIdentifier = NetworkToolkit.obtainPairIdentifier(source, target, networkView.getModel().getNodeList().size());
			
			// Only draw an edge between this source-target pair if one has not been drawn already
			//TODO mulit edges 
			if (!drawnPairs.contains(pairIdentifier)) {
			
				//TODO fix this
				if(edgeView.getVisualProperty(BasicVisualLexicon.EDGE_LINE_TYPE).toString().equals("SOLID") || true){
					
					View<CyNode> sourceView = networkView.getNodeView(source);
					View<CyNode> targetView = networkView.getNodeView(target);
					
					if (sourceView != null && targetView != null) {
						x1 = sourceView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION)/zoomFactor + shape.getBounds2D().getWidth()/2;
						y1 = sourceView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)/zoomFactor + shape.getBounds2D().getHeight()/2;
						
						x2 = targetView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION)/zoomFactor + shape.getBounds2D().getWidth()/2;
						y2 = targetView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)/zoomFactor + shape.getBounds2D().getHeight()/2;
					}else{
						continue;
					}
					
					//draw edge between nodes
					// draw Rectangle2D.Double
					g2.draw(new Line2D.Double((x1+midWidth), (y1+midHeight), (x2+midWidth), (y2+midHeight)));
				}
				
				drawnPairs.add(pairIdentifier);
			}
		}	
	}
	
	public void drawNodes(Graphics2D g2, CyNetworkView networkView, int midWidth, int midHeight, int zoomFactor){
		Shape shape = null;
		
		// networkView.updateView();
		for (View<CyNode> nodeView : networkView.getNodeViews()) {
			
			if(nodeView == null) {
				// MKTODO why does this happen?
				System.err.println("nodeView is null: networkView.getNodeViews() returns: " + networkView.getNodeViews());
				continue;
			}					
			
			//set color						
			g2.setColor(chooseColor(nodeView, null, graphicsData));
										
			// Draw it only if the visual property says it is visible
			if (nodeView.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE)) {
				shape = getShape(networkView, nodeView.getModel(), midWidth, midHeight, zoomFactor);						
				// draw Rectangle2D.Double
				g2.fill(shape);																
			}
		}
	}
	private Color chooseColor(View<CyNode> nodeView, View<CyEdge> edgeView, GraphicsData graphicsData) {
		
		Color visualPropertyColor = null;
		
		Color color = null;
		
		if(nodeView != null){
			
			visualPropertyColor = (Color) nodeView.getVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR);		
	
			
			Long suid = nodeView.getModel().getSUID();
			
			if (nodeView.getVisualProperty(BasicVisualLexicon.NODE_SELECTED)) {
				color = Color.BLUE;
			} 
			else if (suid.equals(graphicsData.getSelectionData().getHoverNodeIndex()) || graphicsData.getPickingData().getPickedNodeIndices().contains(suid)) {
				color = Color.GREEN;
			}								
		}else if(edgeView != null){
			
			visualPropertyColor = (Color) edgeView.getVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR);								
			
			Long suid = edgeView.getModel().getSUID();
			
			if (edgeView.getVisualProperty(BasicVisualLexicon.NODE_SELECTED)) {
				color = Color.BLUE;
			} 
			else if (suid.equals(graphicsData.getSelectionData().getHoverEdgeIndex()) || graphicsData.getPickingData().getPickedEdgeIndices().contains(suid)) {
				color = Color.GREEN;
			}						
		}
		return (color == null) ? visualPropertyColor : color;
	}
	
	/*
	 * Utility method to create a shape for the network (ellipse, triangle, or rectangle)*/
	public static Shape getShape(CyNetworkView networkView, CyNode node, int midWidth, int midHeight, int zoomFactor){
		Shape shape = null;
		
		
		View<CyNode> nodeView = networkView.getNodeView(node);
		
		//cast floats and doubles to int
		float x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION).floatValue()/zoomFactor;// / distanceScale;
		float y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION).floatValue()/zoomFactor;// / distanceScale;			
		
		double width  = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH)/zoomFactor;
		double height = nodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT)/zoomFactor;
		shape = new Rectangle2D.Double((x+midWidth), (y+midHeight), width, height);
		
		
		if(NodeShapeVisualProperty.TRIANGLE.equals(nodeView.getVisualProperty(BasicVisualLexicon.NODE_SHAPE))){
			double yPosition = y+midHeight+height;
			double xPosition = x+midWidth;
			Path2D path = new Path2D.Double();
			path.moveTo(xPosition, yPosition);
			double xShifted  = xPosition + width;			
			path.lineTo(xShifted, yPosition);			
			xPosition = (xPosition + xShifted) / 2;//find x-coord for half base
			double top = yPosition - height;
			path.lineTo(xPosition, top);
			path.closePath();
			shape = path;
		}else if(NodeShapeVisualProperty.ELLIPSE.equals(nodeView.getVisualProperty(BasicVisualLexicon.NODE_SHAPE))){
			shape = new Ellipse2D.Double((x+midWidth), (y+midHeight), width, height);
		}else{
			shape = new Rectangle2D.Double((x+midWidth), (y+midHeight), width, height);
		}
				
				
		return shape;
	}
}

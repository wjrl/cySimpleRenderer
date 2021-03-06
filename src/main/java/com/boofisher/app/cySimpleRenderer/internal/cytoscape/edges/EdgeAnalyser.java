package com.boofisher.app.cySimpleRenderer.internal.cytoscape.edges;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.boofisher.app.cySimpleRenderer.internal.cytoscape.edges.AugmentedEdgeContainer;
import com.boofisher.app.cySimpleRenderer.internal.geometric.Vector3;
import com.boofisher.app.cySimpleRenderer.internal.tools.EdgeCoordinateCalculator;
import com.boofisher.app.cySimpleRenderer.internal.tools.GeometryToolkit;
import com.boofisher.app.cySimpleRenderer.internal.tools.NetworkToolkit;
import com.boofisher.app.cySimpleRenderer.internal.tools.PairIdentifier;
import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;

/**
 * This class is responsible for analyzing the current set of edges in the network and
 * generate edge coordinate data for use with rendering the edges.
 */
public class EdgeAnalyser {
	
	private static final double MIN_LENGTH = Double.MIN_NORMAL;
	
	private static final float DASHED_EDGE_SPACING = 0.07f;
	
	private static final float DOTTED_EDGE_SPACING = 0.057f;
	
	private static final double ARC_SELF_EDGE_MINIMUM_RADIUS = 0.055; // 0.045 default feb 7, 2012
	private static final double ARC_SELF_EDGE_RADIUS_FACTOR = 0.008; // 0.007 default feb 7, 2012
	private static final double ARC_SELF_EDGE_EXPONENTIAL_BASE = 1.25;
	
	/**
	 * The number of straight segments used to approximate a curved edge
	 */
	private static final int NUM_SEGMENTS = 8;
	
	/** 
	 * A set of {@link AugmentedEdgeContainer} objects containing extra generated data relating to each
	 * edge as well as a reference to the edge it contains
	 */
	private Map<View<CyEdge>, AugmentedEdgeContainer> edgeContainers;
	
	/** The frame number that the generated edge data is current for */
//	private long currentFrame;
	
	public EdgeAnalyser() {
		edgeContainers = new HashMap<View<CyEdge>, AugmentedEdgeContainer>();
//		currentFrame = 0L;
	}

	/**
	 * Return a set of analyzed edges containing edge coordinates to be used for rendering. If an up-to-date
	 * data set is available, the set is returned. Otherwise, calculations will be done to re-analyze the current edges.
	 * 
	 * @param networkView The {@link CyNetworkView} containing the edges to be analyzed
	 * @param distanceScale The amount of scaling when converting between Cytoscape coordinates and OpenGL coordinates
	 * @param currentFrame The current frame of rendering.
	 * @return An up-to-date set of analyzed edge data to be used for rendering.
	 */
	public Collection<AugmentedEdgeContainer> getAnalyzedEdges(CyNetworkView networkView, double distanceScale/*, long currentFrame*/) {

//		if (currentFrame - this.currentFrame > 0) {
			calculateEdgeProperties(networkView, distanceScale);			
//			this.currentFrame = currentFrame;
//		}
		
		// Prepare to remove edgeContainers for edges that are no longer in the network
		Set<AugmentedEdgeContainer> edgeContainersToBeRemoved = new HashSet<AugmentedEdgeContainer>();
		
		for (AugmentedEdgeContainer edgeContainer : edgeContainers.values()) {
			// Check if this edge container holds an edge that is no longer in the network
			if (networkView.getModel().getEdge(edgeContainer.getEdgeView().getModel().getSUID()) == null) {
				edgeContainersToBeRemoved.add(edgeContainer);
			}
		}
		
		for (AugmentedEdgeContainer edgeContainer : edgeContainersToBeRemoved) {
			edgeContainers.remove(edgeContainer.getEdgeView());
		}
		
		return edgeContainers.values();		
	}	
	
	private void calculateEdgeProperties(CyNetworkView networkView, double distanceScale) {
		
		// This map maps each node-pair identifier to the number of edges between that pair of nodes
		// The identifier is: max(sourceIndex, targetIndex) * nodeCount + min(sourceIndex, targetIndex)
		Map<PairIdentifier, Integer> pairCoincidenceCount = new HashMap<>();
		
		PairIdentifier identifier;
		long sourceIndex, targetIndex;
		int edgeNumber;
		int nodeCount = networkView.getModel().getNodeCount();
		CyEdge edge;
		
		for (View<CyEdge> edgeView : networkView.getEdgeViews()) {
			
			AugmentedEdgeContainer edgeContainer = edgeContainers.get(edgeView);
			
			if (edgeContainer == null) {
				edgeContainer = new AugmentedEdgeContainer(edgeView);
				edgeContainers.put(edgeView, edgeContainer);
			}
			
			edge = edgeView.getModel();
			
			sourceIndex = edge.getSource().getSUID();
			targetIndex = edge.getTarget().getSUID();
			
			// Assign an identifier to each pair of nodes
			identifier = NetworkToolkit.obtainPairIdentifier(edge.getSource(), edge.getTarget(), networkView.getNodeViews().size());
			
			// Assign a value that represents how many edges have been found between this pair
			if (!pairCoincidenceCount.containsKey(identifier)) {
				edgeNumber = 1;
			} else {
				edgeNumber = pairCoincidenceCount.get(identifier) + 1;
			}
			
			edgeContainer.setPairIdentifier(identifier);
			edgeContainer.setEdgeNumber(edgeNumber);
			pairCoincidenceCount.put(identifier, edgeNumber);
			
			// Check if the edge leads from a node to itself
			if (sourceIndex == targetIndex) {
				edgeContainer.setSelfEdge(true);
			} else {
				edgeContainer.setSelfEdge(false);
			}
			
			// Find edge start and end points
			edgeContainer.setStart(NetworkToolkit.obtainNodeCoordinates(edge.getSource(), networkView, distanceScale));
			edgeContainer.setEnd(NetworkToolkit.obtainNodeCoordinates(edge.getTarget(), networkView, distanceScale));
			
			// Determine if edge has sufficient length to be drawn
			if (edgeContainer.getStart() != null && edgeContainer.getEnd() != null && 
					(edgeContainer.getEnd().distance(edgeContainer.getStart()) >= MIN_LENGTH || edgeContainer.isSelfEdge())) {
				edgeContainer.setSufficientLength(true);
			} else {
				edgeContainer.setSufficientLength(false);
			}
		}

		
		
		// Update the value for the total number of edges between this pair of nodes
		for (AugmentedEdgeContainer edgeContainer : edgeContainers.values()) {
			
			PairIdentifier pairIdentifier = edgeContainer.getPairIdentifier();
			Integer totalCoincidentEdgesCount = pairCoincidenceCount.get(pairIdentifier);
		
			if (totalCoincidentEdgesCount != null) {
				
				edgeContainer.setTotalCoincidentEdges(totalCoincidentEdgesCount);
			
				// If there was only 1 edge for that pair of nodes, make it a straight edge
				if (edgeContainer.getTotalCoincidentEdges() == 1 && !edgeContainer.isSelfEdge()) {
					edgeContainer.setStraightEdge(true);
				}
			
			}
		}
	}
	
	
	/**
	 * Return a 2-tuple containing the appropriate radius for the circular edge arc, as well
	 * as how much it should be rotated in the node-to-node displacement axis.
	 * 
	 * @param edgeContainer The AugmentedEdgeContainer object holding additional information about the
	 * edge, including its index amongst the other edges that connect the same pair of nodes
	 * @param selfEdge Whether or not the edge leads from a node to itself
	 */
	private double[] findArcEdgeMetrics(AugmentedEdgeContainer edgeContainer) {
		
		// Level 1 has 2^2 - 1^1 = 3 edges, level 2 has 3^3 - 2^2 = 5, level 3 has 7
		int edgeLevel = (int) (Math.sqrt((double) edgeContainer.getEdgeNumber()));
		int maxLevel = (int) (Math.sqrt((double) edgeContainer.getTotalCoincidentEdges()));
		
		int edgesInLevel = edgeLevel * 2 + 1;
		
		double curvedEdgeRadius;
		
		if (edgeContainer.isSelfEdge()) {
			// For self-edges, want greater edge level -> greater radius
			curvedEdgeRadius = ARC_SELF_EDGE_MINIMUM_RADIUS
					+ ARC_SELF_EDGE_RADIUS_FACTOR * Math.pow(edgeLevel, ARC_SELF_EDGE_EXPONENTIAL_BASE);
		} else {
			// For regular edges, want greater edge level -> smaller radius (more curvature)
			curvedEdgeRadius = edgeContainer.getStart().distance(edgeContainer.getEnd()) * (0.5 + (double) 3.5 / Math.pow(edgeLevel, 2));			
		}
		
		// The outmost level is usually not completed
		if (edgeLevel == maxLevel) {
			edgesInLevel = (int) (edgeContainer.getTotalCoincidentEdges() - Math.pow(maxLevel, 2) + 1);
		}
		
		double edgeRadialAngle = (double) (edgeContainer.getEdgeNumber() - Math.pow(edgeLevel, 2)) / edgesInLevel * Math.PI * 2;

		// Flip the angle by 180 degrees for every other edge level for aesthetic effect
		if (edgeLevel % 2 == 0) {
			edgeRadialAngle = Math.PI - edgeRadialAngle;
		}
		
		return new double[]{curvedEdgeRadius, edgeRadialAngle};
	}
			
}


package com.boofisher.app.cySimpleRenderer.internal.eventbus;

import java.util.Collection;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;

public class FitInViewEvent {

	private final Collection<View<CyNode>> selectedNodeViews;
	
	public FitInViewEvent(Collection<View<CyNode>> selectedNodeViews) {
		this.selectedNodeViews = selectedNodeViews;
	}
	
	public Collection<View<CyNode>> getSelectedNodeViews() {
		return selectedNodeViews;
	}
	
}
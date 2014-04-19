/*
 * Copyright 2013 John Ahlroos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.jasoft.dragdroplayouts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.DropTarget;
import com.vaadin.event.dd.TargetDetails;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.shared.Connector;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Component;
import com.vaadin.ui.LegacyComponent;

import fi.jasoft.dragdroplayouts.client.ui.LayoutDragMode;
import fi.jasoft.dragdroplayouts.client.ui.accordion.DDAccordionState;
import fi.jasoft.dragdroplayouts.details.AccordionTargetDetails;
import fi.jasoft.dragdroplayouts.events.LayoutBoundTransferable;
import fi.jasoft.dragdroplayouts.interfaces.DragFilter;
import fi.jasoft.dragdroplayouts.interfaces.LayoutDragSource;
import fi.jasoft.dragdroplayouts.interfaces.ShimSupport;

/**
 * Accordion with drag and drop support
 * 
 * @author John Ahlroos / www.jasoft.fi
 * @since 0.4.0
 */
@SuppressWarnings("serial")
public class DDAccordion extends Accordion implements LayoutDragSource,
        DropTarget, ShimSupport, LegacyComponent {

    /**
     * The drop handler which handles dropped components in the layout.
     */
    private DropHandler dropHandler;

    // A filter for dragging components.
    private DragFilter dragFilter = DragFilter.ALL;

    /**
     * {@inheritDoc}
     */
    public Transferable getTransferable(Map<String, Object> rawVariables) {
        if (rawVariables.get("index") != null) {
            int index = Integer.parseInt(rawVariables.get("index").toString());
            Iterator<Component> iter = getComponentIterator();
            int counter = 0;
            Component c = null;
            while (iter.hasNext()) {
                c = iter.next();
                if (counter == index) {
                    break;
                }
                counter++;
            }

            rawVariables.put("component", c);
        } else if (rawVariables.get("component") == null) {
            rawVariables.put("component", DDAccordion.this);
        }

        return new LayoutBoundTransferable(this, rawVariables);
    }

    /**
     * Sets the current handler which handles dropped components on the layout.
     * By setting a drop handler dropping components on the layout is enabled.
     * By setting the dropHandler to null dropping is disabled.
     * 
     * @param dropHandler
     *            The drop handler to handle drop events or null to disable
     *            dropping
     */
    public void setDropHandler(DropHandler dropHandler) {
        if (this.dropHandler != dropHandler) {
            this.dropHandler = dropHandler;
            requestRepaint();
        }
    }

    /**
     * {@inheritDoc}
     */
    public DropHandler getDropHandler() {
        return dropHandler;
    }

    /**
     * {@inheritDoc}
     */
    public TargetDetails translateDropTargetDetails(
            Map<String, Object> clientVariables) {
        return new AccordionTargetDetails(this, clientVariables);
    }

    /**
     * {@inheritDoc}
     */
    public LayoutDragMode getDragMode() {
        return getState().dd.dragMode;
    }

    /**
     * {@inheritDoc}
     */
    public void setDragMode(LayoutDragMode mode) {
        getState().dd.dragMode = mode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        // Add drop handler
        if (dropHandler != null && isEnabled()) {
            dropHandler.getAcceptCriterion().paint(target);
        }
    }

    /**
     * Sets the ratio which determines how a tab is divided into drop zones. The
     * ratio is measured from the left and right borders. For example, setting
     * the ratio to 0.3 will divide the drop zone in three equal parts
     * (left,middle,right). Setting the ratio to 0.5 will disable dropping in
     * the middle and setting it to 0 will disable dropping at the sides.
     * 
     * @param ratio
     *            A ratio between 0 and 0.5. Default is 0.2
     */
    public void setComponentVerticalDropRatio(float ratio) {
        if (ratio != getState().tabTopBottomDropRatio) {
            if (ratio >= 0 && ratio <= 0.5) {
                getState().tabTopBottomDropRatio = ratio;
            } else {
                throw new IllegalArgumentException(
                        "Ratio must be between 0 and 0.5");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setShim(boolean shim) {
        getState().dd.iframeShims = shim;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isShimmed() {
        return getState().dd.iframeShims;
    }

    /**
     * {@inheritDoc}
     */
    public DragFilter getDragFilter() {
        return dragFilter;
    }

    /**
     * {@inheritDoc}
     */
    public void setDragFilter(DragFilter dragFilter) {
        this.dragFilter = dragFilter;
    }

    @Override
    public DDAccordionState getState() {
        return (DDAccordionState) super.getState();
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);

        // Update draggable filter
        Iterator<Component> componentIterator = getComponentIterator();
        getState().dd.draggable = new ArrayList<Connector>();
        while (componentIterator.hasNext()) {
            Component c = componentIterator.next();
            if (dragFilter.isDraggable(c)) {
                getState().dd.draggable.add(c);
            }
        }
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
	// FIXME Remove when drag&drop no longer is legacy
    }
}

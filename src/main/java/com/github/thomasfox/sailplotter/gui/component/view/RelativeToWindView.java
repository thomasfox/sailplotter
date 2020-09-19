package com.github.thomasfox.sailplotter.gui.component.view;

import javax.swing.event.ListSelectionEvent;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.gui.SwingGui;
import com.github.thomasfox.sailplotter.gui.component.panel.ControlPanel;
import com.github.thomasfox.sailplotter.gui.component.panel.ZoomChangeEvent;
import com.github.thomasfox.sailplotter.gui.component.plot.AbstractPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.FullMapPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.VelocityBearingPolarPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedRelativeBearingPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedVelocityMadeGoodPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedVelocityPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedWindwardMapPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.table.TackTablePanel;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.Tack;

public class RelativeToWindView extends AbstractView
{
  private static final long serialVersionUID = 1L;

  private final SwingGui gui;

  private final ZoomedVelocityMadeGoodPlotPanel zoomedVelocityMadeGoodPlotPanel;

  private final ZoomedVelocityPlotPanel zoomedVelocityPlotPanel;

  private final ZoomedRelativeBearingPlotPanel zoomedBearingPlotPanel;

  private final ControlPanel controlPanel;

  private final AbstractPlotPanel fullMapPlotPanel;

  private final AbstractPlotPanel zoomedWindwardMapPlotPanel;

  private final AbstractPlotPanel velocityBearingPolarPlotPanel;

  private final TackTablePanel tackTablePanel;

  private Data data;

  public RelativeToWindView(SwingGui gui)
  {
    this.gui = gui;

    zoomedVelocityMadeGoodPlotPanel = new ZoomedVelocityMadeGoodPlotPanel();
    createLayout()
        .withGridxy(0, 0)
        .withWeightx(0.375).withWeighty(0.333)
        .add(zoomedVelocityMadeGoodPlotPanel);

    zoomedVelocityPlotPanel = new ZoomedVelocityPlotPanel();
    createLayout()
        .withGridxy(0, 1)
        .withWeightx(0.375).withWeighty(0.333)
        .add(zoomedVelocityPlotPanel);

    zoomedBearingPlotPanel = new ZoomedRelativeBearingPlotPanel();
    createLayout()
        .withGridxy(0, 2)
        .withWeightx(0.375).withWeighty(0.333)
        .add(zoomedBearingPlotPanel);


    fullMapPlotPanel = new FullMapPlotPanel();
    createLayout()
        .withGridxy(1, 0)
        .withWeightx(0.375).withWeighty(0.333)
        .add(fullMapPlotPanel);

    zoomedWindwardMapPlotPanel = new ZoomedWindwardMapPlotPanel();
    createLayout()
        .withGridxy(1, 1)
        .withWeightx(0.375).withWeighty(0.333)
        .add(zoomedWindwardMapPlotPanel);

    controlPanel = new ControlPanel(gui);
    createLayout()
        .withGridxy(2, 0)
        .withWeightx(0.25).withWeighty(0.333)
        .add(controlPanel);

    velocityBearingPolarPlotPanel = new VelocityBearingPolarPlotPanel();
    createLayout()
        .withGridxy(2, 1)
        .withWeightx(0.25).withWeighty(0.333)
        .add(velocityBearingPolarPlotPanel);

    tackTablePanel = new TackTablePanel(this::tackSelected);
    createLayout()
        .withGridxy(1, 2)
        .withWeightx(0.625).withWeighty(0.333)
        .withColumnSpan(2)
        .add(tackTablePanel);
  }

  public void redisplay(boolean updateTableContent)
  {
    ZoomChangeEvent zoomChangeEvent = controlPanel.getChangeEventFromCurrentData();
    zoomedVelocityMadeGoodPlotPanel.zoomChanged(zoomChangeEvent);
    zoomedVelocityPlotPanel.zoomChanged(zoomChangeEvent);
    zoomedBearingPlotPanel.zoomChanged(zoomChangeEvent);
    fullMapPlotPanel.zoomChanged(zoomChangeEvent);
    zoomedWindwardMapPlotPanel.zoomChanged(zoomChangeEvent);
    velocityBearingPolarPlotPanel.zoomChanged(zoomChangeEvent);
    if (data != null)
    {
      if (updateTableContent)
      {
        tackTablePanel.updateContent(data.getTackList());
      }
    }
  }

  public void tackSelected(ListSelectionEvent e)
  {
    if (e.getValueIsAdjusting() || gui.inUpdate)
    {
      return;
    }
    int index = tackTablePanel.getSelectedTackIndex();
    Tack tack = data.getTackList().get(index);
    controlPanel.setZoomStartIndex(Math.max(tack.startOfTackDataPointIndex - Constants.NUM_DATAPOINTS_TACK_EXTENSION, 0));
    controlPanel.setZoomEndIndex(tack.endOfTackDataPointIndex + Constants.NUM_DATAPOINTS_TACK_EXTENSION);
  }

  @Override
  public void dataChanged(Data data)
  {
    this.data = data;
    controlPanel.dataChanged(data);
    ZoomChangeEvent zoomChangeEvent = controlPanel.getChangeEventFromCurrentData();
    zoomedVelocityMadeGoodPlotPanel.dataAndZoomChanged(data, zoomChangeEvent);
    zoomedVelocityPlotPanel.dataAndZoomChanged(data, zoomChangeEvent);
    zoomedBearingPlotPanel.dataAndZoomChanged(data, zoomChangeEvent);
    fullMapPlotPanel.dataAndZoomChanged(data, zoomChangeEvent);
    zoomedWindwardMapPlotPanel.dataAndZoomChanged(data, zoomChangeEvent);
    velocityBearingPolarPlotPanel.dataAndZoomChanged(data, zoomChangeEvent);
    tackTablePanel.updateContent(data.getTackList());
  }

  @Override
  public void zoomChanged(ZoomChangeEvent zoomChangeEvent)
  {
    controlPanel.zoomChanged(zoomChangeEvent);
    zoomedVelocityMadeGoodPlotPanel.zoomChanged(zoomChangeEvent);
    zoomedVelocityPlotPanel.zoomChanged(zoomChangeEvent);
    zoomedBearingPlotPanel.zoomChanged(zoomChangeEvent);
    fullMapPlotPanel.zoomChanged(zoomChangeEvent);
    zoomedWindwardMapPlotPanel.zoomChanged(zoomChangeEvent);
    velocityBearingPolarPlotPanel.zoomChanged(zoomChangeEvent);
  }
}

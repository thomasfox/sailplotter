package com.github.thomasfox.sailplotter.gui.component.view;

import javax.swing.event.ListSelectionEvent;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.gui.SwingGui;
import com.github.thomasfox.sailplotter.gui.component.panel.ControlPanel;
import com.github.thomasfox.sailplotter.gui.component.panel.ZoomPanelChangeEvent;
import com.github.thomasfox.sailplotter.gui.component.plot.AbstractPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.FullMapPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.VelocityBearingPolarPlotPanel;
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

  private final ControlPanel controlPanel;

  private final AbstractPlotPanel fullMapPlotPanel;

  private final AbstractPlotPanel zoomedWindwardMapPlotPanel;

  private final AbstractPlotPanel velocityBearingPolarPlotPanel;

  TackTablePanel tackTablePanel;

  private Data data;

  public RelativeToWindView(SwingGui gui)
  {
    this.gui = gui;

    controlPanel = new ControlPanel(gui);

    createLayout()
        .withGridxy(2, 0)
        .withWeightx(0.25).withWeighty(0.25)
        .add(controlPanel);

    zoomedVelocityMadeGoodPlotPanel = new ZoomedVelocityMadeGoodPlotPanel();
    createLayout()
        .withGridxy(0, 0)
        .withWeightx(0.375).withWeighty(0.25)
        .add(zoomedVelocityMadeGoodPlotPanel);

    zoomedVelocityPlotPanel = new ZoomedVelocityPlotPanel();

    createLayout()
        .withGridxy(1, 0)
        .withWeightx(0.375).withWeighty(0.25)
        .add(zoomedVelocityPlotPanel);

    fullMapPlotPanel = new FullMapPlotPanel();
    createLayout()
        .withGridxy(0, 1)
        .withWeightx(0.375).withWeighty(0.5)
        .add(fullMapPlotPanel);

    zoomedWindwardMapPlotPanel = new ZoomedWindwardMapPlotPanel();
    createLayout()
        .withGridxy(1, 1)
        .withWeightx(0.375).withWeighty(0.5)
        .add(zoomedWindwardMapPlotPanel);

    velocityBearingPolarPlotPanel = new VelocityBearingPolarPlotPanel();
    createLayout()
        .withGridxy(2, 1)
        .withWeightx(0.25).withWeighty(0.5)
        .add(velocityBearingPolarPlotPanel);

    tackTablePanel = new TackTablePanel(this::tackSelected);
    createLayout()
        .withGridxy(0, 2)
        .withWeightx(1).withWeighty(0.25)
        .withColumnSpan(3)
        .add(tackTablePanel);
  }

  public void redisplay(boolean updateTableContent)
  {
    ZoomPanelChangeEvent zoomChangeEvent = controlPanel.getChangeEventFromCurrentData();
    zoomedVelocityMadeGoodPlotPanel.zoomChanged(zoomChangeEvent);
    zoomedVelocityPlotPanel.zoomChanged(zoomChangeEvent);
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
    zoomedVelocityMadeGoodPlotPanel.dataChanged(data);
    zoomedVelocityPlotPanel.dataChanged(data);
    fullMapPlotPanel.dataChanged(data);
    zoomedWindwardMapPlotPanel.dataChanged(data);
    velocityBearingPolarPlotPanel.dataChanged(data);
  }

  @Override
  public void processZoomPanelChangeEvent(ZoomPanelChangeEvent e)
  {
    controlPanel.processZoomPanelChangeEvent(e);
  }
}

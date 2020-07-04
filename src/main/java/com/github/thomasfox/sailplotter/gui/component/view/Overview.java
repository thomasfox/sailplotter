package com.github.thomasfox.sailplotter.gui.component.view;

import javax.swing.event.ListSelectionEvent;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.gui.SwingGui;
import com.github.thomasfox.sailplotter.gui.component.panel.ControlPanel;
import com.github.thomasfox.sailplotter.gui.component.panel.ZoomPanelChangeEvent;
import com.github.thomasfox.sailplotter.gui.component.plot.AbstractPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.FullMapPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.FullVelocityBearingOverTimePlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.TackVelocityBearingPolarPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.VelocityBearingPolarPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedBearingHistogramPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedMapPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedVelocityBearingOverTimePlotPanel;
import com.github.thomasfox.sailplotter.gui.component.table.TackSeriesTablePanel;
import com.github.thomasfox.sailplotter.gui.component.table.TackTablePanel;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.Tack;
import com.github.thomasfox.sailplotter.model.TackSeries;

public class Overview extends AbstractView
{
  private static final long serialVersionUID = 1L;

  private final SwingGui gui;

  private final ControlPanel controlPanel;

  private final AbstractPlotPanel fullVelocityBearingOverTimePlotPanel;

  private final AbstractPlotPanel zoomedVelocityBearingOverTimePlotPanel;

  private final AbstractPlotPanel zoomedBearingHistogramPlotPanel;

  private final AbstractPlotPanel fullMapPlotPanel;

  private final AbstractPlotPanel zoomedMapPlotPanel;

  private final AbstractPlotPanel velocityBearingPolarPlotPanel;

  private final AbstractPlotPanel tackVelocityBearingPolarPlotPanel;

  TackTablePanel tackTablePanel;

  TackSeriesTablePanel tackSeriesTablePanel;

  private Data data;

  public Overview(SwingGui gui)
  {
    this.gui = gui;

    controlPanel = new ControlPanel(gui);

    fullVelocityBearingOverTimePlotPanel = new FullVelocityBearingOverTimePlotPanel(controlPanel.getZoomStartIndex(), controlPanel.getZoomIndex());
    createLayout()
        .withGridx(0).withGridy(0)
        .withWeightx(0.333).withWeighty(0.25)
        .add(fullVelocityBearingOverTimePlotPanel);

    zoomedVelocityBearingOverTimePlotPanel = new ZoomedVelocityBearingOverTimePlotPanel(controlPanel.getZoomStartIndex(), controlPanel.getZoomIndex());
    createLayout()
        .withGridx(1).withGridy(0)
        .withWeightx(0.333).withWeighty(0.25)
        .add(zoomedVelocityBearingOverTimePlotPanel);

    zoomedBearingHistogramPlotPanel = new ZoomedBearingHistogramPlotPanel(controlPanel.getZoomStartIndex(), controlPanel.getZoomIndex());
    createLayout()
        .withGridxy(2, 0)
        .withWeightx(0.333).withWeighty(0.25)
        .withColumnSpan(2)
        .add(controlPanel);

    fullMapPlotPanel = new FullMapPlotPanel(controlPanel.getZoomStartIndex(), controlPanel.getZoomIndex());
    createLayout()
        .withGridxy(0, 1)
        .withWeightx(0.333).withWeighty(0.5)
        .add(fullMapPlotPanel);

    zoomedMapPlotPanel = new ZoomedMapPlotPanel(controlPanel.getZoomStartIndex(), controlPanel.getZoomIndex());
    createLayout()
        .withGridxy(1, 1)
        .withWeightx(0.333).withWeighty(0.5)
        .add(zoomedMapPlotPanel);

    tackVelocityBearingPolarPlotPanel = new TackVelocityBearingPolarPlotPanel(controlPanel.getZoomStartIndex(), controlPanel.getZoomIndex());
    createLayout()
        .withGridxy(2, 1)
        .withWeightx(0.166).withWeighty(0.5)
        .add(tackVelocityBearingPolarPlotPanel);

    velocityBearingPolarPlotPanel = new VelocityBearingPolarPlotPanel(controlPanel.getZoomStartIndex(), controlPanel.getZoomIndex());
    createLayout()
        .withGridxy(3, 1)
        .withWeightx(0.166).withWeighty(0.5)
        .add(velocityBearingPolarPlotPanel);

    tackTablePanel = new TackTablePanel(this::tackSelected);
    createLayout()
        .withGridxy(0, 2)
        .withWeightx(0.666).withWeighty(0.25)
        .withColumnSpan(2)
        .add(tackTablePanel);


    tackSeriesTablePanel = new TackSeriesTablePanel(this::tackSeriesSelected);
    createLayout()
        .withGridxy(2, 2)
        .withWeightx(0.666).withWeighty(0.25)
        .withColumnSpan(2)
        .add(tackSeriesTablePanel);
  }

  public void redisplay(boolean updateTableContent)
  {
    int zoomWindowStartIndex = controlPanel.getZoomStartIndex();
    int zoomWindowZoomIndex = controlPanel.getZoomIndex();
    fullVelocityBearingOverTimePlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    zoomedVelocityBearingOverTimePlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    zoomedBearingHistogramPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    fullMapPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    zoomedMapPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    velocityBearingPolarPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    tackVelocityBearingPolarPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    if (data != null)
    {
      if (updateTableContent)
      {
        tackTablePanel.updateContent(data.getTackList());
        tackSeriesTablePanel.updateContent(data.getTackSeriesList());
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

  public void tackSeriesSelected(ListSelectionEvent e)
  {
    if (e.getValueIsAdjusting() || gui.inUpdate)
    {
      return;
    }
    int index = tackSeriesTablePanel.getSelectedTackSeriesIndex();
    TackSeries tackSeries = data.getTackSeriesList().get(index);
    try
    {
      gui.inUpdate = true;
      tackTablePanel.selectInterval(tackSeries.startTackIndex, tackSeries.endTackIndex);
      controlPanel.setZoomStartIndex(Math.max(data.getTackList().get(tackSeries.startTackIndex).startOfTackDataPointIndex - Constants.NUM_DATAPOINTS_TACK_EXTENSION, 0));
      controlPanel.setZoomEndIndex(data.getTackList().get(tackSeries.endTackIndex).endOfTackDataPointIndex +  Constants.NUM_DATAPOINTS_TACK_EXTENSION);
    }
    finally
    {
      gui.inUpdate = false;
    }
    redisplay(false);
  }

  @Override
  public void dataChanged(Data data)
  {
    this.data = data;
    controlPanel.dataChanged(data);
    fullVelocityBearingOverTimePlotPanel.dataChanged(data);
    zoomedVelocityBearingOverTimePlotPanel.dataChanged(data);
    zoomedBearingHistogramPlotPanel.dataChanged(data);
    fullMapPlotPanel.dataChanged(data);
    zoomedMapPlotPanel.dataChanged(data);
    tackVelocityBearingPolarPlotPanel.dataChanged(data);
    velocityBearingPolarPlotPanel.dataChanged(data);
  }

  @Override
  public void processZoomPanelChangeEvent(ZoomPanelChangeEvent e)
  {
    controlPanel.processZoomPanelChangeEvent(e);
  }
}

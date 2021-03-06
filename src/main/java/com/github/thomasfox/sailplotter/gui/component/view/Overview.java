package com.github.thomasfox.sailplotter.gui.component.view;

import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.gui.SwingGui;
import com.github.thomasfox.sailplotter.gui.component.Layout;
import com.github.thomasfox.sailplotter.gui.component.panel.ControlPanel;
import com.github.thomasfox.sailplotter.gui.component.panel.ZoomChangeEvent;
import com.github.thomasfox.sailplotter.gui.component.plot.AbstractPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.FullMapPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.FullVelocityBearingOverTimePlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.TackVelocityBearingPolarPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.VelocityBearingScatteredPolarPlotPanel;
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

    fullVelocityBearingOverTimePlotPanel = new FullVelocityBearingOverTimePlotPanel();
    createLayout()
        .withGridx(0).withGridy(0)
        .withWeightx(0.333).withWeighty(0.35)
        .add(fullVelocityBearingOverTimePlotPanel);

    zoomedVelocityBearingOverTimePlotPanel = new ZoomedVelocityBearingOverTimePlotPanel();
    createLayout()
        .withGridx(1).withGridy(0)
        .withWeightx(0.333).withWeighty(0.35)
        .add(zoomedVelocityBearingOverTimePlotPanel);

    zoomedBearingHistogramPlotPanel = new ZoomedBearingHistogramPlotPanel();
    JPanel topRightPanel = new JPanel();
    topRightPanel.setLayout(new GridBagLayout());
    new Layout(topRightPanel)
        .withGridxy(0, 0)
        .withWeighty(0.5)
        .add(zoomedBearingHistogramPlotPanel);
    new Layout(topRightPanel)
        .withGridxy(0, 1)
        .withWeighty(0.5)
        .add(controlPanel);
    createLayout()
        .withGridxy(2, 0)
        .withWeightx(0.333).withWeighty(0.35)
        .withColumnSpan(2)
        .add(topRightPanel);

    fullMapPlotPanel = new FullMapPlotPanel();
    createLayout()
        .withGridxy(0, 1)
        .withWeightx(0.333).withWeighty(0.45)
        .add(fullMapPlotPanel);

    zoomedMapPlotPanel = new ZoomedMapPlotPanel();
    createLayout()
        .withGridxy(1, 1)
        .withWeightx(0.333).withWeighty(0.45)
        .add(zoomedMapPlotPanel);

    tackVelocityBearingPolarPlotPanel = new TackVelocityBearingPolarPlotPanel();
    createLayout()
        .withGridxy(2, 1)
        .withWeightx(0.166).withWeighty(0.45)
        .add(tackVelocityBearingPolarPlotPanel);

    velocityBearingPolarPlotPanel = new VelocityBearingScatteredPolarPlotPanel();
    createLayout()
        .withGridxy(3, 1)
        .withWeightx(0.166).withWeighty(0.45)
        .add(velocityBearingPolarPlotPanel);

    tackTablePanel = new TackTablePanel(this::tackSelected);
    createLayout()
        .withGridxy(0, 2)
        .withWeightx(0.666).withWeighty(0.2)
        .withColumnSpan(2)
        .add(tackTablePanel);


    tackSeriesTablePanel = new TackSeriesTablePanel(this::tackSeriesSelected);
    createLayout()
        .withGridxy(2, 2)
        .withWeightx(0.666).withWeighty(0.2)
        .withColumnSpan(2)
        .add(tackSeriesTablePanel);
  }

  public void tackSelected(ListSelectionEvent e)
  {
    if (e.getValueIsAdjusting() || gui.inUpdate)
    {
      return;
    }
    int[] selectedIndices = tackTablePanel.getSelectedTackIndices();
    if (selectedIndices == null || selectedIndices.length == 0)
    {
      return;
    }
    Tack firstTack = data.getTackList().get(selectedIndices[0]);
    controlPanel.setZoomStartIndex(Math.max(firstTack.startOfTackDataPointIndex - Constants.NUM_DATAPOINTS_TACK_EXTENSION, 0));
    Tack lastTack = data.getTackList().get(selectedIndices[selectedIndices.length - 1]);
    controlPanel.setZoomEndIndex(lastTack.endOfTackDataPointIndex + Constants.NUM_DATAPOINTS_TACK_EXTENSION);
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
  }

  @Override
  public void dataChanged(Data data)
  {
    this.data = data;
    controlPanel.dataChanged(data);
    ZoomChangeEvent zoomChangeEvent = controlPanel.getChangeEventFromCurrentData();
    fullVelocityBearingOverTimePlotPanel.dataAndZoomChanged(data, zoomChangeEvent);
    zoomedVelocityBearingOverTimePlotPanel.dataAndZoomChanged(data, zoomChangeEvent);
    zoomedBearingHistogramPlotPanel.dataAndZoomChanged(data, zoomChangeEvent);
    fullMapPlotPanel.dataAndZoomChanged(data, zoomChangeEvent);
    zoomedMapPlotPanel.dataAndZoomChanged(data, zoomChangeEvent);
    tackVelocityBearingPolarPlotPanel.dataAndZoomChanged(data, zoomChangeEvent);
    velocityBearingPolarPlotPanel.dataAndZoomChanged(data, zoomChangeEvent);
    tackTablePanel.updateContent(data.getTackList());
    tackSeriesTablePanel.updateContent(data.getTackSeriesList());
  }

  @Override
  public void zoomChanged(ZoomChangeEvent zoomChangeEvent)
  {
    controlPanel.zoomChanged(zoomChangeEvent);
    fullVelocityBearingOverTimePlotPanel.zoomChanged(zoomChangeEvent);
    zoomedVelocityBearingOverTimePlotPanel.zoomChanged(zoomChangeEvent);
    zoomedBearingHistogramPlotPanel.zoomChanged(zoomChangeEvent);
    fullMapPlotPanel.zoomChanged(zoomChangeEvent);
    zoomedMapPlotPanel.zoomChanged(zoomChangeEvent);
    tackVelocityBearingPolarPlotPanel.zoomChanged(zoomChangeEvent);
    velocityBearingPolarPlotPanel.zoomChanged(zoomChangeEvent);
    tackTablePanel.zoomChanged(zoomChangeEvent);
  }
}

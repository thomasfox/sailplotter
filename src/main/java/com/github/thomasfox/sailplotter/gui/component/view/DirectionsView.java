package com.github.thomasfox.sailplotter.gui.component.view;

import com.github.thomasfox.sailplotter.gui.SwingGui;
import com.github.thomasfox.sailplotter.gui.component.panel.ZoomPanel;
import com.github.thomasfox.sailplotter.gui.component.panel.ZoomPanelChangeEvent;
import com.github.thomasfox.sailplotter.gui.component.plot.AbstractPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedBearingOverTimePlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedHeelOverTimePlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedRollOverTimePlotPanel;
import com.github.thomasfox.sailplotter.model.Data;

public class DirectionsView extends AbstractView
{
  private static final long serialVersionUID = 1L;

  private final AbstractPlotPanel zoomedBearingOverTimePlotPanel;

  private final AbstractPlotPanel zoomedHeelOverTimePlotPanel;

  private final AbstractPlotPanel zoomedRollOverTimePlotPanel;

  private final ZoomPanel zoomPanel;

  public DirectionsView(SwingGui gui)
  {
    zoomPanel = new ZoomPanel();
    zoomPanel.addListener(gui::zoomPanelStateChanged);

    zoomedBearingOverTimePlotPanel = new ZoomedBearingOverTimePlotPanel(zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    createLayout()
        .withGridx(0).withGridy(0)
        .withWeightx(0.5).withWeighty(0.5)
        .add(zoomedBearingOverTimePlotPanel);

    zoomedHeelOverTimePlotPanel = new ZoomedHeelOverTimePlotPanel(zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    createLayout()
        .withGridx(1).withGridy(0)
        .withWeightx(0.5).withWeighty(0.5)
        .add(zoomedHeelOverTimePlotPanel);

    zoomedRollOverTimePlotPanel = new ZoomedRollOverTimePlotPanel(zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    createLayout()
        .withGridx(0).withGridy(1)
        .withWeightx(0.5).withWeighty(0.5)
        .add(zoomedRollOverTimePlotPanel);

    createLayout().withGridx(1).withGridy(1).withWeightx(0.5).withWeighty(0.2).withNoFillY()
        .add(zoomPanel);
  }

  public void redisplay()
  {
    int zoomWindowStartIndex = zoomPanel.getStartIndex();
    int zoomWindowZoomIndex = zoomPanel.getZoomIndex();
    zoomedBearingOverTimePlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    zoomedHeelOverTimePlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    zoomedRollOverTimePlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
  }

  public void dataChanged(Data data)
  {
    zoomPanel.setDataSize(data.getPointsWithLocation().size());
    zoomedBearingOverTimePlotPanel.dataChanged(data);
    zoomedHeelOverTimePlotPanel.dataChanged(data);
    zoomedRollOverTimePlotPanel.dataChanged(data);
  }

  @Override
  public void alignZoomPanelToChangeEvent(ZoomPanelChangeEvent e)
  {
    if (!e.isSource(zoomPanel))
    {
      zoomPanel.setStartIndex(e.getStartIndex(), false);
      zoomPanel.setZoomIndex(e.getZoomPosition(), false);
    }
  }

}

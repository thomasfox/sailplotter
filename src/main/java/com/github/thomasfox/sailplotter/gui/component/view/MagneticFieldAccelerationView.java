package com.github.thomasfox.sailplotter.gui.component.view;

import com.github.thomasfox.sailplotter.gui.SwingGui;
import com.github.thomasfox.sailplotter.gui.component.panel.ZoomPanel;
import com.github.thomasfox.sailplotter.gui.component.panel.ZoomPanelChangeEvent;
import com.github.thomasfox.sailplotter.gui.component.plot.AbstractPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedAccelerationPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedMagneticFieldPlotPanel;
import com.github.thomasfox.sailplotter.model.Data;

public class MagneticFieldAccelerationView extends AbstractView
{
  private static final long serialVersionUID = 1L;

  private final AbstractPlotPanel zoomedMagneticFieldAbsValuePanel;

  private final AbstractPlotPanel zoomedMagneticFieldXPanel;

  private final AbstractPlotPanel zoomedMagneticFieldYPanel;

  private final AbstractPlotPanel zoomedMagneticFieldZPanel;

  private final AbstractPlotPanel zoomedAccelerationXPanel;

  private final AbstractPlotPanel zoomedAccelerationYPanel;

  private final AbstractPlotPanel zoomedAccelerationZPanel;

  private final ZoomPanel zoomPanel;

  public MagneticFieldAccelerationView(SwingGui gui)
  {
    zoomPanel = new ZoomPanel();
    zoomPanel.addListener(gui::zoomPanelStateChanged);

    zoomedMagneticFieldAbsValuePanel = new ZoomedMagneticFieldPlotPanel(0);
    createLayout()
        .withGridx(0).withGridy(0)
        .withWeightx(0.5).withWeighty(0.25)
        .add(zoomedMagneticFieldAbsValuePanel);

    zoomedMagneticFieldXPanel = new ZoomedMagneticFieldPlotPanel(1);
    createLayout()
        .withGridx(0).withGridy(1)
        .withWeightx(0.5).withWeighty(0.25)
        .add(zoomedMagneticFieldXPanel);

    zoomedMagneticFieldYPanel = new ZoomedMagneticFieldPlotPanel(2);
    createLayout()
        .withGridx(0).withGridy(2)
        .withWeightx(0.5).withWeighty(0.25)
        .add(zoomedMagneticFieldYPanel);

    zoomedMagneticFieldZPanel = new ZoomedMagneticFieldPlotPanel(3);
    createLayout()
        .withGridx(0).withGridy(3)
        .withWeightx(0.5).withWeighty(0.25)
        .add(zoomedMagneticFieldZPanel);

    createLayout()
        .withGridx(1).withGridy(0)
        .withWeightx(0.5).withWeighty(0.25)
        .withNoFillY()
        .add(zoomPanel);

    zoomedAccelerationXPanel = new ZoomedAccelerationPlotPanel(1);
    createLayout()
        .withGridx(1).withGridy(1)
        .withWeightx(0.5).withWeighty(0.25)
        .add(zoomedAccelerationXPanel);

    zoomedAccelerationYPanel = new ZoomedAccelerationPlotPanel(2);
    createLayout()
        .withGridx(1).withGridy(2)
        .withWeightx(0.5).withWeighty(0.25)
        .add(zoomedAccelerationYPanel);

    zoomedAccelerationZPanel = new ZoomedAccelerationPlotPanel(3);
    createLayout()
        .withGridx(1).withGridy(3)
        .withWeightx(0.5).withWeighty(0.25)
        .add(zoomedAccelerationZPanel);
  }

  public void redisplay()
  {
    ZoomPanelChangeEvent zoomChangeEvent = zoomPanel.getChangeEventFromCurrentData();
    zoomedMagneticFieldAbsValuePanel.zoomChanged(zoomChangeEvent);
    zoomedMagneticFieldXPanel.zoomChanged(zoomChangeEvent);
    zoomedMagneticFieldYPanel.zoomChanged(zoomChangeEvent);
    zoomedMagneticFieldZPanel.zoomChanged(zoomChangeEvent);
    zoomedAccelerationXPanel.zoomChanged(zoomChangeEvent);
    zoomedAccelerationYPanel.zoomChanged(zoomChangeEvent);
    zoomedAccelerationZPanel.zoomChanged(zoomChangeEvent);
  }

  @Override
  public void dataChanged(Data data)
  {
    zoomPanel.setDataSize(data.getPointsWithLocation().size());
    zoomedMagneticFieldAbsValuePanel.dataChanged(data);
    zoomedMagneticFieldXPanel.dataChanged(data);
    zoomedMagneticFieldYPanel.dataChanged(data);
    zoomedMagneticFieldZPanel.dataChanged(data);
    zoomedAccelerationXPanel.dataChanged(data);
    zoomedAccelerationYPanel.dataChanged(data);
    zoomedAccelerationZPanel.dataChanged(data);
  }

  @Override
  public void processZoomPanelChangeEvent(ZoomPanelChangeEvent e)
  {
    zoomPanel.processZoomPanelChangeEvent(e);
  }
}

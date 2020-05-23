package com.github.thomasfox.sailplotter.gui.component.view;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.gui.SwingGui;
import com.github.thomasfox.sailplotter.gui.component.panel.ZoomPanel;
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

  private final ZoomPanel zoomPanel;

  private final AbstractPlotPanel fullVelocityBearingOverTimePlotPanel;

  private final AbstractPlotPanel zoomedVelocityBearingOverTimePlotPanel;

  private final AbstractPlotPanel zoomedBearingHistogramPlotPanel;

  private final AbstractPlotPanel fullMapPlotPanel;

  private final AbstractPlotPanel zoomedMapPlotPanel;

  private final AbstractPlotPanel velocityBearingPolarPlotPanel;

  private final AbstractPlotPanel tackVelocityBearingPolarPlotPanel;

  private final JTextField windDirectionTextField;

  TackTablePanel tackTablePanel;

  TackSeriesTablePanel tackSeriesTablePanel;

  private Data data;

  public List<TackSeries> tackSeriesList;

  public Overview(SwingGui gui)
  {
    this.gui = gui;

    zoomPanel = new ZoomPanel();
    zoomPanel.addListener(gui::zoomPanelStateChanged);

    fullVelocityBearingOverTimePlotPanel = new FullVelocityBearingOverTimePlotPanel(zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    createLayout()
        .withGridx(0).withGridy(0)
        .withWeightx(0.333).withWeighty(0.25)
        .add(fullVelocityBearingOverTimePlotPanel);

    zoomedVelocityBearingOverTimePlotPanel = new ZoomedVelocityBearingOverTimePlotPanel(zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    createLayout()
        .withGridx(1).withGridy(0)
        .withWeightx(0.333).withWeighty(0.25)
        .add(zoomedVelocityBearingOverTimePlotPanel);

    JPanel topRightPanel = new JPanel();
    zoomedBearingHistogramPlotPanel = new ZoomedBearingHistogramPlotPanel(zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    topRightPanel.add(zoomedBearingHistogramPlotPanel);
    topRightPanel.add(zoomPanel);

    JPanel windDirectionPanel = new JPanel();
    JLabel windDirectionLabel = new JLabel("Wind direction");
    windDirectionPanel.add(windDirectionLabel);
    windDirectionTextField = new JTextField();
    Dimension windDirectionTextFieldSize = windDirectionTextField.getPreferredSize();
    windDirectionTextFieldSize.width=30;
    windDirectionTextField.setPreferredSize(windDirectionTextFieldSize);
    if (data == null)
    {
      windDirectionTextField.setText("0");
    }
    else
    {
      windDirectionTextField.setText(Integer.toString(data.getAverageWindDirectionInDegrees()));
    }
    windDirectionTextField.addActionListener(gui::windDirectionChanged);
    windDirectionPanel.add(windDirectionTextField);
    topRightPanel.add(windDirectionPanel);

    topRightPanel.setLayout(new BoxLayout(topRightPanel, BoxLayout.PAGE_AXIS));
    createLayout()
        .withGridxy(2, 0)
        .withWeightx(0.333).withWeighty(0.25)
        .withColumnSpan(2)
        .add(topRightPanel);

    fullMapPlotPanel = new FullMapPlotPanel(zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    createLayout()
        .withGridxy(0, 1)
        .withWeightx(0.333).withWeighty(0.5)
        .add(fullMapPlotPanel);

    zoomedMapPlotPanel = new ZoomedMapPlotPanel(zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    createLayout()
        .withGridxy(1, 1)
        .withWeightx(0.333).withWeighty(0.5)
        .add(zoomedMapPlotPanel);

    tackVelocityBearingPolarPlotPanel = new TackVelocityBearingPolarPlotPanel(zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    createLayout()
        .withGridxy(2, 1)
        .withWeightx(0.166).withWeighty(0.5)
        .add(tackVelocityBearingPolarPlotPanel);

    velocityBearingPolarPlotPanel = new VelocityBearingPolarPlotPanel(zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
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

    tackSeriesTablePanel = new TackSeriesTablePanel(tackSeriesList, this::tackSeriesSelected);
    createLayout()
        .withGridxy(2, 2)
        .withWeightx(0.666).withWeighty(0.25)
        .withColumnSpan(2)
        .add(tackSeriesTablePanel);
  }

  public void redisplay(boolean updateTableContent)
  {
    int zoomWindowStartIndex = zoomPanel.getStartIndex();
    int zoomWindowZoomIndex = zoomPanel.getZoomIndex();
    fullVelocityBearingOverTimePlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    zoomedVelocityBearingOverTimePlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    zoomedBearingHistogramPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    fullMapPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    zoomedMapPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    velocityBearingPolarPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    tackVelocityBearingPolarPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    windDirectionTextField.setText(Integer.toString(data.getAverageWindDirectionInDegrees()));

    if (updateTableContent)
    {
      tackTablePanel.updateContent(data.getTackList());
      tackSeriesTablePanel.updateContent(tackSeriesList);
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
    zoomPanel.setStartIndex(Math.max(tack.startOfTackDataPointIndex - Constants.NUM_DATAPOINTS_TACK_EXTENSION, 0), true);
    zoomPanel.setZoomIndex(Math.min(
        Math.max(
            Constants.NUMER_OF_ZOOM_TICKS * (tack.endOfTackDataPointIndex - tack.startOfTackDataPointIndex + 2 * Constants.NUM_DATAPOINTS_TACK_EXTENSION) / (data.getPointsWithLocation().size()),
            3),
        Constants.NUMER_OF_ZOOM_TICKS),
        true);
  }

  public void tackSeriesSelected(ListSelectionEvent e)
  {
    if (e.getValueIsAdjusting() || gui.inUpdate)
    {
      return;
    }
    int index = tackSeriesTablePanel.getSelectedTackSeriesIndex();
    TackSeries tackSeries = tackSeriesList.get(index);
    try
    {
      gui.inUpdate = true;
      tackTablePanel.selectInterval(tackSeries.startTackIndex, tackSeries.endTackIndex);
      zoomPanel.setStartIndex(Math.max(data.getTackList().get(tackSeries.startTackIndex).startOfTackDataPointIndex - Constants.NUM_DATAPOINTS_TACK_EXTENSION, 0), true);
      zoomPanel.setZoomIndex(Math.min(
          Math.max(
              Constants.NUMER_OF_ZOOM_TICKS * (data.getTackList().get(tackSeries.endTackIndex).endOfTackDataPointIndex - data.getTackList().get(tackSeries.startTackIndex).startOfTackDataPointIndex + 2 * Constants.NUM_DATAPOINTS_TACK_EXTENSION) / (data.getPointsWithLocation().size()),
              3),
          Constants.NUMER_OF_ZOOM_TICKS),
          true);
    }
    finally
    {
      gui.inUpdate = false;
    }
    redisplay(false);
  }

  public void dataChanged(Data data)
  {
    this.data = data;
    zoomPanel.setDataSize(data.getPointsWithLocation().size());
    fullVelocityBearingOverTimePlotPanel.dataChanged(data);
    zoomedVelocityBearingOverTimePlotPanel.dataChanged(data);
    zoomedBearingHistogramPlotPanel.dataChanged(data);
    fullMapPlotPanel.dataChanged(data);
    zoomedMapPlotPanel.dataChanged(data);
    tackVelocityBearingPolarPlotPanel.dataChanged(data);
    velocityBearingPolarPlotPanel.dataChanged(data);
    tackSeriesList = new ArrayList<>(data.getTackSeriesList());
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

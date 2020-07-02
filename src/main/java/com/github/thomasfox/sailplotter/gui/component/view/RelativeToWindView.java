package com.github.thomasfox.sailplotter.gui.component.view;

import java.awt.Dimension;

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
import com.github.thomasfox.sailplotter.gui.component.plot.TackVelocityBearingPolarPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.VelocityBearingPolarPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedWindwardMapPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.table.TackSeriesTablePanel;
import com.github.thomasfox.sailplotter.gui.component.table.TackTablePanel;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.Tack;
import com.github.thomasfox.sailplotter.model.TackSeries;

public class RelativeToWindView extends AbstractView
{
  private static final long serialVersionUID = 1L;

  private final SwingGui gui;

  private final ZoomPanel zoomPanel;

  private final AbstractPlotPanel fullMapPlotPanel;

  private final AbstractPlotPanel zoomedWindwardMapPlotPanel;

  private final AbstractPlotPanel velocityBearingPolarPlotPanel;

  private final AbstractPlotPanel tackVelocityBearingPolarPlotPanel;

  private final JTextField windDirectionTextField;

  TackTablePanel tackTablePanel;

  TackSeriesTablePanel tackSeriesTablePanel;

  private Data data;

  public RelativeToWindView(SwingGui gui)
  {
    this.gui = gui;

    zoomPanel = new ZoomPanel();
    zoomPanel.addListener(gui::zoomPanelStateChanged);

    JPanel topRightPanel = new JPanel();
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

    zoomedWindwardMapPlotPanel = new ZoomedWindwardMapPlotPanel(zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    createLayout()
        .withGridxy(1, 1)
        .withWeightx(0.333).withWeighty(0.5)
        .add(zoomedWindwardMapPlotPanel);

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

    tackSeriesTablePanel = new TackSeriesTablePanel(this::tackSeriesSelected);
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
    fullMapPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    zoomedWindwardMapPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    velocityBearingPolarPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    tackVelocityBearingPolarPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
    if (data != null)
    {
      windDirectionTextField.setText(Integer.toString(data.getAverageWindDirectionInDegrees()));
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
    TackSeries tackSeries = data.getTackSeriesList().get(index);
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
    fullMapPlotPanel.dataChanged(data);
    zoomedWindwardMapPlotPanel.dataChanged(data);
    tackVelocityBearingPolarPlotPanel.dataChanged(data);
    velocityBearingPolarPlotPanel.dataChanged(data);
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

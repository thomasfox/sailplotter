package com.github.thomasfox.sailplotter.gui.component.plot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.github.thomasfox.sailplotter.gui.component.panel.TimeWindowPosition;
import com.github.thomasfox.sailplotter.model.Data;

public class ZoomedRollOverTimePlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final TimeSeriesCollection dataset = new TimeSeriesCollection();

  public ZoomedRollOverTimePlotPanel()
  {
    JFreeChart chart = ChartFactory.createTimeSeriesChart(
        "Roll",
        "Time",
        "Roll [°]",
        dataset,
        false,
        false,
        false);

    onZoomChanged();
    addPanelFor(chart);
  }

  @Override
  protected void onZoomChanged()
  {
    resetDataSeries();
  }

  private void resetDataSeries()
  {
    dataset.removeAllSeries();
    dataset.addSeries(getAccelerationRollTimeSeries());
  }

  private TimeSeries getAccelerationRollTimeSeries()
  {
    return zoomedData.getTimeSeries(
        "roll",
        Data::getPointsWithAcceleration,
        point -> zoomedData.isInSelectedPosition(point, TimeWindowPosition.IN) && point.hasRoll(),
        point -> point.acceleration.roll * 180d / Math.PI);
  }

  @Override
  protected void onDataChanged()
  {
    resetDataSeries();
  }
}

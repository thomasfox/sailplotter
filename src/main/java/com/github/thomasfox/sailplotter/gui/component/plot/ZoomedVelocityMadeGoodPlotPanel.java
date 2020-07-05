package com.github.thomasfox.sailplotter.gui.component.plot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.github.thomasfox.sailplotter.gui.component.panel.TimeWindowPosition;
import com.github.thomasfox.sailplotter.model.DataPoint;

public class ZoomedVelocityMadeGoodPlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final TimeSeriesCollection velocityDataset = new TimeSeriesCollection();

  public ZoomedVelocityMadeGoodPlotPanel()
  {
    JFreeChart chart = ChartFactory.createTimeSeriesChart(
        "Velocity Made Good",
        "Time",
        "VMG [kts]",
        velocityDataset,
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
    velocityDataset.removeAllSeries();
    velocityDataset.addSeries(getVelocityMadeGoodTimeSeries());
  }

  private TimeSeries getVelocityMadeGoodTimeSeries()
  {
    return zoomedData.getLocationTimeSeries(
        "velocity made good",
        TimeWindowPosition.IN,
        this::getVelocityMadeGood);
  }

  private double getVelocityMadeGood(DataPoint point)
  {
    return point.location.velocityFromLatLong
        * Math.cos(point.location.bearingFromLatLong - zoomedData.getData().getAverageWindBearing());
  }

  @Override
  protected void onDataChanged()
  {
    resetDataSeries();
  }
}

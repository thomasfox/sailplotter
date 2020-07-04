package com.github.thomasfox.sailplotter.gui.component.plot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.github.thomasfox.sailplotter.gui.component.panel.TimeWindowPosition;
import com.github.thomasfox.sailplotter.model.Data;

public class ZoomedHeelOverTimePlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final TimeSeriesCollection dataset = new TimeSeriesCollection();

  public ZoomedHeelOverTimePlotPanel(int zoomWindowLocationStartIndex, int zoomWindowLocationSize)
  {
    super(zoomWindowLocationStartIndex, zoomWindowLocationSize);
    JFreeChart chart = ChartFactory.createTimeSeriesChart(
        "Heel",
        "Time",
        "Heel [°]",
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
    dataset.removeAllSeries();
    dataset.addSeries(getAccelerationHeelTimeSeries());
  }

  private TimeSeries getAccelerationHeelTimeSeries()
  {
    return zoomedData.getTimeSeries(
        "heel",
        Data::getPointsWithAcceleration,
        point -> zoomedData.isInSelectedPosition(point, TimeWindowPosition.IN) && point.hasHeel(),
        point -> point.acceleration.heel * 180d / Math.PI);
  }



  @Override
  protected void onDataChanged()
  {
  }
}

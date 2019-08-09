package com.github.thomasfox.sailplotter.gui.component.plot;

import java.awt.Color;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.time.DateRange;
import org.jfree.data.time.TimeSeriesCollection;

import com.github.thomasfox.sailplotter.gui.TimeWindowPosition;
import com.github.thomasfox.sailplotter.model.DataPoint;

public class FullVelocityBearingOverTimePlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final TimeSeriesCollection dataset = new TimeSeriesCollection();

  private final XYPlot plot;

  public FullVelocityBearingOverTimePlotPanel(int zoomWindowLocationStartIndex, int zoomWindowLocationSize)
  {
    super(zoomWindowLocationStartIndex, zoomWindowLocationSize);
    JFreeChart chart = ChartFactory.createTimeSeriesChart(
        "Velocity and Bearing (Full)",
        "Time",
        "Velocity [kts] / Bearing [arcs]",
        dataset,
        false,
        false,
        false);
    plot = (XYPlot) chart.getPlot();
    resetDataSeries();
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
    dataset.addSeries(getVelocityTimeSeries(TimeWindowPosition.BEFORE));
    dataset.addSeries(getVelocityTimeSeries(TimeWindowPosition.IN));
    dataset.addSeries(getVelocityTimeSeries(TimeWindowPosition.AFTER));
    dataset.addSeries(getBearingFromLatLongTimeSeries(TimeWindowPosition.BEFORE));
    dataset.addSeries(getBearingFromLatLongTimeSeries(TimeWindowPosition.IN));
    dataset.addSeries(getBearingFromLatLongTimeSeries(TimeWindowPosition.AFTER));
  }

  @Override
  protected void onDataChanged()
  {
    List<DataPoint> pointsWithLocation = data.getPointsWithLocation();
    if (pointsWithLocation.size() == 0)
    {
      return;
    }
    Range dataRange = new DateRange(
        pointsWithLocation.get(0).time,
        pointsWithLocation.get(pointsWithLocation.size() -1).time);
    plot.getDomainAxis().setRange(dataRange);
    Range valueRange = new DateRange(0, getMaximum(pointsWithLocation, d->d.location.velocityFromLatLong));
    plot.getRangeAxis().setRange(valueRange);
    plot.getRenderer().setSeriesPaint(0, new Color(0x00, 0x00, 0x00));
    plot.getRenderer().setSeriesPaint(1, new Color(0xFF, 0x00, 0x00));
    plot.getRenderer().setSeriesPaint(2, new Color(0x00, 0x00, 0x00));
    plot.getRenderer().setSeriesPaint(3, new Color(0x00, 0x00, 0x00));
    plot.getRenderer().setSeriesPaint(4, new Color(0x00, 0xFF, 0x00));
    plot.getRenderer().setSeriesPaint(5, new Color(0x00, 0x00, 0x00));
  }
}

package com.github.thomasfox.sailplotter.gui.plot;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.time.DateRange;
import org.jfree.data.time.TimeSeriesCollection;

import com.github.thomasfox.sailplotter.gui.TimeWindowPosition;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;

public class FullVelocityBearingOverTimePlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final TimeSeriesCollection fullVelocityBearingOverTimeDataset = new TimeSeriesCollection();

  private final XYPlot fullVelocityBearingOverTimePlot;

  public FullVelocityBearingOverTimePlotPanel(Data data, int zoomWindowLocationStartIndex, int zoomWindowLocationSize)
  {
    super(data, zoomWindowLocationStartIndex, zoomWindowLocationSize);
    JFreeChart fullVelocityBearingOverTimeChart = ChartFactory.createTimeSeriesChart(
        "Velocity and Bearing (Full)",
        "Time",
        "Velocity [kts] / Bearing [arcs]",
        fullVelocityBearingOverTimeDataset,
        false,
        false,
        false);
    fullVelocityBearingOverTimePlot = (XYPlot) fullVelocityBearingOverTimeChart.getPlot();
    onDataChanged();
    onZoomChanged();
    ChartPanel chartPanel = new ChartPanel(fullVelocityBearingOverTimeChart);
    chartPanel.setPreferredSize(null);
    setLayout(new GridLayout(1, 1));
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    add(chartPanel, gridBagConstraints);
  }

  @Override
  protected void onZoomChanged()
  {
    fullVelocityBearingOverTimeDataset.removeAllSeries();
    fullVelocityBearingOverTimeDataset.addSeries(getVelocityTimeSeries(TimeWindowPosition.BEFORE));
    fullVelocityBearingOverTimeDataset.addSeries(getVelocityTimeSeries(TimeWindowPosition.IN));
    fullVelocityBearingOverTimeDataset.addSeries(getVelocityTimeSeries(TimeWindowPosition.AFTER));
    fullVelocityBearingOverTimeDataset.addSeries(getBearingFromLatLongTimeSeries(TimeWindowPosition.BEFORE));
    fullVelocityBearingOverTimeDataset.addSeries(getBearingFromLatLongTimeSeries(TimeWindowPosition.IN));
    fullVelocityBearingOverTimeDataset.addSeries(getBearingFromLatLongTimeSeries(TimeWindowPosition.AFTER));
  }

  @Override
  protected void onDataChanged()
  {
    List<DataPoint> pointsWithLocation = data.getPointsWithLocation();
    Range dataRange = new DateRange(
        pointsWithLocation.get(0).time,
        pointsWithLocation.get(pointsWithLocation.size() -1).time);
    fullVelocityBearingOverTimePlot.getDomainAxis().setRange(dataRange);
    Range valueRange = new DateRange(0, getMaximum(pointsWithLocation, d->d.location.velocityFromLatLong));
    fullVelocityBearingOverTimePlot.getRangeAxis().setRange(valueRange);
    fullVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(0, new Color(0x00, 0x00, 0x00));
    fullVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(1, new Color(0xFF, 0x00, 0x00));
    fullVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(2, new Color(0x00, 0x00, 0x00));
    fullVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(3, new Color(0x00, 0x00, 0x00));
    fullVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(4, new Color(0x00, 0xFF, 0x00));
    fullVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(5, new Color(0x00, 0x00, 0x00));
  }
}

package com.github.thomasfox.sailplotter.gui.component.plot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeriesCollection;

import com.github.thomasfox.sailplotter.gui.component.panel.TimeWindowPosition;

public class ZoomedRelativeBearingPlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final TimeSeriesCollection velocityDataset = new TimeSeriesCollection();

  public ZoomedRelativeBearingPlotPanel()
  {
    JFreeChart chart = ChartFactory.createTimeSeriesChart(
        "Bearing",
        "Time",
        "Bearing [°]",
        velocityDataset,
        false,
        false,
        false);

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
    velocityDataset.removeAllSeries();
    velocityDataset.addSeries(zoomedData.getRelativeBearingInDegreesTimeSeries(TimeWindowPosition.IN));
  }


  @Override
  protected void onDataChanged()
  {
    resetDataSeries();
  }
}

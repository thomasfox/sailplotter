package com.github.thomasfox.sailplotter.gui.component.plot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeriesCollection;

import com.github.thomasfox.sailplotter.gui.component.panel.TimeWindowPosition;

public class ZoomedRollOverTimePlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final TimeSeriesCollection dataset = new TimeSeriesCollection();

  public ZoomedRollOverTimePlotPanel(int zoomWindowLocationStartIndex, int zoomWindowLocationSize)
  {
    super(zoomWindowLocationStartIndex, zoomWindowLocationSize);
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
    dataset.removeAllSeries();
    dataset.addSeries(zoomedData.getAccelerationRollTimeSeries(TimeWindowPosition.IN));
  }

  @Override
  protected void onDataChanged()
  {
  }
}

package com.github.thomasfox.sailplotter.gui.component.plot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.TimeSeriesCollection;

import com.github.thomasfox.sailplotter.gui.component.panel.TimeWindowPosition;

public class ZoomedVelocityPlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final TimeSeriesCollection velocityDataset = new TimeSeriesCollection();

  public ZoomedVelocityPlotPanel(int zoomWindowLocationStartIndex, int zoomWindowLocationSize)
  {
    super(zoomWindowLocationStartIndex, zoomWindowLocationSize);

    JFreeChart chart = ChartFactory.createTimeSeriesChart(
        "Velocity",
        "Time",
        "Velocity [kts]",
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
    velocityDataset.removeAllSeries();
    velocityDataset.addSeries(zoomedData.getVelocityTimeSeries(TimeWindowPosition.IN));
  }


  @Override
  protected void onDataChanged()
  {
  }
}

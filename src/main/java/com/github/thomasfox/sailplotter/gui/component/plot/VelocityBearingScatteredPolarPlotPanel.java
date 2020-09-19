package com.github.thomasfox.sailplotter.gui.component.plot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.github.thomasfox.sailplotter.gui.component.panel.TimeWindowPosition;
import com.github.thomasfox.sailplotter.model.DataPoint;

public class VelocityBearingScatteredPolarPlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final XYSeriesCollection dataset = new XYSeriesCollection();

  public VelocityBearingScatteredPolarPlotPanel()
  {
    JFreeChart chart = ChartFactory.createPolarChart("Velocity over rel. Bearing", dataset, false, false, false);

    PolarPlot plot = (PolarPlot) chart.getPlot();
    PolarScatterRenderer renderer = new PolarScatterRenderer();
    plot.setRenderer(renderer);

    onDataChanged();
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
    if (zoomedData.getData() == null)
    {
      return;
    }
    XYSeries velocity = new XYSeries("velocity");
    for (DataPoint point : zoomedData.getLocationSubset(TimeWindowPosition.IN))
    {
      Double bearing = point.getRelativeBearingAs360Degrees();
      if (bearing != null && point.location.velocityFromLatLong != null)
      {
        velocity.add(bearing, point.location.velocityFromLatLong);
      }
    }
    dataset.addSeries(velocity);
  }

  @Override
  protected void onDataChanged()
  {
    resetDataSeries();
  }
}

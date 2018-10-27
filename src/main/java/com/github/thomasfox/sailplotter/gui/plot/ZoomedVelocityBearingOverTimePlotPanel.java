package com.github.thomasfox.sailplotter.gui.plot;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeriesCollection;

import com.github.thomasfox.sailplotter.gui.TimeWindowPosition;
import com.github.thomasfox.sailplotter.model.Data;

public class ZoomedVelocityBearingOverTimePlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final TimeSeriesCollection dataset = new TimeSeriesCollection();

  private final XYPlot plot;

  public ZoomedVelocityBearingOverTimePlotPanel(Data data, int zoomWindowLocationStartIndex, int zoomWindowLocationSize)
  {
    super(data, zoomWindowLocationStartIndex, zoomWindowLocationSize);
    JFreeChart chart = ChartFactory.createTimeSeriesChart(
        "Velocity and Bearing (Zoom)",
        "Time",
        "Velocity [kts] / Bearing [arcs]",
        dataset,
        false,
        false,
        false);

    plot = (XYPlot) chart.getPlot();
    plot.getRenderer().setSeriesPaint(0, new Color(0xFF, 0x00, 0x00));
    plot.getRenderer().setSeriesPaint(1, new Color(0x00, 0xFF, 0x00));
    ((XYLineAndShapeRenderer) plot.getRenderer()).setSeriesShapesVisible(0, true);
    ((XYLineAndShapeRenderer) plot.getRenderer()).setSeriesShapesVisible(1, true);

    onZoomChanged();
    addPanelFor(chart);
  }

  @Override
  protected void onZoomChanged()
  {
    dataset.removeAllSeries();
    dataset.addSeries(getVelocityTimeSeries(TimeWindowPosition.IN));
    dataset.addSeries(getBearingFromLatLongTimeSeries(TimeWindowPosition.IN));
  }


  @Override
  protected void onDataChanged()
  {
  }
}

package com.github.thomasfox.sailplotter.gui.component.plot;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeriesCollection;

import com.github.thomasfox.sailplotter.gui.TimeWindowPosition;
import com.github.thomasfox.sailplotter.model.Data;

public class ZoomedBearingOverTimePlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final TimeSeriesCollection dataset = new TimeSeriesCollection();

  private final XYPlot plot;

  public ZoomedBearingOverTimePlotPanel(Data data, int zoomWindowLocationStartIndex, int zoomWindowLocationSize)
  {
    super(data, zoomWindowLocationStartIndex, zoomWindowLocationSize);
    JFreeChart chart = ChartFactory.createTimeSeriesChart(
        "Bearing (Zoom)",
        "Time",
        "Bearing [arcs]",
        dataset,
        true,
        false,
        false);
    plot = (XYPlot) chart.getPlot();

    plot.getRenderer().setSeriesPaint(0, new Color(0xFF, 0x00, 0x00));
    plot.getRenderer().setSeriesPaint(1, new Color(0x00, 0xFF, 0x00));
    plot.getRenderer().setSeriesPaint(2, new Color(0x00, 0x00, 0xFF));
    ((XYLineAndShapeRenderer) plot.getRenderer()).setSeriesShapesVisible(0, true);
    ((XYLineAndShapeRenderer) plot.getRenderer()).setSeriesShapesVisible(1, true);
    ((XYLineAndShapeRenderer) plot.getRenderer()).setSeriesShapesVisible(2, true);

    onZoomChanged();
    addPanelFor(chart);
  }

  @Override
  protected void onZoomChanged()
  {
    dataset.removeAllSeries();
    dataset.addSeries(getBearingFromLatLongTimeSeries(TimeWindowPosition.IN));
    dataset.addSeries(getGpsBearingTimeSeries(TimeWindowPosition.IN));
    dataset.addSeries(getCompassBearingTimeSeries(TimeWindowPosition.IN));
  }

  @Override
  protected void onDataChanged()
  {
  }
}

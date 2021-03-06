package com.github.thomasfox.sailplotter.gui.component.plot;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.github.thomasfox.sailplotter.gui.component.panel.TimeWindowPosition;
import com.github.thomasfox.sailplotter.model.Data;

public class ZoomedBearingOverTimePlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final TimeSeriesCollection dataset = new TimeSeriesCollection();

  private final XYPlot plot;

  public ZoomedBearingOverTimePlotPanel()
  {
    JFreeChart chart = ChartFactory.createTimeSeriesChart(
        "Bearing (Zoom)",
        "Time",
        "Bearing [degrees]",
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
    setChart(chart);
  }

  @Override
  protected void onZoomChanged()
  {
    resetDataSeries();
  }

  private void resetDataSeries()
  {
    dataset.removeAllSeries();
    dataset.addSeries(zoomedData.getBearingInDegreesFromLatLongTimeSeries(TimeWindowPosition.IN));
    dataset.addSeries(getGpsBearingInDegreesTimeSeries());
    dataset.addSeries(getCompassBearingInDegreesTimeSeries());
  }

  private TimeSeries getCompassBearingInDegreesTimeSeries()
  {
    return zoomedData.getTimeSeries(
        "compass bearing",
        Data::getPointsWithMagneticField,
        point -> zoomedData.isInSelectedPosition(point, TimeWindowPosition.IN) && point.hasCompassBearing(),
        point -> point.magneticField.getCompassBearingAs360Degrees());
  }

  public TimeSeries getGpsBearingInDegreesTimeSeries()
  {
    return zoomedData.getLocationTimeSeries(
        "gps bearing",
        TimeWindowPosition.IN,
        point -> point.location.getGpsBearingAs360Degrees());
  }



  @Override
  protected void onDataChanged()
  {
    resetDataSeries();
  }
}

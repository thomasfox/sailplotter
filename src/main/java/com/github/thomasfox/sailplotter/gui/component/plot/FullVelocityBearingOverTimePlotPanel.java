package com.github.thomasfox.sailplotter.gui.component.plot;

import java.awt.Color;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.time.DateRange;
import org.jfree.data.time.TimeSeriesCollection;

import com.github.thomasfox.sailplotter.analyze.MinMax;
import com.github.thomasfox.sailplotter.gui.component.panel.TimeWindowPosition;
import com.github.thomasfox.sailplotter.model.DataPoint;

public class FullVelocityBearingOverTimePlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final TimeSeriesCollection velocityDataset = new TimeSeriesCollection();

  private final TimeSeriesCollection bearingDataset = new TimeSeriesCollection();

  private final XYPlot plot;

  public FullVelocityBearingOverTimePlotPanel(int zoomWindowLocationStartIndex, int zoomWindowLocationSize)
  {
    super(zoomWindowLocationStartIndex, zoomWindowLocationSize);

    plot = new XYPlot();
    plot.setDataset(0, velocityDataset);
    plot.setDataset(1, bearingDataset);
    ValueAxis timeAxis = new DateAxis("Time");
    timeAxis.setLowerMargin(0.02);  // reduce the default margins
    timeAxis.setUpperMargin(0.02);
    plot.setDomainAxis(0, timeAxis);
    NumberAxis velocityAxis = new NumberAxis("Velocity [kts]");
    velocityAxis.setAutoRangeIncludesZero(false);
    plot.setRangeAxis(0, velocityAxis);
    NumberAxis bearingAxis = new NumberAxis("Bearing [°]");
    bearingAxis.setAutoRangeIncludesZero(false);
    plot.setRangeAxis(1, bearingAxis);

    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true,
            false);
    plot.setRenderer(renderer);

    plot.mapDatasetToRangeAxis(0, 0);
    plot.mapDatasetToRangeAxis(1, 1);

    JFreeChart chart = new JFreeChart(
        "Velocity and Bearing (Full)",
        JFreeChart.DEFAULT_TITLE_FONT,
        plot,
        false);
    ChartFactory.getChartTheme().apply(chart);

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
    bearingDataset.removeAllSeries();
    velocityDataset.addSeries(zoomedData.getVelocityTimeSeries(TimeWindowPosition.BEFORE));
    velocityDataset.addSeries(zoomedData.getVelocityTimeSeries(TimeWindowPosition.IN));
    velocityDataset.addSeries(zoomedData.getVelocityTimeSeries(TimeWindowPosition.AFTER));
    bearingDataset.addSeries(zoomedData.getBearingInDegreesFromLatLongTimeSeries(TimeWindowPosition.BEFORE));
    bearingDataset.addSeries(zoomedData.getBearingInDegreesFromLatLongTimeSeries(TimeWindowPosition.IN));
    bearingDataset.addSeries(zoomedData.getBearingInDegreesFromLatLongTimeSeries(TimeWindowPosition.AFTER));
  }

  @Override
  protected void onDataChanged()
  {
    List<DataPoint> pointsWithLocation = zoomedData.getPointsWithLocation();
    if (pointsWithLocation.size() == 0)
    {
      return;
    }
    Range dataRange = new DateRange(
        pointsWithLocation.get(0).time,
        pointsWithLocation.get(pointsWithLocation.size() -1).time);
    plot.getDomainAxis().setRange(dataRange);
    Range valueRange = new DateRange(
        0,
        MinMax.getMaximum(pointsWithLocation, d->d.location.velocityFromLatLong));
    plot.getRangeAxis().setRange(valueRange);
    plot.getRenderer().setSeriesPaint(0, new Color(0x00, 0x00, 0x00));
    plot.getRenderer().setSeriesPaint(1, new Color(0xFF, 0x00, 0x00));
    plot.getRenderer().setSeriesPaint(2, new Color(0x00, 0x00, 0x00));
    plot.getRenderer().setSeriesPaint(3, new Color(0x00, 0x00, 0x00));
    plot.getRenderer().setSeriesPaint(4, new Color(0x00, 0xFF, 0x00));
    plot.getRenderer().setSeriesPaint(5, new Color(0x00, 0x00, 0x00));
  }
}

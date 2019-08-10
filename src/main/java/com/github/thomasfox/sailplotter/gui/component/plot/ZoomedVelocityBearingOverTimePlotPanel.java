package com.github.thomasfox.sailplotter.gui.component.plot;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeriesCollection;

import com.github.thomasfox.sailplotter.gui.TimeWindowPosition;

public class ZoomedVelocityBearingOverTimePlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final TimeSeriesCollection velocityDataset = new TimeSeriesCollection();

  private final TimeSeriesCollection bearingDataset = new TimeSeriesCollection();

  private final XYPlot plot;

  public ZoomedVelocityBearingOverTimePlotPanel(int zoomWindowLocationStartIndex, int zoomWindowLocationSize)
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
    velocityAxis.setLabelPaint(Color.RED);
    velocityAxis.setTickLabelPaint(Color.RED);
    plot.setRangeAxis(0, velocityAxis);
    NumberAxis bearingAxis = new NumberAxis("Bearing [°]");
    bearingAxis.setAutoRangeIncludesZero(false);
    plot.setRangeAxis(1, bearingAxis);

    XYLineAndShapeRenderer velocityRenderer = new XYLineAndShapeRenderer(true, false);
    plot.setRenderer(0, velocityRenderer);

    XYLineAndShapeRenderer bearingRenderer = new XYLineAndShapeRenderer(true, false);
    plot.setRenderer(1, bearingRenderer);

    plot.mapDatasetToRangeAxis(0, 0);
    plot.mapDatasetToRangeAxis(1, 1);

    JFreeChart chart = new JFreeChart(
        "Velocity and Bearing (Zoom)",
        JFreeChart.DEFAULT_TITLE_FONT,
        plot,
        false);

    ChartFactory.getChartTheme().apply(chart);
    bearingAxis.setLabelPaint(new Color(0f, 0.5f, 0f));
    bearingAxis.setTickLabelPaint(new Color(0f, 0.5f, 0f));
    velocityAxis.setLabelPaint(Color.RED);
    velocityAxis.setTickLabelPaint(Color.RED);

    velocityRenderer.setSeriesPaint(0, new Color(0xFF, 0x00, 0x00));
    bearingRenderer.setSeriesPaint(0, new Color(0x00, 0xFF, 0x00));
    velocityRenderer.setSeriesShapesVisible(0, true);
    bearingRenderer.setSeriesShapesVisible(0, true);

    onZoomChanged();
    addPanelFor(chart);
  }

  @Override
  protected void onZoomChanged()
  {
    velocityDataset.removeAllSeries();
    bearingDataset.removeAllSeries();
    velocityDataset.addSeries(getVelocityTimeSeries(TimeWindowPosition.IN));
    bearingDataset.addSeries(getBearingInDegreesFromLatLongTimeSeries(TimeWindowPosition.IN));
  }

  @Override
  protected void onDataChanged()
  {
  }
}

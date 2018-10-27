package com.github.thomasfox.sailplotter.gui.plot;

import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeriesCollection;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.gui.TimeWindowPosition;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;

public class FullMapPlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final XYSeriesCollection dataset = new XYSeriesCollection();

  private final XYPlot plot;

  public FullMapPlotPanel(Data data, int zoomWindowLocationStartIndex, int zoomWindowLocationSize)
  {
    super(data, zoomWindowLocationStartIndex, zoomWindowLocationSize);
    JFreeChart chart = ChartFactory.createXYLineChart("Sail Map", "X", "Y", dataset, PlotOrientation.VERTICAL, false, false, false);
    plot = (XYPlot) chart.getPlot();

    onDataChanged();
    onZoomChanged();
    addPanelFor(chart);
  }

  @Override
  protected void onZoomChanged()
  {
    dataset.removeAllSeries();
    DataPoint startPoint = data.getPointsWithLocation().get(0);
    dataset.addSeries(getXySeries(TimeWindowPosition.BEFORE, startPoint.location.getX(), startPoint.location.getY()));
    dataset.addSeries(getXySeries( TimeWindowPosition.IN, startPoint.location.getX(), startPoint.location.getY()));
    dataset.addSeries(getXySeries(TimeWindowPosition.AFTER, startPoint.location.getX(), startPoint.location.getY()));
  }

  @Override
  protected void onDataChanged()
  {
    DataPoint startPoint = data.getPointsWithLocation().get(0);
    Range xRange = new Range(
        getMinimum(data.getPointsWithLocation(), d->d.location.getX()) - startPoint.location.getX(),
        getMaximum(data.getPointsWithLocation(), d->d.location.getX()) - startPoint.location.getX());
    plot.getDomainAxis().setRange(xRange);
    Range yRange = new Range(
        getMinimum(data.getPointsWithLocation(), d->d.location.getY()) - startPoint.location.getY(),
        getMaximum(data.getPointsWithLocation(), d->d.location.getY()) - startPoint.location.getY());
    plot.getRangeAxis().setRange(yRange);
    expandRangesToAspectRatio(plot, Constants.MAP_ASPECT_RATIO);
    plot.getRenderer().setSeriesPaint(0, new Color(0x00, 0x00, 0x00));
    plot.getRenderer().setSeriesPaint(1, new Color(0xFF, 0x00, 0x00));
    plot.getRenderer().setSeriesPaint(2, new Color(0x00, 0x00, 0x00));
  }
}

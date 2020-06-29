package com.github.thomasfox.sailplotter.gui.component.plot;

import java.awt.Color;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeriesCollection;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.gui.component.panel.TimeWindowPosition;
import com.github.thomasfox.sailplotter.model.DataPoint;

public class FullMapPlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final XYSeriesCollection dataset = new XYSeriesCollection();

  private final XYPlot plot;

  public FullMapPlotPanel(int zoomWindowLocationStartIndex, int zoomWindowLocationSize)
  {
    super(zoomWindowLocationStartIndex, zoomWindowLocationSize);
    JFreeChart chart = ChartFactory.createXYLineChart("Sail Map", "X", "Y", dataset, PlotOrientation.VERTICAL, false, false, false);
    plot = (XYPlot) chart.getPlot();

    onZoomChanged();
    addPanelFor(chart);
  }

  @Override
  protected void onZoomChanged()
  {
    dataset.removeAllSeries();
    if (data == null)
    {
      return;
    }
    List<DataPoint> pointsWithLocation = data.getPointsWithLocation();
    if (pointsWithLocation.size() == 0)
    {
      return;
    }
    DataPoint startPoint = pointsWithLocation.get(0);
    dataset.addSeries(getXySeries(TimeWindowPosition.BEFORE,
        p -> (p.location.xyRelativeTo(startPoint.location))));
    dataset.addSeries(getXySeries(TimeWindowPosition.IN,
        p -> (p.location.xyRelativeTo(startPoint.location))));
    dataset.addSeries(getXySeries(TimeWindowPosition.AFTER,
        p -> (p.location.xyRelativeTo(startPoint.location))));
  }

  @Override
  protected void onDataChanged()
  {
    List<DataPoint> pointsWithLocation = data.getPointsWithLocation();
    if (pointsWithLocation.size() == 0)
    {
      return;
    }
    DataPoint startPoint = pointsWithLocation.get(0);
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

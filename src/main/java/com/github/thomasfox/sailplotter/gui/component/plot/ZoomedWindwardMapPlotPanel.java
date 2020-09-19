package com.github.thomasfox.sailplotter.gui.component.plot;

import java.awt.Color;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.gui.component.panel.TimeWindowPosition;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.MapArea;
import com.github.thomasfox.sailplotter.model.vector.TwoDimVector;

public class ZoomedWindwardMapPlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final XYSeriesCollection dataset = new XYSeriesCollection();

  private final XYPlot plot;

  public ZoomedWindwardMapPlotPanel()
  {
    JFreeChart chart = ChartFactory.createXYLineChart("Relative Map Zoom", "windward [m]", "sideward [m]", dataset, PlotOrientation.VERTICAL, false, true, false);
    plot = (XYPlot) chart.getPlot();
    plot.setRenderer(new XYZoomRenderer());
    plot.getRenderer().setSeriesPaint(0, new Color(0xFF, 0x00, 0x00));
    ((XYLineAndShapeRenderer) plot.getRenderer()).setSeriesShapesVisible(0, true);
    ((XYLineAndShapeRenderer) plot.getRenderer()).setBaseToolTipGenerator(new XYTooltipFromLabelGenerator());

    onZoomChanged();
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
    List<DataPoint> pointsWithLocation = zoomedData.getPointsWithLocation();
    if (pointsWithLocation.size() == 0)
    {
      return;
    }
    DataPoint startPoint = pointsWithLocation.get(0);
    dataset.addSeries(getXySeries(
        TimeWindowPosition.IN,
        p -> p.location.xyRelativeTo(startPoint.location)
              .rotate(-zoomedData.getData().getAverageWindBearing())));
    dataset.addSeries(zoomedData.getTackIntersectionSeries(
        TimeWindowPosition.IN,
        l -> l.xyRelativeTo(startPoint.location)
              .rotate(-zoomedData.getData().getAverageWindBearing())));

    updateMapZoomRange();
  }

  private void updateMapZoomRange()
  {
    TwoDimVector offset = zoomedData.getPointsWithLocation().get(0).location.getXY();
    MapArea mapArea = MapArea.calculateFrom(
        zoomedData,
        TimeWindowPosition.IN,
        MapArea.transformLocation(p -> p.subtract(offset).rotate(-zoomedData.getData().getAverageWindBearing())));
    plot.getDomainAxis().setRange(mapArea.getXRangeWithMargin(0.05));
    plot.getRangeAxis().setRange(mapArea.getYRangeWithMargin(0.05));
    expandRangesToAspectRatio(plot, Constants.MAP_ASPECT_RATIO);
  }

  @Override
  protected void onDataChanged()
  {
    resetDataSeries();
  }

}

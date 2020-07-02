package com.github.thomasfox.sailplotter.gui.component.plot;

import java.awt.Color;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.gui.component.panel.TimeWindowPosition;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.MapArea;
import com.github.thomasfox.sailplotter.model.Tack;

public class ZoomedMapPlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final XYSeriesCollection dataset = new XYSeriesCollection();

  private final XYPlot plot;

  public ZoomedMapPlotPanel(int zoomWindowLocationStartIndex, int zoomWindowLocationSize)
  {
    super(zoomWindowLocationStartIndex, zoomWindowLocationSize);
    JFreeChart chart = ChartFactory.createXYLineChart("Sail Map Zoom", "X", "Y", dataset, PlotOrientation.VERTICAL, false, true, false);
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
        p -> (p.location.xyRelativeTo(startPoint.location))));
    dataset.addSeries(getTackIntersectionSeries(zoomedData.getData().getTackList(), TimeWindowPosition.IN, startPoint.location.getX(), startPoint.location.getY()));

    updateMapZoomRange();
  }

  private void updateMapZoomRange()
  {
    MapArea mapArea = MapArea.calculateFrom(zoomedData, TimeWindowPosition.IN);
    plot.getDomainAxis().setRange(mapArea.getXRangeWithMargin(0.05));
    plot.getRangeAxis().setRange(mapArea.getYRangeWithMargin(0.05));
    expandRangesToAspectRatio(plot, Constants.MAP_ASPECT_RATIO);
  }

  @Override
  protected void onDataChanged()
  {
  }

  public XYSeries getTackIntersectionSeries(List<Tack> tacks, TimeWindowPosition position, double xOffset, double yOffset)
  {
    XYSeries series = new XYSeries("XY", false, true);
    for (Tack tack : tacks)
    {
      if (!zoomedData.isInSelectedPosition(tack.start, position)
          && !zoomedData.isInSelectedPosition(tack.end, position))
      {
        continue;
      }
      if (tack.tackStraightLineIntersectionStart != null && tack.tackStraightLineIntersectionEnd != null)
      {
        series.add(
            tack.tackStraightLineIntersectionStart.location.getX() - xOffset,
            tack.tackStraightLineIntersectionStart.location.getY() - yOffset);
        series.add(
            tack.tackStraightLineIntersectionEnd.location.getX() - xOffset,
            tack.tackStraightLineIntersectionEnd.location.getY() - yOffset);
      }
    }
    return series;
  }
}

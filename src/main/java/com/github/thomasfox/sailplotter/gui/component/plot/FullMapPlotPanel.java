package com.github.thomasfox.sailplotter.gui.component.plot;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeriesCollection;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.gui.component.panel.TimeWindowPosition;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.MapArea;

public class FullMapPlotPanel extends AbstractPlotPanel
{
  private static final long serialVersionUID = 1L;

  private final XYSeriesCollection dataset = new XYSeriesCollection();

  private final XYPlot plot;

  public FullMapPlotPanel()
  {
    JFreeChart chart = ChartFactory.createXYLineChart("Map", "north [m]", "east [m]", dataset, PlotOrientation.VERTICAL, false, false, false);
    plot = (XYPlot) chart.getPlot();

    resetDataSeries();
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
    MapArea mapArea = MapArea.calculateFrom(zoomedData, null);
    plot.getDomainAxis().setRange(mapArea.getXRangeWithMargin(0.05));
    plot.getRangeAxis().setRange(mapArea.getYRangeWithMargin(0.05));
    ChartRenderingInfo renderingInfo = chartPanel.getChartRenderingInfo();
    if (renderingInfo != null)
    {
      Rectangle2D dataArea = renderingInfo.getPlotInfo().getDataArea();
      expandRangesToAspectRatio(plot, dataArea.getWidth() / dataArea.getHeight());
    }
    else
    {
      expandRangesToAspectRatio(plot, Constants.MAP_ASPECT_RATIO);
    }
    plot.getRenderer().setSeriesPaint(0, new Color(0x00, 0x00, 0x00));
    plot.getRenderer().setSeriesPaint(1, new Color(0xFF, 0x00, 0x00));
    plot.getRenderer().setSeriesPaint(2, new Color(0x00, 0x00, 0x00));
    resetDataSeries();
  }
}

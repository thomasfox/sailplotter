package com.github.thomasfox.sailplotter.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.chart.renderer.DefaultPolarItemRenderer;
import org.jfree.data.xy.XYDataset;

public class PolarScatterRenderer extends DefaultPolarItemRenderer
{
  private static final long serialVersionUID = 1L;

  @Override
  public void drawSeries(
      Graphics2D graphics,
      Rectangle2D dataArea,
      PlotRenderingInfo info,
      PolarPlot plot,
      XYDataset dataset,
      int seriesIndex)
  {
      int numPoints = dataset.getItemCount(seriesIndex);
      for (int i = 0; i < numPoints; i++)
      {
          double theta = dataset.getXValue(seriesIndex, i);
          double radius = dataset.getYValue(seriesIndex, i);
          Point p = plot.translateValueThetaRadiusToJava2D(
              theta, radius, dataArea);
          Ellipse2D el = new Ellipse2D.Double(p.x, p.y, 5, 5);
          graphics.setColor(Color.RED);
          graphics.fill(el);
          graphics.draw(el);
      }
  }

}
package com.github.thomasfox.sailplotter.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class XYZoomRenderer extends XYLineAndShapeRenderer
{
  /** SerialVersionUID. */
  private static final long serialVersionUID = 1L;

  private final ThreadLocal<XYDataset> currentDataset = new ThreadLocal<>();

  public XYZoomRenderer()
  {
    setUseFillPaint(true);
    setUseOutlinePaint(true);
  }

  @Override
  public Paint getItemFillPaint(int seriesIndex, int itemIndex)
  {
    return getItemOutlinePaint(seriesIndex, itemIndex);
  }

  @Override
  public Paint getItemOutlinePaint(int seriesIndex, int itemIndex)
  {
    XYDataset datasetUncast = currentDataset.get();
    if (!(datasetUncast instanceof XYSeriesCollection))
    {
      return super.getItemPaint(seriesIndex, itemIndex);
    }
    XYSeriesCollection dataset = (XYSeriesCollection) datasetUncast;
    XYSeries series = dataset.getSeries(seriesIndex);
    XYDataItem dataItem = series.getDataItem(itemIndex);
    if (!(dataItem instanceof XYSailDataItem))
    {
      return super.getItemPaint(seriesIndex, itemIndex);
    }
    XYSailDataItem xySailDataItem = (XYSailDataItem) dataItem;
    if (xySailDataItem.isStartOfTack() || xySailDataItem.isEndOfTack())
    {
      return Color.BLUE;
    }
    else if (xySailDataItem.isTackMainPartLimit())
    {
      return Color.BLACK;
    }
    else
    {
      return super.getItemPaint(seriesIndex, itemIndex);
    }
  }

  @Override
  public void drawItem(Graphics2D g2, XYItemRendererState state,
      Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
      ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset, int series,
      int item, CrosshairState crosshairState, int pass)
  {
    try
    {
      currentDataset.set(dataset);
      super.drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis, dataset,
          series, item, crosshairState, pass);
    }
    finally
    {
      currentDataset.remove();
    }
  }


}

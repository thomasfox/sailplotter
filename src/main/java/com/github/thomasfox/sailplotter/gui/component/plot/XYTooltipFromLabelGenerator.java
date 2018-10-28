package com.github.thomasfox.sailplotter.gui.component.plot;

import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class XYTooltipFromLabelGenerator extends StandardXYToolTipGenerator
{
  /** serialVersionUID. */
  private static final long serialVersionUID = 1L;

  @Override
  public String generateToolTip(XYDataset dataset, int series, int item)
  {
    if (!(dataset instanceof XYSeriesCollection))
    {
      return super.generateLabelString(dataset, series, item);
    }
    XYSeries xySeries = ((XYSeriesCollection) dataset).getSeries(series);
    XYDataItem dataItem = xySeries.getDataItem(item);
    if (!(dataItem instanceof XYSailDataItem))
    {
      return super.generateToolTip(dataset, series, item);
    }
    return dataItem.toString();
  }
}

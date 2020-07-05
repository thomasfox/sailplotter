package com.github.thomasfox.sailplotter.gui.component.plot;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Function;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.XYSeries;

import com.github.thomasfox.sailplotter.gui.component.panel.TimeWindowPosition;
import com.github.thomasfox.sailplotter.gui.component.panel.ZoomPanelChangeEvent;
import com.github.thomasfox.sailplotter.listener.DataChangeListener;
import com.github.thomasfox.sailplotter.listener.ZoomChangeListener;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.Tack;
import com.github.thomasfox.sailplotter.model.ZoomedData;
import com.github.thomasfox.sailplotter.model.vector.TwoDimVector;

public abstract class AbstractPlotPanel extends JPanel implements DataChangeListener, ZoomChangeListener
{
  private static final long serialVersionUID = 1L;

  protected ZoomedData zoomedData;

  protected AbstractPlotPanel()
  {
    zoomedData = new ZoomedData(null, 0, 1);
  }

  protected void addPanelFor(JFreeChart chart)
  {
    ChartPanel chartPanel = new ChartPanel(chart);
    setLayout(new GridLayout(1, 1));
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1;
    gridBagConstraints.weighty = 1;
    add(chartPanel, gridBagConstraints);
  }

  @Override
  public void dataChanged(Data data)
  {
    this.zoomedData.setData(data);
    onDataChanged();
  }

  protected abstract void onDataChanged();

  @Override
  public void zoomChanged(ZoomPanelChangeEvent e)
  {
    this.zoomedData.zoomChanged(e);
    onZoomChanged();
  }

  protected abstract void onZoomChanged();

  public TimeSeries getZoomDisplaySeries(List<DataPoint> data)
  {
    TimeSeries series = new TimeSeries("velocity");
    if (data == null)
    {
      return series;
    }
    Millisecond startValue = zoomedData.getStartMillisecond();
    Millisecond endValue = zoomedData.getEndMillisecond();
    series.addOrUpdate(startValue, 2);
    series.addOrUpdate(endValue, 2);
    return series;
  }

  public XYSeries getXySeries(
      TimeWindowPosition position,
      Function<DataPoint, TwoDimVector> xyProvider)
  {
    XYSeries series = new XYSeries("XY" + position, false, true);
    if (zoomedData.getData() == null)
    {
      return series;
    }
    List<Tack> tackList = zoomedData.getData().getTackList();
    if (tackList == null || tackList.size() == 0)
    {
      return series;
    }
    int tackIndex = 0;
    Tack containingTack = tackList.get(tackIndex);
    for (DataPoint point : zoomedData.getLocationSubset(position))
    {
      while (containingTack.endOfTackDataPointIndex < point.index
          && tackIndex < zoomedData.getData().getTackList().size() - 1)
      {
        ++tackIndex;
        containingTack = zoomedData.getData().getTackList().get(tackIndex);
      }
      TwoDimVector xy = xyProvider.apply(point);
      XYSailDataItem item = new XYSailDataItem(xy.x, xy.y, point.getXYLabel());
      if (containingTack.startOfTackDataPointIndex == point.index)
      {
        item.setStartOfTack(tackIndex);
      }
      else if (containingTack.endOfTackDataPointIndex == point.index)
      {
        item.setEndOfTack(tackIndex);
      }
      DataPoint afterStartManeuver = containingTack.getAfterStartManeuver();
      DataPoint bevoreEndManeuver = containingTack.getBeforeEndManeuver();

      if ((afterStartManeuver != null && afterStartManeuver.index == point.index)
          || (bevoreEndManeuver != null && bevoreEndManeuver.index == point.index))
      {
        item.setTackMainPartLimit(true);
      }

      series.add(item);
    }
    return series;
  }

  public void expandRangesToAspectRatio(XYPlot plot, double aspectRatio)
  {
    Range xRange = plot.getDomainAxis().getRange();
    Range yRange = plot.getRangeAxis().getRange();
    if (xRange.getLength() > aspectRatio * yRange.getLength())
    {
      yRange = new Range(
          yRange.getCentralValue() - 0.5d * xRange.getLength() / aspectRatio,
          yRange.getCentralValue() + 0.5d * xRange.getLength() / aspectRatio);
      plot.getRangeAxis().setRange(yRange);
    }
    else
    {
      xRange = new Range(
          xRange.getCentralValue() - 0.5d * yRange.getLength() * aspectRatio,
          xRange.getCentralValue() + 0.5d * yRange.getLength() * aspectRatio);
      plot.getDomainAxis().setRange(xRange);
    }
  }
}

package com.github.thomasfox.sailplotter.gui.plot;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.gui.TimeWindowPosition;
import com.github.thomasfox.sailplotter.gui.XYSailDataItem;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.Tack;

public abstract class AbstractPlotPanel extends JPanel
{
  private static final long serialVersionUID = 1L;

  protected Data data;

  private int zoomWindowLocationStartIndex;

  private int zoomWindowLocationSize;

  protected AbstractPlotPanel(Data data, int zoomWindowLocationStartIndex, int zoomWindowLocationSize)
  {
    this.data = data;
    this.zoomWindowLocationStartIndex = zoomWindowLocationStartIndex;
    this.zoomWindowLocationSize = zoomWindowLocationSize;
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

  public void dataChanged(Data data)
  {
    this.data = data;
    onDataChanged();
  }

  protected abstract void onDataChanged();

  public void zoomChanged(int zoomWindowLocationStartIndex, int zoomWindowLocationSize)
  {
    this.zoomWindowLocationStartIndex = zoomWindowLocationStartIndex;
    this.zoomWindowLocationSize = zoomWindowLocationSize;
    onZoomChanged();
  }

  protected abstract void onZoomChanged();

  protected double getMaximum(List<DataPoint> data, Function<DataPoint, Double> pointFunction)
  {
    double maxValue = Double.MIN_VALUE;
    for (DataPoint dataPoint : data)
    {
      Double pointValue = pointFunction.apply(dataPoint);
      if (pointValue != null && pointValue > maxValue)
      {
        maxValue = pointValue;
      }
    }
    return maxValue;
  }

  protected double getMinimum(List<DataPoint> data, Function<DataPoint, Double> pointFunction)
  {
    double minValue = Double.MAX_VALUE;
    for (DataPoint dataPoint : data)
    {
      Double pointValue = pointFunction.apply(dataPoint);
      if (pointValue != null && pointValue < minValue)
      {
        minValue = pointValue;
      }
    }
    return minValue;
  }

  public TimeSeries getVelocityTimeSeries(TimeWindowPosition position)
  {
    TimeSeries series = new TimeSeries("velocity");
    for (DataPoint point : getLocationSubset(position))
    {
      series.addOrUpdate(point.getMillisecond(), point.location.velocityFromLatLong);
    }
    return series;
  }

  public TimeSeries getBearingFromLatLongTimeSeries(TimeWindowPosition position)
  {
    TimeSeries series = new TimeSeries("bearing");
    for (DataPoint point : getLocationSubset(position))
    {
      series.addOrUpdate(point.getMillisecond(), point.location.bearingFromLatLong);
    }
    return series;
  }

  public TimeSeries getGpsBearingTimeSeries(TimeWindowPosition position)
  {
    TimeSeries series = new TimeSeries("gps bearing");
    for (DataPoint point : getLocationSubset(position))
    {
      series.addOrUpdate(point.getMillisecond(), point.location.bearing);
    }
    return series;
  }

  public TimeSeries getCompassBearingTimeSeries(TimeWindowPosition position)
  {
    TimeSeries series = new TimeSeries("compass bearing");
    for (DataPoint point : data.getAllPoints())
    {
      if (isInSelectedPosition(point, position) && point.hasMagneticField() && point.magneticField.compassBearing != null)
      {
        series.addOrUpdate(point.getMillisecond(), point.magneticField.compassBearing);
      }
    }
    return series;
  }

  public TimeSeries getZoomDisplaySeries(List<DataPoint> data)
  {
    Millisecond startValue = data.get(getLocationDataStartIndex()).getMillisecond();
    Millisecond endValue = data.get(getLocationDataEndIndex()).getMillisecond();
    TimeSeries series = new TimeSeries("velocity");
    series.addOrUpdate(startValue, 2);
    series.addOrUpdate(endValue, 2);
    return series;
  }

  public XYSeries getXySeries(TimeWindowPosition position, double xOffset, double yOffset)
  {
    XYSeries series = new XYSeries("XY" + position, false, true);
    int tackIndex = 0;
    Tack containingTack = data.getTackList().get(tackIndex);
    for (DataPoint point : getLocationSubset(position))
    {
      while (containingTack.endOfTackDataPointIndex < point.index && tackIndex < data.getTackList().size() - 1)
      {
        ++tackIndex;
        containingTack = data.getTackList().get(tackIndex);
      }
      XYSailDataItem item = new XYSailDataItem(point.location.getX() - xOffset, point.location.getY() - yOffset, point.getXYLabel());
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

  List<DataPoint> getLocationSubset(TimeWindowPosition position)
  {
    List<DataPoint> result = new ArrayList<>();
    for (DataPoint point : data.getPointsWithLocation())
    {
      if (!isInSelectedPosition(point, position))
      {
        continue;
      }

      result.add(point);
    }
    return result;
  }

  protected boolean isInSelectedPosition(DataPoint point, TimeWindowPosition position)
  {
    if (position == TimeWindowPosition.BEFORE && point.getLocalDateTime().isAfter(getLocationDataStartTime()))
    {
      return false;
    }
    if (position == TimeWindowPosition.IN
        && (!point.getLocalDateTime().isAfter(getLocationDataStartTime())
            || !point.getLocalDateTime().isBefore(getLocationDataEndTime())))
    {
      return false;
    }
    if (position == TimeWindowPosition.AFTER && point.getLocalDateTime().isBefore(getLocationDataEndTime()))
    {
      return false;
    }
    return true;
  }

  public int getLocationDataStartIndex()
  {
    return zoomWindowLocationStartIndex;
  }

  public int getLocationDataEndIndex()
  {
    int startIndex = getLocationDataStartIndex();
    int result = startIndex + zoomWindowLocationSize * (data.getPointsWithLocation().size() - 1) / Constants.NUMER_OF_ZOOM_TICKS;
    result = Math.min(result, (data.getPointsWithLocation().size() - 1));
    return result;
  }

  public LocalDateTime getLocationDataStartTime()
  {
    return data.getPointsWithLocation().get(getLocationDataStartIndex()).getLocalDateTime();
  }

  public LocalDateTime getLocationDataEndTime()
  {
    return data.getPointsWithLocation().get(getLocationDataEndIndex()).getLocalDateTime();
  }
}

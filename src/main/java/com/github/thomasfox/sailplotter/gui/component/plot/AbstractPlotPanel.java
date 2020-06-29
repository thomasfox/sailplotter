package com.github.thomasfox.sailplotter.gui.component.plot;

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
import com.github.thomasfox.sailplotter.gui.component.panel.TimeWindowPosition;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.Tack;
import com.github.thomasfox.sailplotter.model.vector.TwoDimVector;

public abstract class AbstractPlotPanel extends JPanel
{
  private static final long serialVersionUID = 1L;

  protected Data data;

  private int zoomWindowLocationStartIndex;

  private int zoomWindowLocationSize;

  protected AbstractPlotPanel(int zoomWindowLocationStartIndex, int zoomWindowLocationSize)
  {
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

  public TimeSeries getBearingInDegreesFromLatLongTimeSeries(TimeWindowPosition position)
  {
    TimeSeries series = new TimeSeries("bearing from pos");
    for (DataPoint point : getLocationSubset(position))
    {
      series.addOrUpdate(point.getMillisecond(), point.location.getBearingFromLatLongAs360Degrees());
    }
    return series;
  }

  public TimeSeries getGpsBearingInDegreesTimeSeries(TimeWindowPosition position)
  {
    TimeSeries series = new TimeSeries("gps bearing");
    for (DataPoint point : getLocationSubset(position))
    {
      series.addOrUpdate(point.getMillisecond(), point.location.getGpsBearingAs360Degrees());
    }
    return series;
  }

  public TimeSeries getCompassBearingInDegreesTimeSeries(TimeWindowPosition position)
  {
    TimeSeries series = new TimeSeries("compass bearing");
    if (data == null)
    {
      return series;
    }
    for (DataPoint point : data.getAllPoints())
    {
      if (isInSelectedPosition(point, position) && point.hasMagneticField() && point.magneticField.compassBearing != null)
      {
        series.addOrUpdate(point.getMillisecond(), point.magneticField.getCompassBearingAs360Degrees());
      }
    }
    return series;
  }

  public TimeSeries getAccelerationHeelTimeSeries(TimeWindowPosition position)
  {
    TimeSeries series = new TimeSeries("heel");
    if (data == null)
    {
      return series;
    }
    for (DataPoint point : data.getAllPoints())
    {
      if (isInSelectedPosition(point, position) && point.hasAcceleration() && point.acceleration.heel != null)
      {
        series.addOrUpdate(point.getMillisecond(), point.acceleration.heel * 180d / Math.PI);
      }
    }
    return series;
  }

  public TimeSeries getAccelerationRollTimeSeries(TimeWindowPosition position)
  {
    TimeSeries series = new TimeSeries("roll");
    if (data == null)
    {
      return series;
    }
    for (DataPoint point : data.getAllPoints())
    {
      if (isInSelectedPosition(point, position) && point.hasAcceleration() && point.acceleration.roll != null)
      {
        series.addOrUpdate(point.getMillisecond(), point.acceleration.roll * 180d / Math.PI);
      }
    }
    return series;
  }

  public TimeSeries getMagneticFieldTimeSeries(int coordinateIndex, TimeWindowPosition position)
  {
    TimeSeries series = new TimeSeries("magnetic Field");
    if (data == null)
    {
      return series;
    }
    for (DataPoint point : data.getPointsWithMagneticField())
    {
      if (isInSelectedPosition(point, position))
      {
        series.addOrUpdate(point.getMillisecond(), point.magneticField.getByIndex(coordinateIndex));
      }
    }
    return series;
  }

  public TimeSeries getAccelerationTimeSeries(int coordinateIndex, TimeWindowPosition position)
  {
    TimeSeries series = new TimeSeries("acceleration");
    if (data == null)
    {
      return series;
    }
    for (DataPoint point : data.getPointsWithAcceleration())
    {
      if (isInSelectedPosition(point, position))
      {
        series.addOrUpdate(point.getMillisecond(), point.acceleration.getByIndex(coordinateIndex));
      }
    }
    return series;
  }


  public TimeSeries getZoomDisplaySeries(List<DataPoint> data)
  {
    TimeSeries series = new TimeSeries("velocity");
    if (data == null)
    {
      return series;
    }
    Millisecond startValue = data.get(getLocationDataStartIndex()).getMillisecond();
    Millisecond endValue = data.get(getLocationDataEndIndex()).getMillisecond();
    series.addOrUpdate(startValue, 2);
    series.addOrUpdate(endValue, 2);
    return series;
  }

  public XYSeries getXySeries(
      TimeWindowPosition position,
      Function<DataPoint, TwoDimVector> xyProvider)
  {
    XYSeries series = new XYSeries("XY" + position, false, true);
    if (data == null)
    {
      return series;
    }
    List<Tack> tackList = data.getTackList();
    if (tackList == null || tackList.size() == 0)
    {
      return series;
    }
    int tackIndex = 0;
    Tack containingTack = tackList.get(tackIndex);
    for (DataPoint point : getLocationSubset(position))
    {
      while (containingTack.endOfTackDataPointIndex < point.index && tackIndex < data.getTackList().size() - 1)
      {
        ++tackIndex;
        containingTack = data.getTackList().get(tackIndex);
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

  List<DataPoint> getLocationSubset(TimeWindowPosition position)
  {
    List<DataPoint> result = new ArrayList<>();
    if (data == null)
    {
      return result;
    }

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

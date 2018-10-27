package com.github.thomasfox.sailplotter.gui.plot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.swing.JPanel;

import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.gui.TimeWindowPosition;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;

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

  private boolean isInSelectedPosition(DataPoint point, TimeWindowPosition position)
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

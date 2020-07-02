package com.github.thomasfox.sailplotter.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.gui.component.panel.TimeWindowPosition;
import com.github.thomasfox.sailplotter.listener.ZoomChangeListener;

public class ZoomedData implements ZoomChangeListener
{
  private Data data;

  private int zoomWindowLocationStartIndex;

  private int zoomWindowLocationSize;

  public ZoomedData(Data data, int zoomWindowLocationStartIndex,
      int zoomWindowLocationSize)
  {
    this.data = data;
    this.zoomWindowLocationStartIndex = zoomWindowLocationStartIndex;
    this.zoomWindowLocationSize = zoomWindowLocationSize;
  }

  public Data getData()
  {
    return data;
  }

  public void setData(Data data)
  {
    this.data = data;
  }

  public int getZoomWindowLocationStartIndex()
  {
    return zoomWindowLocationStartIndex;
  }

  public int getZoomWindowLocationSize()
  {
    return zoomWindowLocationSize;
  }

  public int getLocationDataStartIndex()
  {
    return zoomWindowLocationStartIndex;
  }

  public int getLocationDataEndIndex()
  {
    int startIndex = getLocationDataStartIndex();
    int result = startIndex
        + zoomWindowLocationSize * (data.getPointsWithLocation().size() - 1) / Constants.NUMER_OF_ZOOM_TICKS;
    result = Math.min(result, (data.getPointsWithLocation().size() - 1));
    return result;
  }


  @Override
  public void zoomChanged(int zoomWindowLocationStartIndex, int zoomWindowLocationSize)
  {
    this.zoomWindowLocationStartIndex = zoomWindowLocationStartIndex;
    this.zoomWindowLocationSize = zoomWindowLocationSize;
  }

  public LocalDateTime getLocationDataStartTime()
  {
    return data.getPointsWithLocation().get(getLocationDataStartIndex()).getLocalDateTime();
  }

  public LocalDateTime getLocationDataEndTime()
  {
    return data.getPointsWithLocation().get(getLocationDataEndIndex()).getLocalDateTime();
  }

  public List<DataPoint> getPointsWithLocation()
  {
    return data.getPointsWithLocation();
  }

  public boolean isInSelectedPosition(DataPoint point, TimeWindowPosition position)
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

  public List<DataPoint> getLocationSubset(TimeWindowPosition position)
  {
    if (data == null)
    {
      return new ArrayList<>();
    }

    if (position == null)
    {
      return new ArrayList<>(data.getPointsWithLocation());
    }

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

  public Millisecond getStartMillisecond()
  {
    return data.get(getLocationDataStartIndex()).getMillisecond();
  }

  public Millisecond getEndMillisecond()
  {
    return data.get(getLocationDataEndIndex()).getMillisecond();
  }
}

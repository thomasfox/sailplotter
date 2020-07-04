package com.github.thomasfox.sailplotter.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.XYSeries;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.gui.component.panel.TimeWindowPosition;
import com.github.thomasfox.sailplotter.listener.ZoomChangeListener;
import com.github.thomasfox.sailplotter.model.vector.TwoDimVector;

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
        + zoomWindowLocationSize * (data.getPointsWithLocation().size() - 1) / Constants.NUMBER_OF_ZOOM_TICKS;
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
    if (position == null)
    {
      return true;
    }
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

  public TimeSeries getTimeSeries(
      String name,
      Function<Data, List<DataPoint>> pointProvider,
      Predicate<DataPoint> filter,
      Function<DataPoint, Double> mapper)
  {
    TimeSeries series = new TimeSeries(name);
    if (data == null)
    {
      return series;
    }
    for (DataPoint point : pointProvider.apply(data))
    {
      if (filter.test(point))
      {
        series.addOrUpdate(point.getMillisecond(), mapper.apply(point));
      }
    }
    return series;
  }

  public TimeSeries getLocationTimeSeries(
      String name,
      TimeWindowPosition position,
      Function<DataPoint, Double> mapper)
  {
    TimeSeries series = new TimeSeries("velocity");
    for (DataPoint point : getLocationSubset(position))
    {
      series.addOrUpdate(point.getMillisecond(), mapper.apply(point));
    }
    return series;
  }

  public TimeSeries getVelocityTimeSeries(TimeWindowPosition position)
  {
    return getLocationTimeSeries("velocity", position, point -> point.location.velocityFromLatLong);
  }

  public TimeSeries getBearingInDegreesFromLatLongTimeSeries(TimeWindowPosition position)
  {
    return getLocationTimeSeries(
        "bearing from pos",
        position,
        point -> point.location.getBearingFromLatLongAs360Degrees());
  }

  public XYSeries getTackIntersectionSeries(
      TimeWindowPosition position,
      Function<Location, TwoDimVector> xyProvider)
  {
    XYSeries series = new XYSeries("XY", false, true);
    for (Tack tack : data.getTackList())
    {
      if (!isInSelectedPosition(tack.start, position)
          && !isInSelectedPosition(tack.end, position))
      {
        continue;
      }
      if (tack.tackStraightLineIntersectionStart != null && tack.tackStraightLineIntersectionEnd != null)
      {
        TwoDimVector start = xyProvider.apply(tack.tackStraightLineIntersectionStart.location);
        TwoDimVector end = xyProvider.apply(tack.tackStraightLineIntersectionEnd.location);
        series.add(start.x, start.y);
        series.add(end.x, end.y);
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

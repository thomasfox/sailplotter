package com.github.thomasfox.sailplotter.model;

import java.util.List;
import java.util.function.UnaryOperator;

import org.jfree.data.Range;

import com.github.thomasfox.sailplotter.analyze.MinMax;
import com.github.thomasfox.sailplotter.gui.component.panel.TimeWindowPosition;
import com.github.thomasfox.sailplotter.model.vector.TwoDimVector;

/**
 * An rectangular area in a map which defines the viewable section of a map.
 */
public class MapArea
{
  public double minimumX;

  public double maximumX;

  public double minimumY;

  public double maximumY;

  public static MapArea calculateFrom(ZoomedData zoomedData, TimeWindowPosition filter)
  {
    TwoDimVector offset = zoomedData.getPointsWithLocation().get(0).location.getXY();
    return MapArea.calculateFrom(zoomedData, filter, transformLocation(xy -> xy.subtract(offset)));
  }

  public static MapArea calculateFrom(
      ZoomedData zoomedData,
      TimeWindowPosition filter,
      UnaryOperator<DataPoint> transformation)
  {
    MapArea result = new MapArea();
    List<DataPoint> dataSubset = zoomedData.getLocationSubset(filter);
    result.minimumX = MinMax.getMinimum(dataSubset, d->transformation.apply(d).location.getX());
    result.maximumX = MinMax.getMaximum(dataSubset, d->transformation.apply(d).location.getX());
    if (result.maximumX - result.minimumX < 1)
    {
      double offsetX = transformation.apply(zoomedData.getPointsWithLocation().get(0)).location.getX();
      result.minimumX = -1 + offsetX;
      result.maximumX = 1 + offsetX;
    }

    result.minimumY = MinMax.getMinimum(dataSubset, d->transformation.apply(d).location.getY());
    result.maximumY = MinMax.getMaximum(dataSubset, d->transformation.apply(d).location.getY());
    if (result.maximumY - result.minimumY < 1)
    {
      double offsetY = transformation.apply(zoomedData.getPointsWithLocation().get(0)).location.getY();
      result.minimumY = -1 + offsetY;
      result.maximumY = 1 + offsetY;
    }
    return result;
  }

  public static UnaryOperator<DataPoint> transformLocation(UnaryOperator<TwoDimVector> transformation)
  {
    return new UnaryOperator<DataPoint>()
    {

      @Override
      public DataPoint apply(DataPoint dataPoint)
      {
        DataPoint result = new DataPoint(dataPoint);
        result.location = Location.fromXY(transformation.apply(dataPoint.location.getXY()));
        return result;
      }
    };
  }

  public Range getXRangeWithMargin(double margin)
  {
    return new Range(
        minimumX - (maximumX - minimumX) * margin,
        maximumX + (maximumX - minimumX) * margin);
  }

  public Range getYRangeWithMargin(double margin)
  {
    return new Range(
        minimumY - (maximumY - minimumY) * margin,
        maximumY + (maximumY - minimumY) * margin);
  }
}

package com.github.thomasfox.sailplotter.analyze;

import java.util.List;
import java.util.function.Function;

public class MinMax
{
  public static <T> double getMinimum(List<T> data, Function<T, Double> pointFunction)
  {
    double minValue = Double.MAX_VALUE;
    for (T dataPoint : data)
    {
      Double pointValue = pointFunction.apply(dataPoint);
      if (pointValue != null && pointValue < minValue)
      {
        minValue = pointValue;
      }
    }
    return minValue;
  }

  public static <T> double getMaximum(List<T> data, Function<T, Double> pointFunction)
  {
    double maxValue = -Double.MAX_VALUE;
    for (T dataPoint : data)
    {
      Double pointValue = pointFunction.apply(dataPoint);
      if (pointValue != null && pointValue > maxValue)
      {
        maxValue = pointValue;
      }
    }
    return maxValue;
  }
}

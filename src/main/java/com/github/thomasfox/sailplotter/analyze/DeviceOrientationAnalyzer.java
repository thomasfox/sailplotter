package com.github.thomasfox.sailplotter.analyze;

import java.util.List;

import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.vector.ThreeDimVector;
import com.github.thomasfox.sailplotter.model.vector.TwoDimVector;

public class DeviceOrientationAnalyzer
{
  public Data analyze(Data data)
  {
    // assuming average is upright position of boat
    ThreeDimVector uprightAcceleration = getAverageAcceleration(data.getAllPoints());
    if (uprightAcceleration.length() < 0.1)
    {
      // we have no data or we are not on earth or we have no defined upright position
      return data;
    }
    // for naming sake, assume z direction is approximately up
    ThreeDimVector xHorizontal = new ThreeDimVector(uprightAcceleration.z, 0d, -uprightAcceleration.x).normalize();
    ThreeDimVector yHorizontal = uprightAcceleration.crossProduct(xHorizontal).normalize();

    double relativeBearingInArcs = getMaximumOfRelativeBearingOfCompassToGpsInArcs(data.getAllPoints(), xHorizontal, yHorizontal);

    return data;
  }

  private ThreeDimVector getAverageAcceleration(List<DataPoint> points)
  {
    ThreeDimVector averageAcceleration = new ThreeDimVector(0d, 0d, 0d);
    int accelerationCount = 0;

    for (int i = 1; i < points.size() - 1; ++i)
    {
      DataPoint point = points.get(i);
      if (point.hasAcceleration())
      {
        averageAcceleration.add(point.acceleration);
        accelerationCount++;
      }
    }
    if (accelerationCount > 0)
    {
      averageAcceleration.multiplyBy(1d / accelerationCount);
    }
    return averageAcceleration;
  }

  private Double getMaximumOfRelativeBearingOfCompassToGpsInArcs(
      List<DataPoint> points,
      ThreeDimVector xHorizontal,
      ThreeDimVector yHorizontal)
  {
    int histSize = 10;
    int[] bearingHistogram = new int[histSize];
    for (int i = 1; i < points.size() - 1; ++i)
    {
      DataPoint point = points.get(i);
      if (point.hasMagneticField() && point.location != null && point.location.bearing != null)
      {
        TwoDimVector horizontalMagneticField = new TwoDimVector(
            xHorizontal.scalarProduct(point.magneticField),
            yHorizontal.scalarProduct(point.magneticField));
        double compassBearing = new Double(horizontalMagneticField.getBearingToXInArcs());
        int relativeBearingForHist = new Double(histSize * (compassBearing - point.location.bearing) / 2 / Math.PI).intValue();
        if (relativeBearingForHist < 0)
        {
          relativeBearingForHist += histSize;
        }
        bearingHistogram[relativeBearingForHist]++;
      }
    }
    Integer relativeBearing = null;
    int maxBearingCount = 0;
    for (int bearing = 0; bearing < histSize; bearing++)
    {
      if (bearingHistogram[bearing] > maxBearingCount)
      {
        maxBearingCount = bearingHistogram[bearing];
        relativeBearing = bearing;
      }
    }
    return relativeBearing * 2 * Math.PI / 1000;
  }
}

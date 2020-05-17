package com.github.thomasfox.sailplotter;

import com.github.thomasfox.sailplotter.model.Acceleration;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.Location;
import com.github.thomasfox.sailplotter.model.MagneticField;
import com.github.thomasfox.sailplotter.model.vector.ThreeDimVector;

public class TestData
{
  public static Data givenAccelerationDataIs(ThreeDimVector... accelerationData)
  {
    Data data = new Data();
    for (ThreeDimVector acceleration : accelerationData)
    {
      DataPoint point = new DataPoint(0);
      point.acceleration = new Acceleration(acceleration.x, acceleration.y, acceleration.z);
      data.add(point);
    }
    return data;
  }

  public static void givenMagneticFieldIs(Data data, MagneticField... magenticFields)
  {
    int i = 0;
    for (DataPoint point : data.getAllPoints())
    {
      point.magneticField = magenticFields[i];
      ++i;
    }
    data.resetCache();
  }

  public static void givenCompassBearingIs(Data data, Double... compassBearings)
  {
    int i = 0;
    for (DataPoint point : data.getAllPoints())
    {
      if (point.magneticField == null)
      {
        point.magneticField = new MagneticField();
      }
      point.magneticField.compassBearing = compassBearings[i];
      ++i;
    }
    data.resetCache();
  }

  public static void givenGpsBearingIs(Data data, Double... bearings)
  {
    int i = 0;
    for (DataPoint point : data.getAllPoints())
    {
      point.location = new Location();
      point.location.bearing = bearings[i];
      ++i;
    }
    data.resetCache();
  }
}

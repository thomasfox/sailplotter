package com.github.thomasfox.sailplotter.analyze;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.data.Offset;
import org.junit.Test;

import com.github.thomasfox.sailplotter.model.Acceleration;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.Location;
import com.github.thomasfox.sailplotter.model.MagneticField;
import com.github.thomasfox.sailplotter.model.vector.CoordinateSystem;
import com.github.thomasfox.sailplotter.model.vector.ThreeDimVector;

public class DeviceOrientationAnalyzerTest
{
  DeviceOrientationAnalyzer sut = new DeviceOrientationAnalyzer();

  @Test
  public void getAverageOrientation()
  {
    // arrange
    List<DataPoint> points = givenAccelerationDataIs(
        new ThreeDimVector(1d, 0d, 0d),
        new ThreeDimVector(1d, -2d, 6d));

    // act
    ThreeDimVector averageAcceleration = sut.getAverageAcceleration(points);

    // assert
    assertThat(averageAcceleration).isEqualTo(new ThreeDimVector(1d, -1d, 3d));
  }

  @Test
  public void getAverageOrientation_noPoints()
  {
    // arrange
    List<DataPoint> points = new ArrayList<DataPoint>();

    // act
    ThreeDimVector averageAcceleration = sut.getAverageAcceleration(points);

    // assert
    assertThat(averageAcceleration).isNull();
  }

  @Test
  public void getAverageOrientation_noAcceleration()
  {
    // arrange
    List<DataPoint> points = new ArrayList<DataPoint>();
    points.add(new DataPoint(0));

    // act
    ThreeDimVector averageAcceleration = sut.getAverageAcceleration(points);

    // assert
    assertThat(averageAcceleration).isNull();
  }

  @Test
  public void getHorizontalCoordinateSystemFromAverageAcceleration_averageAccelerationInXZPlane()
  {
    // arrange
    List<DataPoint> points = givenAccelerationDataIs(new ThreeDimVector(1d, 0d, 4d));

    // act
    CoordinateSystem coordinateSystem = sut.getHorizontalCoordinateSystemFromAverageAcceleration(points);

    // assert
    assertThat(coordinateSystem.coordinatesAreCloseTo(new CoordinateSystem(
        new ThreeDimVector(0.8d, 0d, -0.2d).normalize(),
        new ThreeDimVector(0d, 1d, 0d),
        new ThreeDimVector(0.2d, 0d, 0.8d).normalize()),
      0.000001d)).isTrue();
  }

  @Test
  public void getHorizontalCoordinateSystemFromAverageAcceleration_averageAccelerationInYZPlane()
  {
    // arrange
    List<DataPoint> points = givenAccelerationDataIs(new ThreeDimVector(0d, 1d, 4d));

    // act
    CoordinateSystem coordinateSystem = sut.getHorizontalCoordinateSystemFromAverageAcceleration(points);

    // assert
    assertThat(coordinateSystem.coordinatesAreCloseTo(new CoordinateSystem(
        new ThreeDimVector(1d, 0d, 0d),
        new ThreeDimVector(0d, 0.8d, -0.2d).normalize(),
        new ThreeDimVector(0d, 0.2d, 0.8d).normalize()),
      0.000001d)).isTrue();
  }

  @Test
  public void getHorizontalCoordinateSystemFromAverageAcceleration_noData()
  {
    // arrange
    List<DataPoint> points = new ArrayList<>();

    // act
    CoordinateSystem coordinateSystem = sut.getHorizontalCoordinateSystemFromAverageAcceleration(points);

    // assert
    assertThat(coordinateSystem).isNull();
  }

  @Test
  public void getNormalizedRelativeBearingOfCompassToGps()
  {
    // arrange
    List<DataPoint> points = givenAccelerationDataIs(new ThreeDimVector(0d, 0d, 0d)); // acceleration is ignored
    givenCompassBearingIs(points, Math.PI / 2);
    givenGpsBearingIs(points, Math.PI);

    // act
    Double normalizedRelativeBearing = sut.getNormalizedRelativeBearingOfCompassToGps(points.get(0));

    // assert
    assertThat(normalizedRelativeBearing).isCloseTo(0.75, Offset.offset(0.00001));
  }

  @Test
  public void getAccelerationAt()
  {
    // arrange
    List<DataPoint> points = givenAccelerationDataIs(
        new ThreeDimVector(10d, 0d, 0d),
        new ThreeDimVector(1d, 10d, 0d),
        new ThreeDimVector(1d, 0d, 10d),
        new ThreeDimVector(10d, 0d, 0d));
    points.add(2, new DataPoint(0));
    points.get(0).time = 0l;
    points.get(1).time = 500l;
    points.get(2).time = 800l;
    points.get(3).time = 1000l;
    points.get(4).time = 1500l;

    // act
    ThreeDimVector result = sut.getAccelerationAt(2, points);

    // assert
    assertThat(result.x).isCloseTo(1d, within(0.001));
    assertThat(result.y).isCloseTo(4d, within(0.001));
    assertThat(result.z).isCloseTo(6d, within(0.001));
  }

  @Test
  public void getAccelerationAt_exactPointFound()
  {
    // arrange
    List<DataPoint> points = givenAccelerationDataIs(
        new ThreeDimVector(10d, 0d, 0d),
        new ThreeDimVector(1d, 10d, 2d),
        new ThreeDimVector(10d, 0d, 0d),
        new ThreeDimVector(10d, 0d, 0d));
    points.get(0).time = 0l;
    points.get(1).time = 500l;
    points.get(2).time = 1000l;
    points.get(3).time = 1500l;

    // act
    ThreeDimVector result = sut.getAccelerationAt(1, points);

    // assert
    assertThat(result.x).isCloseTo(1d, within(0.001));
    assertThat(result.y).isCloseTo(10d, within(0.001));
    assertThat(result.z).isCloseTo(2d, within(0.001));
  }

  @Test
  public void getAccelerationAt_intervalTooLong()
  {
    // arrange
    List<DataPoint> points = givenAccelerationDataIs(
        new ThreeDimVector(10d, 0d, 0d),
        new ThreeDimVector(1d, 10d, 2d));
    points.add(1, new DataPoint(0));
    points.get(0).time = 0l;
    points.get(1).time = 500l;
    points.get(2).time = 5000l;

    // act
    ThreeDimVector result = sut.getAccelerationAt(1, points);

    // assert
    assertThat(result).isNull();
  }

  @Test
  public void getAccelerationAt_noDataBelow()
  {
    // arrange
    List<DataPoint> points = givenAccelerationDataIs(
        new ThreeDimVector(10d, 0d, 0d),
        new ThreeDimVector(1d, 10d, 2d));
    points.add(0, new DataPoint(0));
    points.get(0).time = 0l;
    points.get(1).time = 1000l;
    points.get(2).time = 5000l;

    // act
    ThreeDimVector result = sut.getAccelerationAt(0, points);

    // assert
    assertThat(result).isNull();
  }

  @Test
  public void getAccelerationAt_noDataAbove()
  {
    // arrange
    List<DataPoint> points = givenAccelerationDataIs(
        new ThreeDimVector(10d, 0d, 0d),
        new ThreeDimVector(1d, 10d, 2d));
    points.add(new DataPoint(0));
    points.get(0).time = 0l;
    points.get(1).time = 100l;
    points.get(2).time = 200l;

    // act
    ThreeDimVector result = sut.getAccelerationAt(2, points);

    // assert
    assertThat(result).isNull();
  }


  private List<DataPoint> givenAccelerationDataIs(ThreeDimVector... accelerationData)
  {
    List<DataPoint> points = new ArrayList<DataPoint>();
    for (ThreeDimVector acceleration : accelerationData)
    {
      DataPoint point = new DataPoint(0);
      point.acceleration = new Acceleration(acceleration.x, acceleration.y, acceleration.z);
      points.add(point);
    }
    return points;
  }

  private void givenMagneticFieldIs(List<DataPoint> points, MagneticField... magenticFields)
  {
    int i = 0;
    for (DataPoint point : points)
    {
      point.magneticField = magenticFields[i];
      ++i;
    }
  }

  private void givenCompassBearingIs(List<DataPoint> points, Double... compassBearings)
  {
    int i = 0;
    for (DataPoint point : points)
    {
      if (point.magneticField == null)
      {
        point.magneticField = new MagneticField();
      }
      point.magneticField.compassBearing = compassBearings[i];
      ++i;
    }
  }

  private void givenGpsBearingIs(List<DataPoint> points, Double... bearings)
  {
    int i = 0;
    for (DataPoint point : points)
    {
      point.location = new Location();
      point.location.bearing = bearings[i];
      ++i;
    }
  }
}

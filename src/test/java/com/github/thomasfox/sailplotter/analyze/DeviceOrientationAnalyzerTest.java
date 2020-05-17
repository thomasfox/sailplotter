package com.github.thomasfox.sailplotter.analyze;

import static com.github.thomasfox.sailplotter.TestData.givenAccelerationDataIs;
import static com.github.thomasfox.sailplotter.TestData.givenCompassBearingIs;
import static com.github.thomasfox.sailplotter.TestData.givenGpsBearingIs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.assertj.core.data.Offset;
import org.junit.Test;

import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.vector.CoordinateSystem;
import com.github.thomasfox.sailplotter.model.vector.ThreeDimVector;

public class DeviceOrientationAnalyzerTest
{
  DeviceOrientationAnalyzer sut = new DeviceOrientationAnalyzer();

  @Test
  public void getHorizontalCoordinateSystemFromAverageAcceleration_averageAccelerationInXZPlane()
  {
    // arrange
    Data data = givenAccelerationDataIs(new ThreeDimVector(1d, 0d, 4d));

    // act
    CoordinateSystem coordinateSystem = sut.getHorizontalCoordinateSystemFromAverageAcceleration(data);

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
    Data data = givenAccelerationDataIs(new ThreeDimVector(0d, 1d, 4d));

    // act
    CoordinateSystem coordinateSystem = sut.getHorizontalCoordinateSystemFromAverageAcceleration(data);

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
    Data data = new Data();

    // act
    CoordinateSystem coordinateSystem = sut.getHorizontalCoordinateSystemFromAverageAcceleration(data);

    // assert
    assertThat(coordinateSystem).isNull();
  }

  @Test
  public void getNormalizedRelativeBearingOfCompassToGps()
  {
    // arrange
    Data data = givenAccelerationDataIs(new ThreeDimVector(0d, 0d, 0d)); // acceleration is ignored
    givenCompassBearingIs(data, Math.PI / 2);
    givenGpsBearingIs(data, Math.PI);

    // act
    Double normalizedRelativeBearing
        = sut.getNormalizedRelativeBearingOfCompassToGps(data.getAllPoints().get(0));

    // assert
    assertThat(normalizedRelativeBearing).isCloseTo(0.75, Offset.offset(0.00001));
  }

  @Test
  public void getAccelerationAt()
  {
    // arrange
    Data data = givenAccelerationDataIs(
        new ThreeDimVector(10d, 0d, 0d),
        new ThreeDimVector(1d, 10d, 0d),
        new ThreeDimVector(1d, 0d, 10d),
        new ThreeDimVector(10d, 0d, 0d));
    data.add(2, new DataPoint(0));
    data.getAllPoints().get(0).time = 0l;
    data.getAllPoints().get(1).time = 500l;
    data.getAllPoints().get(2).time = 800l;
    data.getAllPoints().get(3).time = 1000l;
    data.getAllPoints().get(4).time = 1500l;
    data.resetCache();

    // act
    ThreeDimVector result = sut.getAccelerationAt(2, data);

    // assert
    assertThat(result.x).isCloseTo(1d, within(0.001));
    assertThat(result.y).isCloseTo(4d, within(0.001));
    assertThat(result.z).isCloseTo(6d, within(0.001));
  }

  @Test
  public void getAccelerationAt_exactPointFound()
  {
    // arrange
    Data data = givenAccelerationDataIs(
        new ThreeDimVector(10d, 0d, 0d),
        new ThreeDimVector(1d, 10d, 2d),
        new ThreeDimVector(10d, 0d, 0d),
        new ThreeDimVector(10d, 0d, 0d));
    data.getAllPoints().get(0).time = 0l;
    data.getAllPoints().get(1).time = 500l;
    data.getAllPoints().get(2).time = 1000l;
    data.getAllPoints().get(3).time = 1500l;
    data.resetCache();

    // act
    ThreeDimVector result = sut.getAccelerationAt(1, data);

    // assert
    assertThat(result.x).isCloseTo(1d, within(0.001));
    assertThat(result.y).isCloseTo(10d, within(0.001));
    assertThat(result.z).isCloseTo(2d, within(0.001));
  }

  @Test
  public void getAccelerationAt_intervalTooLong()
  {
    // arrange
    Data data = givenAccelerationDataIs(
        new ThreeDimVector(10d, 0d, 0d),
        new ThreeDimVector(1d, 10d, 2d));
    data.add(1, new DataPoint(0));
    data.getAllPoints().get(0).time = 0l;
    data.getAllPoints().get(1).time = 500l;
    data.getAllPoints().get(2).time = 5000l;
    data.resetCache();

    // act
    ThreeDimVector result = sut.getAccelerationAt(1, data);

    // assert
    assertThat(result).isNull();
  }

  @Test
  public void getAccelerationAt_noDataBelow()
  {
    // arrange
    Data data = givenAccelerationDataIs(
        new ThreeDimVector(10d, 0d, 0d),
        new ThreeDimVector(1d, 10d, 2d));
    data.add(0, new DataPoint(0));
    data.getAllPoints().get(0).time = 0l;
    data.getAllPoints().get(1).time = 1000l;
    data.getAllPoints().get(2).time = 5000l;
    data.resetCache();

    // act
    ThreeDimVector result = sut.getAccelerationAt(0, data);

    // assert
    assertThat(result).isNull();
  }

  @Test
  public void getAccelerationAt_noDataAbove()
  {
    // arrange
    Data data = givenAccelerationDataIs(
        new ThreeDimVector(10d, 0d, 0d),
        new ThreeDimVector(1d, 10d, 2d));
    data.add(new DataPoint(0));
    data.getAllPoints().get(0).time = 0l;
    data.getAllPoints().get(1).time = 100l;
    data.getAllPoints().get(2).time = 200l;
    data.resetCache();

    // act
    ThreeDimVector result = sut.getAccelerationAt(2, data);

    // assert
    assertThat(result).isNull();
  }
}

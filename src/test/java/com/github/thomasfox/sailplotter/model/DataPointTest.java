package com.github.thomasfox.sailplotter.model;

import static  org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.jfree.data.time.Millisecond;
import org.junit.Test;

import com.github.thomasfox.sailplotter.model.vector.ThreeDimVector;

public class DataPointTest
{
  @Test
  public void testCopyConstructor()
  {
    // prepare
    DataPoint point = new DataPoint(37);
    point.time = 1234567890l;

    point.location.latitude = 0.1d;
    point.location.longitude = -0.2d;
    point.location.bearingFromLatLong = 0.3d;
    point.location.velocityFromLatLong = 4d;
    point.location.velocityBearingAveragedOverDistance = 5d;

    point.wind = new Wind();
    point.wind.direction = 0.5d;
    point.wind.velocity = 6d;

    point.magneticField = new ThreeDimVector(70d, -80d, 90d);
    point.acceleration = new ThreeDimVector(71d, -81d, 91d);

    point.manoeuverState = ManoeuverState.IN_TACK;

    // execute
    DataPoint copy = new DataPoint(point);

    // verify
    assertThat(copy).isNotSameAs(point);

    assertThat(copy.index).isEqualTo(37);
    assertThat(copy.time).isEqualTo(1234567890l);

    assertThat(copy.location).isNotSameAs(point.location);
    assertThat(copy.location.latitude).isEqualTo(0.1d);
    assertThat(copy.location.longitude).isEqualTo(-0.2d);
    assertThat(copy.location.bearingFromLatLong).isEqualTo(0.3d);
    assertThat(copy.location.velocityFromLatLong).isEqualTo(4d);
    assertThat(copy.location.velocityBearingAveragedOverDistance).isEqualTo(5d);

    assertThat(copy.wind).isNotSameAs(point.wind);
    assertThat(copy.wind.direction).isEqualTo(0.5d);
    assertThat(copy.wind.velocity).isEqualTo(6d);

    assertThat(copy.magneticField).isNotSameAs(point.magneticField);
    assertThat(copy.magneticField.x).isEqualTo(70d);
    assertThat(copy.magneticField.y).isEqualTo(-80d);
    assertThat(copy.magneticField.z).isEqualTo(90d);

    assertThat(copy.acceleration).isNotSameAs(point.acceleration);
    assertThat(copy.acceleration.x).isEqualTo(71d);
    assertThat(copy.acceleration.y).isEqualTo(-81d);
    assertThat(copy.acceleration.z).isEqualTo(91d);

    assertThat(copy.manoeuverState).isEqualTo(ManoeuverState.IN_TACK);
  }

  @Test
  public void testGetMillisecond()
  {
    // prepare
    DataPoint point = new DataPoint(-1);
    point.time = 315630245006l;

    // execute
    Millisecond millisecond = point.getMillisecond();

    // verify
    assertThat(millisecond).isEqualTo(new Millisecond(6, 5, 4, 4, 2, 1, 1980));
  }

  @Test
  public void testAverageTime()
  {
    // prepare
    DataPoint point = new DataPoint(-1);
    point.time = 123L;
    DataPoint other = new DataPoint(-1);
    other.time = 127L;

    // execute
    long averageTime = point.averageTime(other);

    // verify
    assertThat(averageTime).isEqualTo(125L);
  }

  @Test
  public void testTimeDistanceMillis()
  {
    // prepare
    DataPoint point = new DataPoint(-1);
    point.time = 10000127L;
    DataPoint other = new DataPoint(-1);
    other.time = 10000123L;

    // execute
    long timeDistanceMillis = point.timeDistanceMillis(other);

    // verify
    assertThat(timeDistanceMillis).isEqualTo(4L);
  }

  @Test
  public void testGetRelativeBearingInArcs()
  {
    // prepare
    DataPoint point = new DataPoint(-1);
    point.location.bearingFromLatLong = 2d;
    point.wind = new Wind();
    point.wind.direction = 1d;

    // execute
    double relativeBearingInArcs = point.getRelativeBearingInArcs();

    // verify
    assertThat(relativeBearingInArcs).isCloseTo(1, within(0.00001d));
  }

  @Test
  public void testGetRelativeBearingInArcs_modulo2Pi()
  {
    // prepare
    DataPoint point = new DataPoint(-1);
    point.location.bearingFromLatLong = 1d;
    point.wind = new Wind();
    point.wind.direction = 2d;

    // execute
    double relativeBearingInArcs = point.getRelativeBearingInArcs();

    // verify
    assertThat(relativeBearingInArcs).isCloseTo(2 * Math.PI - 1, within(0.00001d));
  }

  @Test
  public void testGetRelativeBearingAs360Degrees()
  {
    // prepare
    DataPoint point = new DataPoint(-1);
    point.location.bearingFromLatLong = 1.5 * Math.PI;
    point.wind = new Wind();
    point.wind.direction = Math.PI / 2;

    // execute
    double relativeBearingInArcs = point.getRelativeBearingAs360Degrees();

    // verify
    assertThat(relativeBearingInArcs).isCloseTo(180d, within(0.00001d));
  }

  @Test
  public void testGetRelativeBearingInArcs_modulo360()
  {
    // prepare
    DataPoint point = new DataPoint(-1);
    point.location.bearingFromLatLong = Math.PI / 2;
    point.wind = new Wind();
    point.wind.direction = Math.PI;

    // execute
    double relativeBearingInArcs = point.getRelativeBearingAs360Degrees();

    // verify
    assertThat(relativeBearingInArcs).isCloseTo(-90d, within(0.00001d));
  }

  @Test
  public void testGetPointOfSail()
  {
    // prepare
    DataPoint point = new DataPoint(-1);
    point.location.bearingFromLatLong = Math.PI / 2;
    point.wind = new Wind();
    point.wind.direction = Math.PI;

    // execute
    PointOfSail pointOfSail = point.getPointOfSail();

    // verify
    assertThat(pointOfSail).isEqualTo(PointOfSail.BEAM_REACH_PORT);
  }

  @Test
  public void testBearingTo_North()
  {
    // prepare
    DataPoint point1 = new DataPoint(0);
    point1.location.latitude = 0d;
    point1.location.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.location.latitude = 0.00001d;
    point2.location.longitude = 0d;

    // execute
    double bearingArcs = point1.getBearingTo(point2);

    // verify
    assertThat(bearingArcs).isCloseTo(0d, within(0.0001d));
  }

  @Test
  public void testBearingTo_NorthNorthEast()
  {
    // prepare
    DataPoint point1 = new DataPoint(0);
    point1.location.latitude = 0d;
    point1.location.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.location.latitude = 0.00001d * Math.cos(22.5d / 360 * 2 * Math.PI);
    point2.location.longitude = 0.00001d * Math.sin(22.5d / 360 * 2 * Math.PI);

    // execute
    double bearingArcs = point1.getBearingTo(point2);

    // verify
    assertThat(bearingArcs).isCloseTo(Math.PI / 8, within(0.0001d));
  }

  @Test
  public void testBearingTo_NorthEast()
  {
    // prepare
    DataPoint point1 = new DataPoint(0);
    point1.location.latitude = 0d;
    point1.location.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.location.latitude = 0.00001d;
    point2.location.longitude = 0.00001d;

    // execute
    double bearingArcs = point1.getBearingTo(point2);

    // verify
    assertThat(bearingArcs).isCloseTo(Math.PI / 4, within(0.0001d));
  }

  @Test
  public void testBearingTo_EastNorthEast()
  {
    // prepare
    DataPoint point1 = new DataPoint(0);
    point1.location.latitude = 0d;
    point1.location.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.location.latitude = 0.00001d * Math.cos(67.5d / 360 * 2 * Math.PI);
    point2.location.longitude = 0.00001d * Math.sin(67.5d / 360 * 2 * Math.PI);

    // execute
    double bearingArcs = point1.getBearingTo(point2);

    // verify
    assertThat(bearingArcs).isCloseTo(3 * Math.PI / 8, within(0.0001d));
  }

  @Test
  public void testBearingTo_East()
  {
    // prepare
    DataPoint point1 = new DataPoint(0);
    point1.location.latitude = 0d;
    point1.location.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.location.latitude = 0d;
    point2.location.longitude = 0.00001d;

    // execute
    double bearingArcs = point1.getBearingTo(point2);

    // verify
    assertThat(bearingArcs).isCloseTo(Math.PI / 2, within(0.0001d));
  }

  @Test
  public void testBearingTo_EastSouthEast()
  {
    // prepare
    DataPoint point1 = new DataPoint(0);
    point1.location.latitude = 0d;
    point1.location.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.location.latitude = 0.00001d * Math.cos(112.5d / 360 * 2 * Math.PI);
    point2.location.longitude = 0.00001d * Math.sin(112.5d / 360 * 2 * Math.PI);

    // execute
    double bearingArcs = point1.getBearingTo(point2);

    // verify
    assertThat(bearingArcs).isCloseTo(5 * Math.PI / 8, within(0.0001d));
  }

  @Test
  public void testBearingTo_SouthEast()
  {
    // prepare
    DataPoint point1 = new DataPoint(0);
    point1.location.latitude = 0d;
    point1.location.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.location.latitude = -0.00001d;
    point2.location.longitude = 0.00001d;

    // execute
    double bearingArcs = point1.getBearingTo(point2);

    // verify
    assertThat(bearingArcs).isCloseTo(3 * Math.PI / 4, within(0.0001d));
  }

  @Test
  public void testBearingTo_South()
  {
    // prepare
    DataPoint point1 = new DataPoint(0);
    point1.location.latitude = 0d;
    point1.location.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.location.latitude = -0.00001d;
    point2.location.longitude = 0d;

    // execute
    double bearingArcs = point1.getBearingTo(point2);

    // verify
    assertThat(bearingArcs).isCloseTo(Math.PI, within(0.0001d));
  }

  @Test
  public void testBearingTo_SouthWest()
  {
    // prepare
    DataPoint point1 = new DataPoint(0);
    point1.location.latitude = 0d;
    point1.location.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.location.latitude = -0.00001d;
    point2.location.longitude = -0.00001d;

    // execute
    double bearingArcs = point1.getBearingTo(point2);

    // verify
    assertThat(bearingArcs).isCloseTo(5 * Math.PI / 4, within(0.0001d));
  }

  @Test
  public void testBearingTo_West()
  {
    // prepare
    DataPoint point1 = new DataPoint(0);
    point1.location.latitude = 0d;
    point1.location.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.location.latitude = 0d;
    point2.location.longitude = -0.00001d;

    // execute
    double bearingArcs = point1.getBearingTo(point2);

    // verify
    assertThat(bearingArcs).isCloseTo(3 * Math.PI / 2, within(0.0001d));
  }

  @Test
  public void testBearingTo_NorthWest()
  {
    // prepare
    DataPoint point1 = new DataPoint(0);
    point1.location.latitude = 0d;
    point1.location.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.location.latitude = 0.00001d;
    point2.location.longitude = -0.00001d;

    // execute
    double bearingArcs = point1.getBearingTo(point2);

    // verify
    assertThat(bearingArcs).isCloseTo(7 * Math.PI / 4, within(0.0001d));
  }

  @Test
  public void testIntersection1()
  {
    // prepare
    Location line1Point1 = new Location();
    line1Point1.setXAndY(-2, 0);
    Location line1Point2 = new Location();
    line1Point2.setXAndY(-1, 0.5);
    Location line2Point1 = new Location();
    line2Point1.setXAndY(2, 0);
    Location line2Point2 = new Location();
    line2Point2.setXAndY(3, -0.5);

    // execute
    Location intersection = Location.intersection(line1Point1, line1Point2, line2Point1, line2Point2);

    // verify
    assertThat(intersection.getX()).isCloseTo(0d, within(0.00001d));
    assertThat(intersection.getY()).isCloseTo(1d, within(0.00001d));
  }

  @Test
  public void testIntersection2()
  {
    // prepare
    Location line1Point1 = new Location();
    line1Point1.setXAndY(-1, -1);
    Location line1Point2 = new Location();
    line1Point2.setXAndY(0, -0.5);
    Location line2Point1 = new Location();
    line2Point1.setXAndY(0, 3);
    Location line2Point2 = new Location();
    line2Point2.setXAndY(0.5, 1.5);

    // execute
    Location intersection = Location.intersection(line1Point1, line1Point2, line2Point1, line2Point2);

    // verify
    assertThat(intersection.getX()).isCloseTo(1d, within(0.00001d));
    assertThat(intersection.getY()).isCloseTo(0d, within(0.00001d));
  }

  @Test
  public void testToString()
  {
    // prepare
    DataPoint dataPoint = new DataPoint(-1);
    dataPoint.time = 315576000000l;
    dataPoint.location.setXAndY(2d, 7d);
    dataPoint.location.velocityFromLatLong = 12d;
    dataPoint.location.bearingFromLatLong = Math.PI / 2;
    dataPoint.wind = new Wind();
    dataPoint.wind.direction = Math.PI;

    // execute
    String result = dataPoint.toString();

    // verify
    assertThat(result).isEqualTo("DataPoint: 1980-01-01T13:00 (2m,7m) 12,0kts 90,0°Abs -90,0°Rel");
  }

  @Test
  public void testToString_nullValues()
  {
    // prepare
    DataPoint dataPoint = new DataPoint(-1);
    dataPoint.wind = new Wind();

    // execute
    String result = dataPoint.toString();

    // verify
    assertThat(result).isEqualTo("DataPoint: ");
  }

  @Test
  public void testToString_nullObjects()
  {
    // prepare
    DataPoint dataPoint = new DataPoint(-1);
    dataPoint.location = null;
    dataPoint.wind = null;

    // execute
    String result = dataPoint.toString();

    // verify
    assertThat(result).isEqualTo("DataPoint: ");
  }
}

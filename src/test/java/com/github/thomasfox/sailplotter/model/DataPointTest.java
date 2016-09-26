package com.github.thomasfox.sailplotter.model;

import static  org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.Test;

public class DataPointTest
{

  @Test
  public void testBearingTo_North()
  {
    // prepare
    DataPoint point1 = new DataPoint(0);
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.latitude = 0.00001d;
    point2.longitude = 0d;

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
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.latitude = 0.00001d * Math.cos(22.5d / 360 * 2 * Math.PI);
    point2.longitude = 0.00001d * Math.sin(22.5d / 360 * 2 * Math.PI);

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
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.latitude = 0.00001d;
    point2.longitude = 0.00001d;

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
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.latitude = 0.00001d * Math.cos(67.5d / 360 * 2 * Math.PI);
    point2.longitude = 0.00001d * Math.sin(67.5d / 360 * 2 * Math.PI);

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
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.latitude = 0d;
    point2.longitude = 0.00001d;

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
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.latitude = 0.00001d * Math.cos(112.5d / 360 * 2 * Math.PI);
    point2.longitude = 0.00001d * Math.sin(112.5d / 360 * 2 * Math.PI);

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
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.latitude = -0.00001d;
    point2.longitude = 0.00001d;

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
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.latitude = -0.00001d;
    point2.longitude = 0d;

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
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.latitude = -0.00001d;
    point2.longitude = -0.00001d;

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
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.latitude = 0d;
    point2.longitude = -0.00001d;

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
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint(1);
    point2.latitude = 0.00001d;
    point2.longitude = -0.00001d;

    // execute
    double bearingArcs = point1.getBearingTo(point2);

    // verify
    assertThat(bearingArcs).isCloseTo(7 * Math.PI / 4, within(0.0001d));
  }

  @Test
  public void testSetXAndY()
  {
    // prepare
    DataPoint point = new DataPoint(0);

    // execute
    point.setXAndY(137345, 2500000);

    // verify
    assertThat(point.getX()).isCloseTo(137345d, within(0.00001d));
    assertThat(point.getY()).isCloseTo(2500000d, within(0.00001d));

  }

  @Test
  public void testIntersection1()
  {
    // prepare
    DataPoint line1Point1 = new DataPoint(0);
    line1Point1.setXAndY(-2, 0);
    DataPoint line1Point2 = new DataPoint(1);
    line1Point2.setXAndY(-1, 0.5);
    DataPoint line2Point1 = new DataPoint(2);
    line2Point1.setXAndY(2, 0);
    DataPoint line2Point2 = new DataPoint(3);
    line2Point2.setXAndY(3, -0.5);

    // execute
    DataPoint intersection = DataPoint.intersection(line1Point1, line1Point2, line2Point1, line2Point2);

    // verify
    assertThat(intersection.getX()).isCloseTo(0d, within(0.00001d));
    assertThat(intersection.getY()).isCloseTo(1d, within(0.00001d));
  }

  @Test
  public void testIntersection2()
  {
    // prepare
    DataPoint line1Point1 = new DataPoint(0);
    line1Point1.setXAndY(-1, -1);
    DataPoint line1Point2 = new DataPoint(1);
    line1Point2.setXAndY(0, -0.5);
    DataPoint line2Point1 = new DataPoint(2);
    line2Point1.setXAndY(0, 3);
    DataPoint line2Point2 = new DataPoint(3);
    line2Point2.setXAndY(0.5, 1.5);

    // execute
    DataPoint intersection = DataPoint.intersection(line1Point1, line1Point2, line2Point1, line2Point2);

    // verify
    assertThat(intersection.getX()).isCloseTo(1d, within(0.00001d));
    assertThat(intersection.getY()).isCloseTo(0d, within(0.00001d));
  }
}

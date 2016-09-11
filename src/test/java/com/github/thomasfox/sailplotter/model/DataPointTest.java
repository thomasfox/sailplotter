package com.github.thomasfox.sailplotter.model;

import static  org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.Test;

import com.github.thomasfox.sailplotter.model.DataPoint;

public class DataPointTest
{

  @Test
  public void testBearingTo_North()
  {
    DataPoint point1 = new DataPoint();
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint();
    point2.latitude = 0.00001d;
    point2.longitude = 0d;

    double bearingArcs = point1.getBearingTo(point2);

    assertThat(bearingArcs).isCloseTo(0d, within(0.0001d));
  }

  @Test
  public void testBearingTo_NorthNorthEast()
  {
    DataPoint point1 = new DataPoint();
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint();
    point2.latitude = 0.00001d * Math.cos(22.5d / 360 * 2 * Math.PI);
    point2.longitude = 0.00001d * Math.sin(22.5d / 360 * 2 * Math.PI);

    double bearingArcs = point1.getBearingTo(point2);

    assertThat(bearingArcs).isCloseTo(Math.PI / 8, within(0.0001d));
  }

  @Test
  public void testBearingTo_NorthEast()
  {
    DataPoint point1 = new DataPoint();
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint();
    point2.latitude = 0.00001d;
    point2.longitude = 0.00001d;

    double bearingArcs = point1.getBearingTo(point2);

    assertThat(bearingArcs).isCloseTo(Math.PI / 4, within(0.0001d));
  }

  @Test
  public void testBearingTo_EastNorthEast()
  {
    DataPoint point1 = new DataPoint();
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint();
    point2.latitude = 0.00001d * Math.cos(67.5d / 360 * 2 * Math.PI);
    point2.longitude = 0.00001d * Math.sin(67.5d / 360 * 2 * Math.PI);

    double bearingArcs = point1.getBearingTo(point2);

    assertThat(bearingArcs).isCloseTo(3 * Math.PI / 8, within(0.0001d));
  }

  @Test
  public void testBearingTo_East()
  {
    DataPoint point1 = new DataPoint();
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint();
    point2.latitude = 0d;
    point2.longitude = 0.00001d;

    double bearingArcs = point1.getBearingTo(point2);

    assertThat(bearingArcs).isCloseTo(Math.PI / 2, within(0.0001d));
  }

  @Test
  public void testBearingTo_EastSouthEast()
  {
    DataPoint point1 = new DataPoint();
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint();
    point2.latitude = 0.00001d * Math.cos(112.5d / 360 * 2 * Math.PI);
    point2.longitude = 0.00001d * Math.sin(112.5d / 360 * 2 * Math.PI);

    double bearingArcs = point1.getBearingTo(point2);

    assertThat(bearingArcs).isCloseTo(5 * Math.PI / 8, within(0.0001d));
  }

  @Test
  public void testBearingTo_SouthEast()
  {
    DataPoint point1 = new DataPoint();
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint();
    point2.latitude = -0.00001d;
    point2.longitude = 0.00001d;

    double bearingArcs = point1.getBearingTo(point2);

    assertThat(bearingArcs).isCloseTo(3 * Math.PI / 4, within(0.0001d));
  }

  @Test
  public void testBearingTo_South()
  {
    DataPoint point1 = new DataPoint();
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint();
    point2.latitude = -0.00001d;
    point2.longitude = 0d;

    double bearingArcs = point1.getBearingTo(point2);

    assertThat(bearingArcs).isCloseTo(Math.PI, within(0.0001d));
  }

  @Test
  public void testBearingTo_SouthWest()
  {
    DataPoint point1 = new DataPoint();
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint();
    point2.latitude = -0.00001d;
    point2.longitude = -0.00001d;

    double bearingArcs = point1.getBearingTo(point2);

    assertThat(bearingArcs).isCloseTo(5 * Math.PI / 4, within(0.0001d));
  }

  @Test
  public void testBearingTo_West()
  {
    DataPoint point1 = new DataPoint();
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint();
    point2.latitude = 0d;
    point2.longitude = -0.00001d;

    double bearingArcs = point1.getBearingTo(point2);

    assertThat(bearingArcs).isCloseTo(3 * Math.PI / 2, within(0.0001d));
  }

  @Test
  public void testBearingTo_NorthWest()
  {
    DataPoint point1 = new DataPoint();
    point1.latitude = 0d;
    point1.longitude = 0d;
    DataPoint point2 = new DataPoint();
    point2.latitude = 0.00001d;
    point2.longitude = -0.00001d;

    double bearingArcs = point1.getBearingTo(point2);

    assertThat(bearingArcs).isCloseTo(7 * Math.PI / 4, within(0.0001d));
  }
}

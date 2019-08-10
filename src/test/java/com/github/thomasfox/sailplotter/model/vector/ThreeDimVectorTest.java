package com.github.thomasfox.sailplotter.model.vector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ThreeDimVectorTest
{
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void emptyConstructor()
  {
    ThreeDimVector vector = new ThreeDimVector();

    assertThat(vector.x).isEqualTo(0d);
    assertThat(vector.y).isEqualTo(0d);
    assertThat(vector.z).isEqualTo(0d);
  }

  @Test
  public void valuesConstructor()
  {
    ThreeDimVector vector = new ThreeDimVector(2d, 3d, 4d);

    assertThat(vector.x).isEqualTo(2d);
    assertThat(vector.y).isEqualTo(3d);
    assertThat(vector.z).isEqualTo(4d);
  }

  @Test
  public void copyConstructor()
  {
    ThreeDimVector vector = new ThreeDimVector(2d, 3d, 4d);

    ThreeDimVector result = new ThreeDimVector(vector);

    assertThat(result).isNotSameAs(vector);
    assertThat(result.x).isEqualTo(vector.x);
    assertThat(result.y).isEqualTo(vector.y);
    assertThat(result.z).isEqualTo(vector.z);
  }

  @Test
  public void copy()
  {
    ThreeDimVector vector = new ThreeDimVector(2d, 3d, 4d);

    ThreeDimVector result = ThreeDimVector.copy(vector);

    assertThat(result).isNotSameAs(vector);
    assertThat(result.x).isEqualTo(vector.x);
    assertThat(result.y).isEqualTo(vector.y);
    assertThat(result.z).isEqualTo(vector.z);
  }

  @Test
  public void copy_null()
  {
    ThreeDimVector result = ThreeDimVector.copy(null);

    assertThat(result).isNull();
  }

  @Test
  public void lenght()
  {
    ThreeDimVector vector = new ThreeDimVector(2d, 3d, 4d);

    assertThat(vector.length()).isCloseTo(Math.sqrt(2*2 + 3*3 + 4*4), within(0.001));
  }

  @Test
  public void normalize()
  {
    ThreeDimVector vector = new ThreeDimVector(2d, 3d, 4d);

    ThreeDimVector result = vector.normalize();

    assertThat(result.x).isCloseTo(2 / vector.length(), within(0.001));
    assertThat(result.y).isCloseTo(3 / vector.length(), within(0.001));
    assertThat(result.z).isCloseTo(4 / vector.length(), within(0.001));
  }

  @Test
  public void add()
  {
    ThreeDimVector vector = new ThreeDimVector(2d, 3d, 4d);

    vector.add(new ThreeDimVector(1d, 3d, 5d));

    assertThat(vector.x).isCloseTo(3, within(0.001));
    assertThat(vector.y).isCloseTo(6, within(0.001));
    assertThat(vector.z).isCloseTo(9, within(0.001));
  }

  @Test
  public void multiplyBy()
  {
    ThreeDimVector vector = new ThreeDimVector(2d, 3d, 4d);

    ThreeDimVector result = vector.multiplyBy(2d);

    assertThat(result.x).isCloseTo(4, within(0.001));
    assertThat(result.y).isCloseTo(6, within(0.001));
    assertThat(result.z).isCloseTo(8, within(0.001));
  }

  @Test
  public void crossProduct()
  {
    ThreeDimVector vector = new ThreeDimVector(2d, 3d, 4d);

    ThreeDimVector result = vector.crossProduct(new ThreeDimVector(-1d, 5d, 6d));

    assertThat(result.x).isCloseTo(3 * 6 - 4 * 5, within(0.001));
    assertThat(result.y).isCloseTo(4 * (-1) - 6 * 2 , within(0.001));
    assertThat(result.z).isCloseTo(2 * 5 - 3 * (-1), within(0.001));
  }

  @Test
  public void scalarProduct()
  {
    ThreeDimVector vector = new ThreeDimVector(2d, 3d, 4d);

    double result = vector.scalarProduct(new ThreeDimVector(-1d, 5d, 6d));

    assertThat(result).isCloseTo(2 * (-1) + 3 * 5 + 4 * 6, within(0.001));
  }

  @Test
  public void coordinatesAreCloseTo()
  {
    ThreeDimVector vector = new ThreeDimVector(2d, 3d, 4d);

    assertThat(vector.coordinatesAreCloseTo(new ThreeDimVector(2.1d, 2.9d, 4.1d), 0.15d)).isTrue();
  }

  @Test
  public void coordinatesAreCloseTo_wrongx()
  {
    ThreeDimVector vector = new ThreeDimVector(2d, 3d, 4d);

    assertThat(vector.coordinatesAreCloseTo(new ThreeDimVector(2.2d, 2.9d, 4.1d), 0.15d)).isFalse();
  }

  @Test
  public void coordinatesAreCloseTo_wrongy()
  {
    ThreeDimVector vector = new ThreeDimVector(2d, 3d, 4d);

    assertThat(vector.coordinatesAreCloseTo(new ThreeDimVector(2.1d, 2.8d, 4.1d), 0.15d)).isFalse();
  }

  @Test
  public void coordinatesAreCloseTo_wrongz()
  {
    ThreeDimVector vector = new ThreeDimVector(2d, 3d, 4d);

    assertThat(vector.coordinatesAreCloseTo(new ThreeDimVector(2.1d, 2.9d, 4.2d), 0.15d)).isFalse();
  }

  @Test
  public void weightedAdd()
  {
    ThreeDimVector vector1 = new ThreeDimVector(2d, 3d, 4d);
    ThreeDimVector vector2 = new ThreeDimVector(-1d, 5d, 6d);

    ThreeDimVector result = ThreeDimVector.weightedAdd(vector1, 2d, vector2, 8d);

    assertThat(result.x).isCloseTo(0.2 * 2 + 0.8 * (-1), within(0.01));
    assertThat(result.y).isCloseTo(0.2 * 3 + 0.8 * 5, within(0.01));
    assertThat(result.z).isCloseTo(0.2 * 4 + 0.8 * 6, within(0.01));
  }

  @Test
  public void weightedAdd_negativeWeight1()
  {
    ThreeDimVector vector1 = new ThreeDimVector(2d, 3d, 4d);
    ThreeDimVector vector2 = new ThreeDimVector(-1d, 5d, 6d);

    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("weight1 must be >= 0, was -2.0");

    ThreeDimVector.weightedAdd(vector1, -2d, vector2, 8d);
  }

  @Test
  public void weightedAdd_negativeWeight2()
  {
    ThreeDimVector vector1 = new ThreeDimVector(2d, 3d, 4d);
    ThreeDimVector vector2 = new ThreeDimVector(-1d, 5d, 6d);

    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("weight2 must be >= 0, was -8.0");

    ThreeDimVector.weightedAdd(vector1, 2d, vector2, -8d);
  }

  @Test
  public void testToString()
  {
    ThreeDimVector vector = new ThreeDimVector(2d, 3d, 4d);

    assertThat(vector.toString()).isEqualTo("(2.0, 3.0, 4.0)");
  }

}

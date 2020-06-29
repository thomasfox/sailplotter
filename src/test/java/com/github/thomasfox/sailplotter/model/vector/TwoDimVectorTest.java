package com.github.thomasfox.sailplotter.model.vector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.Test;

public class TwoDimVectorTest
{
  TwoDimVector sut;

  @Test
  public void testRotate_XBy0Deg()
  {
    sut = new TwoDimVector(2d, 0d);

    TwoDimVector result = sut.rotate(0d);

    assertThat(result).isSameAs(sut);
    assertThat(result.x).isCloseTo(2, within(0.000001));
    assertThat(result.y).isCloseTo(0, within(0.000001));
  }

  @Test
  public void testRotate_XBy90Deg()
  {
    sut = new TwoDimVector(2d, 0d);

    TwoDimVector result = sut.rotate(Math.PI / 2);

    assertThat(result).isSameAs(sut);
    assertThat(result.x).isCloseTo(0, within(0.000001));
    assertThat(result.y).isCloseTo(-2, within(0.000001));
  }

  @Test
  public void testRotate_XBy180Deg()
  {
    sut = new TwoDimVector(2d, 0d);

    TwoDimVector result = sut.rotate(Math.PI);

    assertThat(result).isSameAs(sut);
    assertThat(result.x).isCloseTo(-2, within(0.000001));
    assertThat(result.y).isCloseTo(0, within(0.000001));
  }

  @Test
  public void testRotate_XBy270Deg()
  {
    sut = new TwoDimVector(2d, 0d);

    TwoDimVector result = sut.rotate(3 * Math.PI / 2);

    assertThat(result).isSameAs(sut);
    assertThat(result.x).isCloseTo(0, within(0.000001));
    assertThat(result.y).isCloseTo(2, within(0.000001));
  }

  @Test
  public void testRotate_XBy45Deg()
  {
    sut = new TwoDimVector(Math.sqrt(2), Math.sqrt(2));

    TwoDimVector result = sut.rotate(Math.PI / 4);

    assertThat(result).isSameAs(sut);
    assertThat(result.x).isCloseTo(2, within(0.000001));
    assertThat(result.y).isCloseTo(0, within(0.000001));
  }

}

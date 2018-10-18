package com.github.thomasfox.sailplotter.model.vector;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ThreeDimMatrixTest
{
  @Test
  public void getRotationMatrixUpToEast()
  {
    ThreeDimVector up = new ThreeDimVector(0d, 0d, 1d);
    ThreeDimVector east = new ThreeDimVector(1d, 0d, 0d);

    ThreeDimMatrix result = ThreeDimMatrix.getRotationMatrix(up,  east);

    assertThat(result.isCloseTo(new ThreeDimMatrix(
            0, 0, 1,
            0, 1, 0,
            -1, 0, 0),
        0.00001d)).isTrue();
  }

  @Test
  public void getRotationMatrixUpToNorth()
  {
    ThreeDimVector up = new ThreeDimVector(0d, 0d, 1d);
    ThreeDimVector north = new ThreeDimVector(0d, 1d, 0d);

    ThreeDimMatrix result = ThreeDimMatrix.getRotationMatrix(up,  north);

    assertThat(result.isCloseTo(new ThreeDimMatrix(
            1, 0, 0,
            0, 0, 1,
            0, -1, 0),
        0.00001d)).isTrue();
  }

  @Test
  public void getRotationMatrixEastToNorth()
  {
    ThreeDimVector east = new ThreeDimVector(1d, 0d, 0d);
    ThreeDimVector north = new ThreeDimVector(0d, 1d, 0d);

    ThreeDimMatrix result = ThreeDimMatrix.getRotationMatrix(east,  north);

    assertThat(result.isCloseTo(new ThreeDimMatrix(
            0, -1, 0,
            1, 0, 0,
            0, 0, 1),
        0.00001d)).isTrue();
  }

  @Test
  public void getRotationMatrixExample1()
  {
    ThreeDimVector from = new ThreeDimVector(4d, -2d, 7d).normalize();
    ThreeDimVector to = new ThreeDimVector(-3d, 8d, 6d).normalize();

    ThreeDimMatrix result = ThreeDimMatrix.getRotationMatrix(from, to);

    assertThat(result.multiply(from.normalize())
        .coordinatesAreCloseTo(to.normalize(), 0.00001d))
        .isTrue();
  }
}

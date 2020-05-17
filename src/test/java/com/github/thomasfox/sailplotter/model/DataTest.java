package com.github.thomasfox.sailplotter.model;

import static com.github.thomasfox.sailplotter.TestData.givenAccelerationDataIs;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.github.thomasfox.sailplotter.model.vector.ThreeDimVector;

public class DataTest
{


  @Test
  public void getAverageOrientation()
  {
    // arrange
    Data data = givenAccelerationDataIs(
        new ThreeDimVector(1d, 0d, 0d),
        new ThreeDimVector(1d, -2d, 6d));

    // act
    ThreeDimVector averageAcceleration = data.getAverageAcceleration();

    // assert
    assertThat(averageAcceleration).isEqualTo(new ThreeDimVector(1d, -1d, 3d));
  }

  @Test
  public void getAverageOrientation_noPoints()
  {
    // arrange
    Data data = new Data();

    // act
    ThreeDimVector averageAcceleration = data.getAverageAcceleration();

    // assert
    assertThat(averageAcceleration).isNull();
  }

  @Test
  public void getAverageOrientation_noAcceleration()
  {
    // arrange
    Data data = new Data();
    data.add(new DataPoint(0));

    // act
    ThreeDimVector averageAcceleration = data.getAverageAcceleration();

    // assert
    assertThat(averageAcceleration).isNull();
  }


}

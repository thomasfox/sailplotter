package com.github.thomasfox.sailplotter.analyze;

import java.util.ArrayList;
import java.util.List;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.PointOfSail;
import com.github.thomasfox.sailplotter.model.Tack;

public class TackListByPointOfSailAnalyzer
{
  public List<Tack> analyze(List<DataPoint> points)
  {
    List<Tack> firstPass = new ArrayList<>();
    Tack currentTack = null;
    for (int dataPointIndex = 0; dataPointIndex < points.size(); ++dataPointIndex)
    {
      DataPoint point = points.get(dataPointIndex);
      PointOfSail pointOfSail = point.getPointOfSail();
      if (pointOfSail == null)
      {
        if (currentTack == null)
        {
          continue;
        }
        else
        {
          currentTack.end = point;
          currentTack.endIndex = dataPointIndex;
          firstPass.add(currentTack);
          currentTack = null;
        }
      }
      else // if (pointOfSail != null)
      {
        if (currentTack == null)
        {
          currentTack = new Tack();
          currentTack.start(point, dataPointIndex);
          continue;
        }
        if (currentTack.pointOfSail != pointOfSail
            && !(currentTack.start.distance(point) < Constants.MINIMAL_TACK_LENGTH))
        {
          currentTack.end(point, dataPointIndex);
          firstPass.add(currentTack);
          currentTack = new Tack();
          currentTack.start(point, dataPointIndex);
        }
      }
    }
    // TODO last tack

    int j = -1;
    List<Tack> secondPass = new ArrayList<>();
    for (int i = 0; i < firstPass.size(); ++i)
    {
      Tack tack = firstPass.get(i);
      if (i > 0
          && tack.getLength() < Constants.MINIMAL_TACK_LENGTH)
      {
        Tack previousTack = secondPass.get(j);
        previousTack.end(tack.end, tack.endIndex);
        previousTack.pointOfSail = PointOfSail.ofRelativeBearing(
            previousTack.getAverageRelativeBearingInArcs());
      }
      else
      {
        secondPass.add(tack);
        j++;
      }
    }
    return secondPass;
  }

}

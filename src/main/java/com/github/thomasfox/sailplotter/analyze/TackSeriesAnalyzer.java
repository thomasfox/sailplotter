package com.github.thomasfox.sailplotter.analyze;

import java.util.ArrayList;
import java.util.List;

import com.github.thomasfox.sailplotter.model.ManeuverType;
import com.github.thomasfox.sailplotter.model.PointOfSail;
import com.github.thomasfox.sailplotter.model.Tack;
import com.github.thomasfox.sailplotter.model.TackSeries;

public class TackSeriesAnalyzer
{
  public List<TackSeries> analyze(List<Tack> tacks)
  {
    List<TackSeries> result = new ArrayList<>();
    TackSeries currentTackSeries = null;
    for (int tackIndex = 0; tackIndex < tacks.size(); ++tackIndex)
    {
      Tack tack = tacks.get(tackIndex);
      if (tack.pointOfSail == PointOfSail.CLOSE_HAULED_PORT
          || tack.pointOfSail == PointOfSail.BEAM_REACH_PORT
          || tack.pointOfSail == PointOfSail.CLOSE_HAULED_STARBOARD
          || tack.pointOfSail == PointOfSail.BEAM_REACH_STARBOARD)
      {
        if (currentTackSeries == null)
        {
          currentTackSeries = new TackSeries(tackIndex);
        }
        else if (tack.maneuverTypeAtStart != ManeuverType.TACK || !tack.hasMainPoints())
        {
          if (currentTackSeries.getNumberOfTacks() >= 4)
          {
            result.add(currentTackSeries);
          }
          currentTackSeries = new TackSeries(tackIndex);
        }
        currentTackSeries.addTack(tack, tackIndex);
      }
      else if (currentTackSeries != null)
      {
        if (currentTackSeries.getNumberOfTacks() >= 4)
        {
          result.add(currentTackSeries);
        }
        currentTackSeries = null;
      }
    }
    return result;
  }
}

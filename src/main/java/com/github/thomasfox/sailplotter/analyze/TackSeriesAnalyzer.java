package com.github.thomasfox.sailplotter.analyze;

import java.util.ArrayList;
import java.util.List;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.model.Tack;
import com.github.thomasfox.sailplotter.model.TackSeries;
import com.github.thomasfox.sailplotter.model.TackSeriesType;

/**
 * Analyzes a list of tacks and extracts the contained tack series (legs).
 * This class is not thread safe.
 */
public class TackSeriesAnalyzer
{
  public List<TackSeries> result;

  TackSeries currentTackSeries;

  List<Tack> tacks;

  public List<TackSeries> analyze(List<Tack> tacks)
  {
    this.tacks = tacks;
    result = new ArrayList<>();
    currentTackSeries = null;
    for (int tackIndex = 0; tackIndex < tacks.size(); ++tackIndex)
    {
      boolean handled = false;
      handled = handleTack(tackIndex, TackSeriesType.WINDWARD);
      if (!handled)
      {
        handled = handleTack(tackIndex, TackSeriesType.DOWNWIND);
      }
      if (!handled)
      {
        if (currentTackSeries != null)
        {
          if (currentTackSeries.getNumberOfTacks() >= Constants.MIN_TACK_SERIES_SIZE)
          {
            result.add(currentTackSeries);
          }
          currentTackSeries = null;
        }
      }
    }
    return result;
  }

  private boolean handleTack(int tackIndex, TackSeriesType tackSeriesType)
  {
    if (currentTackSeries != null && currentTackSeries.type != tackSeriesType)
    {
      return false;
    }

    Tack tack = tacks.get(tackIndex);
    if (!tackSeriesType.getPointsOfSail().contains(tack.pointOfSail))
    {
      return false;
    }

    if (currentTackSeries == null)
    {
      currentTackSeries = new TackSeries(tackIndex, tackSeriesType);
    }
    else if (!tackSeriesType.getManeuverTypes().contains(tack.maneuverTypeAtStart) || !tack.hasMainPoints())
    {
      if (currentTackSeries.getNumberOfTacks() >= Constants.MIN_TACK_SERIES_SIZE)
      {
        result.add(currentTackSeries);
      }
      currentTackSeries = new TackSeries(tackIndex, TackSeriesType.WINDWARD);
    }
    currentTackSeries.addTack(tack, tackIndex);
    return true;
  }
}

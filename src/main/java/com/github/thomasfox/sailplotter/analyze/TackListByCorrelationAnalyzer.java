package com.github.thomasfox.sailplotter.analyze;

import java.util.List;

import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.Location;
import com.github.thomasfox.sailplotter.model.ManeuverType;
import com.github.thomasfox.sailplotter.model.PointOfSail;
import com.github.thomasfox.sailplotter.model.Tack;
import com.github.thomasfox.sailplotter.model.TackList;

public class TackListByCorrelationAnalyzer
{
  private static final double OFF_TACK_BEARING_THRESHOLD = Math.PI / 4; // 45 degrees

  private static final int OFF_TACK_COUNTS_STARTS_NEW = 2;

  private static final int ADJUSTMENT_RADIUS = 10;

  public TackList analyze(Data data)
  {
    List<DataPoint> points = data.getPointsWithLocation();
    TackList tackList = calculateTacksByMaxOffBearing(points);
    tackList = adjustTackStartAndEndPoint(tackList, points);

    for (int i = 1; i < tackList.size(); ++i)
    {
      Tack lastTack = tackList.get(i - 1);
      Tack nextTack = tackList.get(i);
      ManeuverType maneuverTypeBetweenTacks = determineManeuverTypeBetweenTacks(lastTack, nextTack);
      lastTack.maneuverTypeAtEnd = maneuverTypeBetweenTacks;
      nextTack.maneuverTypeAtStart = maneuverTypeBetweenTacks;
    }

    for (int i = 1; i < tackList.size(); ++i)
    {
      Tack lastTack = tackList.get(i - 1);
      Tack nextTack = tackList.get(i);
      if (lastTack.hasMainPoints() && nextTack.hasMainPoints())
      {
        Location intersection = Location.intersection(
            lastTack.getAfterStartManeuver().location,
            lastTack.getBeforeEndManeuver().location,
            nextTack.getAfterStartManeuver().location,
            nextTack.getBeforeEndManeuver().location);
        lastTack.tackStraightLineIntersectionEnd = new DataPoint(-1);
        lastTack.tackStraightLineIntersectionEnd.location = intersection;
        nextTack.tackStraightLineIntersectionStart = new DataPoint(-1);
        nextTack.tackStraightLineIntersectionStart.location = new Location(intersection);

        calculateTackIntersectionTimes(lastTack, nextTack);
      }
    }
    return tackList;
  }

  private TackList adjustTackStartAndEndPoint(TackList tacks, List<DataPoint> points)
  {
    for (int tackIndex = 1; tackIndex < tacks.size(); ++tackIndex)
    {
      Tack lastTack = tacks.get(tackIndex - 1);
      Tack nextTack = tacks.get(tackIndex);
      Double lastTackBearing = lastTack.getAbsoluteBearingInArcs();
      Double nextTackBearing = nextTack.getAbsoluteBearingInArcs();
      int startIndex = Math.max(lastTack.endOfTackDataPointIndex - ADJUSTMENT_RADIUS, lastTack.startOfTackDataPointIndex);
      int endIndex = Math.min(lastTack.endOfTackDataPointIndex + ADJUSTMENT_RADIUS, nextTack.endOfTackDataPointIndex);
      int countNearerToLast = 0;
      int countNearerToNext = 0;
      for (int pointIndex = startIndex; pointIndex <= endIndex; ++pointIndex)
      {
        Double bearingDifferenceToLastTack = points.get(pointIndex).getBearingDifference(lastTackBearing);
        Double bearingDifferenceToNextTack = points.get(pointIndex).getBearingDifference(nextTackBearing);
        if (bearingDifferenceToLastTack != null && bearingDifferenceToNextTack != null)
        {
          if (Math.abs(bearingDifferenceToLastTack) > Math.abs(bearingDifferenceToNextTack))
          {
            countNearerToNext++;
          }
          else if (Math.abs(bearingDifferenceToLastTack) < Math.abs(bearingDifferenceToNextTack))
          {
            countNearerToLast++;
          }
        }
      }
      int change = (countNearerToLast - countNearerToNext) / 2;
      if (-change < lastTack.endOfTackDataPointIndex - lastTack.startOfTackDataPointIndex)
      {
        lastTack.end(points.get(lastTack.endOfTackDataPointIndex + change), lastTack.endOfTackDataPointIndex + change, points);
      }
      if (change < lastTack.endOfTackDataPointIndex - lastTack.startOfTackDataPointIndex)
      {
        int dataPointIndex = nextTack.startOfTackDataPointIndex + change;
        if (dataPointIndex < 0)
        {
          dataPointIndex = 0;
        }
        nextTack.start(points.get(dataPointIndex), dataPointIndex);
      }
    }
    return tacks;
  }

  private ManeuverType determineManeuverTypeBetweenTacks(Tack lastTack, Tack nextTack)
  {
    if (((lastTack.pointOfSail == PointOfSail.CLOSE_HAULED_PORT || lastTack.pointOfSail == PointOfSail.BEAM_REACH_PORT)
          && (nextTack.pointOfSail == PointOfSail.CLOSE_HAULED_STARBOARD || nextTack.pointOfSail == PointOfSail.BEAM_REACH_STARBOARD))
        || (lastTack.pointOfSail == PointOfSail.CLOSE_HAULED_STARBOARD || lastTack.pointOfSail == PointOfSail.BEAM_REACH_STARBOARD
            && (nextTack.pointOfSail == PointOfSail.CLOSE_HAULED_PORT || nextTack.pointOfSail == PointOfSail.BEAM_REACH_PORT)))
    {
      return ManeuverType.TACK;
    }
    if ((lastTack.pointOfSail == PointOfSail.CLOSE_HAULED_PORT
        && (nextTack.pointOfSail == PointOfSail.BEAM_REACH_PORT || nextTack.pointOfSail == PointOfSail.BROAD_REACH_PORT))
      || (lastTack.pointOfSail == PointOfSail.BEAM_REACH_PORT
        && nextTack.pointOfSail == PointOfSail.BROAD_REACH_PORT)
      || (lastTack.pointOfSail == PointOfSail.CLOSE_HAULED_STARBOARD
          && (nextTack.pointOfSail == PointOfSail.BEAM_REACH_STARBOARD || nextTack.pointOfSail == PointOfSail.BROAD_REACH_STARBOARD))
      || (lastTack.pointOfSail == PointOfSail.BEAM_REACH_STARBOARD
          && nextTack.pointOfSail == PointOfSail.BROAD_REACH_STARBOARD))
    {
      return ManeuverType.BEAR_AWAY;
    }
    if ((lastTack.pointOfSail == PointOfSail.BROAD_REACH_PORT
        && nextTack.pointOfSail == PointOfSail.BEAM_REACH_PORT || nextTack.pointOfSail == PointOfSail.CLOSE_HAULED_PORT)
      || (lastTack.pointOfSail == PointOfSail.BROAD_REACH_PORT
        && nextTack.pointOfSail == PointOfSail.BEAM_REACH_PORT)
      || (lastTack.pointOfSail == PointOfSail.BROAD_REACH_STARBOARD
          && nextTack.pointOfSail == PointOfSail.BEAM_REACH_STARBOARD || nextTack.pointOfSail == PointOfSail.CLOSE_HAULED_STARBOARD)
      || (lastTack.pointOfSail == PointOfSail.BROAD_REACH_STARBOARD
          && nextTack.pointOfSail == PointOfSail.BEAM_REACH_STARBOARD))
    {
      return ManeuverType.HEAD_UP;
    }
    if ((lastTack.pointOfSail == PointOfSail.BROAD_REACH_PORT
          && nextTack.pointOfSail == PointOfSail.BROAD_REACH_STARBOARD)
        || (lastTack.pointOfSail == PointOfSail.BROAD_REACH_STARBOARD
            && nextTack.pointOfSail == PointOfSail.BROAD_REACH_PORT))
    {
      return ManeuverType.JIBE;
    }
    return nextTack.maneuverTypeAtStart = ManeuverType.UNKNOWN;
  }

  /**
   * Calculates the tack list by determining the points which bearing is off
   * from the average bearing of the tack by more than a certain amount.
   * If there are several off bearing points in a row, a new tack is started
   * at the off-bearing point where the maximum of off-bearing points
   * is exceeded.
   * This method tends to put the end tacks too late.
   *
   * @param points the measured data points
   * @return the list of tacks determined from the points.
   */
  private TackList calculateTacksByMaxOffBearing(List<DataPoint> points)
  {
    int tackIndex = 0;
    TackList firstPass = new TackList();
    Tack currentTack = null;
    int offTackCounter = 0;
    for (int dataPointIndex = 0; dataPointIndex < points.size(); ++dataPointIndex)
    {
      DataPoint point = points.get(dataPointIndex);
      if (point.location == null)
      {
        continue;
      }
      if (point.location.bearingFromLatLong == null)
      {
        if (currentTack == null)
        {
          continue;
        }
        // bearing == null means that current velocity is zero. Does not end current tack.
        // Does not increase offTackCounter but does not reset it either.
        currentTack.end(point, dataPointIndex, points);
      }
      else // if (point.bearing != null)
      {
        if (currentTack == null)
        {
          currentTack = new Tack();
          currentTack.index = tackIndex;
          currentTack.start(point, dataPointIndex);
          continue;
        }
        currentTack.end(point, dataPointIndex, points);
        Double bearingDifference = point.getBearingDifference(currentTack.getAbsoluteBearingInArcs());
        if (bearingDifference == null)
        {
          // null may happen if points have the same coordinates. ignore.
        }
        else
        {
          if (Math.abs(bearingDifference) > OFF_TACK_BEARING_THRESHOLD)
          {
            offTackCounter++;
          }
          else
          {
            offTackCounter = 0;
          }
        }
        currentTack.end(point, dataPointIndex, points);
        if (offTackCounter >= OFF_TACK_COUNTS_STARTS_NEW)
        {
          firstPass.add(currentTack);
          tackIndex++;
          currentTack = new Tack();
          currentTack.index = tackIndex;
          currentTack.start(point, dataPointIndex);
          offTackCounter = 0;
        }
      }
    }
    // TODO last tack
    return firstPass;
  }

  void calculateTackIntersectionTimes(Tack lastTack, Tack nextTack)
  {
    if (Math.abs(lastTack.getBeforeEndManeuver().location.getY() - lastTack.getAfterStartManeuver().location.getY()) > 1d)
    {
      lastTack.tackStraightLineIntersectionEnd.time = Double.valueOf(lastTack.getBeforeEndManeuver().time
          + (lastTack.getBeforeEndManeuver().time - lastTack.getAfterStartManeuver().time)
            * (lastTack.tackStraightLineIntersectionEnd.location.getY()- lastTack.getBeforeEndManeuver().location.getY())
            / (lastTack.getBeforeEndManeuver().location.getY() - lastTack.getAfterStartManeuver().location.getY()))
        .longValue();
    }
    else if (Math.abs(lastTack.getBeforeEndManeuver().location.getX() - lastTack.getAfterStartManeuver().location.getX()) > 1d)
    {
      lastTack.tackStraightLineIntersectionEnd.time = Double.valueOf(lastTack.getBeforeEndManeuver().time
            + (lastTack.getBeforeEndManeuver().time - lastTack.getAfterStartManeuver().time)
              * (lastTack.tackStraightLineIntersectionEnd.location.getX() - lastTack.getBeforeEndManeuver().location.getX())
              / (lastTack.getBeforeEndManeuver().location.getX() - lastTack.getAfterStartManeuver().location.getX()))
          .longValue();
    }

    if (Math.abs(nextTack.getBeforeEndManeuver().location.getY() - nextTack.getAfterStartManeuver().location.getY()) > 1d)
    {
      nextTack.tackStraightLineIntersectionStart.time = Double.valueOf(nextTack.getAfterStartManeuver().time
          + (nextTack.getBeforeEndManeuver().time - nextTack.getAfterStartManeuver().time)
            * (nextTack.tackStraightLineIntersectionStart.location.getY() - nextTack.getAfterStartManeuver().location.getY())
            / (nextTack.getBeforeEndManeuver().location.getY() - nextTack.getAfterStartManeuver().location.getY()))
        .longValue();
    }
    else if (Math.abs(nextTack.getBeforeEndManeuver().location.getX() - nextTack.getAfterStartManeuver().location.getX()) > 1d)
    {
      nextTack.tackStraightLineIntersectionStart.time = Double.valueOf(nextTack.getAfterStartManeuver().time
            + (nextTack.getBeforeEndManeuver().time - nextTack.getAfterStartManeuver().time)
              * (nextTack.tackStraightLineIntersectionStart.location.getX()- nextTack.getAfterStartManeuver().location.getX())
              / (nextTack.getBeforeEndManeuver().location.getX() - nextTack.getAfterStartManeuver().location.getX()))
          .longValue();
    }
  }
}

package com.github.thomasfox.sailplotter.analyze;

import java.util.ArrayList;
import java.util.List;

import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.ManeuverType;
import com.github.thomasfox.sailplotter.model.PointOfSail;
import com.github.thomasfox.sailplotter.model.Tack;

public class TackListByCorrelationAnalyzer
{
  private static final double OFF_TACK_BEARING = Math.PI / 4; // 45 degrees

  private static final int OFF_TACK_COUNTS_STARTS_NEW = 2;

  public List<Tack> analyze(List<DataPoint> points)
  {
    List<Tack> firstPass = new ArrayList<>();
    Tack currentTack = null;
    int offTackCounter = 0;
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
        offTackCounter++;
        currentTack.end = point;
        if (offTackCounter < OFF_TACK_COUNTS_STARTS_NEW)
        {
          continue;
        }
        currentTack.end(point, dataPointIndex, points);
        firstPass.add(currentTack);
        currentTack = null;
        offTackCounter = 0;
      }
      else // if (pointOfSail != null)
      {
        if (currentTack == null)
        {
          currentTack = new Tack();
          currentTack.start(point, dataPointIndex);
          continue;
        }
        currentTack.end(point, dataPointIndex, points);
        if (point.bearing == null)
        {
          offTackCounter++;
        }
        else if (currentTack.getAbsoluteBearingInArcs() == null)
        {
          // null may happen if the first points have the same coordinates. ignore.
        }
        else
        {
          double bearingDifference = point.bearing - currentTack.getAbsoluteBearingInArcs();
          if (bearingDifference > Math.PI)
          {
            bearingDifference -= 2 * Math.PI;
          }
          if (bearingDifference < -Math.PI)
          {
            bearingDifference += 2 * Math.PI;
          }
          if (Math.abs(bearingDifference) > OFF_TACK_BEARING)
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
          currentTack = new Tack();
          currentTack.start(point, dataPointIndex);
          offTackCounter = 0;
        }
      }
    }
    // TODO last tack

    for (int i = 1; i < firstPass.size(); ++i)
    {
      Tack lastTack = firstPass.get(i - 1);
      Tack nextTack = firstPass.get(i);
      if (((lastTack.pointOfSail == PointOfSail.CLOSE_HAULED_PORT || lastTack.pointOfSail == PointOfSail.BEAM_REACH_PORT)
            && (nextTack.pointOfSail == PointOfSail.CLOSE_HAULED_STARBOARD || nextTack.pointOfSail == PointOfSail.BEAM_REACH_STARBOARD))
          || (lastTack.pointOfSail == PointOfSail.CLOSE_HAULED_STARBOARD || lastTack.pointOfSail == PointOfSail.BEAM_REACH_STARBOARD
              && (nextTack.pointOfSail == PointOfSail.CLOSE_HAULED_PORT || nextTack.pointOfSail == PointOfSail.BEAM_REACH_PORT)))
      {
        lastTack.maneuverTypeAtEnd = ManeuverType.TACK;
        nextTack.maneuverTypeAtStart = ManeuverType.TACK;
      }
      else if ((lastTack.pointOfSail == PointOfSail.CLOSE_HAULED_PORT
          && (nextTack.pointOfSail == PointOfSail.BEAM_REACH_PORT || nextTack.pointOfSail == PointOfSail.BROAD_REACH_PORT))
        || (lastTack.pointOfSail == PointOfSail.BEAM_REACH_PORT
          && nextTack.pointOfSail == PointOfSail.BROAD_REACH_PORT)
        || (lastTack.pointOfSail == PointOfSail.CLOSE_HAULED_STARBOARD
            && (nextTack.pointOfSail == PointOfSail.BEAM_REACH_STARBOARD || nextTack.pointOfSail == PointOfSail.BROAD_REACH_STARBOARD))
        || (lastTack.pointOfSail == PointOfSail.BEAM_REACH_STARBOARD
            && nextTack.pointOfSail == PointOfSail.BROAD_REACH_STARBOARD))
      {
        lastTack.maneuverTypeAtEnd = ManeuverType.BEAR_AWAY;
        nextTack.maneuverTypeAtStart = ManeuverType.BEAR_AWAY;
      }
      else if ((lastTack.pointOfSail == PointOfSail.BROAD_REACH_PORT
          && nextTack.pointOfSail == PointOfSail.BEAM_REACH_PORT || nextTack.pointOfSail == PointOfSail.CLOSE_HAULED_PORT)
        || (lastTack.pointOfSail == PointOfSail.BROAD_REACH_PORT
          && nextTack.pointOfSail == PointOfSail.BEAM_REACH_PORT)
        || (lastTack.pointOfSail == PointOfSail.BROAD_REACH_STARBOARD
            && nextTack.pointOfSail == PointOfSail.BEAM_REACH_STARBOARD || nextTack.pointOfSail == PointOfSail.CLOSE_HAULED_STARBOARD)
        || (lastTack.pointOfSail == PointOfSail.BROAD_REACH_STARBOARD
            && nextTack.pointOfSail == PointOfSail.BEAM_REACH_STARBOARD))
      {
        lastTack.maneuverTypeAtEnd = ManeuverType.HEAD_UP;
        nextTack.maneuverTypeAtStart = ManeuverType.HEAD_UP;
      }
      else if ((lastTack.pointOfSail == PointOfSail.BROAD_REACH_PORT
            && nextTack.pointOfSail == PointOfSail.BROAD_REACH_STARBOARD)
          || (lastTack.pointOfSail == PointOfSail.BROAD_REACH_STARBOARD
              && nextTack.pointOfSail == PointOfSail.BROAD_REACH_PORT))
      {
        lastTack.maneuverTypeAtEnd = ManeuverType.JIBE;
        nextTack.maneuverTypeAtStart = ManeuverType.JIBE;
      }
      else
      {
        lastTack.maneuverTypeAtEnd = ManeuverType.UNKNOWN;
        nextTack.maneuverTypeAtStart = ManeuverType.UNKNOWN;
      }
      if (lastTack.hasMainPoints() && nextTack.hasMainPoints())
      {
        DataPoint intersection = DataPoint.intersection(
            lastTack.getAfterStartManeuver(),
            lastTack.getBeforeEndManeuver(),
            nextTack.getAfterStartManeuver(),
            nextTack.getBeforeEndManeuver());
        lastTack.tackStraightLineIntersectionEnd = new DataPoint(intersection);
        nextTack.tackStraightLineIntersectionStart = new DataPoint(intersection);

        calculateTackIntersectionTimes(lastTack, nextTack);
      }
    }
    return firstPass;
  }

  void calculateTackIntersectionTimes(Tack lastTack, Tack nextTack)
  {
    if (Math.abs(lastTack.getBeforeEndManeuver().getY() - lastTack.getAfterStartManeuver().getY()) > 1d)
    {
      lastTack.tackStraightLineIntersectionEnd.time = new Double(lastTack.getBeforeEndManeuver().time
          + (lastTack.getBeforeEndManeuver().time - lastTack.getAfterStartManeuver().time)
            * (lastTack.tackStraightLineIntersectionEnd.getY()- lastTack.getBeforeEndManeuver().getY())
            / (lastTack.getBeforeEndManeuver().getY() - lastTack.getAfterStartManeuver().getY()))
        .longValue();
    }
    else if (Math.abs(lastTack.getBeforeEndManeuver().getX() - lastTack.getAfterStartManeuver().getX()) > 1d)
    {
      lastTack.tackStraightLineIntersectionEnd.time = new Double(lastTack.getBeforeEndManeuver().time
            + (lastTack.getBeforeEndManeuver().time - lastTack.getAfterStartManeuver().time)
              * (lastTack.tackStraightLineIntersectionEnd.getX() - lastTack.getBeforeEndManeuver().getX())
              / (lastTack.getBeforeEndManeuver().getX() - lastTack.getAfterStartManeuver().getX()))
          .longValue();
    }

    if (Math.abs(nextTack.getBeforeEndManeuver().getY() - nextTack.getAfterStartManeuver().getY()) > 1d)
    {
      nextTack.tackStraightLineIntersectionStart.time = new Double(nextTack.getAfterStartManeuver().time
          + (nextTack.getBeforeEndManeuver().time - nextTack.getAfterStartManeuver().time)
            * (nextTack.tackStraightLineIntersectionStart.getY() - nextTack.getAfterStartManeuver().getY())
            / (nextTack.getBeforeEndManeuver().getY() - nextTack.getAfterStartManeuver().getY()))
        .longValue();
    }
    else if (Math.abs(nextTack.getBeforeEndManeuver().getX() - nextTack.getAfterStartManeuver().getX()) > 1d)
    {
      nextTack.tackStraightLineIntersectionStart.time = new Double(nextTack.getAfterStartManeuver().time
            + (nextTack.getBeforeEndManeuver().time - nextTack.getAfterStartManeuver().time)
              * (nextTack.tackStraightLineIntersectionStart.getX()- nextTack.getAfterStartManeuver().getX())
              / (nextTack.getBeforeEndManeuver().getX() - nextTack.getAfterStartManeuver().getX()))
          .longValue();
    }
  }
}

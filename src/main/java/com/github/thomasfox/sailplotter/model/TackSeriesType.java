package com.github.thomasfox.sailplotter.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public enum TackSeriesType
{
  WINDWARD(
      new PointOfSail[] {PointOfSail.CLOSE_HAULED_PORT, PointOfSail.BEAM_REACH_PORT, PointOfSail.CLOSE_HAULED_STARBOARD, PointOfSail.BEAM_REACH_STARBOARD},
      new ManeuverType[] {ManeuverType.TACK}),
  DOWNWIND(
      new PointOfSail[] {PointOfSail.BROAD_REACH_PORT, PointOfSail.BROAD_REACH_STARBOARD},
      new ManeuverType[] {ManeuverType.JIBE});

  private Set<PointOfSail> pointsOfSail;

  private Set<ManeuverType> maneuverTypes;

  private TackSeriesType(PointOfSail[] pointsOfSails, ManeuverType[] maneuverTypes)
  {
    this.pointsOfSail = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(pointsOfSails)));
    this.maneuverTypes = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(maneuverTypes)));
  }

  public Set<PointOfSail> getPointsOfSail()
  {
    return pointsOfSail;
  }

  public Set<ManeuverType> getManeuverTypes()
  {
    return maneuverTypes;
  }
}

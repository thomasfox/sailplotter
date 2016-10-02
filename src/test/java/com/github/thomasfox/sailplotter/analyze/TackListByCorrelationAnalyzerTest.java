package com.github.thomasfox.sailplotter.analyze;

import static  org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.Test;

import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.Tack;

public class TackListByCorrelationAnalyzerTest
{
  TackListByCorrelationAnalyzer sut = new TackListByCorrelationAnalyzer();

  @Test
  public void testCalculateTackIntersectionTimesByYDifference()
  {
    Tack lastTack = new Tack();
    lastTack.tackStraightLineIntersectionEnd = new DataPoint(-1);
    lastTack.tackStraightLineIntersectionEnd.setXAndY(0, 110);
    lastTack.pointsWithinTack = new ArrayList<>();
    DataPoint start = new DataPoint(0);
    start.time = 0l;
    start.setXAndY(0, 0);
    lastTack.pointsWithinTack.add(start);
    DataPoint point1 = new DataPoint(1);
    point1.time = 3000l;
    point1.setXAndY(0, 30);
    lastTack.pointsWithinTack.add(point1);
    DataPoint point2 = new DataPoint(2);
    point2.time = 7000l;
    point2.setXAndY(0, 70);
    lastTack.pointsWithinTack.add(point2);
    DataPoint end = new DataPoint(3);
    end.time = 11500l;
    end.setXAndY(0, 115);
    lastTack.pointsWithinTack.add(end);
    lastTack.startIndex = 0;
    lastTack.endIndex = 3;
    lastTack.start = start;
    lastTack.end = end;

    Tack nextTack = new Tack();
    nextTack.tackStraightLineIntersectionStart = new DataPoint(-1);
    nextTack.tackStraightLineIntersectionStart.setXAndY(0, 120);
    nextTack.pointsWithinTack = new ArrayList<>();
    start = end;
    nextTack.pointsWithinTack.add(start);
    point1 = new DataPoint(4);
    point1.time = 16000l;
    point1.setXAndY(0, 160);
    nextTack.pointsWithinTack.add(point1);
    point2 = new DataPoint(5);
    point2.time = 20000l;
    point2.setXAndY(0, 200);
    nextTack.pointsWithinTack.add(point2);
    end = new DataPoint(6);
    end.time = 23000l;
    end.setXAndY(0, 230);
    nextTack.pointsWithinTack.add(end);
    nextTack.startIndex = 3;
    nextTack.endIndex = 6;
    nextTack.start = start;
    nextTack.end = end;

    // execute
    sut.calculateTackIntersectionTimes(lastTack, nextTack);

    // verify
    assertThat(lastTack.tackStraightLineIntersectionEnd.time).isBetween(10999l, 11001l);
    assertThat(nextTack.tackStraightLineIntersectionStart.time).isBetween(11999l, 12001l);
  }

  @Test
  public void testCalculateTackIntersectionTimesByXDifference()
  {
    Tack lastTack = new Tack();
    lastTack.tackStraightLineIntersectionEnd = new DataPoint(-1);
    lastTack.tackStraightLineIntersectionEnd.setXAndY(110, 0d);
    lastTack.pointsWithinTack = new ArrayList<>();
    DataPoint start = new DataPoint(0);
    start.time = 0l;
    start.setXAndY(0, 0);
    lastTack.pointsWithinTack.add(start);
    DataPoint point1 = new DataPoint(1);
    point1.time = 3000l;
    point1.setXAndY(30, 0);
    lastTack.pointsWithinTack.add(point1);
    DataPoint point2 = new DataPoint(2);
    point2.time = 7000l;
    point2.setXAndY(70, 0);
    lastTack.pointsWithinTack.add(point2);
    DataPoint end = new DataPoint(3);
    end.time = 11500l;
    end.setXAndY(115, 0);
    lastTack.pointsWithinTack.add(end);
    lastTack.startIndex = 0;
    lastTack.endIndex = 3;
    lastTack.start = start;
    lastTack.end = end;

    Tack nextTack = new Tack();
    nextTack.tackStraightLineIntersectionStart = new DataPoint(-1);
    nextTack.tackStraightLineIntersectionStart.setXAndY(120, 0d);
    nextTack.pointsWithinTack = new ArrayList<>();
    start = end;
    nextTack.pointsWithinTack.add(start);
    point1 = new DataPoint(5);
    point1.time = 16000l;
    point1.setXAndY(160, 0);
    nextTack.pointsWithinTack.add(point1);
    point2 = new DataPoint(6);
    point2.time = 20000l;
    point2.setXAndY(200, 0);
    nextTack.pointsWithinTack.add(point2);
    end = new DataPoint(7);
    end.time = 23000l;
    end.setXAndY(230, 0);
    nextTack.pointsWithinTack.add(end);
    nextTack.startIndex = 4;
    nextTack.endIndex = 7;
    nextTack.start = start;
    nextTack.end = end;

    // execute
    sut.calculateTackIntersectionTimes(lastTack, nextTack);

    // verify
    assertThat(lastTack.tackStraightLineIntersectionEnd.time).isBetween(10999l, 11001l);
    assertThat(nextTack.tackStraightLineIntersectionStart.time).isBetween(11999l, 12001l);
  }
}

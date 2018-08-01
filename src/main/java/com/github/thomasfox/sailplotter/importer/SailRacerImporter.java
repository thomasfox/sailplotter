package com.github.thomasfox.sailplotter.importer;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.util.ArrayList;
import java.util.List;

import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.Location;

public class SailRacerImporter implements Importer
{
  private static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
      .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
      .appendLiteral('.')
      .appendValue(MONTH_OF_YEAR, 2)
      .appendLiteral('.')
      .appendValue(DAY_OF_MONTH, 2)
      .appendLiteral(' ')
      .appendValue(HOUR_OF_DAY, 2)
      .appendLiteral(':')
      .appendValue(MINUTE_OF_HOUR, 2)
      .optionalStart()
      .appendLiteral(':')
      .appendValue(SECOND_OF_MINUTE, 2)
      .appendLiteral(':')
      .appendFraction(NANO_OF_SECOND, 0, 9, false)
      .toFormatter();


  @Override
  public Data read(File file)
  {
    Data result = new Data();
    List<SailRacerPoint> rawData = readFileInternal(file);
    int index = 0;
    for (SailRacerPoint rawPoint : rawData)
    {
      DataPoint dataPoint = new DataPoint(index);
      dataPoint.location = new Location();
      dataPoint.location.latitude = rawPoint.lat / 180d * Math.PI;
      dataPoint.location.longitude = rawPoint.lon / 180d * Math.PI;
      dataPoint.time = rawPoint.datetime.toInstant(ZoneOffset.UTC).toEpochMilli();
      result.add(dataPoint);
      index++;
    }
    return result;
  }

  public List<SailRacerPoint> readFileInternal(File file)
  {
    List<SailRacerPoint> result = new ArrayList<>();
    try (InputStream is = new FileInputStream(file))
    {
      BufferedReader reader = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"));
      String line;
      while ((line = reader.readLine()) != null)
      {
        result.add(new SailRacerPoint(line));
      }
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
    return result;
  }

  public static final class SailRacerPoint
  {
    /** 2016.09.21 14:02:16:000. The Datetime is in GMT*/
    public LocalDateTime datetime;

    public double lat;

    public double lon;

    /** absolute? relative ? */
    public int bearing1;

    /** unit? perhaps m/s? */
    public double velocity;

    /** absolute? relative ? */
    public int bearing2;

    /** not sure what this is. */
    public double race;

    public SailRacerPoint(String line)
    {
      String[] token = line.split("\t");
      datetime = LocalDateTime.parse(token[0], dateTimeFormatter);
      lat = Double.parseDouble(token[1]);
      lon = Double.parseDouble(token[2]);
      bearing1 = Integer.parseInt(token[3]);
      velocity = Double.parseDouble(token[4]);
      bearing2 = Integer.parseInt(token[5]);
      race = Double.parseDouble(token[6]);
    }
  }
}

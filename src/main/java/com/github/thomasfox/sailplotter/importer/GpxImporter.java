package com.github.thomasfox.sailplotter.importer;

import com.github.thomasfox.sailplotter.gui.component.progress.LoadProgress;
import com.github.thomasfox.sailplotter.importer.saillogger.SailLoggerData;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.Location;
import com.urizev.gpx.GPXParser;
import com.urizev.gpx.beans.GPX;
import com.urizev.gpx.beans.Track;
import com.urizev.gpx.beans.Waypoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Set;

public class GpxImporter implements Importer {

    private final LoadProgress loadProgress;

    public GpxImporter(LoadProgress loadProgress)
    {
        this.loadProgress = loadProgress;
    }

    @Override
    public Data read(File file) {

        try
        {
            Data result = new Data();

            System.out.println("reading gpx" + file);
            loadProgress.fileReadingStarted();
            GPXParser p = new GPXParser();
            FileInputStream in = new FileInputStream(file);
            GPX gpx = p.parseGPX(in);
            in.close();

            Set<Track> tracks = gpx.getTracks();
            System.out.println("track is : "+tracks.size());

            ArrayList<Waypoint> trkpt = tracks.iterator().next().getTrackPoints();
            System.out.println("length " + trkpt.size());
            int index = 0;

            for (int i = 0; i < trkpt.size(); i++) {
                //System.out.println("idx: " +i);
                DataPoint dataPoint = new DataPoint(index);
                dataPoint.location = new Location();
                dataPoint.location.latitude = trkpt.get(i).getLatitude();
                dataPoint.location.longitude = trkpt.get(i).getLongitude();
                try {
                    dataPoint.time = trkpt.get(i).getTime().toInstant().toEpochMilli();
                } catch (Exception e) {
                    System.out.println("pass idx: " +i);
                    continue;
                }
                result.add(dataPoint);
                index++;
            }

            loadProgress.fileReadingFinished();

            return result;
        }
        catch (Exception e)
        {
            loadProgress.finished();
            throw new RuntimeException(e);
        }
    }
}

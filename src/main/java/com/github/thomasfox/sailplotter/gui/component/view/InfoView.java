package com.github.thomasfox.sailplotter.gui.component.view;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import com.github.thomasfox.sailplotter.gui.SwingGui;
import com.github.thomasfox.sailplotter.gui.component.panel.CommentPanel;
import com.github.thomasfox.sailplotter.gui.component.panel.ZoomPanelChangeEvent;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.vector.CoordinateSystem;

public class InfoView extends AbstractView
{
  private static final long serialVersionUID = 1L;

  private final JLabel filenameLabel = new JLabel();

  private final JLabel dataSizeLabel = new JLabel();

  private final JLabel dataFrequencyLabel = new JLabel();

  private final JLabel startTimeLabel = new JLabel();

  private final JLabel endTimeLabel = new JLabel();

  private final JLabel coordinateSystemLabel = new JLabel();

  private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

  private final CommentPanel commentPanel;

  private Data data;

  public InfoView(SwingGui gui)
  {
    commentPanel = new CommentPanel();
    createLayout()
        .withWeighty(0.01)
        .add(filenameLabel);
    filenameLabel.setBorder(new EmptyBorder(0, 10, 0, 10));
    createLayout()
        .withGridy(1)
        .withWeighty(0.01)
        .add(dataSizeLabel);
    dataSizeLabel.setBorder(new EmptyBorder(0, 10, 0, 10));
    createLayout()
    .withGridy(2)
    .withWeighty(0.01)
    .add(dataFrequencyLabel);
    dataFrequencyLabel.setBorder(new EmptyBorder(0, 10, 0, 10));
    createLayout()
        .withGridy(3)
        .withWeighty(0.01)
        .add(startTimeLabel);
    startTimeLabel.setBorder(new EmptyBorder(0, 10, 0, 10));
    createLayout()
        .withGridy(4)
        .withWeighty(0.01)
        .add(endTimeLabel);
    endTimeLabel.setBorder(new EmptyBorder(0, 10, 0, 10));
    createLayout()
        .withGridy(5)
        .withWeighty(0.01)
        .add(coordinateSystemLabel);
    coordinateSystemLabel.setBorder(new EmptyBorder(0, 10, 0, 10));
    createLayout()
        .withGridy(6)
        .withWeighty(0.96)
        .add(commentPanel);
    commentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
  }

  public void redisplay()
  {
    if (data == null)
    {
      commentPanel.setText(null);
    }
    else
    {
      commentPanel.setText(data.comment);
    }
  }

  public void dataChanged(Data data)
  {
    this.data = data;
    filenameLabel.setText("File: " + data.getFile().getAbsolutePath());
    setDataSizeLabelText(data);
    setDataFrequencyLabelText(data);
    setStartTimeLabelText(data);
    setEndTimeLabelText(data);
    setCoordinateSystemText(data);
    commentPanel.setTextConsumer(data::setComment);
  }

  private void setDataSizeLabelText(Data data)
  {
    dataSizeLabel.setText("Data size: " + data.size()
        + " (location data size: "+ data.getPointsWithLocation().size()
        + ", magnetic field data size: " + data.getPointsWithMagneticField().size()
        + ", acceleration data size: " + data.getPointsWithAcceleration().size()
        + ")");
  }

  private void setDataFrequencyLabelText(Data data)
  {
    dataFrequencyLabel.setText("location frequency: "
         + new DecimalFormat("#.###").format(data.getAverageLocationPointFrequency())
         + " magnetic field frequency: "
             + new DecimalFormat("#.###").format(data.getAverageMagneticFieldPointFrequency())
         + " acceleration frequency: "
             + new DecimalFormat("#.###").format(data.getAverageAccelerationPointFrequency()));
  }

  private void setStartTimeLabelText(Data data)
  {
    String startTime = "Start time: " + dateTimeFormat.format(data.getStartTime()) + " (";
    boolean comma = false;
    if (data.getPointsWithLocation().size() > 0)
    {
      startTime += "location data start: " + dateTimeFormat.format(data.getLocationStartTime());
      comma = true;
    }
    if (data.getPointsWithMagneticField().size() > 0)
    {
      if (comma)
      {
        startTime += ", ";
      }
      startTime += "magnetic field data start: " + dateTimeFormat.format(data.getMagneticFieldStartTime());
      comma = true;
    }
    if (data.getPointsWithAcceleration().size() > 0)
    {
      if (comma)
      {
        startTime += ", ";
      }
      startTime += "acceleration data start: "
          + dateTimeFormat.format(data.getPointsWithAcceleration().get(0).time);
    }
    startTime += ")";
    startTimeLabel.setText(startTime);
  }

  private void setEndTimeLabelText(Data data)
  {
    String endTime = "End time: " + dateTimeFormat.format(data.getEndTime()) + " (";
    boolean comma = false;
    if (data.getPointsWithLocation().size() > 0)
    {
      endTime += "location data end: " + dateTimeFormat.format(data.getLocationEndTime());
      comma = true;
    }
    if (data.getPointsWithMagneticField().size() > 0)
    {
      if (comma)
      {
        endTime += ", ";
      }
      endTime += "magnetic field data end: " + dateTimeFormat.format(data.getMagneticFieldEndTime());
      comma = true;
    }
    if (data.getPointsWithAcceleration().size() > 0)
    {
      if (comma)
      {
        endTime += ", ";
      }
      endTime += "acceleration data end: "
          + dateTimeFormat.format(data.getPointsWithAcceleration().get(data.getPointsWithAcceleration().size() - 1).time);
    }
    endTime += ")";
    endTimeLabel.setText(endTime);
  }

  private void setCoordinateSystemText(Data data)
  {
    StringBuilder text = new StringBuilder("Boat coordinate system in device coordinates:");
    CoordinateSystem boatCoordinates = data.getBoatCoordinatesInDeviceCoordinates();
    if (boatCoordinates != null)
    {
      text.append(" front: ").append(boatCoordinates.x.toString(3));
      text.append(" left: ").append(boatCoordinates.y.toString(3));
      text.append(" up:").append(boatCoordinates.z.toString(3));
    }
    coordinateSystemLabel.setText(text.toString());
  }

  @Override
  public void processZoomPanelChangeEvent(ZoomPanelChangeEvent e)
  {
    // no action needed as we have no zoom panel
  }

}

package com.github.thomasfox.sailplotter.gui.component.view;

import java.text.SimpleDateFormat;

import javax.swing.JLabel;

import com.github.thomasfox.sailplotter.gui.CommentPanel;
import com.github.thomasfox.sailplotter.gui.SwingGui;
import com.github.thomasfox.sailplotter.gui.ZoomPanelChangeEvent;
import com.github.thomasfox.sailplotter.model.Data;

public class InfoView extends AbstractView
{
  private static final long serialVersionUID = 1L;

  private final JLabel filenameLabel = new JLabel();

  private final JLabel dataSizeLabel = new JLabel();

  private final JLabel startTimeLabel = new JLabel();

  private final JLabel endTimeLabel = new JLabel();

  private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

  private final CommentPanel commentPanel;

  private Data data;

  public InfoView(SwingGui gui)
  {
    commentPanel = new CommentPanel();
    createLayout()
        .withWeighty(0.01)
        .add(filenameLabel);
    createLayout()
        .withGridy(1)
        .withWeighty(0.01)
        .add(dataSizeLabel);
    createLayout()
        .withGridy(2)
        .withWeighty(0.01)
        .add(startTimeLabel);
    createLayout()
        .withGridy(3)
        .withWeighty(0.01)
        .add(endTimeLabel);
    createLayout()
        .withGridy(4)
        .withWeighty(0.96)
        .add(commentPanel);
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
    dataSizeLabel.setText("Data size: " + data.getAllPoints().size()
        + " (location data size: "+ data.getPointsWithLocation().size()
        + ", magnetic field data size: " + data.getPointsWithMagneticField().size()
        + ", acceleration data size: " + data.getPointsWithAcceleration().size()
        + ")");
    setStartTimeLabelText(data);
    setEndTimeLabelText(data);
    commentPanel.setTextConsumer(data::setComment);
  }

  private void setStartTimeLabelText(Data data)
  {
    String startTime = "Start time: " + dateTimeFormat.format(data.getAllPoints().get(0).time) + " (";
    boolean comma = false;
    if (data.getPointsWithLocation().size() > 0)
    {
      startTime += "location data start: "
          + dateTimeFormat.format(data.getPointsWithLocation().get(0).time);
      comma = true;
    }
    if (data.getPointsWithMagneticField().size() > 0)
    {
      if (comma)
      {
        startTime += ", ";
      }
      startTime += "magnetic field data start: "
          + dateTimeFormat.format(data.getPointsWithMagneticField().get(0).time);
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
    String endTime = "End time: " + dateTimeFormat.format(data.getAllPoints().get(data.getAllPoints().size() - 1).time) + " (";
    boolean comma = false;
    if (data.getPointsWithLocation().size() > 0)
    {
      endTime += "location data end: "
          + dateTimeFormat.format(data.getPointsWithLocation().get(data.getPointsWithLocation().size() - 1).time);
      comma = true;
    }
    if (data.getPointsWithMagneticField().size() > 0)
    {
      if (comma)
      {
        endTime += ", ";
      }
      endTime += "magnetic field data end: "
          + dateTimeFormat.format(data.getPointsWithMagneticField().get(data.getPointsWithMagneticField().size() - 1).time);
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

  @Override
  public void alignZoomPanelToChangeEvent(ZoomPanelChangeEvent e)
  {
    // no action needed as we have no zoom panel
  }

}

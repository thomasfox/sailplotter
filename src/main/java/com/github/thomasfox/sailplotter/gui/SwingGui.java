package com.github.thomasfox.sailplotter.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.analyze.DeviceOrientationAnalyzer;
import com.github.thomasfox.sailplotter.analyze.LocationInterpolator;
import com.github.thomasfox.sailplotter.analyze.TackListByCorrelationAnalyzer;
import com.github.thomasfox.sailplotter.analyze.TackSeriesAnalyzer;
import com.github.thomasfox.sailplotter.analyze.UseGpsTimeDataCorrector;
import com.github.thomasfox.sailplotter.analyze.VelocityBearingAnalyzer;
import com.github.thomasfox.sailplotter.exporter.Exporter;
import com.github.thomasfox.sailplotter.gui.component.plot.AbstractPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.FullMapPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.FullVelocityBearingOverTimePlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.TackVelocityBearingPolarPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.VelocityBearingPolarPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedBearingHistogramPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedBearingOverTimePlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedHeelOverTimePlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedMapPlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedRollOverTimePlotPanel;
import com.github.thomasfox.sailplotter.gui.component.plot.ZoomedVelocityBearingOverTimePlotPanel;
import com.github.thomasfox.sailplotter.gui.component.table.TackSeriesTablePanel;
import com.github.thomasfox.sailplotter.gui.component.table.TackTablePanel;
import com.github.thomasfox.sailplotter.importer.FormatAwareImporter;
import com.github.thomasfox.sailplotter.model.Data;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.Tack;
import com.github.thomasfox.sailplotter.model.TackSeries;

public class SwingGui
{
  private static final String OVERVIEW_VIEW_NAME = "Overview";

  private static final String DIRECTIONS_VIEW_NAME = "Directions";

  private static final String COMMENTS_VIEW_NAME = "Comments";

  private final JFrame frame;

  private final ZoomPanel zoomPanel;

  private final Menubar menubar;

  private final AbstractPlotPanel fullVelocityBearingOverTimePlotPanel;

  private final AbstractPlotPanel zoomedVelocityBearingOverTimePlotPanel;

  private final AbstractPlotPanel zoomedBearingHistogramPlotPanel;

  private final AbstractPlotPanel fullMapPlotPanel;

  private final AbstractPlotPanel zoomedMapPlotPanel;

  private final AbstractPlotPanel velocityBearingPolarPlotPanel;

  private final AbstractPlotPanel tackVelocityBearingPolarPlotPanel;

  private final AbstractPlotPanel zoomedBearingOverTimePlotPanel;

  private final AbstractPlotPanel zoomedHeelOverTimePlotPanel;

  private final AbstractPlotPanel zoomedRollOverTimePlotPanel;

  Data data;

  List<DataPoint> pointsWithLocation;

  List<TackSeries> tackSeriesList;

  TackTablePanel tackTablePanel;

  TackSeriesTablePanel tackSeriesTablePanel;

  JPanel views;

  CommentPanel commentPanel;

  double windBearing;

  boolean inUpdate = false;

  public SwingGui(String filePath, int windDirectionInDegrees)
  {
    this.windBearing = 2 * Math.PI * windDirectionInDegrees / 360d;
    data = new FormatAwareImporter().read(new File(filePath));
    analyze();
    zoomPanel = new ZoomPanel(pointsWithLocation.size());

    MainPanel overview = new MainPanel();
    MainPanel directions = new MainPanel();
    MainPanel comments = new MainPanel();

    views = new JPanel(new CardLayout());
    views.add(overview, OVERVIEW_VIEW_NAME);
    views.add(directions, DIRECTIONS_VIEW_NAME);
    views.add(comments, COMMENTS_VIEW_NAME);

    frame = new JFrame("SailPlotter");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    menubar = new Menubar(frame)
        .addLoadFileMenuItem(new File(filePath), this::loadFile)
        .addSaveFileMenuItem(new Exporter().replaceExtension(new File(filePath)), this::saveFile)
        .addViews(this::changeView,
            OVERVIEW_VIEW_NAME,
            DIRECTIONS_VIEW_NAME,
            COMMENTS_VIEW_NAME);
    frame.setJMenuBar(menubar);

    frame.getContentPane().add(views, BorderLayout.CENTER);

    fullVelocityBearingOverTimePlotPanel = new FullVelocityBearingOverTimePlotPanel(data, zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    overview.layoutForAdding().gridx(0).gridy(0).weightx(0.333).weighty(0.25)
        .add(fullVelocityBearingOverTimePlotPanel);

    zoomedVelocityBearingOverTimePlotPanel = new ZoomedVelocityBearingOverTimePlotPanel(data, zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    overview.layoutForAdding().gridx(1).gridy(0).weightx(0.333).weighty(0.25)
        .add(zoomedVelocityBearingOverTimePlotPanel);

    JPanel topRightPanel = new JPanel();
    zoomedBearingHistogramPlotPanel = new ZoomedBearingHistogramPlotPanel(data, zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    topRightPanel.add(zoomedBearingHistogramPlotPanel);

    zoomPanel.addListener(this::zoomPanelStateChanged);
    topRightPanel.add(zoomPanel);

    JPanel windDirectionPanel = new JPanel();
    JLabel windDirectionLabel = new JLabel("Wind direction");
    windDirectionPanel.add(windDirectionLabel);
    JTextField windDirectionTextField = new JTextField();
    Dimension windDirectionTextFieldSize = windDirectionTextField.getPreferredSize();
    windDirectionTextFieldSize.width=30;
    windDirectionTextField.setPreferredSize(windDirectionTextFieldSize);
    windDirectionTextField.setText(Integer.toString(windDirectionInDegrees));
    windDirectionTextField.addActionListener(this::windDirectionChanged);
    windDirectionPanel.add(windDirectionTextField);
    topRightPanel.add(windDirectionPanel);

    topRightPanel.setLayout(new BoxLayout(topRightPanel, BoxLayout.PAGE_AXIS));
    overview.layoutForAdding().gridx(2).gridy(0).weightx(0.333).weighty(0.25).columnSpan(2)
        .add(topRightPanel);

    fullMapPlotPanel = new FullMapPlotPanel(data, zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    overview.layoutForAdding().gridx(0).gridy(1).weightx(0.333).weighty(0.5)
        .add(fullMapPlotPanel);

    zoomedMapPlotPanel = new ZoomedMapPlotPanel(data, zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    overview.layoutForAdding().gridx(1).gridy(1).weightx(0.333).weighty(0.5)
        .add(zoomedMapPlotPanel);

    tackVelocityBearingPolarPlotPanel = new TackVelocityBearingPolarPlotPanel(data, zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    overview.layoutForAdding().gridx(2).gridy(1).weightx(0.166).weighty(0.5)
        .add(tackVelocityBearingPolarPlotPanel);

    velocityBearingPolarPlotPanel = new VelocityBearingPolarPlotPanel(data, zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    overview.layoutForAdding().gridx(3).gridy(1).weightx(0.166).weighty(0.5)
        .add(velocityBearingPolarPlotPanel);

    tackTablePanel = new TackTablePanel(data.getTackList(), this::tackSelected);
    overview.layoutForAdding().gridx(0).gridy(2).weightx(0.666).weighty(0.25).columnSpan(2)
        .add(tackTablePanel);

    tackSeriesTablePanel = new TackSeriesTablePanel(tackSeriesList, this::tackSeriesSelected);
    overview.layoutForAdding().gridx(2).gridy(2).weightx(0.666).weighty(0.25).columnSpan(2)
        .add(tackSeriesTablePanel);

    zoomedBearingOverTimePlotPanel = new ZoomedBearingOverTimePlotPanel(data, zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    directions.layoutForAdding().gridx(0).gridy(0).weightx(0.5).weighty(0.5)
        .add(zoomedBearingOverTimePlotPanel);

    zoomedHeelOverTimePlotPanel = new ZoomedHeelOverTimePlotPanel(data, zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    directions.layoutForAdding().gridx(1).gridy(0).weightx(0.5).weighty(0.5)
        .add(zoomedHeelOverTimePlotPanel);

    zoomedRollOverTimePlotPanel = new ZoomedRollOverTimePlotPanel(data, zoomPanel.getStartIndex(), zoomPanel.getZoomIndex());
    directions.layoutForAdding().gridx(0).gridy(1).weightx(0.5).weighty(0.5)
        .add(zoomedRollOverTimePlotPanel);

    commentPanel = new CommentPanel(data.comment, data::setComment);
    comments.layoutForAdding().gridx(0).gridy(0).weightx(1).weighty(0.9)
      .add(commentPanel);

    frame.pack();
    frame.setVisible(true);
  }

  public static void main(String[] args)
  {
    if (args.length != 2)
    {
      printUsage();
      return;
    }
    String filename = args[0];
    File file;
    try
    {
      file = new File(filename);
    }
    catch (Exception e)
    {
      printUsage();
      return;
    }
    if (!file.canRead())
    {
      System.out.println("File " + filename + " cannot be read");
      return;
    }
    int windDirectionInDegreees;
    try
    {
      windDirectionInDegreees = Integer.parseInt(args[1]);
    }
    catch (Exception e)
    {
      printUsage();
      return;
    }
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run() {
        new SwingGui(filename, windDirectionInDegreees);
      }
    });
  }

  private static void printUsage()
  {
    System.out.println("Usage: ${startcommand} ${file} ${windDirectionInDegreees}");
  }

  public void analyze()
  {
    pointsWithLocation = data.getPointsWithLocation();
    new UseGpsTimeDataCorrector().correct(data);
    new LocationInterpolator().interpolateLocation(data);
    new VelocityBearingAnalyzer().analyze(data, windBearing);
    data.getTackList().clear();
    data.getTackList().addAll(new TackListByCorrelationAnalyzer().analyze(data));
    tackSeriesList = new TackSeriesAnalyzer().analyze(data.getTackList());
    new DeviceOrientationAnalyzer().analyze(data);
  }

  public void redisplay(boolean updateTableContent)
  {
    try
    {
      inUpdate = true;
      int zoomWindowStartIndex = zoomPanel.getStartIndex();
      int zoomWindowZoomIndex = zoomPanel.getZoomIndex();
      fullVelocityBearingOverTimePlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
      zoomedVelocityBearingOverTimePlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
      zoomedBearingHistogramPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
      fullMapPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
      zoomedMapPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
      velocityBearingPolarPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
      tackVelocityBearingPolarPlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
      zoomedBearingOverTimePlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
      zoomedHeelOverTimePlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
      zoomedRollOverTimePlotPanel.zoomChanged(zoomWindowStartIndex, zoomWindowZoomIndex);
      commentPanel.setText(data.comment);
      if (updateTableContent)
      {
        tackTablePanel.updateContent(data.getTackList());
        tackSeriesTablePanel.updateContent(tackSeriesList);
      }
    }
    finally
    {
      inUpdate = false;
    }
  }

  public void zoomPanelStateChanged(ZoomPanelChangeEvent e)
  {
    redisplay(false);
  }

  public void tackSelected(ListSelectionEvent e)
  {
    if (e.getValueIsAdjusting() || inUpdate)
    {
      return;
    }
    int index = tackTablePanel.getSelectedTackIndex();
    Tack tack = data.getTackList().get(index);
    zoomPanel.setStartIndex(Math.max(tack.startOfTackDataPointIndex - Constants.NUM_DATAPOINTS_TACK_EXTENSION, 0));
    zoomPanel.setZoomIndex(Math.min(
        Math.max(
            Constants.NUMER_OF_ZOOM_TICKS * (tack.endOfTackDataPointIndex - tack.startOfTackDataPointIndex + 2 * Constants.NUM_DATAPOINTS_TACK_EXTENSION) / (pointsWithLocation.size()),
            3),
        Constants.NUMER_OF_ZOOM_TICKS));
  }

  public void windDirectionChanged(ActionEvent event)
  {
    String inputValue = event.getActionCommand();
    try
    {
      int newWindDirection = Integer.parseInt(inputValue);
      this.windBearing = newWindDirection * Math.PI / 180d;
      dataChanged();
      redisplay(true);
    }
    catch (Exception e)
    {
      System.err.println("Could not update wind direction");
      e.printStackTrace(System.err);
    }
  }

  public void tackSeriesSelected(ListSelectionEvent e)
  {
    if (e.getValueIsAdjusting() || inUpdate)
    {
      return;
    }
    int index = tackSeriesTablePanel.getSelectedTackSeriesIndex();
    TackSeries tackSeries = tackSeriesList.get(index);
    try
    {
      inUpdate = true;
      tackTablePanel.selectInterval(tackSeries.startTackIndex, tackSeries.endTackIndex);
      zoomPanel.setStartIndex(Math.max(data.getTackList().get(tackSeries.startTackIndex).startOfTackDataPointIndex - Constants.NUM_DATAPOINTS_TACK_EXTENSION, 0));
      zoomPanel.setZoomIndex(Math.min(
          Math.max(
              Constants.NUMER_OF_ZOOM_TICKS * (data.getTackList().get(tackSeries.endTackIndex).endOfTackDataPointIndex - data.getTackList().get(tackSeries.startTackIndex).startOfTackDataPointIndex + 2 * Constants.NUM_DATAPOINTS_TACK_EXTENSION) / (pointsWithLocation.size()),
              3),
          Constants.NUMER_OF_ZOOM_TICKS));
    }
    finally
    {
      inUpdate = false;
    }
    redisplay(false);
  }

  public void loadFile(File file)
  {
    try
    {
      data = new FormatAwareImporter().read(file);
      menubar.setLoadStartFile(file);
      menubar.setSaveStartFile(new Exporter().replaceExtension(file));
      dataChanged();
      redisplay(true);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      JOptionPane.showMessageDialog(
          frame,
          "Could not load File: " + e.getClass().getName() + ":" + e.getMessage(),
          "Error loading File",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  public void dataChanged()
  {
    analyze();
    zoomPanel.setDataSize(pointsWithLocation.size());
    fullVelocityBearingOverTimePlotPanel.dataChanged(data);
    zoomedVelocityBearingOverTimePlotPanel.dataChanged(data);
    zoomedBearingHistogramPlotPanel.dataChanged(data);
    fullMapPlotPanel.dataChanged(data);
    zoomedMapPlotPanel.dataChanged(data);
    tackVelocityBearingPolarPlotPanel.dataChanged(data);
    velocityBearingPolarPlotPanel.dataChanged(data);
    zoomedBearingOverTimePlotPanel.dataChanged(data);
    zoomedHeelOverTimePlotPanel.dataChanged(data);
    zoomedRollOverTimePlotPanel.dataChanged(data);
  }

  public void saveFile(File file)
  {
    try
    {
      if (file.exists()) {
        JOptionPane.showMessageDialog(
            frame,
            "File exists" ,
            "Error saving File",
            JOptionPane.ERROR_MESSAGE);
      }
      else
      {
        new Exporter().save(file, data);
        JOptionPane.showMessageDialog(
            frame,
            "File saved: " + file.getName() ,
            "File saved",
            JOptionPane.INFORMATION_MESSAGE);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      JOptionPane.showMessageDialog(
          frame,
          "Could not save File: " + e.getClass().getName() + ":" + e.getMessage(),
          "Error saving File",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  public void changeView(String viewName)
  {
    CardLayout cl = (CardLayout)(views.getLayout());
    cl.show(views, viewName);
  }
}
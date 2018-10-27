package com.github.thomasfox.sailplotter.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.analyze.DeviceOrientationAnalyzer;
import com.github.thomasfox.sailplotter.analyze.LocationInterpolator;
import com.github.thomasfox.sailplotter.analyze.TackListByCorrelationAnalyzer;
import com.github.thomasfox.sailplotter.analyze.TackSeriesAnalyzer;
import com.github.thomasfox.sailplotter.analyze.UseGpsTimeDataCorrector;
import com.github.thomasfox.sailplotter.analyze.VelocityBearingAnalyzer;
import com.github.thomasfox.sailplotter.exporter.Exporter;
import com.github.thomasfox.sailplotter.gui.plot.AbstractPlotPanel;
import com.github.thomasfox.sailplotter.gui.plot.FullVelocityBearingOverTimePlotPanel;
import com.github.thomasfox.sailplotter.gui.plot.ZoomedVelocityBearingOverTimePlotPanel;
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

  SimpleHistogramDataset bearingHistogramDataset;

  List<SimpleHistogramBin> bearingHistogramBins = new ArrayList<>();

  XYSeriesCollection velocityBearingPolar = new XYSeriesCollection();

  XYSeriesCollection tackVelocityBearingPolar = new XYSeriesCollection();

  XYSeriesCollection xyDataset = new XYSeriesCollection();

  XYSeriesCollection zoomXyDataset = new XYSeriesCollection();

  XYPlot mapPlot;

  XYPlot zoomMapPlot;

  Data data;

  List<DataPoint> pointsWithLocation;

  List<Tack> tackList;

  List<TackSeries> tackSeriesList;

  TackTablePanel tackTablePanel;

  TackSeriesTablePanel tackSeriesTablePanel;

  JPanel views;

  TimeSeriesCollection zoomedBearingOverTimeDataset = new TimeSeriesCollection();

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
    bearingHistogramDataset = new SimpleHistogramDataset("Relative Bearing");
    bearingHistogramDataset.setAdjustForBinSize(false);
    for (int i = 0; i < Constants.NUMBER_OF_BEARING_BINS; ++i)
    {
      SimpleHistogramBin bin = new SimpleHistogramBin(
          (i * 360d / Constants.NUMBER_OF_BEARING_BINS) - 180d,
          ((i + 1) * 360d / Constants.NUMBER_OF_BEARING_BINS) - 180d,
          true,
          false);
      bearingHistogramBins.add(bin);
      bearingHistogramDataset.addBin(bin);
    }
    updateBearingHistogramDataset();
    JFreeChart bearingHistogramChart = ChartFactory.createHistogram("Relative Bearing", "Relative Bearing [°]", "Occurances",  bearingHistogramDataset, PlotOrientation.VERTICAL, false, false, false);
    ChartPanel bearingChartPanel = new ChartPanel(bearingHistogramChart);
    topRightPanel.add(bearingChartPanel);

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

    updateXyDataset();
    JFreeChart xyChart = ChartFactory.createXYLineChart("Sail Map", "X", "Y", xyDataset, PlotOrientation.VERTICAL, false, false, false);
    mapPlot = (XYPlot) xyChart.getPlot();
    resetMapPlot();
    ChartPanel xyChartPanel = new ChartPanel(xyChart);
    overview.layoutForAdding().gridx(0).gridy(1).weightx(0.333).weighty(0.5)
        .add(xyChartPanel);

    updateZoomXyDataset();
    JFreeChart zoomXyChart = ChartFactory.createXYLineChart("Sail Map Zoom", "X", "Y", zoomXyDataset, PlotOrientation.VERTICAL, false, true, false);
    zoomMapPlot = (XYPlot) zoomXyChart.getPlot();
    updateMapZoomRange();
    zoomMapPlot.setRenderer(new XYZoomRenderer());
    zoomMapPlot.getRenderer().setSeriesPaint(0, new Color(0xFF, 0x00, 0x00));
    ((XYLineAndShapeRenderer) zoomMapPlot.getRenderer()).setSeriesShapesVisible(0, true);
    ((XYLineAndShapeRenderer) zoomMapPlot.getRenderer()).setBaseToolTipGenerator(new XYTooltipFromLabelGenerator());
    ChartPanel zoomXyChartPanel = new ChartPanel(zoomXyChart);
    overview.layoutForAdding().gridx(1).gridy(1).weightx(0.333).weighty(0.5)
        .add(zoomXyChartPanel);

    updateTackVelocityBearingPolar();
    JFreeChart tackVelocityBearingChart = ChartFactory.createPolarChart("Tack Velocity over rel. Bearing", tackVelocityBearingPolar, false, true, false);
    PolarPlot tackVelocityBearingPlot = (PolarPlot) tackVelocityBearingChart.getPlot();
    PolarScatterRenderer tackVelocityRenderer = new PolarScatterRenderer();
    tackVelocityRenderer.setBaseToolTipGenerator(new XYTooltipFromLabelGenerator());
    tackVelocityBearingPlot.setRenderer(tackVelocityRenderer);

    ChartPanel tackVelocityBearingChartPanel = new ChartPanel(tackVelocityBearingChart);
    overview.layoutForAdding().gridx(2).gridy(1).weightx(0.166).weighty(0.5)
        .add(tackVelocityBearingChartPanel);

    updateVelocityBearingPolar();
    JFreeChart chart = ChartFactory.createPolarChart("Velocity over rel. Bearing", velocityBearingPolar, false, false, false);
    ChartPanel chartPanel = new ChartPanel(chart);
    overview.layoutForAdding().gridx(3).gridy(1).weightx(0.166).weighty(0.5)
        .add(chartPanel);

    tackTablePanel = new TackTablePanel(tackList, this::tackSelected);
    overview.layoutForAdding().gridx(0).gridy(2).weightx(0.666).weighty(0.25).columnSpan(2)
        .add(tackTablePanel);

    tackSeriesTablePanel = new TackSeriesTablePanel(tackSeriesList, this::tackSeriesSelected);
    overview.layoutForAdding().gridx(2).gridy(2).weightx(0.666).weighty(0.25).columnSpan(2)
        .add(tackSeriesTablePanel);

    updateZoomedBearingOverTimeDataset();
    JFreeChart zoomedBearingOverTimeChart = ChartFactory.createTimeSeriesChart("Bearing (Zoom)", "Time", "Bearing [arcs]", zoomedBearingOverTimeDataset, true, false, false);
    XYPlot zoomedBearingOverTimePlot = (XYPlot) zoomedBearingOverTimeChart.getPlot();
    zoomedBearingOverTimePlot.getRenderer().setSeriesPaint(0, new Color(0xFF, 0x00, 0x00));
    zoomedBearingOverTimePlot.getRenderer().setSeriesPaint(1, new Color(0x00, 0xFF, 0x00));
    zoomedBearingOverTimePlot.getRenderer().setSeriesPaint(2, new Color(0x00, 0x00, 0xFF));
    ((XYLineAndShapeRenderer) zoomedBearingOverTimePlot.getRenderer()).setSeriesShapesVisible(0, true);
    ((XYLineAndShapeRenderer) zoomedBearingOverTimePlot.getRenderer()).setSeriesShapesVisible(1, true);
    ((XYLineAndShapeRenderer) zoomedBearingOverTimePlot.getRenderer()).setSeriesShapesVisible(2, true);
    ChartPanel zoomedBearingOverTimeChartPanel = new ChartPanel(zoomedBearingOverTimeChart);
    directions.layoutForAdding().gridx(0).gridy(0).weightx(0.5).weighty(0.5)
        .add(zoomedBearingOverTimeChartPanel);

    commentPanel = new CommentPanel(data.comment, data::setComment);
    comments.layoutForAdding().gridx(0).gridy(0).weightx(1).weighty(0.9)
      .add(commentPanel);

    frame.pack();
    frame.setVisible(true);
  }


  private void resetMapPlot()
  {
    Range xRange = new Range(
        getMinimum(pointsWithLocation, d->d.location.getX()) - pointsWithLocation.get(0).location.getX(),
        getMaximum(pointsWithLocation, d->d.location.getX()) - pointsWithLocation.get(0).location.getX());
    mapPlot.getDomainAxis().setRange(xRange);
    Range yRange = new Range(
        getMinimum(pointsWithLocation, d->d.location.getY()) - pointsWithLocation.get(0).location.getY(),
        getMaximum(pointsWithLocation, d->d.location.getY()) - pointsWithLocation.get(0).location.getY());
    mapPlot.getRangeAxis().setRange(yRange);
    expandRangesToAspectRatio(mapPlot, Constants.MAP_ASPECT_RATIO);
    mapPlot.getRenderer().setSeriesPaint(0, new Color(0x00, 0x00, 0x00));
    mapPlot.getRenderer().setSeriesPaint(1, new Color(0xFF, 0x00, 0x00));
    mapPlot.getRenderer().setSeriesPaint(2, new Color(0x00, 0x00, 0x00));
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

  private void updateZoomedBearingOverTimeDataset()
  {
    zoomedBearingOverTimeDataset.removeAllSeries();
    zoomedBearingOverTimeDataset.addSeries(getBearingFromLatLongTimeSeries(TimeWindowPosition.IN));
    zoomedBearingOverTimeDataset.addSeries(getGpsBearingTimeSeries(TimeWindowPosition.IN));
    zoomedBearingOverTimeDataset.addSeries(getCompassBearingTimeSeries(TimeWindowPosition.IN));
  }

  public static TimeSeries getLatitudeTimeSeries(List<DataPoint> data)
  {
    TimeSeries series = new TimeSeries("latitude");
    for (DataPoint point: data)
    {
      series.addOrUpdate(point.getMillisecond(), point.location.latitude);
    }
    return series;
  }

  private double getMaximum(List<DataPoint> data, Function<DataPoint, Double> pointFunction)
  {
    double maxValue = Double.MIN_VALUE;
    for (DataPoint dataPoint : data)
    {
      Double pointValue = pointFunction.apply(dataPoint);
      if (pointValue != null && pointValue > maxValue)
      {
        maxValue = pointValue;
      }
    }
    return maxValue;
  }

  private double getMinimum(List<DataPoint> data, Function<DataPoint, Double> pointFunction)
  {
    double minValue = Double.MAX_VALUE;
    for (DataPoint dataPoint : data)
    {
      Double pointValue = pointFunction.apply(dataPoint);
      if (pointValue != null && pointValue < minValue)
      {
        minValue = pointValue;
      }
    }
    return minValue;
  }

  public TimeSeries getVelocityTimeSeries(TimeWindowPosition position)
  {
    TimeSeries series = new TimeSeries("velocity");
    for (DataPoint point : getLocationSubset(position))
    {
      series.addOrUpdate(point.getMillisecond(), point.location.velocityFromLatLong);
    }
    return series;
  }

  public TimeSeries getBearingFromLatLongTimeSeries(TimeWindowPosition position)
  {
    TimeSeries series = new TimeSeries("bearing");
    for (DataPoint point : getLocationSubset(position))
    {
      series.addOrUpdate(point.getMillisecond(), point.location.bearingFromLatLong);
    }
    return series;
  }

  public TimeSeries getGpsBearingTimeSeries(TimeWindowPosition position)
  {
    TimeSeries series = new TimeSeries("gps bearing");
    for (DataPoint point : getLocationSubset(position))
    {
      series.addOrUpdate(point.getMillisecond(), point.location.bearing);
    }
    return series;
  }

  public TimeSeries getCompassBearingTimeSeries(TimeWindowPosition position)
  {
    TimeSeries series = new TimeSeries("compass bearing");
    for (DataPoint point : data.getAllPoints())
    {
      if (isInSelectedPosition(point, position) && point.hasMagneticField() && point.magneticField.compassBearing != null)
      {
        series.addOrUpdate(point.getMillisecond(), point.magneticField.compassBearing);
      }
    }
    return series;
  }

  public TimeSeries getZoomDisplaySeries(List<DataPoint> data)
  {
    Millisecond startValue = data.get(getLocationDataStartIndex()).getMillisecond();
    Millisecond endValue = data.get(getLocationDataEndIndex()).getMillisecond();
    TimeSeries series = new TimeSeries("velocity");
    series.addOrUpdate(startValue, 2);
    series.addOrUpdate(endValue, 2);
    return series;
  }

  public int getLocationDataStartIndex()
  {
    return zoomPanel.getStartIndex();
  }

  public int getLocationDataEndIndex()
  {
    int zoom = zoomPanel.getZoomIndex();
    int startIndex = getLocationDataStartIndex();
    int result = startIndex + zoom * (pointsWithLocation.size() - 1) / Constants.NUMER_OF_ZOOM_TICKS;
    result = Math.min(result, (pointsWithLocation.size() - 1));
    return result;
  }

  public LocalDateTime getLocationDataStartTime()
  {
    return pointsWithLocation.get(getLocationDataStartIndex()).getLocalDateTime();
  }

  public LocalDateTime getLocationDataEndTime()
  {
    return pointsWithLocation.get(getLocationDataEndIndex()).getLocalDateTime();
  }

  public void updateBearingHistogramDataset()
  {
    for (SimpleHistogramBin bin : bearingHistogramBins)
    {
      bin.setItemCount(0);
    }
    for (DataPoint point : pointsWithLocation)
    {
      if (point.getLocalDateTime().isAfter(getLocationDataStartTime())
          && point.getLocalDateTime().isBefore(getLocationDataEndTime()))
      {
        if (point.location.bearingFromLatLong != null)
        {
          bearingHistogramDataset.addObservation(point.getRelativeBearingAs360Degrees());
        }
      }
    }
  }

  public void updateVelocityBearingPolar()
  {
    List<List<Double>> velocityBuckets = new ArrayList<>(Constants.NUMBER_OF_BEARING_BINS);
    for (int i = 0; i < Constants.NUMBER_OF_BEARING_BINS; ++i)
    {
      velocityBuckets.add(new ArrayList<Double>());
    }
    for (DataPoint point : pointsWithLocation)
    {
      if (point.getLocalDateTime().isAfter(getLocationDataStartTime())
          && point.getLocalDateTime().isBefore(getLocationDataEndTime()))
      {
        if (point.location.bearingFromLatLong != null && point.location.velocityFromLatLong != null)
        {
          int bucket = new Double(point.getRelativeBearingInArcs() * Constants.NUMBER_OF_BEARING_BINS / 2 / Math.PI).intValue();
          velocityBuckets.get(bucket).add(point.location.velocityFromLatLong);
        }
      }
    }
    int max = 0;
    for (List<Double> bucket : velocityBuckets)
    {
      if (bucket.size() > max)
      {
        max = bucket.size();
      }
    }
    for (List<Double> bucket : velocityBuckets)
    {
      if (bucket.size() < max / Constants.HISTOGRAM_IGNORE_THRESHOLD_FRACTION)
      {
        bucket.clear();
      }
    }

    XYSeries maxVelocity = new XYSeries("maxVelocity");
    XYSeries medianVelocity = new XYSeries("medianVelocity");
    for (int i = 0; i < Constants.NUMBER_OF_BEARING_BINS; ++i)
    {
      List<Double> velocityDistribution = velocityBuckets.get(i);
      Collections.sort(velocityDistribution);
      if (!velocityDistribution.isEmpty())
      {
        maxVelocity.add(i * 360d / Constants.NUMBER_OF_BEARING_BINS, velocityDistribution.get(velocityDistribution.size() - 1));
      }
      else
      {
        maxVelocity.add(i * 360d / Constants.NUMBER_OF_BEARING_BINS, 0);
      }
      if (velocityDistribution.size() >= 1)
      {
        medianVelocity.add(i * 360d / Constants.NUMBER_OF_BEARING_BINS, velocityDistribution.get(velocityDistribution.size() / 2));
      }
      else
      {
        medianVelocity.add(i * 360d / Constants.NUMBER_OF_BEARING_BINS, 0);
      }
    }
    velocityBearingPolar.removeAllSeries();
    velocityBearingPolar.addSeries(maxVelocity);
    velocityBearingPolar.addSeries(medianVelocity);
  }

  public void updateTackVelocityBearingPolar()
  {
    XYSeries tackVelocity = new XYSeries("tackVelocity", false, true);
    for (Tack tack : tackList)
    {
      if (tack.end.getLocalDateTime().isAfter(getLocationDataStartTime())
          && tack.start.getLocalDateTime().isBefore(getLocationDataEndTime())
          && tack.hasMainPoints())
      {
        if (tack.getRelativeBearingInDegrees() != null && tack.getVelocityInKnots() != null)
        {
          tackVelocity.add(new XYSailDataItem(
              tack.getRelativeBearingInDegrees(),
              tack.getVelocityInKnots(),
              tack.getLabel()));
        }
      }
    }
    tackVelocityBearingPolar.removeAllSeries();
    tackVelocityBearingPolar.addSeries(tackVelocity);
  }

  private boolean isInSelectedPosition(DataPoint point, TimeWindowPosition position)
  {
    if (position == TimeWindowPosition.BEFORE && point.getLocalDateTime().isAfter(getLocationDataStartTime()))
    {
      return false;
    }
    if (position == TimeWindowPosition.IN
        && (!point.getLocalDateTime().isAfter(getLocationDataStartTime())
            || !point.getLocalDateTime().isBefore(getLocationDataEndTime())))
    {
      return false;
    }
    if (position == TimeWindowPosition.AFTER && point.getLocalDateTime().isBefore(getLocationDataEndTime()))
    {
      return false;
    }
    return true;
  }

  private void updateXyDataset()
  {
    xyDataset.removeAllSeries();
    xyDataset.addSeries(getXySeries(pointsWithLocation, TimeWindowPosition.BEFORE, pointsWithLocation.get(0).location.getX(), pointsWithLocation.get(0).location.getY()));
    xyDataset.addSeries(getXySeries(pointsWithLocation, TimeWindowPosition.IN, pointsWithLocation.get(0).location.getX(), pointsWithLocation.get(0).location.getY()));
    xyDataset.addSeries(getXySeries(pointsWithLocation, TimeWindowPosition.AFTER, pointsWithLocation.get(0).location.getX(), pointsWithLocation.get(0).location.getY()));
  }

  private void updateZoomXyDataset()
  {
    zoomXyDataset.removeAllSeries();
    zoomXyDataset.addSeries(getXySeries(pointsWithLocation, TimeWindowPosition.IN, pointsWithLocation.get(0).location.getX(), pointsWithLocation.get(0).location.getY()));
    zoomXyDataset.addSeries(getTackIntersectionSeries(tackList, TimeWindowPosition.IN, pointsWithLocation.get(0).location.getX(), pointsWithLocation.get(0).location.getY()));
  }

  private void updateMapZoomRange()
  {
    List<DataPoint> dataSubset = getLocationSubset(TimeWindowPosition.IN);
    double startX = pointsWithLocation.get(0).location.getX();
    double minimumX = getMinimum(dataSubset, d->d.location.getX());
    double maximumX = getMaximum(dataSubset, d->d.location.getX());
    if (maximumX - minimumX < 1)
    {
      minimumX = -1 + startX;
      maximumX = 1 + startX;
    }
    Range zoomXRange = new Range(
        minimumX - startX - (maximumX - minimumX) * 0.1,
        maximumX - startX + (maximumX - minimumX) * 0.1);

    double startY = pointsWithLocation.get(0).location.getY();
    double minimumY = getMinimum(dataSubset, d->d.location.getY());
    double maximumY = getMaximum(dataSubset, d->d.location.getY());
    if (maximumY - minimumY < 1)
    {
      minimumY = -1 + startY;
      maximumY = 1 + startY;
    }
    Range zoomYRange = new Range(
        minimumY - startY - (maximumY - minimumY) * 0.1,
        maximumY - startY + (maximumY - minimumY) * 0.1);

    zoomMapPlot.getDomainAxis().setRange(zoomXRange);
    zoomMapPlot.getRangeAxis().setRange(zoomYRange);
    expandRangesToAspectRatio(zoomMapPlot, Constants.MAP_ASPECT_RATIO);
  }

  public void expandRangesToAspectRatio(XYPlot plot, double aspectRatio)
  {
    Range xRange = plot.getDomainAxis().getRange();
    Range yRange = plot.getRangeAxis().getRange();
    if (xRange.getLength() > aspectRatio * yRange.getLength())
    {
      yRange = new Range(
          yRange.getCentralValue() - 0.5d * xRange.getLength() / aspectRatio,
          yRange.getCentralValue() + 0.5d * xRange.getLength() / aspectRatio);
      plot.getRangeAxis().setRange(yRange);
    }
    else
    {
      xRange = new Range(
          xRange.getCentralValue() - 0.5d * yRange.getLength() * aspectRatio,
          xRange.getCentralValue() + 0.5d * yRange.getLength() * aspectRatio);
      plot.getDomainAxis().setRange(xRange);
    }
  }

  public XYSeries getXySeries(List<DataPoint> data, TimeWindowPosition position, double xOffset, double yOffset)
  {
    XYSeries series = new XYSeries("XY" + position, false, true);
    int tackIndex = 0;
    Tack containingTack = tackList.get(tackIndex);
    for (DataPoint point : getLocationSubset(position))
    {
      while (containingTack.endOfTackDataPointIndex < point.index && tackIndex < tackList.size() - 1)
      {
        ++tackIndex;
        containingTack = tackList.get(tackIndex);
      }
      XYSailDataItem item = new XYSailDataItem(point.location.getX() - xOffset, point.location.getY() - yOffset, point.getXYLabel());
      if (containingTack.startOfTackDataPointIndex == point.index)
      {
        item.setStartOfTack(tackIndex);
      }
      else if (containingTack.endOfTackDataPointIndex == point.index)
      {
        item.setEndOfTack(tackIndex);
      }
      DataPoint afterStartManeuver = containingTack.getAfterStartManeuver();
      DataPoint bevoreEndManeuver = containingTack.getBeforeEndManeuver();

      if ((afterStartManeuver != null && afterStartManeuver.index == point.index)
          || (bevoreEndManeuver != null && bevoreEndManeuver.index == point.index))
      {
        item.setTackMainPartLimit(true);
      }

      series.add(item);
    }
    return series;
  }

  public XYSeries getTackIntersectionSeries(List<Tack> tacks, TimeWindowPosition position, double xOffset, double yOffset)
  {
    XYSeries series = new XYSeries("XY", false, true);
    for (Tack tack : tacks)
    {
      if (!isInSelectedPosition(tack.start, position) && !isInSelectedPosition(tack.end, position))
      {
        continue;
      }
      if (tack.tackStraightLineIntersectionStart != null && tack.tackStraightLineIntersectionEnd != null)
      {
        series.add(
            tack.tackStraightLineIntersectionStart.location.getX() - xOffset,
            tack.tackStraightLineIntersectionStart.location.getY() - yOffset);
        series.add(
            tack.tackStraightLineIntersectionEnd.location.getX() - xOffset,
            tack.tackStraightLineIntersectionEnd.location.getY() - yOffset);
      }
    }
    return series;
  }

  List<DataPoint> getLocationSubset(TimeWindowPosition position)
  {
    List<DataPoint> result = new ArrayList<>();
    for (DataPoint point : pointsWithLocation)
    {
      if (!isInSelectedPosition(point, position))
      {
        continue;
      }

      result.add(point);
    }
    return result;
  }


  public void analyze()
  {
    pointsWithLocation = data.getPointsWithLocation();
    new UseGpsTimeDataCorrector().correct(data);
    new LocationInterpolator().interpolateLocation(data);
    new VelocityBearingAnalyzer().analyze(data, windBearing);
    tackList = new TackListByCorrelationAnalyzer().analyze(data);
    tackSeriesList = new TackSeriesAnalyzer().analyze(tackList);
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
      updateBearingHistogramDataset();
      updateVelocityBearingPolar();
      updateXyDataset();
      updateTackVelocityBearingPolar();
      updateZoomXyDataset();
      updateMapZoomRange();
      updateZoomedBearingOverTimeDataset();
      commentPanel.setText(data.comment);
      if (updateTableContent)
      {
        tackTablePanel.updateContent(tackList);
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
    Tack tack = tackList.get(index);
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
      analyze();
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
      zoomPanel.setStartIndex(Math.max(tackList.get(tackSeries.startTackIndex).startOfTackDataPointIndex - Constants.NUM_DATAPOINTS_TACK_EXTENSION, 0));
      zoomPanel.setZoomIndex(Math.min(
          Math.max(
              Constants.NUMER_OF_ZOOM_TICKS * (tackList.get(tackSeries.endTackIndex).endOfTackDataPointIndex - tackList.get(tackSeries.startTackIndex).startOfTackDataPointIndex + 2 * Constants.NUM_DATAPOINTS_TACK_EXTENSION) / (pointsWithLocation.size()),
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
      analyze();
      zoomPanel.setDataSize(pointsWithLocation.size());
      fullVelocityBearingOverTimePlotPanel.dataChanged(data);
      resetMapPlot();
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
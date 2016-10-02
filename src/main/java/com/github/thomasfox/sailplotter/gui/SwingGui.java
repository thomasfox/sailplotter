package com.github.thomasfox.sailplotter.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;

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
import org.jfree.data.time.DateRange;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.github.thomasfox.sailplotter.Constants;
import com.github.thomasfox.sailplotter.analyze.TackListByCorrelationAnalyzer;
import com.github.thomasfox.sailplotter.analyze.TackSeriesAnalyzer;
import com.github.thomasfox.sailplotter.analyze.VelocityBearingAnalyzer;
import com.github.thomasfox.sailplotter.importer.SailRacerImporter;
import com.github.thomasfox.sailplotter.importer.ViewRangerImporter;
import com.github.thomasfox.sailplotter.model.DataPoint;
import com.github.thomasfox.sailplotter.model.Tack;
import com.github.thomasfox.sailplotter.model.TackSeries;

public class SwingGui
{
  private final ZoomPanel zoomPanel;

  private final TimeSeriesCollection fullVelocityBearingOverTimeDataset = new TimeSeriesCollection();

  private final TimeSeriesCollection zoomedVelocityBearingOverTimeDataset = new TimeSeriesCollection();

  SimpleHistogramDataset bearingHistogramDataset;

  List<SimpleHistogramBin> bearingHistogramBins = new ArrayList<>();

  XYSeriesCollection velocityBearingPolar = new XYSeriesCollection();

  XYSeriesCollection tackVelocityBearingPolar = new XYSeriesCollection();

  XYSeriesCollection xyDataset = new XYSeriesCollection();

  XYSeriesCollection zoomXyDataset = new XYSeriesCollection();

  XYPlot zoomXyPlot;

  List<DataPoint> data;

  List<Tack> tackList;

  List<TackSeries> tackSeriesList;

  JTable tacksTable;

  boolean inUpdate = false;

  DefaultTableModel tackTableModel;

  JTable tackSeriesTable;

  double windBearing;

  public SwingGui(String filePath, int windDirectionInDegrees)
  {
    this.windBearing = 2 * Math.PI * windDirectionInDegrees / 360d;
    data = getData(filePath);
    analyze();
    zoomPanel = new ZoomPanel(data.size());

    JFrame frame = new JFrame("SailPlotter");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().setLayout(new GridBagLayout());

    updateFullVelocityBearingOverTimeDataset();
    JFreeChart fullVelocityBearingOverTimeChart = ChartFactory.createTimeSeriesChart(
        "Velocity and Bearing (Full)",
        "Time",
        "Velocity [kts] / Bearing [arcs]",
        fullVelocityBearingOverTimeDataset,
        false,
        false,
        false);
    XYPlot fullVelocityBearingOverTimePlot = (XYPlot) fullVelocityBearingOverTimeChart.getPlot();
    Range dataRange = new DateRange(data.get(0).time, data.get(data.size() -1).time);
    fullVelocityBearingOverTimePlot.getDomainAxis().setRange(dataRange);
    Range valueRange = new DateRange(0, getMaximum(data, DataPoint::getVelocity));
    fullVelocityBearingOverTimePlot.getRangeAxis().setRange(valueRange);
    fullVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(0, new Color(0x00, 0x00, 0x00));
    fullVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(1, new Color(0xFF, 0x00, 0x00));
    fullVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(2, new Color(0x00, 0x00, 0x00));
    fullVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(3, new Color(0x00, 0x00, 0x00));
    fullVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(4, new Color(0x00, 0xFF, 0x00));
    fullVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(5, new Color(0x00, 0x00, 0x00));
    ChartPanel fullVelocityBearingOverTimeChartPanel = new ChartPanel(fullVelocityBearingOverTimeChart);
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.weightx = 0.333;
    gridBagConstraints.weighty = 0.25;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    frame.getContentPane().add(fullVelocityBearingOverTimeChartPanel, gridBagConstraints);

    updateZoomedVelocityBearingOverTimeDataset();
    JFreeChart zoomedVelocityBearingOverTimeChart = ChartFactory.createTimeSeriesChart("Velocity and Bearing (Zoom)", "Time", "Velocity [kts] / Bearing [arcs]", zoomedVelocityBearingOverTimeDataset, false, false, false);
    XYPlot zoomedVelocityBearingOverTimePlot = (XYPlot) zoomedVelocityBearingOverTimeChart.getPlot();
    zoomedVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(0, new Color(0xFF, 0x00, 0x00));
    zoomedVelocityBearingOverTimePlot.getRenderer().setSeriesPaint(1, new Color(0x00, 0xFF, 0x00));
    ((XYLineAndShapeRenderer) zoomedVelocityBearingOverTimePlot.getRenderer()).setSeriesShapesVisible(0, true);
    ((XYLineAndShapeRenderer) zoomedVelocityBearingOverTimePlot.getRenderer()).setSeriesShapesVisible(1, true);
    ChartPanel zoomedvelocityBearingOverTimeChartPanel = new ChartPanel(zoomedVelocityBearingOverTimeChart);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.weightx = 0.333;
    gridBagConstraints.weighty = 0.25;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    frame.getContentPane().add(zoomedvelocityBearingOverTimeChartPanel, gridBagConstraints);

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
    windDirectionTextField.setText(Integer.toString(windDirectionInDegrees));
    windDirectionTextField.addActionListener(this::windDirectionChanged);
    windDirectionPanel.add(windDirectionTextField);
    topRightPanel.add(windDirectionPanel);

    topRightPanel.setLayout(new BoxLayout(topRightPanel, BoxLayout.PAGE_AXIS));
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.weightx = 0.333;
    gridBagConstraints.weighty = 0.25;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    frame.getContentPane().add(topRightPanel, gridBagConstraints);

    updateXyDataset();
    JFreeChart xyChart = ChartFactory.createXYLineChart("Sail Map", "X", "Y", xyDataset, PlotOrientation.VERTICAL, false, false, false);
    XYPlot xyPlot = (XYPlot) xyChart.getPlot();
    Range xRange = new Range(
        getMinimum(data, DataPoint::getX) - data.get(0).getX(),
        getMaximum(data, DataPoint::getX) - data.get(0).getX());
    xyPlot.getDomainAxis().setRange(xRange);
    Range yRange = new Range(
        getMinimum(data, DataPoint::getY) - data.get(0).getY(),
        getMaximum(data, DataPoint::getY) - data.get(0).getY());
    xyPlot.getRangeAxis().setRange(yRange);
    expandRangesToAspectRatio(xyPlot, Constants.MAP_ASPECT_RATIO);
    xyPlot.getRenderer().setSeriesPaint(0, new Color(0x00, 0x00, 0x00));
    xyPlot.getRenderer().setSeriesPaint(1, new Color(0xFF, 0x00, 0x00));
    xyPlot.getRenderer().setSeriesPaint(2, new Color(0x00, 0x00, 0x00));
    ChartPanel xyChartPanel = new ChartPanel(xyChart);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.weightx = 0.333;
    gridBagConstraints.weighty = 0.5;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    frame.getContentPane().add(xyChartPanel, gridBagConstraints);

    updateZoomXyDataset();
    JFreeChart zoomXyChart = ChartFactory.createXYLineChart("Sail Map Zoom", "X", "Y", zoomXyDataset, PlotOrientation.VERTICAL, false, true, false);
    zoomXyPlot = (XYPlot) zoomXyChart.getPlot();
    updateZoomXyRange();
    zoomXyPlot.setRenderer(new XYZoomRenderer());
    zoomXyPlot.getRenderer().setSeriesPaint(0, new Color(0xFF, 0x00, 0x00));
    ((XYLineAndShapeRenderer) zoomXyPlot.getRenderer()).setSeriesShapesVisible(0, true);
    ((XYLineAndShapeRenderer) zoomXyPlot.getRenderer()).setBaseToolTipGenerator(new XYTooltipFromLabelGenerator());
    ChartPanel zoomXyChartPanel = new ChartPanel(zoomXyChart);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.weightx = 0.333;
    gridBagConstraints.weighty = 0.5;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    frame.getContentPane().add(zoomXyChartPanel, gridBagConstraints);

    updateTackVelocityBearingPolar();
    JFreeChart tackVelocityBearingChart = ChartFactory.createPolarChart("Tack Velocity over Relative Bearing", tackVelocityBearingPolar, false, false, false);
    PolarPlot tackVelocityBearingPlot = (PolarPlot) tackVelocityBearingChart.getPlot();
    tackVelocityBearingPlot.setRenderer(new PolarScatterRenderer());
    ChartPanel tackVelocityBearingChartPanel = new ChartPanel(tackVelocityBearingChart);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.weightx = 0.2;
    gridBagConstraints.weighty = 0.5;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    frame.getContentPane().add(tackVelocityBearingChartPanel, gridBagConstraints);

    updateVelocityBearingPolar();
    JFreeChart chart = ChartFactory.createPolarChart("Velocity over Relative Bearing", velocityBearingPolar, false, false, false);
    ChartPanel chartPanel = new ChartPanel(chart);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.weightx = 0.2;
    gridBagConstraints.weighty = 0.5;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 1;
    frame.getContentPane().add(chartPanel, gridBagConstraints);

    JScrollPane tacksTablePane = createTacksTablePane();
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.weightx = 0.333;
    gridBagConstraints.weighty = 0.25;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    frame.getContentPane().add(tacksTablePane, gridBagConstraints);

    JScrollPane tackSeriesTablePane = createTackSeriesTablePane();
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.weightx = 0.25;
    gridBagConstraints.weighty = 0.25;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    frame.getContentPane().add(tackSeriesTablePane, gridBagConstraints);

    frame.pack();
    frame.setVisible(true);

  }

  private JScrollPane createTacksTablePane()
  {
    tackTableModel = new DefaultTableModel(
        new String[] {
            "#",
            "Point of Sail",
            "length [m]",
            "duration [s]",
            "absolute Bearing [deg]",
            "relative Bearing [deg]",
            "Speed [kts]",
            "VMG [kts]",
            "Maneuver at Start",
            "Maneuver loss at Start (s)",
            "Tacking angle at Start",
            "Maneuver at End"},
        0);
    updateTackTableContent();
    tacksTable = new JTable(tackTableModel);
    JScrollPane scrollPane = new JScrollPane(tacksTable);
    tacksTable.setFillsViewportHeight(true);
    tacksTable.getSelectionModel().addListSelectionListener(this::tackSelected);
    return scrollPane;
  }

  private void updateTackTableContent()
  {
    while (tackTableModel.getRowCount() > 0)
    {
      tackTableModel.removeRow(0);
    }
    Tack lastTack = null;
    int i = 0;
    for (Tack tack : tackList)
    {
      tackTableModel.addRow(new Object[] {
          i,
          tack.pointOfSail,
          new DecimalFormat("0").format(tack.getLength()),
          new DecimalFormat("0.0").format(tack.getDuration() / 1000d),
          tack.getAbsoluteBearingInDegrees() == null
          ? ""
          : new DecimalFormat("0").format(tack.getAbsoluteBearingInDegrees()),
          tack.getRelativeBearingInDegrees() == null
            ? ""
            : new DecimalFormat("0").format(tack.getRelativeBearingInDegrees()),
          new DecimalFormat("0.0").format(tack.getVelocityInKnots()),
          tack.getAverageVMGInKnots() == null
            ? ""
            : new DecimalFormat("0.0").format(tack.getAverageVMGInKnots()),
          tack.maneuverTypeAtStart == null ? "" : tack.maneuverTypeAtStart.toString(),
          tack.getIntersectionTimeDistance(lastTack) == null
            ? ""
            : new DecimalFormat("0.0").format(tack.getIntersectionTimeDistance(lastTack)),
          tack.getIntersectionAnglesInDegrees(lastTack) == null
            ? ""
            : new DecimalFormat("0").format(Math.abs(tack.getIntersectionAnglesInDegrees(lastTack))),
          tack.maneuverTypeAtEnd == null ? "" : tack.maneuverTypeAtEnd.toString()});
      lastTack = tack;
      ++i;
    }
  }

  private JScrollPane createTackSeriesTablePane()
  {
    DefaultTableModel model = new DefaultTableModel(
        new String[] {
            "Tacks",
            "Type",
            "Wind direction [°]",
            "Angle to Wind [°]",
            "Velocity Main Parts Starboard [knots]",
            "Velocity Main Parts Port [knots]"},
        0);

    for (TackSeries tackSeries : tackSeriesList)
    {
      model.addRow(new Object[] {
          tackSeries.startTackIndex + " - " + tackSeries.endTackIndex,
          tackSeries.type,
          tackSeries.getAverageWindDirectionInDegrees(),
          tackSeries.getAverageAngleToWindInDegrees(),
          new DecimalFormat("0.0").format(tackSeries.getAverageMainPartVelocityStarboard()),
          new DecimalFormat("0.0").format(tackSeries.getAverageMainPartVelocityPort())});
    }
    tackSeriesTable = new JTable(model);
    JScrollPane scrollPane = new JScrollPane(tackSeriesTable);
    tackSeriesTable.setFillsViewportHeight(true);
    tackSeriesTable.getSelectionModel().addListSelectionListener(this::tackSeriesSelected);
    return scrollPane;
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

  private List<DataPoint> getData(String filePath)
  {
    File file = new File(filePath);
    List<DataPoint> data;
    if (filePath.endsWith(".log"))
    {
      data = new SailRacerImporter().read(file);
    }
    else
    {
      data = new ViewRangerImporter().read(file);
    }
    return data;
  }

  private void updateFullVelocityBearingOverTimeDataset()
  {
    fullVelocityBearingOverTimeDataset.removeAllSeries();
    fullVelocityBearingOverTimeDataset.addSeries(getVelocityTimeSeries(data, TimeWindowPosition.BEFORE));
    fullVelocityBearingOverTimeDataset.addSeries(getVelocityTimeSeries(data, TimeWindowPosition.IN));
    fullVelocityBearingOverTimeDataset.addSeries(getVelocityTimeSeries(data, TimeWindowPosition.AFTER));
    fullVelocityBearingOverTimeDataset.addSeries(getBearingTimeSeries(data, TimeWindowPosition.BEFORE));
    fullVelocityBearingOverTimeDataset.addSeries(getBearingTimeSeries(data, TimeWindowPosition.IN));
    fullVelocityBearingOverTimeDataset.addSeries(getBearingTimeSeries(data, TimeWindowPosition.AFTER));
  }

  private void updateZoomedVelocityBearingOverTimeDataset()
  {
    zoomedVelocityBearingOverTimeDataset.removeAllSeries();
    zoomedVelocityBearingOverTimeDataset.addSeries(getVelocityTimeSeries(data, TimeWindowPosition.IN));
    zoomedVelocityBearingOverTimeDataset.addSeries(getBearingTimeSeries(data, TimeWindowPosition.IN));
  }

  public static TimeSeries getLatitudeTimeSeries(List<DataPoint> data)
  {
    TimeSeries series = new TimeSeries("latitude");
    for (DataPoint point: data)
    {
      series.addOrUpdate(point.getMillisecond(), point.latitude);
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

  public TimeSeries getVelocityTimeSeries(List<DataPoint> data, TimeWindowPosition position)
  {
    TimeSeries series = new TimeSeries("velocity");
    for (DataPoint point : getSubset(data, position))
    {
      series.addOrUpdate(point.getMillisecond(), point.velocity);
    }
    return series;
  }

  public TimeSeries getBearingTimeSeries(List<DataPoint> data, TimeWindowPosition position)
  {
    TimeSeries series = new TimeSeries("bearing");
    for (DataPoint point : getSubset(data, position))
    {
      series.addOrUpdate(point.getMillisecond(), point.bearing);
    }
    return series;
  }

  public TimeSeries getZoomDisplaySeries(List<DataPoint> data)
  {
    Millisecond startValue = data.get(getDataStartIndex(data)).getMillisecond();
    Millisecond endValue = data.get(getDataEndIndex(data)).getMillisecond();
    TimeSeries series = new TimeSeries("velocity");
    series.addOrUpdate(startValue, 2);
    series.addOrUpdate(endValue, 2);
    return series;
  }

  public int getDataStartIndex(List<DataPoint> data)
  {
    return zoomPanel.getStartIndex();
  }

  public int getDataEndIndex(List<DataPoint> data)
  {
    int zoom = zoomPanel.getZoomIndex();
    int startIndex = getDataStartIndex(data);
    int result = startIndex + zoom * (data.size() - 1) / Constants.NUMER_OF_ZOOM_TICKS;
    result = Math.min(result, (data.size() - 1));
    return result;
  }

  public LocalDateTime getDataStartTime()
  {
    return data.get(getDataStartIndex(data)).getLocalDateTime();
  }

  public LocalDateTime getDataEndTime()
  {
    return data.get(getDataEndIndex(data)).getLocalDateTime();
  }

  public void updateBearingHistogramDataset()
  {
    for (SimpleHistogramBin bin : bearingHistogramBins)
    {
      bin.setItemCount(0);
    }
    for (DataPoint point : data)
    {
      if (point.getLocalDateTime().isAfter(getDataStartTime())
          && point.getLocalDateTime().isBefore(getDataEndTime()))
      {
        if (point.bearing != null)
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
    for (DataPoint point : data)
    {
      if (point.getLocalDateTime().isAfter(getDataStartTime())
          && point.getLocalDateTime().isBefore(getDataEndTime()))
      {
        if (point.bearing != null && point.velocity != null)
        {
          int bucket = new Double(point.getRelativeBearingInArcs() * Constants.NUMBER_OF_BEARING_BINS / 2 / Math.PI).intValue();
          velocityBuckets.get(bucket).add(point.velocity);
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
      if (tack.end.getLocalDateTime().isAfter(getDataStartTime())
          && tack.start.getLocalDateTime().isBefore(getDataEndTime()))
      {
        if (tack.getRelativeBearingInDegrees() != null && tack.getVelocityInKnots() != null)
        {
          tackVelocity.add(tack.getRelativeBearingInDegrees(), tack.getVelocityInKnots());
        }
      }
    }
    tackVelocityBearingPolar.removeAllSeries();
    tackVelocityBearingPolar.addSeries(tackVelocity);
  }

  private boolean isInSelectedPosition(DataPoint point, TimeWindowPosition position)
  {
    if (position == TimeWindowPosition.BEFORE && point.getLocalDateTime().isAfter(getDataStartTime()))
    {
      return false;
    }
    if (position == TimeWindowPosition.IN
        && (!point.getLocalDateTime().isAfter(getDataStartTime())
            || !point.getLocalDateTime().isBefore(getDataEndTime())))
    {
      return false;
    }
    if (position == TimeWindowPosition.AFTER && point.getLocalDateTime().isBefore(getDataEndTime()))
    {
      return false;
    }
    return true;
  }

  private void updateXyDataset()
  {
    xyDataset.removeAllSeries();
    xyDataset.addSeries(getXySeries(data, TimeWindowPosition.BEFORE, data.get(0).getX(), data.get(0).getY()));
    xyDataset.addSeries(getXySeries(data, TimeWindowPosition.IN, data.get(0).getX(), data.get(0).getY()));
    xyDataset.addSeries(getXySeries(data, TimeWindowPosition.AFTER, data.get(0).getX(), data.get(0).getY()));
  }

  private void updateZoomXyDataset()
  {
    zoomXyDataset.removeAllSeries();
    zoomXyDataset.addSeries(getXySeries(data, TimeWindowPosition.IN, data.get(0).getX(), data.get(0).getY()));
    zoomXyDataset.addSeries(getTackIntersectionSeries(tackList, TimeWindowPosition.IN, data.get(0).getX(), data.get(0).getY()));
  }

  private void updateZoomXyRange()
  {
    List<DataPoint> dataSubset = getSubset(data, TimeWindowPosition.IN);
    double minimumX = getMinimum(dataSubset, DataPoint::getX);
    double maximumX = getMaximum(dataSubset, DataPoint::getX);
    Range zoomXRange = new Range(
        minimumX - data.get(0).getX() - (maximumX - minimumX) * 0.1,
        maximumX - data.get(0).getX() + (maximumX - minimumX) * 0.1);
    double minimumY = getMinimum(dataSubset, DataPoint::getY);
    double maximumY = getMaximum(dataSubset, DataPoint::getY);
    Range zoomYRange = new Range(
        minimumY - data.get(0).getY() - (maximumY - minimumY) * 0.1,
        maximumY - data.get(0).getY() + (maximumY - minimumY) * 0.1);

    zoomXyPlot.getDomainAxis().setRange(zoomXRange);
    zoomXyPlot.getRangeAxis().setRange(zoomYRange);
    expandRangesToAspectRatio(zoomXyPlot, Constants.MAP_ASPECT_RATIO);
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
    for (DataPoint point : getSubset(data, position))
    {
      while (containingTack.endIndex < point.index && tackIndex < tackList.size() - 1)
      {
        ++tackIndex;
        containingTack = tackList.get(tackIndex);
      }
      XYSailDataItem item = new XYSailDataItem(point.getX() - xOffset, point.getY() - yOffset, point.getXYLabel());
      if (containingTack.startIndex == point.index)
      {
        item.setStartOfTack(tackIndex);
      }
      else if (containingTack.endIndex == point.index)
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
            tack.tackStraightLineIntersectionStart.getX() - xOffset,
            tack.tackStraightLineIntersectionStart.getY() - yOffset);
        series.add(
            tack.tackStraightLineIntersectionEnd.getX() - xOffset,
            tack.tackStraightLineIntersectionEnd.getY() - yOffset);
      }
    }
    return series;
  }

  List<DataPoint> getSubset(List<DataPoint> data, TimeWindowPosition position)
  {
    List<DataPoint> result = new ArrayList<>();
    for (DataPoint point : data)
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
    new VelocityBearingAnalyzer().analyze(data, windBearing);
    tackList = new TackListByCorrelationAnalyzer().analyze(data);
    tackSeriesList = new TackSeriesAnalyzer().analyze(tackList);
  }

  public void redisplay(boolean updateTableContent)
  {
    try
    {
      inUpdate = true;
      updateFullVelocityBearingOverTimeDataset();
      updateZoomedVelocityBearingOverTimeDataset();
      updateBearingHistogramDataset();
      updateVelocityBearingPolar();
      updateXyDataset();
      updateTackVelocityBearingPolar();
      updateZoomXyDataset();
      updateZoomXyRange();
      if (updateTableContent)
      {
        updateTackTableContent();
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
    ListSelectionModel model = tacksTable.getSelectionModel();
    int index = model.getAnchorSelectionIndex();
    Tack tack = tackList.get(index);
    zoomPanel.setStartIndex(Math.max(tack.startIndex - Constants.NUM_DATAPOINTS_TACK_EXTENSION, 0));
    zoomPanel.setZoomIndex(Math.min(
        Math.max(
            Constants.NUMER_OF_ZOOM_TICKS * (tack.endIndex - tack.startIndex + 2 * Constants.NUM_DATAPOINTS_TACK_EXTENSION) / (data.size()),
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
    ListSelectionModel model = tackSeriesTable.getSelectionModel();
    int index = model.getAnchorSelectionIndex();
    TackSeries tackSeries = tackSeriesList.get(index);
    try
    {
      inUpdate = true;
      tacksTable.getSelectionModel().setSelectionInterval(tackSeries.startTackIndex, tackSeries.endTackIndex);
      Rectangle firstRectangleToSelect = tacksTable.getCellRect(tackSeries.startTackIndex, 0, true);
      Rectangle lastRectangleToSelect = tacksTable.getCellRect(tackSeries.endTackIndex, 0, true);
      tacksTable.scrollRectToVisible(lastRectangleToSelect);
      tacksTable.scrollRectToVisible(firstRectangleToSelect);
      zoomPanel.setStartIndex(Math.max(tackList.get(tackSeries.startTackIndex).startIndex - Constants.NUM_DATAPOINTS_TACK_EXTENSION, 0));
      zoomPanel.setZoomIndex(Math.min(
          Math.max(
              Constants.NUMER_OF_ZOOM_TICKS * (tackList.get(tackSeries.endTackIndex).endIndex - tackList.get(tackSeries.startTackIndex).startIndex + 2 * Constants.NUM_DATAPOINTS_TACK_EXTENSION) / (data.size()),
              3),
          Constants.NUMER_OF_ZOOM_TICKS));
    }
    finally
    {
      inUpdate = false;
    }
    redisplay(false);
  }

}
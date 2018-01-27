package com.github.thomasfox.sailplotter.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.function.Consumer;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class Menubar extends JMenuBar
{
  /** SerialVersionUID. */
  private static final long serialVersionUID = 1L;

  private final JMenu fileMenu = new JMenu("File");

  private final JFrame applicationFrame;

  private final JFileChooser fileChooser = new JFileChooser();

  Consumer<File> loadFileConsumer;

  /**
   * Constructor for the menubar of the application.
   *
   * @param applicationFrame The application's main window, not null.
   * @param startFile the file which is selected by the fileChooser  at the start.
   * @param loadFileConsumer the function which is called whenever the user opens a file.
   */
  public Menubar(JFrame applicationFrame, File startFile, Consumer<File> loadFileConsumer)
  {
    this.applicationFrame = applicationFrame;
    this.loadFileConsumer = loadFileConsumer;
    if (startFile != null)
    {
      fileChooser.setSelectedFile(startFile);
    }

    fileMenu.setMnemonic(KeyEvent.VK_F);
    JMenuItem loadFile = new JMenuItem("load", KeyEvent.VK_T);
    loadFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));
    loadFile.addActionListener(this::loadFile);
    fileMenu.add(loadFile);
    add(fileMenu);
  }

  public void loadFile(ActionEvent e)
  {
    int returnVal = fileChooser.showOpenDialog(applicationFrame);
    if (returnVal == JFileChooser.APPROVE_OPTION)
    {
      File file = fileChooser.getSelectedFile();
      loadFileConsumer.accept(file);
    }
  }
}

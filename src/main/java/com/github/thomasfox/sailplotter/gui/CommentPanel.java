package com.github.thomasfox.sailplotter.gui;

import java.awt.Font;
import java.util.function.Consumer;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class CommentPanel extends JScrollPane implements DocumentListener
{
  /** SerialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** Is called with the new content of the text whenever the text changes. */
  private Consumer<String> textConsumer;

  private final JTextArea textArea;

  public CommentPanel()
  {
    this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    setTextConsumer(textConsumer);
    textArea = new JTextArea();
    textArea.setFont(new Font("Serif", Font.ITALIC, 16));
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    setViewportView(textArea);
    textArea.getDocument().addDocumentListener(this);
  }

  public void setTextConsumer(Consumer<String> textConsumer)
  {
    this.textConsumer = textConsumer;
  }

  public void setText(String text)
  {
    textArea.setText(text);
  }

  @Override
  public void changedUpdate(DocumentEvent e)
  {
    textChanged(e);
  }

  @Override
  public void insertUpdate(DocumentEvent e)
  {
    textChanged(e);
  }

  @Override
  public void removeUpdate(DocumentEvent e)
  {
    textChanged(e);
  }

  private void textChanged(DocumentEvent event)
  {
    Document document = event.getDocument();
    try
    {
      textConsumer.accept(document.getText(0, document.getLength()));
    }
    catch (BadLocationException e)
    {
      throw new RuntimeException(e);
    }
  }
}

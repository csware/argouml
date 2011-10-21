package org.argouml.ui;

import java.awt.BorderLayout;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.argouml.application.Main;
import org.argouml.util.GATEHelper;

/**
 * Klasse zum Anzeigen der Aufgabenstellung
 * 
 * @author Joachim Schramm
 * 
 */
public class ActionShowTask implements Runnable {

    public ActionShowTask() {

    }

    public void showTask() {
        // Dummythread starten
        Thread thread = new Thread(this);
        thread.start();
        // Windows Auflösung erkennen
        GraphicsEnvironment env = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        GraphicsDevice gd = env.getDefaultScreenDevice();
        DisplayMode dm = gd.getDisplayMode();
        // Fenster erzeugen
        JFrame workWindows = new JFrame("Aufgabenstellung");
        workWindows.setLayout(new BorderLayout());

        JButton uploadToGate = new JButton("Export2GATE");
        uploadToGate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                new ActionExport2Gate().actionPerformed(arg0);
            }
        });
        workWindows.add(uploadToGate, BorderLayout.SOUTH);

        JTextPane aufgabenstellungPane = new JTextPane();

        HTMLDocument f = new HTMLDocument();
        StringReader reader = new StringReader(
                GATEHelper.retrieve("/ShowTask?onlydescription=true&taskid="
                        + Main.taskID));
        try {
            new HTMLEditorKit().read(reader, f, 0);
        } catch (IOException ioe) {
        } catch (BadLocationException ble) {
        }

        aufgabenstellungPane.setContentType("text/html");
        aufgabenstellungPane.setStyledDocument(f);
        aufgabenstellungPane.setEditable(false);
        aufgabenstellungPane.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(aufgabenstellungPane,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        workWindows.add(scrollPane, BorderLayout.CENTER);

        // Unschließbar machen
        workWindows.setDefaultCloseOperation(0);

        // Groesse des Fenster dynamisch festlegen
        int heigh = new Double(dm.getHeight() / 2.4).intValue();
        int width = new Double(dm.getWidth() / 3.6).intValue();
        int heighTaskLeiste = new Double(dm.getHeight() / 22).intValue();
        workWindows.setSize(width, heigh);
        workWindows.setLocation(0, dm.getHeight() - heigh - heighTaskLeiste);
        // Immer im Vordergrund
        workWindows.setAlwaysOnTop(true);

        workWindows.setVisible(true);
    }

    // Dummythread: Ständige Anfrage an Server, um das Ausloggen zu verhindern
    public void run() {
        try {
            while (!Thread.interrupted()) {
                GATEHelper.retrieve("/Nope");
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e1) {
                    Thread.interrupted();
                }
            }
        } catch (Exception e) {
        }
    }
}

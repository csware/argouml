package org.argouml.ui;

import java.awt.BorderLayout;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.argouml.application.Main;
import org.argouml.util.GATEHelper;

/**
 * Klasse zum Anzeigen des Feedbacks
 * @author Joachim Schramm
 *
 */
public class ActionShowFeedback {
    public void showFeedback() {
        //Windows Auflösung erkennen
        GraphicsEnvironment env = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        GraphicsDevice gd = env.getDefaultScreenDevice();
        DisplayMode dm = gd.getDisplayMode();
        //Fenster erzeugen
        JFrame workWindows = new JFrame("Feedback");
        String input = "";
        final JLabel label = new JLabel(input);
        JButton button = new JButton("Feedback");
        //Scrollbares Label ins Fenster packen
        JScrollPane scrollPane = new JScrollPane(label,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        workWindows.add(scrollPane, BorderLayout.CENTER);
        workWindows.add(button, BorderLayout.SOUTH);
        //Unschließbar machen
        workWindows.setDefaultCloseOperation(0);
        //Groesse des Fenster dynamisch festlegen
        int heigh = new Double(dm.getHeight() / 2.4).intValue();
        int width = new Double(dm.getWidth() / 1.4).intValue();
        int heighTaskLeiste = new Double(dm.getHeight() / 22).intValue();
        int widthShowTask = new Double(dm.getWidth() / 3.6).intValue();

        workWindows.setSize(width, heigh);
        workWindows.setLocation(widthShowTask, dm.getHeight() - heigh
                - heighTaskLeiste);
        
        ActionListener al = new ActionListener() {
            //Bei Buttonklick Feedback holen aus GATE - System
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equals("Feedback")) {
                    if(ActionExport2Gate.giveFeedback){
                        ActionExport2Gate.giveFeedback = false;
                        label.setText(GATEHelper.retrieve("/PerformTest?sid="+Main.sID+"&testid="+Main.testID));
                    }
                    else{
                        JOptionPane.showMessageDialog(null, "Bitte erst Export2Gate ausführen");
                    }
                }
                    }
                 
        };
        button.addActionListener(al);
        workWindows.setAlwaysOnTop(true);
        workWindows.setVisible(true);
    }
}

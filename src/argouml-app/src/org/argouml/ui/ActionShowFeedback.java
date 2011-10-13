package org.argouml.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.argouml.application.Main;

/**
 * Klasse zum Anzeigen des Feedbacks
 * @author Joachim Schramm
 *
 */
public class ActionShowFeedback {

    public ActionShowFeedback() {

    }

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
                        DefaultHttpClient client = new DefaultHttpClient();
                        BasicClientCookie cookie = new BasicClientCookie(
                                "JSESSIONID", Main.sessionID);

                        // cookie.setSecure(true);
                        cookie.setPath("/");
                        cookie.setVersion(1);
                        //cookie.setDomain("localhost");
                        cookie.setDomain("si.in.tu-clausthal.de");
                        client.getCookieStore().addCookie(cookie);
                        
                        try {
                            
                            //HttpGet post = new HttpGet(
                              //      "http://localhost:8080/SubmissionInterface/servlets/PerformTest?sid="+Main.sID+"&testid="+Main.testID);
                            
                            HttpGet post = new HttpGet(
                                    "http://si.in.tu-clausthal.de/umlgate/servlets/PerformTest?sid="+Main.sID+"&testid="+Main.testID);
                            
                            HttpClient httpclient = new DefaultHttpClient();
                            httpclient.getParams().setParameter(
                                    CoreProtocolPNames.PROTOCOL_VERSION,
                                    HttpVersion.HTTP_1_1);
                            
                            HttpResponse response = client.execute(post);
                            HttpEntity resEntity = response.getEntity();
                            System.out.println(response.getStatusLine());
                            if (resEntity != null) {
                                label.setText(EntityUtils.toString(resEntity));
                            }
                            if (resEntity != null) {
                                resEntity.consumeContent();
                            }
                            try {
                                Thread.sleep(1000*2);
                            } catch (InterruptedException e1) {
                                Thread.interrupted();
                            }

                        } catch (Exception e1) {
                        }
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

package org.argouml.ui;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

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
 * Klasse zum Anzeigen der Aufgabenstellung
 * @author Joachim Schramm
 *
 */
public class ActionShowTask implements Runnable {

    public ActionShowTask() {

    }

    public void showTask() {
        
        //Dummythread starten
        Thread thread = new Thread(this);
        thread.start();
        //Windows Auflösung erkennen
        GraphicsEnvironment env = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        GraphicsDevice gd = env.getDefaultScreenDevice();
        DisplayMode dm = gd.getDisplayMode();
        //Fenster erzeugen
        JFrame workWindows = new JFrame("Aufgabenstellung");
        //Aufgabenstellung parsen
        String aufgabenstellung = parseHTML(Main.taskDescription);
        //Scrollbare Textarea mit Aufgabenstellung ins Fenster packen
        JTextArea textArea = new JTextArea(aufgabenstellung, 30, 30);

        JScrollPane scrollPane = new JScrollPane(textArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        textArea.setEditable(false);
        textArea.setCaretPosition(0);

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(new Color(238,238,238));
        
        workWindows.add(scrollPane);

        workWindows.setVisible(true);
        //Unschließbar machen
        workWindows.setDefaultCloseOperation(0);
        //Groesse des Fenster dynamisch festlegen
        int heigh = new Double(dm.getHeight() / 2.4).intValue();
        int width = new Double(dm.getWidth() / 3.6).intValue();
        int heighTaskLeiste = new Double(dm.getHeight() / 22).intValue();
        
        workWindows.setSize(width, heigh);
        workWindows.setLocation(0, dm.getHeight() - heigh - heighTaskLeiste);
        //Immer im Vordergrund
        workWindows.setAlwaysOnTop(true);

    }
    //Alle HTML Sonderzeichen parsen
    public String parseHTML(String html) {
        String text;
        text = html.replaceAll("&Auml;", "Ae");
        text = text.replaceAll("&Ouml;", "Oe");
        text = text.replaceAll("&Uuml;", "Ue");
        text = text.replaceAll("&szlig;", "ss");
        text = text.replaceAll("&auml;", "ae");
        text = text.replaceAll("&uuml;", "ue");
        text = text.replaceAll("&ouml;", "oe");
        return text;
    }
    //Dummythread: Ständige Anfrage an Server, um das Ausloggen zu verhindern 
    public void run() {
        DefaultHttpClient client = new DefaultHttpClient();
        BasicClientCookie cookie = new BasicClientCookie("JSESSIONID",
                Main.sessionID);

        URL gateURL = null;
        try {
            gateURL = new URL(Main.servletPath);
        } catch (MalformedURLException e) {
        }

        cookie.setPath("/");
        cookie.setVersion(1);
        cookie.setDomain(gateURL.getHost());
        if (gateURL.getProtocol().equals("https"))
            cookie.setSecure(true);
        client.getCookieStore().addCookie(cookie);
        try {
            while (!Thread.interrupted()) {
                HttpGet post = new HttpGet(
                        Main.servletPath + "/Nope");
                HttpClient httpclient = new DefaultHttpClient();
                httpclient.getParams().setParameter(
                        CoreProtocolPNames.PROTOCOL_VERSION,
                        HttpVersion.HTTP_1_1);
                System.out
                        .println("executing request " + post.getRequestLine());
                HttpResponse response = client.execute(post);
                HttpEntity resEntity = response.getEntity();
                System.out.println(response.getStatusLine());
                if (resEntity != null) {
                    System.out.println(EntityUtils.toString(resEntity));
                }
                if (resEntity != null) {
                    resEntity.consumeContent();
                }
                try{
                    Thread.sleep(1000 * 2);
                }catch(InterruptedException e1){
                    Thread.interrupted();
                }
            }
        } catch (Exception e) {
        }

    }

}

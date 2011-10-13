package org.argouml.ui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.argouml.application.Main;
import org.argouml.i18n.Translator;
import org.argouml.persistence.PersistenceManager;
import org.argouml.uml.ui.ActionSaveProject;
import org.argouml.uml.ui.SaveGraphicsManager;
import org.tigris.gef.base.SaveGraphicsAction;


/**
 * Klasse zum Hochladen einer XMI und ZARGO Datei ins GATE - System
 * @author Joachim Schramm
 *
 */
public class ActionExport2Gate extends AbstractAction {

    private static final Logger LOG = Logger.getLogger(ActionSaveProject.class);
    
    //Singleton
    private boolean feedbackOn = false;
    public static boolean giveFeedback = false;

    public ActionExport2Gate() {
        super(Translator.localize("action.export-project2Gate"));
    }

    public void actionPerformed(ActionEvent e) {

        PersistenceManager pm = PersistenceManager.getInstance();

        //Zwei Tempdateien erzeugen und lokal speichern
        File theFile2 = null;
        try {
            theFile2 = File.createTempFile("abc", ".zargo");
            theFile2.deleteOnExit();
        } catch (IOException e2) {
            // TODO: Auto-generated catch block
            LOG.error("Exception", e2);
        }
        
        File theFile = null;
        try {
            theFile = File.createTempFile("abcde", ".xmi");
            theFile.deleteOnExit();
        } catch (IOException e2) {
            // TODO: Auto-generated catch block
            LOG.error("Exception", e2);
        }

        File theFile3 = null;
        try {
            theFile3 = File.createTempFile("abcde", ".png");
            theFile3.deleteOnExit();
        } catch (IOException e2) {
            // TODO: Auto-generated catch block
            LOG.error("Exception", e2);
        }

        if (theFile != null) {
            ProjectBrowser.getInstance().trySaveWithProgressMonitor(true,
                    theFile, false);
        }

        ProjectBrowser.getInstance().trySaveWithProgressMonitor(true, theFile2,
                false);

        // save active diagram as image
        SaveGraphicsAction sga = SaveGraphicsManager.getInstance().getSaveActionBySuffix("png");
        FileOutputStream fo = null;
        try {
            fo = new FileOutputStream(theFile3);
        } catch (FileNotFoundException e2) {
            LOG.error("Exception", e2);
        }
        sga.setStream(fo);
        sga.setScale(1);
        try {
            sga.actionPerformed(null);
        } finally {
            try {
                fo.close();
            } catch (IOException e1) {
            }
        }

        try {
            //Hochladen
            upload(Main.taskID, Main.sessionID, theFile.getAbsolutePath(), "xmi");
            upload(Main.taskID, Main.sessionID, theFile2.getAbsolutePath(), "zargo");
            upload(Main.taskID, Main.sessionID, theFile3.getAbsolutePath(), "png");
            giveFeedback = true;
            JOptionPane.showMessageDialog(null, "Upload erfolgreich");
            
        } catch (ClientProtocolException e1) {
            JOptionPane.showMessageDialog(null, "Upload nicht erfolgreich. Bitte nocheinmal probieren");
            LOG.error("Exception", e1);
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(null, "Upload nicht erfolgreich. Bitte nocheinmal probieren");
            LOG.error("Exception", e1);
        } catch (InterruptedException e1) {
            JOptionPane.showMessageDialog(null, "Upload nicht erfolgreich. Bitte nocheinmal probieren");
            LOG.error("Exception", e1);
        }

        // Aufrufen des Feedbackfensters
        
        if (!feedbackOn){
            feedbackOn = true;
            ActionShowFeedback feedback = new ActionShowFeedback();
            feedback.showFeedback();
        }
        

    }

    public void upload(String taskID, String sessionID, String fileString, String fileExtension)
        throws ClientProtocolException, IOException, InterruptedException {
        URL gateURL = null;
        try {
            gateURL = new URL(Main.servletPath);
        } catch (MalformedURLException e) {
        }

        DefaultHttpClient client = new DefaultHttpClient();
        BasicClientCookie cookie = new BasicClientCookie("JSESSIONID",
                sessionID);

        cookie.setPath("/");
        cookie.setVersion(1);
        cookie.setDomain(gateURL.getHost());
        if (gateURL.getProtocol().equals("https"))
            cookie.setSecure(true);
        client.getCookieStore().addCookie(cookie);

        HttpPost post = new HttpPost(
                Main.servletPath + "/SubmitSolution?taskid=" + taskID);
        
        HttpClient httpclient = new DefaultHttpClient();
        httpclient.getParams().setParameter(
                CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        File file = new File(fileString);
        // FileEntity reqEntity = new FileEntity(file,
        // "application/octet-stream");

        MultipartEntity reqEntity2 = new MultipartEntity();

        FileBody bin = null;
        if (fileExtension.equals("png"))
        {
            bin = new FileBody(file, "loesung."+fileExtension, "image/png");
        } else {
            bin = new FileBody(file, "loesung."+fileExtension,
                "text/xml", "utf-8");
        }

        //Wichtig. Timingproblem
        while(bin.getContentLength()!=file.length()) {
            Thread.sleep(100);
        }
        //FileBody bin = new FileBody(file);
        reqEntity2.addPart("file", bin);

        post.setEntity(reqEntity2);
        System.out.println("executing request " + post.getRequestLine());
        HttpResponse response = client.execute(post);
        Main.sID = response.getFirstHeader("SID").getValue();
        Main.testID = response.getFirstHeader("TID").getValue();

        HttpEntity resEntity = response.getEntity();
        System.out.println(response.getStatusLine());
        if (resEntity != null) {
            System.out.println(EntityUtils.toString(resEntity));
        }
        if (resEntity != null) {
            resEntity.consumeContent();
        }
        httpclient.getConnectionManager().shutdown();

        
    }
/*
    public String parseFilePath(File file) {
        String filePath;
        filePath = file.toString();
        filePath = filePath.replace("\\", "\\" + "\\");
        return filePath;

    }
   */
}
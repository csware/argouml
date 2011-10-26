package org.argouml.ui;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.argouml.application.Main;
import org.argouml.i18n.Translator;
import org.argouml.persistence.PersistenceManager;
import org.argouml.ui.targetmanager.TargetEvent;
import org.argouml.ui.targetmanager.TargetListener;
import org.argouml.ui.targetmanager.TargetManager;
import org.argouml.uml.ui.ActionSaveProject;
import org.argouml.uml.ui.SaveGraphicsManager;
import org.argouml.util.GATEHelper;
import org.argouml.util.GATEPartnerSelectionDialog;
import org.tigris.gef.base.SaveGraphicsAction;

/**
 * Klasse zum Hochladen einer XMI und ZARGO Datei ins GATE - System
 * 
 * @author Joachim Schramm
 * 
 */
public class ActionExport2Gate extends AbstractAction {

    private static final Logger LOG = Logger.getLogger(ActionSaveProject.class);

    // Singleton
    private boolean feedbackOn = false;

    public static boolean giveFeedback = false;

    public ActionExport2Gate() {
        super(Translator.localize("action.export-project2Gate"));
    }

    private static ActionExport2Gate targetFollower;

    public static ActionExport2Gate getTargetFollower() {
        if (targetFollower == null) {
            targetFollower  = new ActionExport2Gate();
            TargetManager.getInstance().addTargetListener(new TargetListener() {
                public void targetAdded(TargetEvent e) {
                    setTarget();
                }
                public void targetRemoved(TargetEvent e) {
                    setTarget();
                }

                public void targetSet(TargetEvent e) {
                    setTarget();
                }
                private void setTarget() {
                    targetFollower.setEnabled(Main.taskID != null);
                }
            });
            targetFollower.setEnabled(Main.taskID != null);
        }
        return targetFollower;
    }

    public void actionPerformed(ActionEvent e) {

        PersistenceManager pm = PersistenceManager.getInstance();

        List<Integer> selectedPartners = new LinkedList<Integer>();

        Main.sID = null;
        if (GATEHelper.retrieve("/ShowTask?onlydescription=true&taskid=" + Main.taskID).equals("")) {
            JOptionPane.showMessageDialog(null, "Verbindungsaufbau zum Server fehlgeschlagen.\nBitte mit \"Projekt speichern unter\" lokal abspeichern, ArgoUML neu starten und erneut versuchen.");
            Main.taskID = null;
            if (e != null && e.getSource() instanceof JButton) {
                ((JButton)e.getSource()).setEnabled(false);
            }
            return;
        }
        if (Main.sID == null) {
            // recheck SID
            GATEPartnerSelectionDialog psf = new GATEPartnerSelectionDialog();
            if (psf.success == false) return;
            selectedPartners = psf.selectedPartners;
        }

        // Zwei Tempdateien erzeugen und lokal speichern
        File theFile2 = null;
        try {
            theFile2 = File.createTempFile("argoumlloesung", ".zargo");
            theFile2.deleteOnExit();
        } catch (IOException e2) {
            // TODO: Auto-generated catch block
            LOG.error("Exception", e2);
        }

        File theFile = null;
        try {
            theFile = File.createTempFile("argoumlloesung", ".xmi");
            theFile.deleteOnExit();
        } catch (IOException e2) {
            // TODO: Auto-generated catch block
            LOG.error("Exception", e2);
        }

        File theFile3 = null;
        try {
            theFile3 = File.createTempFile("argoumlloesung", ".png");
            theFile3.deleteOnExit();
        } catch (IOException e2) {
            // TODO: Auto-generated catch block
            LOG.error("Exception", e2);
        }

        if (theFile != null) {
            ProjectBrowser.getInstance().trySave(true,
                    theFile, null);
        }

        ProjectBrowser.getInstance().trySave(true, theFile2,
                null);

        // save active diagram as image
        SaveGraphicsAction sga = SaveGraphicsManager.getInstance()
                .getSaveActionBySuffix("png");
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
            // Hochladen
            GATEHelper.upload(Main.taskID, Main.sessionID, theFile, "xmi",
                    selectedPartners);
            GATEHelper.upload(Main.taskID, Main.sessionID, theFile2, "zargo",
                    new LinkedList<Integer>());
            GATEHelper.upload(Main.taskID, Main.sessionID, theFile3, "png",
                    new LinkedList<Integer>());
            giveFeedback = (Main.testID != null && !Main.testID.equals(""));
            JOptionPane.showMessageDialog(null, "Upload erfolgreich");
        } catch (ClientProtocolException e1) {
            JOptionPane.showMessageDialog(null,
                    "Upload nicht erfolgreich. Bitte noch einmal probieren");
            LOG.error("Exception", e1);
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(null,
                    "Upload nicht erfolgreich. Bitte noch einmal probieren");
            LOG.error("Exception", e1);
        } catch (InterruptedException e1) {
            JOptionPane.showMessageDialog(null,
                    "Upload nicht erfolgreich. Bitte noch einmal probieren");
            LOG.error("Exception", e1);
        }

        // Aufrufen des Feedbackfensters
        if (!feedbackOn && giveFeedback) {
            feedbackOn = true;
            ActionShowFeedback feedback = new ActionShowFeedback();
            feedback.showFeedback();
        }
    }
}

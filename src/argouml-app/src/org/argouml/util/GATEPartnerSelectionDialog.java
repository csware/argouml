package org.argouml.util;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.argouml.application.Main;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class GATEPartnerSelectionDialog extends ArgoDialog {

    public boolean success = false;

    public List<Integer> selectedPartners = new LinkedList<Integer>();

    private List<PossiblePartner> possiblePartners = new LinkedList<PossiblePartner>();

    private JComboBox partnerComboBoxes[] = null;

    private JButton okButton = new JButton();

    private JButton cancelButton = new JButton();

    private JLabel text;

    public GATEPartnerSelectionDialog() {
        super("Abgabe starten", ArgoDialog.CLOSE_OPTION, true);

        setLayout(new BorderLayout());

        text = new JLabel("Info");
        add(text, BorderLayout.NORTH);

        nameButton(okButton, "Absenden");
        okButton.setMnemonic('s');
        nameButton(cancelButton, "button.cancel");

        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                selectedPartners.clear();
                if (partnerComboBoxes != null) {
                    for (int i = 0; i < partnerComboBoxes.length; i++) {
                        if (partnerComboBoxes[i].getSelectedItem() != null) {
                            selectedPartners.add(((PossiblePartner)partnerComboBoxes[i].getSelectedItem()).getId());
                        }
                    }
                }
                success = true;
                hide();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        JPanel mainPanel = new JPanel();
        add(new JScrollPane(mainPanel), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        setSize(300, 200);
        setLocationRelativeTo(getParent());

        String partnersXML = GATEHelper
                .retrieve("/SubmitSolution?onlypartners=true&taskid="
                        + Main.taskID);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(new InputSource(new StringReader(
                    partnersXML)));
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        }
        if (document == null
                || document.getFirstChild() == null
                || !document.getFirstChild().getNodeName()
                        .equals("possiblepartners")) {
            JOptionPane.showMessageDialog(null,
                    "Something strange happened. :(");
        } else {
            Node possiblePartnersNode = document.getFirstChild();
            if (possiblePartnersNode.getFirstChild() == null) {
                JOptionPane.showMessageDialog(null,
                        "Something strange happened. :(");
                return;
            }

            if (possiblePartnersNode.getFirstChild().getNodeName()
                    .equals("info")) {
                Node info = possiblePartnersNode.getFirstChild();
                text.setText("<HTML>" + info.getTextContent() + "</HTML>");
            } else {
                Node partners = possiblePartnersNode.getFirstChild();
                Node maxAttribute = partners.getAttributes().getNamedItem(
                        "maxPartners");
                if (maxAttribute != null) {
                    int maxPartners = 0;
                    try {
                        maxPartners = Integer.parseInt(maxAttribute
                                .getNodeValue());
                    } catch (NumberFormatException e) {
                    }
                    if (maxPartners > 0) {
                        add(mainPanel, BorderLayout.CENTER);
                        mainPanel.setLayout(new GridLayout(maxPartners, 1));
                    }
                    if (partners.getAttributes().getNamedItem("info") != null) {
                        text.setText("<HTML>"
                                + partners.getAttributes().getNamedItem("info")
                                        .getNodeValue() + "</HTML>");
                    }
                    NodeList partnersNodeList = partners.getChildNodes();
                    possiblePartners.add(new PossiblePartner(0, "-"));
                    for (int i = 0; i < partnersNodeList.getLength(); i++) {
                        Node possiblePartner = partnersNodeList.item(i);
                        possiblePartners.add(new PossiblePartner(Integer
                                .parseInt(possiblePartner.getAttributes()
                                        .getNamedItem("id").getNodeValue()),
                                possiblePartner.getTextContent()));
                    }
                    if (maxPartners > 0) {
                        partnerComboBoxes = new JComboBox[maxPartners];
                        for (int i = 0; i < maxPartners; i++) {
                            partnerComboBoxes[i] = new JComboBox();
                            for (int j = 0; j < possiblePartners.size(); j++) {
                                partnerComboBoxes[i].addItem(possiblePartners.get(j));
                            }
                            mainPanel.add(partnerComboBoxes[i]);
                        }
                    }
                }
            }
            setVisible(true);
        }
    }

    private class PossiblePartner {
        private int id;

        private String name;

        public PossiblePartner(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
        @Override
        public String toString() {
            return getName();
        }
    }
}

/*
 * Created on 2005-11-15
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.sap.engine.services.webservices.espbase.client.dynamic.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.sap.engine.services.webservices.espbase.client.dynamic.DGenericService;
import com.sap.engine.services.webservices.espbase.client.dynamic.GenericServiceFactory;
import com.sap.engine.services.webservices.espbase.client.dynamic.ServiceFactoryConfig;

/**
 * @author ivan-m
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ServiceFactoryManagementPanel extends JPanel implements ActionListener {
  
  private static final String OPEN_WSDL_ACTION_COMMAND = "open.wsdl.action";
  private static final String OPEN_TEMP_DIR_ACTION_COMMAND = "open.temp.dir.action";
  private static final String CREATE_ACTION_COMMAND = "create.action";
  
  private JTextField tempDirField; 
  private JTextField addClasspathField;
  private JTextField proxyHostField;
  private JTextField proxyPortField;
  private JTextField proxyBypassField;
  private JTextField javacPathField; 
  private JTextField wsdlPathField;
  private MetadataVisualFrame metadataVisualFrame;
  private JTextArea errorTextArea;
  
  protected ServiceFactoryManagementPanel(MetadataVisualFrame metadataVisualFrame) {
    this.metadataVisualFrame = metadataVisualFrame;
    visualInit();
  }
  
  private void visualInit() {
    setLayout(new GridBagLayout());
    GridBagConstraints gridBagConstr = new GridBagConstraints();
    gridBagConstr.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstr.anchor = GridBagConstraints.WEST;
    Insets insets = new Insets(5, 5, 0, 5);
    gridBagConstr.insets = insets;
    
    tempDirField = new JTextField(); 
    addClasspathField = new JTextField();
    proxyHostField = new JTextField();
    proxyPortField = new JTextField();
    proxyBypassField = new JTextField();
    javacPathField = new JTextField();
    wsdlPathField = new JTextField();
    
    addJLabel(gridBagConstr, 0, "temporary dir: ");
    addFileManagementPanel(tempDirField, gridBagConstr, 0, OPEN_TEMP_DIR_ACTION_COMMAND);
    insets.top = 0;
    
    addJLabel(gridBagConstr, 1, "additional classpath: ");
    addJTextField(gridBagConstr, 1, addClasspathField);
    
    addJLabel(gridBagConstr, 2, "proxy host: ");
    addJTextField(gridBagConstr, 2, proxyHostField);
    
    addJLabel(gridBagConstr, 3, "proxy port: ");
    addJTextField(gridBagConstr, 3, proxyPortField);
    
    addJLabel(gridBagConstr, 4, "proxy bypass: ");
    addJTextField(gridBagConstr, 4, proxyBypassField);
    
    addJLabel(gridBagConstr, 5, "javac path: ");
    addJTextField(gridBagConstr, 5, javacPathField);
    
    addJLabel(gridBagConstr, 6, "wsdl path: ");
    addFileManagementPanel(wsdlPathField, gridBagConstr, 6, OPEN_WSDL_ACTION_COMMAND);
    
    gridBagConstr.gridx = 0;
    gridBagConstr.gridy = 7;
    gridBagConstr.weightx = 0.0;
    JButton createButton = new JButton("create");
    createButton.setActionCommand(CREATE_ACTION_COMMAND);
    createButton.addActionListener(this);
    add(createButton, gridBagConstr);
    
    gridBagConstr.gridy = 8;
    insets.top = 5;
    add(new JLabel("Errors:"), gridBagConstr);

    insets.top = 0;
    insets.bottom = 5;
    gridBagConstr.anchor = GridBagConstraints.CENTER;
    gridBagConstr.fill = GridBagConstraints.BOTH;
    gridBagConstr.gridy = 9;
    gridBagConstr.gridwidth = 2;
    gridBagConstr.weightx = 1.0;
    gridBagConstr.weighty = 1.0;
    errorTextArea = new JTextArea();
    JScrollPane scrollPane = new JScrollPane(errorTextArea);
    add(scrollPane, gridBagConstr);
  }
  
  private void addFileManagementPanel(JTextField textField, GridBagConstraints gridBagConstr, int gridy, String actionCommand) {
    JPanel managementPanel = createFileManagementPanel(textField, actionCommand);
    gridBagConstr.gridx = 1;
    gridBagConstr.gridy = gridy;
    gridBagConstr.weightx = 1.0;
    add(managementPanel, gridBagConstr);
  }
  
  private JPanel createFileManagementPanel(JTextField textField, String actionCommand) {
    JPanel wsdlManagementPanel = new JPanel();
    wsdlManagementPanel.setLayout(new GridBagLayout());
    GridBagConstraints gridBagConstr = new GridBagConstraints();
    gridBagConstr.fill = GridBagConstraints.HORIZONTAL;
    
    gridBagConstr.gridx = 0;
    gridBagConstr.weightx = 1.0;
    wsdlManagementPanel.add(textField, gridBagConstr);
    
    gridBagConstr.gridx = 1;
    gridBagConstr.weightx = 0.0;
    JButton openButton = new JButton("open");
    openButton.setActionCommand(actionCommand);
    openButton.addActionListener(this);
    wsdlManagementPanel.add(openButton, gridBagConstr);
    
    return(wsdlManagementPanel);
  }
  
  private void addJLabel(GridBagConstraints gridBagConstr, int gridy, String label) {
    gridBagConstr.gridx = 0;
    gridBagConstr.gridy = gridy;
    gridBagConstr.weightx = 0.0;
    add(new JLabel(label), gridBagConstr);
  }
  
  private void addJTextField(GridBagConstraints gridBagConstr, int gridy, JTextField textField) {
    gridBagConstr.gridx = 1;
    gridBagConstr.gridy = gridy;
    gridBagConstr.weightx = 1.0;
    add(textField, gridBagConstr);
  }
  
  private void initMetadataVisualFrame() {
    setServiceFactoryConfigProperty(tempDirField, ServiceFactoryConfig.TEMP_DIR);
    setServiceFactoryConfigProperty(addClasspathField, ServiceFactoryConfig.ADD_CLASSPATH);
    setServiceFactoryConfigProperty(proxyHostField, ServiceFactoryConfig.INET_PROXY_HOST);
    setServiceFactoryConfigProperty(proxyPortField, ServiceFactoryConfig.INET_PROXY_PORT);
    setServiceFactoryConfigProperty(proxyBypassField, ServiceFactoryConfig.INET_PROXY_BYPASS);
    setServiceFactoryConfigProperty(javacPathField, ServiceFactoryConfig.JAVAC_PATH);
  }
  
  private void setServiceFactoryConfigProperty(JTextField textField, String key) {
    String value = textField.getText();
    if(value.equals("")) {
      metadataVisualFrame.removeProperty(key);
    } else {
      metadataVisualFrame.setPropertyString(key, value);
    }
  }
  
  public void actionPerformed(ActionEvent actionEvent) {
    String actionCommand = actionEvent.getActionCommand();
    if(actionCommand.equals(OPEN_WSDL_ACTION_COMMAND)) {
      performChooseFile(wsdlPathField, JFileChooser.FILES_ONLY);
    } else if(actionCommand.equals(OPEN_TEMP_DIR_ACTION_COMMAND)) {
      performChooseFile(tempDirField, JFileChooser.DIRECTORIES_ONLY);
    } else if(actionCommand.equals(CREATE_ACTION_COMMAND)) {
      createAndVisualizeMetadata();
    }
  }
  
  private void performChooseFile(JTextField textField, int fileSelectionMode) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileSelectionMode(fileSelectionMode);
    int fileChooseResult = fileChooser.showOpenDialog(this);
    File choosedFile = fileChooser.getSelectedFile();
    if(choosedFile != null) {
      try {
        textField.setText(choosedFile.getCanonicalPath());
      } catch(Exception exc) {
        exc.printStackTrace();
      }
    }
  }
  
  private void createAndVisualizeMetadata() {
    try {
      errorTextArea.setText("");
      initMetadataVisualFrame();
      metadataVisualFrame.visualizeOnlyService(wsdlPathField.getText());
    } catch(Exception exc) {
      displayError(exc);
    }
  }
  
  private void displayError(Throwable tr) {
    errorTextArea.append(tr.toString() + "\n");
    ByteArrayOutputStream throwableByteArrayOutput = new ByteArrayOutputStream();
    PrintStream throwablePrintStream = new PrintStream(throwableByteArrayOutput);
    tr.printStackTrace(throwablePrintStream);
    throwablePrintStream.flush();
    errorTextArea.append(new String(throwableByteArrayOutput.toByteArray()) + "\n"); //$JL-I18N$
    throwablePrintStream.close();
  }
}

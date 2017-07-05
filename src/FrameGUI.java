

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.json.JSONException;

import com.jlrfid.service.RFIDException;

import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JComboBox;

public class FrameGUI extends Thread {

	private JFrame rfidFrame;
	private JTextField ipTextField;
	private JLabel portLabel;
	private JTextField portTextField;
	private JTable dataTable;
	private JLabel ipLabel;
	private JToggleButton connectToggle;
	private JLabel antennaLabel;
	private JCheckBox antenna1Box;
	private JCheckBox antenna2Box;
	private JCheckBox antenna3Box;
	private JCheckBox antenna4Box;
	private JLabel readCtrlLabel;
	private JToggleButton onceLoopToggle;
	private JToggleButton startToggle;
	private JButton resetButton;
	private JButton saveButton;
	private JScrollPane scrollPane;
	private String[] readerList = {"Reader1", "Reader2", "Reader3"};
	public static JComboBox<Object> comboBox;
	
	private RfidController rfidCtrl;
	
	public static void main(String[] args){
		FrameGUI window = new FrameGUI();
		window.rfidFrame.setVisible(true);
		window.start();
	}
	

	@Override
	public void run() {
		while (true) {
			try {
				update();
				Thread.sleep(1);
			} catch (Exception e){
//				e.printStackTrace();	
			}
		}
	}

	public FrameGUI() {
		rfidCtrl = new RfidController();
		initialize();
		setControllerEnable(false);
	}

	private void initialize() {
		rfidCtrl.setReader(readerList[0]);
		System.out.println(readerList[0]);
		rfidFrame = new JFrame();
		rfidFrame.setTitle("RFID CONTROLLER");
		rfidFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		rfidFrame.addWindowListener(new WindowAdapter() {
			@Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        int reply = JOptionPane.showConfirmDialog(rfidFrame, 
		            "Are you sure to close this window?", "Really Closing?", 
		            JOptionPane.YES_NO_OPTION);
		        if (reply == JOptionPane.YES_OPTION){      	
		        	if (startToggle.isSelected()) {
		        		rfidCtrl.disconnect();
		        	}
		        	rfidFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		        }
		    }
		});
		rfidFrame.setBounds(100, 100, 800, 490);

		rfidFrame.setResizable(false);
		
		ipLabel = new JLabel("IP");
		ipLabel.setBounds(15, 15, 25, 25);
		
		ipTextField = new JTextField();
		ipTextField.setText("192.168.1.201");
		ipTextField.setBounds(45, 15, 120, 25);
		ipTextField.setColumns(10);
		
		portLabel = new JLabel("Port");
		portLabel.setBounds(15, 45, 25, 25);
		
		portTextField = new JTextField();
		portTextField.setText("20058");
		portTextField.setBounds(45, 45, 120, 25);
		portTextField.setColumns(10);
		rfidFrame.getContentPane().setLayout(null);
		rfidFrame.getContentPane().add(ipLabel);
		rfidFrame.getContentPane().add(ipTextField);
		rfidFrame.getContentPane().add(portLabel);
		rfidFrame.getContentPane().add(portTextField);
		
		connectToggle = new JToggleButton("Connect");
		connectToggle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (connectToggle.isSelected()) {
					boolean isConnect = rfidCtrl.connectRFID(
							String.valueOf(ipTextField.getText()), 
							0, 
							Integer.parseInt(portTextField.getText())
					);
					if (!isConnect) {
						connectToggle.setSelected(false);
						connectToggle.setText("Connect");
						setControllerEnable(false);
						JOptionPane.showMessageDialog(rfidFrame, "Plese check your connection and try again.");
					} else {
						setControllerEnable(true);
						connectToggle.setText("Disconnect");
					}
				} else {
					connectToggle.setText("Connect");
					setControllerEnable(false);
					rfidCtrl.disconnect();
				}
			}
		});
		connectToggle.setBounds(15, 75, 150, 25);
		rfidFrame.getContentPane().add(connectToggle);
		
		comboBox = new JComboBox<Object> (readerList);
		comboBox.setBounds(15, 410, 150, 25);
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				rfidCtrl.setReader((String)comboBox.getSelectedItem());
			}
		});
		rfidFrame.getContentPane().add(comboBox);
		
		antennaLabel = new JLabel("Antenna");
		antennaLabel.setBounds(15, 110, 150, 25);
		rfidFrame.getContentPane().add(antennaLabel);
		
		antenna1Box = new JCheckBox("Antenna 1");
		antenna1Box.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					rfidCtrl.setAntenna(antenna1Box.isSelected(), 0);
				} catch (RFIDException e1) {
					e1.printStackTrace();
				}
			}
		});
		antenna1Box.setHorizontalAlignment(SwingConstants.CENTER);
		antenna1Box.setBounds(15, 140, 150, 25);
		rfidFrame.getContentPane().add(antenna1Box);
		
		antenna2Box = new JCheckBox("Antenna 2");
		antenna2Box.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					rfidCtrl.setAntenna(antenna2Box.isSelected(), 1);
				} catch (RFIDException e1) {
					e1.printStackTrace();
				}
			}
		});
		antenna2Box.setHorizontalAlignment(SwingConstants.CENTER);
		antenna2Box.setBounds(15, 170, 150, 25);
		rfidFrame.getContentPane().add(antenna2Box);
		
		antenna3Box = new JCheckBox("Antenna 3");
		antenna3Box.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					rfidCtrl.setAntenna(antenna3Box.isSelected(), 2);
				} catch (RFIDException e1) {
					e1.printStackTrace();
				}
			}
		});
		antenna3Box.setHorizontalAlignment(SwingConstants.CENTER);
		antenna3Box.setBounds(15, 200, 150, 25);
		rfidFrame.getContentPane().add(antenna3Box);
		
		antenna4Box = new JCheckBox("Antenna 4");
		antenna4Box.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					rfidCtrl.setAntenna(antenna4Box.isSelected(), 3);
				} catch (RFIDException e1) {
					e1.printStackTrace();
				}
			}
		});
		antenna4Box.setHorizontalAlignment(SwingConstants.CENTER);
		antenna4Box.setBounds(15, 230, 150, 25);
		rfidFrame.getContentPane().add(antenna4Box);
		
		readCtrlLabel = new JLabel("Read Controller");
		readCtrlLabel.setBounds(15, 500, 150, 25);
		rfidFrame.getContentPane().add(readCtrlLabel);
		
		onceLoopToggle = new JToggleButton("Once");
		onceLoopToggle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (onceLoopToggle.isSelected()) {
					onceLoopToggle.setText("Loop");
				} else {
					onceLoopToggle.setText("Once");
				}
				rfidCtrl.setOnceLoop(onceLoopToggle.isSelected());
			}
		});
		onceLoopToggle.setBounds(15, 290, 150, 25);
		rfidFrame.getContentPane().add(onceLoopToggle);
		
		startToggle = new JToggleButton("Start");
		startToggle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (startToggle.isSelected()) {
					startToggle.setText("Stop");
					comboBox.setEnabled(false);
				} else {
					startToggle.setText("Start");
					comboBox.setEnabled(true);
				}
				try {
					rfidCtrl.setStart(startToggle.isSelected());
				} catch (RFIDException e1) {
					e1.printStackTrace();
				}
			}
		});
		startToggle.setBounds(15, 320, 150, 25);
		rfidFrame.getContentPane().add(startToggle);
		
		resetButton = new JButton("Reset");
		resetButton.setBounds(15, 350, 150, 25);
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				rfidCtrl.resetHashMap();
			}
		});
		rfidFrame.getContentPane().add(resetButton);
		
		saveButton = new JButton("Save File");
		saveButton.setBounds(15, 380, 150, 25);
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					rfidCtrl.convertJson();
					JOptionPane.showMessageDialog(rfidFrame,
						     "Successfully Copied JSON Objectc to File.");
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		rfidFrame.getContentPane().add(saveButton);
		
		String[][] tableData = null;
		String[] tableHeader = {"No.", "Time", "Tag_ID", "Reader", "Antenna"};
		dataTable = new JTable();
		dataTable.setModel(new DefaultTableModel(tableData, tableHeader) {
			private static final long serialVersionUID = -9098365186691267440L;
			public boolean isCellEditable(int row, int column){
				return false;
			}
		});
		dataTable.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		dataTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		dataTable.getColumnModel().getColumn(1).setPreferredWidth(158);
		dataTable.getColumnModel().getColumn(2).setPreferredWidth(250);
		dataTable.getColumnModel().getColumn(3).setPreferredWidth(60);
		dataTable.getColumnModel().getColumn(3).setPreferredWidth(60);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		dataTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		dataTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
		scrollPane = new JScrollPane();
		scrollPane.setBounds(184, 0, 598, 433);
		rfidFrame.getContentPane().add(scrollPane);
		scrollPane.setViewportView(dataTable);
	}
	
	private boolean isMakeTable;
	public void update() {
		if (!isMakeTable) {
			updateTable();
		}
		if (!RfidController.database.isUpThreadRun) {
			try {
				RfidController.database.updata_database();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void updateTable() {
		Runnable runnable = new Runnable () {
			@Override
			public void run() {
				isMakeTable = true;
				String[][] tableData = rfidCtrl.getTable();
				DefaultTableModel model = (DefaultTableModel)dataTable.getModel();
				if (tableData.length != model.getRowCount()) {
					model.setRowCount(tableData.length);
					for (int i = 0; i < tableData.length; i++) {
						for (int j = 0; j < tableData[i].length; j++) {
							model.setValueAt(tableData[i][j], i, j);
						}
					}
				}
				isMakeTable = false;
			}
		};
		new Thread(runnable).start();
	}
	
		
	private void setControllerEnable(boolean enable) {
		ipTextField.setEnabled(!enable);
		portTextField.setEnabled(!enable);
		antenna1Box.setEnabled(enable);
		antenna2Box.setEnabled(enable);
		antenna3Box.setEnabled(enable);
		antenna4Box.setEnabled(enable);
		onceLoopToggle.setEnabled(enable);
		startToggle.setEnabled(enable);
	}
}

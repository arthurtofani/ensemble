/******************************************************************************

Copyright 2011 Leandro Ferrari Thomaz

This file is part of Ensemble.

Ensemble is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Ensemble is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Ensemble.  If not, see <http://www.gnu.org/licenses/>.

******************************************************************************/

package ensemble.sniffer;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;


import javax.swing.JPanel;
import javax.swing.JLabel;

import java.util.HashMap;

import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.border.EtchedBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;

import ensemble.Command;
import ensemble.Constants;
import ensemble.Parameters;
import ensemble.router.RouterClient;
import ensemble.tools.Loader;

import java.awt.Color;
import java.awt.Component;

// TODO: Auto-generated Javadoc
/**
 * The Class Sniffer.
 */
public class Sniffer extends Agent implements RouterClient {

	/** The agents. */
	HashMap<String, AgentInfo> 	agents;
	
	/** The frame. */
	JFrame frame = new JFrame();
	
	/** The selected node. */
	DefaultMutableTreeNode 	selectedNode;

	/** The root node. */
	DefaultMutableTreeNode 	rootNode;
	
	/** The env node. */
	DefaultMutableTreeNode 	envNode;
	
	/** The tree model. */
	DefaultTreeModel 		treeModel;
	
	/** The txt command. */
	private JTextField txtCommand;
	
	/** The txt name. */
	private JTextField txtName;
	
	/** The txt class. */
	private JTextField txtClass;
	
	/** The txt state. */
	private JTextField txtState;
	
	/** The tree. */
	private JTree 		tree;
	
	/** The txt type. */
	private JTextField txtType;
	
	/** The lbl name. */
	private JLabel lblName;
	
	/** The lbl state. */
	private JLabel lblState;
	
	/** The lbl class. */
	private JLabel lblClass;
	
	/** The lbl type. */
	private JLabel lblType;
	
	/** The pnl parameters. */
	private JPanel pnlParameters;
	
	/** The btn add component. */
	private JButton btnAddComponent;
	
	/** The btn destroy agent. */
	private JButton btnDestroyAgent;
	
	/** The btn start simulation. */
	private JButton btnStartSimulation;
	
	/** The btn stop simulation. */
	private JButton btnStopSimulation;
	
	/** The btn send command. */
	private JButton btnSendCommand;
	
	/** The btn remove component. */
	private JButton btnRemoveComponent;
	
	/** The btn create agent. */
	private JButton btnCreateAgent;
	
	/** The btn facts. */
	private JButton btnFacts;
	
	/** The tbl parameters model. */
	private DefaultTableModel tblParametersModel;
	
	/** The tbl parameters. */
	private JTable tblParameters;
	
	/** The scroll pane_1. */
	private JScrollPane scrollPane_1;
	
	/** The txt xml file. */
	private JTextField txtXMLFile;

	/**
	 * Create the application.
	 * @wbp.parser.entryPoint
	 */
	protected void setup() {
		
		// GUI
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		frame.setTitle("Ensemble Sniffer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		initialize();
		frame.setVisible(true);
		
        // Receive messages
		this.addBehaviour(new ReceiveMessages(this));
		
	}

	/** 
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frame.setBounds(100, 100, 638, 552);
		frame.getContentPane().setLayout(null);
		
		JPanel infoPanel = new JPanel();
		infoPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		infoPanel.setBounds(274, 6, 348, 379);
		frame.getContentPane().add(infoPanel);
		infoPanel.setLayout(null);
		
		lblName = new JLabel("NAME");
		lblName.setBounds(6, 12, 37, 16);
		infoPanel.add(lblName);
		
		lblClass = new JLabel("CLASS");
		lblClass.setBounds(6, 46, 51, 16);
		infoPanel.add(lblClass);
		
		txtName = new JTextField();
		txtName.setEditable(false);
		txtName.setBounds(44, 6, 294, 28);
		infoPanel.add(txtName);
		txtName.setColumns(10);
		
		txtClass = new JTextField();
		txtClass.setEditable(false);
		txtClass.setColumns(10);
		txtClass.setBounds(44, 40, 294, 28);
		infoPanel.add(txtClass);
		
		lblState = new JLabel("STATE");
		lblState.setBounds(6, 80, 51, 16);
		infoPanel.add(lblState);
		
		txtState = new JTextField();
		txtState.setEditable(false);
		txtState.setColumns(10);
		txtState.setBounds(44, 74, 294, 28);
		infoPanel.add(txtState);
		
		lblType = new JLabel("TYPE");
		lblType.setBounds(6, 114, 51, 16);
		infoPanel.add(lblType);
		
		txtType = new JTextField();
		txtType.setEditable(false);
		txtType.setColumns(10);
		txtType.setBounds(44, 108, 294, 28);
		infoPanel.add(txtType);
		
		btnDestroyAgent = new JButton("Destroy Agent");
		btnDestroyAgent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (selectedNode != null) {
					Command cmd = new Command(getAddress(), 
							"/" + Constants.FRAMEWORK_NAME + "/"+selectedNode.toString(), 
							"DESTROY_AGENT");
					cmd.addParameter("NAME", selectedNode.toString());
					sendCommand(cmd);
				}
			}
		});
		btnDestroyAgent.setBounds(180, 339, 158, 29);
		infoPanel.add(btnDestroyAgent);
		
		btnRemoveComponent = new JButton("Remove Component");
		btnRemoveComponent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (selectedNode != null) {
					Command cmd = new Command(getAddress(), 
							"/" + Constants.FRAMEWORK_NAME + "/"+selectedNode.getParent().toString(), 
							"REMOVE_COMPONENT");
					cmd.addParameter("NAME", selectedNode.toString());
					sendCommand(cmd);
				}
			}
		});
		btnRemoveComponent.setBounds(180, 339, 158, 29);
		infoPanel.add(btnRemoveComponent);
		
		btnCreateAgent = new JButton("Create Agent...");
		btnCreateAgent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SnifferDialog dialog = new SnifferDialog(0);
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.setResizable(false);
				dialog.setVisible(true);
				if (dialog.result) {
					Command cmd = new Command(getAddress(), 
							"/" + Constants.FRAMEWORK_NAME + "/" + Constants.ENVIRONMENT_AGENT,
							"CREATE_AGENT");
					// Returns the command string
					cmd.addParameter("NAME", dialog.txtName.getText());
					cmd.addParameter("CLASS", dialog.txtClass.getText());
					for (int i = 0; i < dialog.tableModel.getRowCount(); i++) {
						cmd.addUserParameter((String)dialog.tableModel.getValueAt(i, 0), (String)dialog.tableModel.getValueAt(i,1));
					}
					sendCommand(cmd);
				}
			}
		});
		btnCreateAgent.setBounds(180, 339, 158, 29);
		infoPanel.add(btnCreateAgent);
		
		pnlParameters = new JPanel();
		pnlParameters.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		pnlParameters.setBounds(6, 147, 332, 181);
		infoPanel.add(pnlParameters);
		pnlParameters.setLayout(null);
		
		scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(10, 22, 312, 148);
		pnlParameters.add(scrollPane_1);
		
		tblParameters = new JTable();
		scrollPane_1.setViewportView(tblParameters);
		tblParameters.setRowSelectionAllowed(false);
		tblParametersModel = new DefaultTableModel(
				new Object[][] {},
				new String[] {
					"NAME", "VALUE"
				}) {
			boolean[] columnEditables = new boolean[] {
					false, true
				};
				public boolean isCellEditable(int row, int column) {
					return columnEditables[column];
				}
			};
		tblParameters.setModel(tblParametersModel);
//		tblParameters.setDefaultEditor(String.class, new MyTableEditor());
		tblParameters.getColumnModel().getColumn(0).setMinWidth(30);
		tblParameters.getColumnModel().getColumn(1).setCellEditor(new MyTableCellEditor());
		tblParameters.setBorder(new LineBorder(Color.LIGHT_GRAY));
		
		btnFacts = new JButton("Facts...");
		btnFacts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SnifferFactsDialog dialog = new SnifferFactsDialog();
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.setLocationRelativeTo(frame);
				dialog.setResizable(false);
				// Populates facts table
				Parameters facts = ((ComponentInfo)selectedNode.getUserObject()).facts;
				for (String key : facts.keySet()) {
					dialog.tableModel.addRow(new String[] {key, facts.get(key)});
				}
				dialog.setVisible(true);
				if (dialog.result) {
					Command cmd = new Command(getAddress(), 
							"/" + Constants.FRAMEWORK_NAME + "/" + selectedNode.getParent().toString() + "/" + selectedNode.toString(), 
							"UPDATE_FACTS");
					// Returns the command string
					String parameters = "{";
					for (int i = 0; i < dialog.tableModel.getRowCount(); i++) {
						parameters += dialog.tableModel.getValueAt(i, 0) + "=" + dialog.tableModel.getValueAt(i, 1) + ";";
					}
					parameters += "}";
					cmd.addParameter("FACTS", parameters);
					sendCommand(cmd);
				}
			}
		});
		btnFacts.setBounds(16, 339, 158, 28);
		infoPanel.add(btnFacts);
		
				btnAddComponent = new JButton("Add Component...");
				btnAddComponent.setBounds(16, 339, 158, 29);
				infoPanel.add(btnAddComponent);
				btnAddComponent.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						SnifferDialog dialog = new SnifferDialog(1);
						dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						dialog.setResizable(false);
						dialog.setVisible(true);
						if (dialog.result) {
							Command cmd = new Command(getAddress(), 
									"/" + Constants.FRAMEWORK_NAME + "/" + selectedNode.toString(),
									"ADD_COMPONENT");
							// Returns the command string
							cmd.addParameter("NAME", dialog.txtName.getText());
							cmd.addParameter("CLASS", dialog.txtClass.getText());
							for (int i = 0; i < dialog.tableModel.getRowCount(); i++) {
								cmd.addUserParameter((String)dialog.tableModel.getValueAt(i, 0), (String)dialog.tableModel.getValueAt(i,1));
							}
							if (!dialog.txtEvtType.equals("")) {
								cmd.addUserParameter("EVT_TYPE", dialog.txtEvtType.getText());
							}
							sendCommand(cmd);
						}
					}
				});
				btnAddComponent.setVisible(false);
		
		rootNode = new DefaultMutableTreeNode("Ensemble");
		treeModel = new DefaultTreeModel(rootNode);
		
		JPanel listPanel = new JPanel();
		listPanel.setBounds(6, 6, 256, 379);
		frame.getContentPane().add(listPanel);
		listPanel.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 256, 378);
		listPanel.add(scrollPane);
		
		tree = new JTree(treeModel);
		tree.setShowsRootHandles(true);
		tree.setCellRenderer(new MyTreeCellRenderer());
		scrollPane.setViewportView(tree);
		
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new MyTreeSelectionListener());

		JPanel commandPanel = new JPanel();
		commandPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		commandPanel.setBounds(6, 396, 616, 81);
		frame.getContentPane().add(commandPanel);
		commandPanel.setLayout(null);
		
		JLabel lblCustomCommand = new JLabel("Command");
		lblCustomCommand.setBounds(6, 12, 69, 16);
		commandPanel.add(lblCustomCommand);
		
		txtCommand = new JTextField();
		txtCommand.setBounds(74, 6, 532, 28);
		commandPanel.add(txtCommand);
		txtCommand.setColumns(10);
		
		btnSendCommand = new JButton("Send");
		btnSendCommand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Command cmd = Command.parse(txtCommand.getText());
				cmd.setSender("/console/Sniffer");
				if (selectedNode.getDepth() == 1) {
					cmd.setRecipient("/" + Constants.FRAMEWORK_NAME + "/" + selectedNode);
				} else {
					cmd.setRecipient("/" + Constants.FRAMEWORK_NAME + "/" + selectedNode.getParent() + "/" + selectedNode);
				}
				System.out.println(cmd);
				sendCommand(cmd);
			}
		});
		btnSendCommand.setBounds(489, 41, 117, 29);
		btnSendCommand.setEnabled(false);
		commandPanel.add(btnSendCommand);
		
		btnStartSimulation = new JButton("Start Simulation");
		btnStartSimulation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnStartSimulation.setEnabled(false);
				btnStopSimulation.setEnabled(true);
			}
		});
		btnStartSimulation.setBounds(6, 484, 150, 29);
		frame.getContentPane().add(btnStartSimulation);
		
		btnStopSimulation = new JButton("Stop Simulation");
		btnStopSimulation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Command cmd = new Command(getAddress(), 
						"/" + Constants.FRAMEWORK_NAME + "/" + Constants.ENVIRONMENT_AGENT,
						"STOP_SIMULATION");
				sendCommand(cmd);
				btnCreateAgent.setEnabled(false);
				btnStartSimulation.setEnabled(true);
				btnStopSimulation.setEnabled(false);
			}
		});
		btnStopSimulation.setBounds(472, 484, 150, 29);
		frame.getContentPane().add(btnStopSimulation);
		
		lblName.setVisible(false);
		txtName.setVisible(false);
		lblClass.setVisible(false);
		txtClass.setVisible(false);
		lblState.setVisible(false);
		txtState.setVisible(false);
		lblType.setVisible(false);
		txtType.setVisible(false);
		pnlParameters.setVisible(false);
		btnDestroyAgent.setVisible(false);
		btnRemoveComponent.setVisible(false);
		btnFacts.setVisible(false);
		btnStartSimulation.setEnabled(false);
		btnStopSimulation.setEnabled(true);
		
		txtXMLFile = new JTextField();
		txtXMLFile.setEnabled(false);
		txtXMLFile.setBounds(166, 486, 266, 25);
		frame.getContentPane().add(txtXMLFile);
		txtXMLFile.setColumns(10);
	}
	
	/**
	 * The Class MyTreeCellRenderer.
	 */
	class MyTreeCellRenderer extends DefaultTreeCellRenderer {
	    
		/** The ensemble icon. */
		ImageIcon ensembleIcon;
		
		/** The ma icon. */
		ImageIcon maIcon;
	    
    	/** The ea icon. */
    	ImageIcon eaIcon;
	    
    	/** The comp icon. */
    	ImageIcon compIcon;
	    
    	/** The kb icon. */
    	ImageIcon kbIcon;
	    
    	/** The sensor icon. */
    	ImageIcon sensorIcon;
	    
    	/** The actuator icon. */
    	ImageIcon actuatorIcon;
	    
    	/** The reasoning icon. */
    	ImageIcon reasoningIcon;
	 
	    /**
    	 * Instantiates a new my tree cell renderer.
    	 */
    	public MyTreeCellRenderer() {
	    	ensembleIcon = new ImageIcon("media/ensemble.png");
	        eaIcon = new ImageIcon("media/world.png");
	        maIcon = new ImageIcon("media/agent.png");
	        compIcon = new ImageIcon("media/gear.png");
	        kbIcon = new ImageIcon("media/kb.png");
	        sensorIcon = new ImageIcon("media/sensor.png");
	        actuatorIcon = new ImageIcon("media/actuator.png");
	        reasoningIcon = new ImageIcon("media/reasoning.png");
	    }
	 
	    /* (non-Javadoc)
    	 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
    	 */
    	public Component getTreeCellRendererComponent(JTree tree,
	      Object value,boolean sel,boolean expanded,boolean leaf,
	      int row,boolean hasFocus) {
	 
	        super.getTreeCellRendererComponent(tree, value, sel, 
	          expanded, leaf, row, hasFocus);
	 
	        Object nodeObj = ((DefaultMutableTreeNode)value).getUserObject();
	        // check whatever you need to on the node user object
	        if (nodeObj instanceof AgentInfo) {
	        	if (((AgentInfo)nodeObj).name.equals(Constants.ENVIRONMENT_AGENT)) {
	        		setIcon(eaIcon);
	        	} else {
	        		setIcon(maIcon);
	        	}
	        } else if (nodeObj instanceof ComponentInfo) {
	        	String compType = ((ComponentInfo)nodeObj).type;
	        	if (compType.equals(Constants.COMP_KB)) {
	        		setIcon(kbIcon);
	        	} else if (compType.equals(Constants.COMP_REASONING)) {
	        		setIcon(reasoningIcon);
	        	} else if (compType.equals(Constants.COMP_SENSOR)) {
	        		setIcon(sensorIcon);
	        	} else if (compType.equals(Constants.COMP_ACTUATOR)) {
	        		setIcon(actuatorIcon);
	        	}
	    	} else if (nodeObj instanceof WorldInfo ||
	        			nodeObj instanceof EventServerInfo) {
	            setIcon(compIcon);
	        } else {
	            setIcon(ensembleIcon);
	        }
	        return this;
	    }
	}
	
	/**
	 * The listener interface for receiving myTreeSelection events.
	 * The class that is interested in processing a myTreeSelection
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addMyTreeSelectionListener<code> method. When
	 * the myTreeSelection event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see MyTreeSelectionEvent
	 */
	class MyTreeSelectionListener implements TreeSelectionListener {
		
		/* (non-Javadoc)
		 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
		 */
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			selectedNode = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
			if (selectedNode == null) {
				return;
			}	
			Object nodeInfo = selectedNode.getUserObject();
			if (nodeInfo instanceof AgentInfo) {
				AgentInfo info = (AgentInfo)nodeInfo;
				txtName.setText(info.name);
				txtClass.setText(info.className);
				txtState.setText(info.state);
				while (tblParametersModel.getRowCount() > 0) {
					tblParametersModel.removeRow(0);
				}
				for (String key : info.parameters.keySet()) {
					tblParametersModel.addRow(new String[] {key, info.parameters.get(key)});
				}
				lblName.setVisible(true);
				txtName.setVisible(true);
				lblClass.setVisible(true);
				txtClass.setVisible(true);
				lblState.setVisible(true);
				txtState.setVisible(true);
				lblType.setVisible(false);
				txtType.setVisible(false);
				pnlParameters.setVisible(true);
				btnAddComponent.setVisible(true);
				btnDestroyAgent.setVisible(true);
				btnRemoveComponent.setVisible(false);
				btnCreateAgent.setVisible(false);
				btnSendCommand.setEnabled(true);
				btnFacts.setVisible(false);
			} else if (nodeInfo instanceof ComponentInfo) {
				ComponentInfo info = (ComponentInfo)nodeInfo;
				txtName.setText(info.name);
				txtClass.setText(info.className);
				txtState.setText(info.state);
				txtType.setText(info.type);
				while (tblParametersModel.getRowCount() > 0) {
					tblParametersModel.removeRow(0);
				}
				for (String key : info.parameters.keySet()) {
					tblParametersModel.addRow(new String[] {key, info.parameters.get(key)});
				}
				lblName.setVisible(true);
				txtName.setVisible(true);
				lblClass.setVisible(true);
				txtClass.setVisible(true);
				lblState.setVisible(true);
				txtState.setVisible(true);
				lblType.setVisible(true);
				txtType.setVisible(true);
				pnlParameters.setVisible(true);
				btnAddComponent.setVisible(false);
				btnDestroyAgent.setVisible(false);
				btnRemoveComponent.setVisible(true);
				btnCreateAgent.setVisible(false);
				btnSendCommand.setEnabled(true);
				btnFacts.setVisible(info.type.equals(Constants.COMP_KB));
			} else if (nodeInfo instanceof EventServerInfo) {
				EventServerInfo info = (EventServerInfo)nodeInfo;
				txtName.setText(info.evt_type);
				txtClass.setText(info.className);
				txtState.setText(info.state);
				while (tblParametersModel.getRowCount() > 0) {
					tblParametersModel.removeRow(0);
				}
				for (String key : info.parameters.keySet()) {
					tblParametersModel.addRow(new String[] {key, info.parameters.get(key)});
				}
				lblName.setVisible(true);
				txtName.setVisible(true);
				lblClass.setVisible(true);
				txtClass.setVisible(true);
				lblState.setVisible(true);
				txtState.setVisible(true);
				lblType.setVisible(false);
				txtType.setVisible(false);
				pnlParameters.setVisible(true);
				btnAddComponent.setVisible(false);
				btnDestroyAgent.setVisible(false);
				btnRemoveComponent.setVisible(false);
				btnCreateAgent.setVisible(false);
				btnSendCommand.setEnabled(true);
				btnFacts.setVisible(false);
			} else if (nodeInfo instanceof WorldInfo) {
				WorldInfo info = (WorldInfo)nodeInfo;
				txtName.setText(Constants.WORLD);
				txtClass.setText(info.className);
				while (tblParametersModel.getRowCount() > 0) {
					tblParametersModel.removeRow(0);
				}
				for (String key : info.parameters.keySet()) {
					tblParametersModel.addRow(new String[] {key, info.parameters.get(key)});
				}
				lblName.setVisible(true);
				txtName.setVisible(true);
				lblClass.setVisible(true);
				txtClass.setVisible(true);
				lblState.setVisible(false);
				txtState.setVisible(false);
				lblType.setVisible(false);
				txtType.setVisible(false);
				pnlParameters.setVisible(true);
				btnAddComponent.setVisible(false);
				btnDestroyAgent.setVisible(false);
				btnRemoveComponent.setVisible(false);
				btnCreateAgent.setVisible(false);
				btnSendCommand.setEnabled(true);
				btnFacts.setVisible(false);
			} else {
				lblName.setVisible(false);
				txtName.setVisible(false);
				lblClass.setVisible(false);
				txtClass.setVisible(false);
				lblState.setVisible(false);
				txtState.setVisible(false);
				lblType.setVisible(false);
				txtType.setVisible(false);
				pnlParameters.setVisible(false);
				btnAddComponent.setVisible(false);
				btnDestroyAgent.setVisible(false);
				btnRemoveComponent.setVisible(false);
				btnCreateAgent.setVisible(true);
				btnFacts.setVisible(false);
				btnSendCommand.setEnabled(false);
			}
		}
	}
	
	/**
	 * The Class MyTableCellEditor.
	 */
	class MyTableCellEditor extends AbstractCellEditor 
						implements TableCellEditor {

		/** The component. */
		JTextField component = new JTextField();
		
		/** The column. */
		int row, column;
		
		/* (non-Javadoc)
		 * @see javax.swing.CellEditor#getCellEditorValue()
		 */
		@Override
		public Object getCellEditorValue() {
			String recipient;
			if (selectedNode.getDepth() == 1) {
				recipient = "/" + Constants.FRAMEWORK_NAME + "/" + selectedNode.toString(); 
			} else {
				recipient = "/" + Constants.FRAMEWORK_NAME + "/" + selectedNode.getParent().toString() + "/" + selectedNode.toString(); 
			}
			Command cmd = new Command(getAddress(), 
						recipient, 
						Constants.CMD_PARAMETER);
			cmd.addParameter("NAME", (String)tblParametersModel.getValueAt(row, column-1));
			cmd.addParameter("VALUE", ((JTextField)component).getText());
			sendCommand(cmd);
			return ((JTextField)component).getText();
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
		 */
		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {

			((JTextField)component).setText((String)value);
			this.row = row;
			this.column = column;
			
			return component;
		}
		
	}
	

	//--------------------------------------------------------------------------------
	// JADE Message Control 
	//--------------------------------------------------------------------------------

	/**
	 * The Class ReceiveMessages.
	 */
	private final class ReceiveMessages extends CyclicBehaviour {

		/** The mt. */
		MessageTemplate mt;
		
		/**
		 * Instantiates a new receive messages.
		 *
		 * @param a the a
		 */
		public ReceiveMessages(Agent a) {
			super(a);
			mt = MessageTemplate.MatchConversationId("CommandRouter");
		}
		
		/* (non-Javadoc)
		 * @see jade.core.behaviours.Behaviour#action()
		 */
		public void action() {
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				if (msg.getPerformative() != ACLMessage.FAILURE) {
					String sender = msg.getSender().getLocalName();
					Command cmd = Command.parse(msg.getContent());
					if (cmd != null) {
						receiveCommand(cmd);
					}
				}
			}
			else {
				block();
			}
		}
	
	}
	
	/* (non-Javadoc)
	 * @see ensemble.router.RouterClient#getAddress()
	 */
	@Override
	public String getAddress() {
		return "/console/Sniffer";
	}

	/* (non-Javadoc)
	 * @see ensemble.router.RouterClient#processCommand(ensemble.Command)
	 */
	@Override
	public void processCommand(Command cmd) {
	}

	/* (non-Javadoc)
	 * @see ensemble.router.RouterClient#receiveCommand(ensemble.Command)
	 */
	@Override
	public void receiveCommand(Command cmd) {
//		System.out.println("SNIFFER: Recebi mensagem - " + cmd.toString());
		if (cmd.getCommand().equals("CREATE")) {
			String agentName = cmd.getParameter("AGENT");
			// It is a component
			if (cmd.containsParameter("COMPONENT")) {
				// Searches for agent's node
				String compName = cmd.getParameter("COMPONENT");
				for (int i = 0; i < rootNode.getChildCount(); i++) {
					DefaultMutableTreeNode agentNode = (DefaultMutableTreeNode)rootNode.getChildAt(i); 
					if (agentNode.toString().equals(agentName)) {
						ComponentInfo compInfo = new ComponentInfo();
						compInfo.agent = agentName;
						compInfo.name = compName;
						compInfo.state = "CREATED";
						String className = cmd.getParameter("CLASS");
						compInfo.className = className.substring(6); 
						compInfo.type = cmd.getParameter("TYPE");
						compInfo.evt_type = cmd.getParameter("EVT_TYPE");
						compInfo.parameters = cmd.getUserParameters();
						if (cmd.containsParameter("FACTS")) {
							compInfo.facts = Parameters.parse(cmd.getParameter("FACTS"));
						}
						DefaultMutableTreeNode compNode = new DefaultMutableTreeNode(compInfo);
						treeModel.insertNodeInto(compNode, agentNode, agentNode.getChildCount());
					}
				}
			}
			// It is an Event Server
			else if (cmd.containsParameter("EVENT_SERVER")) {
				// Searches for agent's node
				EventServerInfo esInfo = new EventServerInfo();
				esInfo.evt_type = cmd.getParameter("EVENT_SERVER");
				esInfo.state = "CREATED";
				String className = cmd.getParameter("CLASS");
				esInfo.className = className.substring(6); 
				esInfo.parameters = cmd.getUserParameters();
				DefaultMutableTreeNode esNode = new DefaultMutableTreeNode(esInfo);
				treeModel.insertNodeInto(esNode, envNode, envNode.getChildCount());
			}
			// It is an Event Server
			else if (cmd.containsParameter("WORLD")) {
				WorldInfo worldInfo = new WorldInfo();
				String className = cmd.getParameter("CLASS");
				worldInfo.className = className.substring(6); 
				worldInfo.parameters = cmd.getUserParameters();
				DefaultMutableTreeNode worldNode = new DefaultMutableTreeNode(worldInfo);
				treeModel.insertNodeInto(worldNode, envNode, envNode.getChildCount());
			}
			// It is an Event Server
			else if (cmd.containsParameter("LAW")) {
				
			}
			// It is an agent
			else {
				AgentInfo agentInfo = new AgentInfo();
				agentInfo.name = agentName;
				String className = cmd.getParameter("CLASS");
				agentInfo.className = className.substring(6); 
				agentInfo.state = "CREATED";
				agentInfo.parameters = cmd.getUserParameters();
				DefaultMutableTreeNode agentNode = new DefaultMutableTreeNode(agentInfo);
				treeModel.insertNodeInto(agentNode, rootNode, rootNode.getChildCount());
				tree.expandRow(0);
				if (agentName.equals(Constants.ENVIRONMENT_AGENT)) {
					envNode = agentNode;
				}
			}
		}
		else if (cmd.getCommand().equals("UPDATE")) {
			// Searches for the node
			DefaultMutableTreeNode agentNode = null;
			String agentName = cmd.getParameter("AGENT");
			for (int i = 0; i < rootNode.getChildCount(); i++) {
				agentNode = (DefaultMutableTreeNode)rootNode.getChildAt(i);
				if (agentNode.toString().equals(agentName)) {
					break;
				}
				agentNode = null;
			}
			if (agentNode == null) {
				System.err.println("[Sniffer] ERROR: agent does not exist!");
				return;
			}
			// If it is a component
			if (cmd.containsParameter("COMPONENT")) {
				// Searches for comp's node
				DefaultMutableTreeNode compNode = null;
				String compName = cmd.getParameter("COMPONENT");
				for (int i = 0; i < agentNode.getChildCount(); i++) {
					compNode = (DefaultMutableTreeNode)agentNode.getChildAt(i);
					if (compNode.toString().equals(compName)) {
						break;
					}
				}
				if (compNode == null) {
					System.err.println("[Sniffer] ERROR: component does not exist!");
					return;
				}
				if (cmd.containsParameter("STATE")) {
					((ComponentInfo)compNode.getUserObject()).state = cmd.getParameter("STATE");
				} else {
					((ComponentInfo)compNode.getUserObject()).parameters.put(cmd.getParameter("NAME"), cmd.getParameter("VALUE"));
				}
			}
			// If it is an event server
			else if (cmd.containsParameter("EVENT_SERVER")) {
				// Searches for comp's node
				DefaultMutableTreeNode esNode = null;
				String esName = cmd.getParameter("EVENT_SERVER");
				for (int i = 0; i < agentNode.getChildCount(); i++) {
					esNode = (DefaultMutableTreeNode)agentNode.getChildAt(i);
					if (esNode.toString().equals(esName)) {
						break;
					}
				}
				if (esNode == null) {
					System.err.println("[Sniffer] ERROR: component does not exist!");
					return;
				}
				if (cmd.containsParameter("STATE")) {
					((EventServerInfo)esNode.getUserObject()).state = cmd.getParameter("STATE");
				} else {
					((EventServerInfo)esNode.getUserObject()).parameters.put(cmd.getParameter("NAME"), cmd.getParameter("VALUE"));
				}
			}
			// If it is a component
			else if (cmd.containsParameter("WORLD")) {
				// Searches for comp's node
				DefaultMutableTreeNode worldNode = null;
				String worldName = cmd.getParameter("WORLD");
				for (int i = 0; i < agentNode.getChildCount(); i++) {
					worldNode = (DefaultMutableTreeNode)agentNode.getChildAt(i);
					if (worldNode.toString().equals(Constants.WORLD)) {
						break;
					}
					worldNode = null;
				}
				if (worldNode == null) {
					System.err.println("[Sniffer] ERROR: component does not exist!");
					return;
				}
				((WorldInfo)worldNode.getUserObject()).parameters.put(cmd.getParameter("NAME"), cmd.getParameter("VALUE"));
			}
			// If it is an agent
			else {
				if (cmd.containsParameter("STATE")) {
					((AgentInfo)agentNode.getUserObject()).state = cmd.getParameter("STATE");
				} else {
					((AgentInfo)agentNode.getUserObject()).parameters.put(cmd.getParameter("NAME"), cmd.getParameter("VALUE"));
				}
			}
		}
		else if (cmd.getCommand().equals("DESTROY")) {
			DefaultMutableTreeNode agentNode = null;
			String agentName = cmd.getParameter("AGENT");
			for (int i = 0; i < rootNode.getChildCount(); i++) {
				agentNode = (DefaultMutableTreeNode)rootNode.getChildAt(i);
				if (agentNode.toString().equals(agentName)) {
					break;
				}
			}
			// If it is a component
			if (cmd.containsParameter("COMPONENT")) {
				// Searches for comp's node
				DefaultMutableTreeNode compNode = null;
				String compName = cmd.getParameter("COMPONENT");
				for (int i = 0; i < agentNode.getChildCount(); i++) {
					compNode = (DefaultMutableTreeNode)agentNode.getChildAt(i);
					if (compNode.toString().equals(compName)) {
						treeModel.removeNodeFromParent(compNode);
						tree.setSelectionPath(new TreePath(agentNode.getPath()));
						return;
					}
				}
			}
			else if (cmd.containsParameter("EVENT_SERVER")) {
				// Searches for comp's node
				DefaultMutableTreeNode esNode = null;
				String esName = cmd.getParameter("EVENT_SERVER");
				for (int i = 0; i < agentNode.getChildCount(); i++) {
					esNode = (DefaultMutableTreeNode)agentNode.getChildAt(i);
					if (esNode.toString().equals(esName)) {
						treeModel.removeNodeFromParent(esNode);
						tree.setSelectionPath(new TreePath(agentNode.getPath()));
						return;
					}
				}
			}
			else if (cmd.containsParameter("WORLD")) {
				// Searches for comp's node
				DefaultMutableTreeNode esNode = null;
				String esName = cmd.getParameter("WORLD");
				for (int i = 0; i < agentNode.getChildCount(); i++) {
					esNode = (DefaultMutableTreeNode)agentNode.getChildAt(i);
					if (esNode.toString().equals(esName)) {
						treeModel.removeNodeFromParent(esNode);
						tree.setSelectionPath(new TreePath(agentNode.getPath()));
						return;
					}
				}
			}
			// If it is an agent
			else {
				treeModel.removeNodeFromParent(agentNode);
				tree.setSelectionPath(new TreePath(rootNode.getPath()));
			}
		}
	}
	

	/* (non-Javadoc)
	 * @see ensemble.router.RouterClient#sendCommand(ensemble.Command)
	 */
	@Override
	public void sendCommand(Command cmd) {
		System.out.println("[Sniffer] sendCommand(): " + cmd);
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(new AID("Router", AID.ISLOCALNAME));
		msg.setConversationId("CommandRouter");
		msg.setContent(cmd.toString());
    	send(msg);
	}
}

class AgentInfo {
	String 		name;
	String 		className;
	String 		state;
	Parameters 	parameters;
	
	@Override
	public String toString() {
		return name;
	}
}

class ComponentInfo {
	String 		agent;
	String 		name;
	String 		className;
	String 		type;
	String 		state;
	String 		evt_type;
	Parameters 	parameters;
	Parameters 	facts;

	@Override
	public String toString() {
		return name;
	}
}

class EventServerInfo {
	String 		evt_type;
	String		state;
	String 		className;
	Parameters	parameters;
	
	@Override
	public String toString() {
		return evt_type;
	}
}

class WorldInfo {
	String 	 	className;
	Parameters 	parameters;
	String[] 	laws;

	@Override
	public String toString() {
		return Constants.WORLD;
	}
}
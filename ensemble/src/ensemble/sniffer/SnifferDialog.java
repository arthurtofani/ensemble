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

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import java.awt.Color;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import javax.swing.SwingConstants;

// TODO: Auto-generated Javadoc
/**
 * The Class SnifferDialog.
 */
public class SnifferDialog extends JDialog implements TableModelListener {
	
	/** The mode. */
	private int mode;
	
	/** The result. */
	public boolean result = false;
	
	/** The content panel. */
	private final JPanel contentPanel = new JPanel();
	
	/** The lbl name. */
	private JLabel lblName;
	
	/** The lbl class. */
	private JLabel lblClass;
	
	/** The lbl event type. */
	private JLabel lblEventType;
	
	/** The txt name. */
	public JTextField txtName;
	
	/** The txt class. */
	public JTextField txtClass;
	
	/** The txt evt type. */
	public JTextField txtEvtType;
	
	/** The table. */
	private JTable table;
	
	/** The table model. */
	public DefaultTableModel tableModel;

	/**
	 * Create the dialog.
	 *
	 * @param mode the mode
	 */
	public SnifferDialog(final int mode) {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		
		setBounds(100, 100, 343, 322);
		getContentPane().setLayout(null);
		contentPanel.setBounds(0, 0, 326, 242);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel);
		contentPanel.setLayout(null);
		{
			lblName = new JLabel("Name");
			lblName.setHorizontalAlignment(SwingConstants.RIGHT);
			lblName.setBounds(33, 15, 38, 14);
			contentPanel.add(lblName);
		}
		{
			txtName = new JTextField();
			txtName.setBounds(75, 12, 243, 20);
			contentPanel.add(txtName);
			txtName.setColumns(10);
		}
		{
			lblClass = new JLabel("Class");
			lblClass.setHorizontalAlignment(SwingConstants.RIGHT);
			lblClass.setBounds(35, 46, 36, 14);
			contentPanel.add(lblClass);
		}
		{
			txtClass = new JTextField();
			txtClass.setBounds(75, 43, 243, 20);
			contentPanel.add(txtClass);
			txtClass.setColumns(20);
		}
		{
			lblEventType = new JLabel("Event Type");
			lblEventType.setHorizontalAlignment(SwingConstants.RIGHT);
			lblEventType.setBounds(5, 77, 66, 14);
			contentPanel.add(lblEventType);
		}
		{
			txtEvtType = new JTextField();
			txtEvtType.setBounds(75, 74, 243, 20);
			contentPanel.add(txtEvtType);
			txtEvtType.setColumns(10);
		}
		{
			JPanel panel = new JPanel();
			panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			panel.setBounds(15, 102, 303, 139);
			contentPanel.add(panel);
			panel.setLayout(null);
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(61, 22, 221, 106);
			panel.add(scrollPane);
			
			tableModel = new DefaultTableModel(
					new Object[][] {},
					new String[] {
						"NAME", "VALUE"
					}
				);
			table = new JTable();
			scrollPane.setViewportView(table);
			table.setModel(tableModel);
			table.setBorder(new LineBorder(Color.LIGHT_GRAY));
			table.setRowSelectionAllowed(false);
			
			JButton btnInsert = new JButton("+");
			btnInsert.setBounds(10, 80, 41, 23);
			panel.add(btnInsert);
			
			JButton btnRemove = new JButton("-");
			btnRemove.setBounds(10, 105, 41, 23);
			panel.add(btnRemove);
			btnRemove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int i = table.getSelectedRow();
					if (i >= 0) {
						tableModel.removeRow(i);
					}
				}
			});
			btnInsert.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					tableModel.addRow(new String[] {"",""});
				}
			});
			table.getModel().addTableModelListener(this);

			JPanel buttonPane = new JPanel();
			buttonPane.setBounds(10, 240, 307, 33);
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane);
			{
				JButton okButton = new JButton("Create");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						result = true;
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		
		if (mode == 0) {
			setTitle("Create Agent...");
			lblEventType.setVisible(false);
			txtEvtType.setVisible(false);
		}
		else if (mode == 1) {
			setTitle("Create Component...");
		}
		
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
	 */
	public void tableChanged(TableModelEvent e) {
//		int row = e.getFirstRow();
//		int column = e.getColumn();
//		TableModel model = (TableModel)e.getSource();
//		String columnName = model.getColumnName(column);
//		Object data = model.getValueAt(row, column);
	}
}

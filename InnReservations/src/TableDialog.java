import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTable;
import javax.swing.JScrollPane;


public class TableDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTable table;

	/**
	 * Create the dialog.
	 */
	public TableDialog(Vector<String> columns, Vector<Vector<String>> rows) {
		setTitle("Availability");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane, BorderLayout.CENTER);
			{
				table = new JTable();
				TableColumnAdjuster tca = new TableColumnAdjuster(table);
				table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				scrollPane.setViewportView(table);
				
				table.setModel(new DefaultTableModel(
						rows,
						columns
					));
				
				tca.adjustColumns();
			}
		}
	}

}

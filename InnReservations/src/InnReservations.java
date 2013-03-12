import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.RowSpec;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JScrollBar;
import javax.swing.border.TitledBorder;
import java.awt.FlowLayout;
import javax.swing.JTextField;


public class InnReservations {

	private JFrame frmBedBreakfast;
	private JTable table;
	private JTextField textField;
	private JTextField textField_1;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					InnReservations window = new InnReservations();
					window.frmBedBreakfast.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public InnReservations() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmBedBreakfast = new JFrame();
		frmBedBreakfast.setTitle("Bed & Breakfast Inn Reservation System");
		frmBedBreakfast.setBounds(100, 100, 465, 356);
		frmBedBreakfast.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frmBedBreakfast.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JPanel guestPanel = new JPanel();
		tabbedPane.addTab("Guest", null, guestPanel, null);
		
		JPanel ownerPanel = new JPanel();
		tabbedPane.addTab("Owner", null, ownerPanel, null);
		ownerPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		ownerPanel.add(panel, BorderLayout.WEST);
		panel.setLayout(new VerticalFlowLayout(FlowLayout.LEADING, 5, 5));
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "First", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(panel_2);
		panel_2.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				ColumnSpec.decode("86px"),},
			new RowSpec[] {
				RowSpec.decode("21px"),
				RowSpec.decode("20px"),}));
		
		textField_1 = new JTextField();
		panel_2.add(textField_1, "2, 1, left, top");
		textField_1.setColumns(10);
		
		textField = new JTextField();
		panel_2.add(textField, "2, 2, left, top");
		textField.setColumns(10);
		
		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new TitledBorder(null, "Second", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(panel_3);
		
		JPanel panel_1 = new JPanel();
		ownerPanel.add(panel_1, BorderLayout.CENTER);
		
		JPanel adminPanel = new JPanel();
		adminPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		tabbedPane.addTab("Admin", null, adminPanel, null);
		adminPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel statusPanel = new JPanel();
		adminPanel.add(statusPanel, BorderLayout.WEST);
		statusPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				FormFactory.MIN_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.LINE_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JLabel lblStatus = new JLabel("Database status:");
		statusPanel.add(lblStatus, "2, 2, left, default");
		
		JLabel lblReservations = new JLabel("Reservations:");
		statusPanel.add(lblReservations, "2, 4, left, center");
		
		JLabel lblRooms = new JLabel("Rooms:");
		statusPanel.add(lblRooms, "2, 6, left, center");
		
		JLabel lblDiagnostics = new JLabel("Diagnostics:");
		statusPanel.add(lblDiagnostics, "2, 8, left, center");
		
		JButton btnViewRooms = new JButton("View Rooms");
		statusPanel.add(btnViewRooms, "2, 10, fill, top");
		
		JButton btnViewReservations = new JButton("View Reservations");
		statusPanel.add(btnViewReservations, "2, 12, fill, top");
		
		JButton btnLoadDB = new JButton("Load DB");
		statusPanel.add(btnLoadDB, "2, 14, fill, top");
		
		JButton btnClearDB = new JButton("Clear DB");
		statusPanel.add(btnClearDB, "2, 16, fill, top");
		
		JButton btnRemoveDB = new JButton("Remove DB");
		statusPanel.add(btnRemoveDB, "2, 18, fill, top");
		
		JPanel outputPanel = new JPanel();
		adminPanel.add(outputPanel, BorderLayout.CENTER);
		outputPanel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		outputPanel.add(scrollPane, BorderLayout.CENTER);
		
		table = new JTable();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setModel(new DefaultTableModel(
			new Object[][] {
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
				{null, null, null, null, null, null, null, null},
			},
			new String[] {
				"New column", "New column", "New column", "New column", "New column", "New column", "New column", "New column"
			}
		));
		scrollPane.setViewportView(table);
	}

}

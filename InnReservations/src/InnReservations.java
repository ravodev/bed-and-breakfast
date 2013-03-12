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
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JTextField;


public class InnReservations {

	private JFrame frmBedBreakfast;
	private JTable table;
	private JTextField textField;
	private JTextField textField_1;
	private static Connection conn;

	/**
	 * Launch the application.
	 */

	public static void main(String[] args) {
		BufferedReader in = null;
		String username = null, password = null, url = null;
		try {
			in = new BufferedReader(new FileReader("ServerSettings.txt"));
			url = in.readLine();
			username = in.readLine();
			password = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} 
		
		System.out.println(url);
		System.out.println(username);
		System.out.println(password);
		   
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        }
        catch (Exception ex)
        {
            System.out.println("Driver not found");
        };

        conn = null;
        try { 
           conn = DriverManager.getConnection(url, username, password);
        }
        catch (Exception ex)
        {
            System.out.println("Could not open connection");
        };
       
        System.out.println("Connected");

        if (!tableExists("Rooms"))
        	createRoomsTable();
        
        if (!tableExists("Reservations"))
        	createReservations();
        /*
        try
        {
	        Statement s1 = conn.createStatement();
	        ResultSet result = s1.executeQuery("SELECT table_name " + 
	                                           "FROM user_tables " +
	        		                           "WHERE table_name = 'Rooms' OR " +
	        		                                 "table_name = 'Reservations'");
			if (result.next() && result.next())
				System.out.println("Both exist!");
			else {
				System.out.println("Both do not exist!");
				Statement s2 = conn.createStatement();
				Statement s3 = conn.createStatement();
		        String table1 = "CREATE TABLE Rooms ( " +
		        		"RoomId VARCHAR(3) PRIMARY KEY, " +
		        		"roomName VARCHAR(24), " +
		        		"beds INTEGER, " +
		        		"bedType VARCHAR(6), " +
		        		"maxOccupancy INTEGER, " +
		        		"basePrice INTEGER, " +
		        		"decor VARCHAR(11), " +
		        		"UNIQUE(roomName) " +
		        		")";

		        String table2 = "CREATE TABLE Reservations ( " +
		        		"Code INTEGER PRIMARY KEY, " +
		        		"Room VARCHAR(3) REFERENCES Rooms, " +
		        		"CheckIn DATE, " +
		        		"CheckOut DATE, " +
		        		"Rate FLOAT, " +
		        		"LastName VARCHAR(13), " +
		        		"FirstName VARCHAR(10), " +
		        		"Adults INTEGER, " +
		        		"Kids INTEGER " +
		        		")";
		        s2.executeUpdate(table1);
		        s3.executeUpdate(table2);
			}
        }
        catch (Exception ee)
        {
        	System.out.println(ee);
        	System.exit(1);
        }
        */
        
        try {
            conn.close();
        }
        catch (Exception ex)
        {
            System.out.println("Unable to close connection");
        }; 
        
        System.out.println("Closed connection");
		
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

	private static void createReservations() {
		// TODO Auto-generated method stub
		
	}

	private static void createRoomsTable() {
		// TODO Auto-generated method stub
		
	}

	private static boolean tableExists(String string) {
		String query = "SELECT table_name " + 
				       "FROM user_tables " +
                       "WHERE table_name = '?'";
		try {
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, string);
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				System.out.println(result.getString(1));
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e);
			System.exit(1);
		}
		
		System.out.println("Not found");
		return false;
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

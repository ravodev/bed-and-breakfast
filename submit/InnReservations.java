/* Authors: Luke Larson,
 *          Andrew Sinclair,
 *          Alexander Spotnitz 
 *
 * Name: Bed & Breakfast Inn Reservation System
 * Description: A database enabled Java application designed to 
 *              manage a small bed and breakfast inn.
 * Date: 3/18/2013
 * Assignment: Lab 9
 * Professor: Dr. Alexander Dekhtyar
 * Class: cpe365
 *
 * Instructions:
 *     Run 'make test'
 */

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
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;

public class InnReservations {
	public enum OwnerMode {
	    OCCUPANCY, RESERVATIONS, ROOMS
	}
	
	public OwnerMode ownerMode;
	
	public JFrame frmBedBreakfast;
	public static Connection conn = null;
	public InnReservations window;
	
	// Admin panel variables
	static ArrayList<String> adminI = new ArrayList<String>();
	private JTable adminTable;
	private TableColumnAdjuster adminTca;
	private JLabel lblAdminStatus = null;
	public JLabel lblAdminReservations = null;
	private JLabel lblAdminRooms = null;
	private JLabel lblAdminDiagnostics = null;
	private JButton btnAdminLoadDB = null;
	static int adminReservationCount;
	private static int adminRoomCount;
	private static Vector<String> adminColumns;
	private static Vector<Vector<String>> adminData;
	
	
	public JTable tblGuestDetailedRoomInfo;
	public JTable tblGuestOutputPanel;
	private TableColumnAdjuster guestOutputPanelTca;
	private TableColumnAdjuster tblGuestDetailedRoomInfoTca;
	public JTextField textFieldGuestOutputPanelHeaderCheckin;
	public JTextField textFieldGuestOutputPanelHeaderCheckOut;
	
	public JTextField textFieldOwnerStart;
	public JTextField textFieldOwnerEnd;
	public JTextField textFieldOwnerRoom;
	private JTable tableOwnerDetailed;
	private JTable tableOwnerReservations;
	private JTable tableOwnerRight;
	
	private TableColumnAdjuster tableOwnerDetailedTca;
	private TableColumnAdjuster tableOwnerReservationsTca;
	private TableColumnAdjuster tableOwnerRightTca;

	
	private class OwnerReservationRowListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent event) {
			if (event.getValueIsAdjusting()) {
				return;
			}
			int i = tableOwnerReservations.getSelectionModel().getLeadSelectionIndex();
			if (i < 0)
				return;
			
			if (!tableExists("Reservations"))
				return;
			
			String query = "SELECT * " +
		                   "FROM Reservations " +
					       "WHERE Code = " + tableOwnerReservations.getValueAt(i, 0);
			
			Vector<String> tableColumns = new Vector<String>();
			Vector<Vector<String>> tableData = new Vector<Vector<String>>();
			
			try {
				Statement s = conn.createStatement();
				ResultSet rs = s.executeQuery(query);
				ResultSetMetaData md = rs.getMetaData();
		        int columns = md.getColumnCount();
		        
	            tableColumns.addElement("Attribute");
	            tableColumns.addElement("Value");
		        
				if (!rs.next()) return;
				
				for (int i1 = 1; i1 <= columns; i1++) {
					Vector<String> row = new Vector<String>(2);
					
					row.addElement(md.getColumnName(i1));
	                row.addElement(rs.getString(i1));

	                tableData.addElement(row);
				}
				
			} catch (SQLException e) {
				return;
			}
			
			tableOwnerDetailed.setModel(new DefaultTableModel(
					tableData,
					tableColumns
				));
			
			tableOwnerDetailedTca.adjustColumns();
		}
	}
	
	private class OwnerDetailedRowListenerSingle implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent event) {
			if (event.getValueIsAdjusting()) {
				return;
			}
			int i = tableOwnerRight.getSelectionModel().getLeadSelectionIndex();
			if (i < 0)
				return;
			String start = textFieldOwnerStart.getText();
			String end = textFieldOwnerEnd.getText();

			if (!tableExists("Reservations"))
				return;

			if (ownerMode == OwnerMode.OCCUPANCY) {
				if (end.length() == 0) {
					String query = "SELECT * " +
							"FROM Reservations " +
							"WHERE Room = '" + tableOwnerRight.getValueAt(i, 1) + "' AND " +
							"TO_DATE('" + start + "', 'DD-MON-YY') BETWEEN CheckIn AND CheckOut-1";

					Vector<String> tableColumns = new Vector<String>();
					Vector<Vector<String>> tableData = new Vector<Vector<String>>();

					try {
						Statement s = conn.createStatement();
						ResultSet rs = s.executeQuery(query);
						ResultSetMetaData md = rs.getMetaData();
						int columns = md.getColumnCount();

						tableColumns.addElement("Attribute");
						tableColumns.addElement("Value");

						if (!rs.next()) return;

						for (int i1 = 1; i1 <= columns; i1++) {
							Vector<String> row = new Vector<String>(2);

							row.addElement(md.getColumnName(i1));
							row.addElement(rs.getString(i1));

							tableData.addElement(row);
						}

					} catch (SQLException e) {
						return;
					}

					tableOwnerDetailed.setModel(new DefaultTableModel(
							tableData,
							tableColumns
							));

					tableOwnerDetailedTca.adjustColumns();
				}
				else {
					String query = "select Code, LastName " +
							"from reservations " + 
							"where room = ? and ((checkin <= to_date(?, 'DD-MON-YY') and " +
							"checkout > to_date(?, 'DD-MON-YY')) or " +
							"(checkin >= to_date(?, 'DD-MON-YY') and " +
							"checkin < to_date(?, 'DD-MON-YY')) or " +
							"(checkout < to_date(?, 'DD-MON-YY') and " +
							"checkout > to_date(?, 'DD-MON-YY')))";

					//String query = "SELECT Code, LastName " +
					//		"FROM Reservations " +
					//		"WHERE Room = '" + tableOwnerRight.getValueAt(i, 0) + "' AND " +
					//		"TO_DATE('" + start + "', 'DD-MON-YY') BETWEEN CheckIn AND CheckOut-1";

					Vector<String> tableColumns = new Vector<String>();
					Vector<Vector<String>> tableData = new Vector<Vector<String>>();

					try {
						PreparedStatement erq = conn.prepareStatement(query);
						erq.setString(2, start);
						erq.setString(4, start);
						erq.setString(7, start);
						erq.setString(3, end);
						erq.setString(5, end);
						erq.setString(6, end);
						erq.setString(1, tableOwnerRight.getValueAt(i, 0).toString());
						ResultSet rs = erq.executeQuery();

						ResultSetMetaData md = rs.getMetaData();
						int columns = md.getColumnCount();

						for (int i1 = 1; i1 <= columns; i1++)
							tableColumns.addElement(md.getColumnName(i1));

						while (rs.next()) {
							Vector<String> row = new Vector<String>(columns);

							for (int i1 = 1; i1 <= columns; i1++)
								row.addElement(rs.getString(i1));

							tableData.addElement(row);
						}

					} catch (SQLException e) {
						return;
					}

					tableOwnerReservations.setModel(new DefaultTableModel(
							tableData,
							tableColumns
							));

					tableOwnerReservationsTca.adjustColumns();
				}
			}
			else if (ownerMode == OwnerMode.RESERVATIONS) {
				String query = "SELECT * " +
						"FROM Reservations " +
						"WHERE Code = " + tableOwnerRight.getValueAt(i, 0);

				Vector<String> tableColumns = new Vector<String>();
				Vector<Vector<String>> tableData = new Vector<Vector<String>>();

				try {
					Statement s = conn.createStatement();
					ResultSet rs = s.executeQuery(query);
					ResultSetMetaData md = rs.getMetaData();
					int columns = md.getColumnCount();

					tableColumns.addElement("Attribute");
					tableColumns.addElement("Value");

					if (!rs.next()) return;

					for (int i1 = 1; i1 <= columns; i1++) {
						Vector<String> row = new Vector<String>(2);

						row.addElement(md.getColumnName(i1));
						row.addElement(rs.getString(i1));

						tableData.addElement(row);
					}

				} catch (SQLException e) {
					return;
				}

				tableOwnerDetailed.setModel(new DefaultTableModel(
						tableData,
						tableColumns
						));
				tableOwnerDetailedTca.adjustColumns();
			}
			else if (ownerMode == OwnerMode.ROOMS) {
				if (!tableExists("Rooms"))
					return;
				
				String query = "SELECT * " +
			                   "FROM Rooms " +
						       "WHERE RoomId = '" + tableOwnerRight.getValueAt(i, 0) + "'";
				
				Vector<String> tableColumns = new Vector<String>();
				Vector<Vector<String>> tableData = new Vector<Vector<String>>();
				
				try {
					Statement s = conn.createStatement();
					ResultSet rs = s.executeQuery(query);
					ResultSetMetaData md = rs.getMetaData();
			        int columns = md.getColumnCount();
			        
		            tableColumns.addElement("Attribute");
		            tableColumns.addElement("Value");
			        
					rs.next();
					
					for (int i1 = 1; i1 <= columns; i1++) {
						Vector<String> row = new Vector<String>(2);
						
						row.addElement(md.getColumnName(i1));
		                row.addElement(rs.getString(i1));

		                tableData.addElement(row);
					}
					
					Vector<String> row = new Vector<String>(2);
					row.addElement("NightsOccupied");
					row.addElement(getNightsOccupied((String) tableOwnerRight.getValueAt(i, 0)));
					tableData.addElement(row);
					
					row = new Vector<String>(2);
					row.addElement("PercentOccupied");
					row.addElement(getPercentOccupied((String) tableOwnerRight.getValueAt(i, 0)));
					tableData.addElement(row);
					
					row = new Vector<String>(2);
					row.addElement("TotalRevenue");
					row.addElement(getTotalRevenue((String) tableOwnerRight.getValueAt(i, 0)));
					tableData.addElement(row);
					
					row = new Vector<String>(2);
					row.addElement("PercentRevenue");
					row.addElement(getPercentRevenue((String) tableOwnerRight.getValueAt(i, 0)));
					tableData.addElement(row);
				} catch (SQLException e) {
					return;
				}
				
				tableOwnerDetailed.setModel(new DefaultTableModel(
						tableData,
						tableColumns
					));
				
				tableOwnerDetailedTca.adjustColumns();
				
				if (!tableExists("Reservations"))
					return;
				
				query = "select Code, LastName " +
						"from reservations " + 
						"where room = ? " +
						"ORDER BY CheckIn";

			    tableColumns = new Vector<String>();
				tableData = new Vector<Vector<String>>();

				try {
					PreparedStatement erq = conn.prepareStatement(query);
					erq.setString(1, tableOwnerRight.getValueAt(i, 0).toString());
					ResultSet rs = erq.executeQuery();

					ResultSetMetaData md = rs.getMetaData();
					int columns = md.getColumnCount();

					for (int i1 = 1; i1 <= columns; i1++)
						tableColumns.addElement(md.getColumnName(i1));

					while (rs.next()) {
						Vector<String> row = new Vector<String>(columns);

						for (int i1 = 1; i1 <= columns; i1++)
							row.addElement(rs.getString(i1));

						tableData.addElement(row);
					}

				} catch (SQLException e) {
					return;
				}

				tableOwnerReservations.setModel(new DefaultTableModel(
						tableData,
						tableColumns
						));

				tableOwnerReservationsTca.adjustColumns();
			}
		}
	}
	
	private class RowListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent event) {
			if (event.getValueIsAdjusting()) {
				return;
			}
			int i = tblGuestOutputPanel.getSelectionModel().getLeadSelectionIndex();
			if (i < 0)
				return;
			
			if (!tableExists("Rooms"))
				return;
			
			String query = "SELECT * " +
		                   "FROM Rooms " +
					       "WHERE RoomId = '" + tblGuestOutputPanel.getValueAt(i, 0) + "'";
			
			Vector<String> tableColumns = new Vector<String>();
			Vector<Vector<String>> tableData = new Vector<Vector<String>>();
			
			try {
				Statement s = conn.createStatement();
				ResultSet rs = s.executeQuery(query);
				ResultSetMetaData md = rs.getMetaData();
		        int columns = md.getColumnCount();
		        
	            tableColumns.addElement("Attribute");
	            tableColumns.addElement("Value");
		        
				rs.next();
				
				for (int i1 = 1; i1 <= columns; i1++) {
					Vector<String> row = new Vector<String>(2);
					
					row.addElement(md.getColumnName(i1));
	                row.addElement(rs.getString(i1));

	                tableData.addElement(row);
				}
				
			} catch (SQLException e) {
				return;
			}
			
			tblGuestDetailedRoomInfo.setModel(new DefaultTableModel(
					tableData,
					tableColumns
				));
			
			tblGuestDetailedRoomInfoTca.adjustColumns();
		}
	}
	 
//	private void outputSelection() {
//		output.append(String.format("Lead: %d, %d. ",
//				table.getSelectionModel().getLeadSelectionIndex(),
//				table.getColumnModel().getSelectionModel().
//				getLeadSelectionIndex()));
//		output.append("Rows:");
//		for (int c : table.getSelectedRows()) {
//			output.append(String.format(" %d", c));
//		}
//		output.append(". Columns:");
//		for (int c : table.getSelectedColumns()) {
//			output.append(String.format(" %d", c));
//		}
//		output.append(".\n");
//	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		BufferedReader in = null;
		String username = null, password = null, url = null;
		
		adminI.add("INSERT INTO Rooms VALUES('RND', 'Recluse and defiance', 1, 'King', 2, 150, 'modern')");
		adminI.add("INSERT INTO Rooms VALUES('IBS', 'Interim but salutary', 1, 'King', 2, 150, 'traditional')");
		adminI.add("INSERT INTO Rooms VALUES('AOB', 'Abscond or bolster', 2, 'Queen', 4, 175, 'traditional')");
		adminI.add("INSERT INTO Rooms VALUES('MWC', 'Mendicant with cryptic', 2, 'Double', 4, 125, 'modern')");
		adminI.add("INSERT INTO Rooms VALUES('HBB', 'Harbinger but bequest', 1, 'Queen', 2, 100, 'modern')");
		adminI.add("INSERT INTO Rooms VALUES('IBD', 'Immutable before decorum', 2, 'Queen', 4, 150, 'rustic')");
		adminI.add("INSERT INTO Rooms VALUES('TAA', 'Thrift and accolade', 1, 'Double', 2, 75, 'modern')");
		adminI.add("INSERT INTO Rooms VALUES('CAS', 'Convoke and sanguine', 2, 'King', 4, 175, 'traditional')");
		adminI.add("INSERT INTO Rooms VALUES('RTE', 'Riddle to exculpate', 2, 'Queen', 4, 175, 'rustic')");
		adminI.add("INSERT INTO Rooms VALUES('FNA', 'Frugal not apropos', 2, 'King', 4, 250, 'traditional')");
		adminI.add("INSERT INTO Reservations VALUES(47496, 'RND', '01-JAN-10', '06-JAN-10', 150.00, 'KLEVER', 'ERASMO', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(41112, 'RND', '06-JAN-10', '11-JAN-10', 135.00, 'HOOLEY', 'EUGENIO', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(76809, 'RND', '12-JAN-10', '14-JAN-10', 187.50, 'WISWELL', 'JERROD', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(70172, 'RND', '23-JAN-10', '25-JAN-10', 150.00, 'ALMANZA', 'PHEBE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(44358, 'RND', '25-JAN-10', '27-JAN-10', 150.00, 'BOBROW', 'CLINTON', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(55344, 'RND', '30-JAN-10', '31-JAN-10', 135.00, 'RENSCH', 'LIANA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(99471, 'RND', '31-JAN-10', '01-FEB-10', 135.00, 'ABRAHAMS', 'ANNETT', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(81473, 'RND', '01-FEB-10', '02-FEB-10', 127.50, 'EVERITT', 'YUK', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(49253, 'RND', '03-FEB-10', '06-FEB-10', 150.00, 'NANI', 'GARRY', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(16748, 'RND', '21-FEB-10', '23-FEB-10', 135.00, 'KLIMKO', 'DONTE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(69316, 'RND', '26-FEB-10', '07-MAR-10', 150.00, 'SULOUFF', 'JESSICA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(69844, 'RND', '07-MAR-10', '11-MAR-10', 172.50, 'BONIOL', 'CLINT', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(96839, 'RND', '11-MAR-10', '12-MAR-10', 150.00, 'ARANAS', 'ROD', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(43911, 'RND', '12-MAR-10', '13-MAR-10', 127.50, 'NEIN', 'TEODORO', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(48382, 'RND', '13-MAR-10', '14-MAR-10', 150.00, 'SCHLADWEILER', 'ELEASE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(77032, 'RND', '14-MAR-10', '17-MAR-10', 172.50, 'FRANC', 'HERBERT', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(30043, 'RND', '17-MAR-10', '18-MAR-10', 150.00, 'HELFRITZ', 'RHEA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(48539, 'RND', '21-MAR-10', '28-MAR-10', 135.00, 'CHINAULT', 'EDWARDO', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(23850, 'RND', '11-APR-10', '17-APR-10', 150.00, 'PENNELLA', 'LAKIA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(97303, 'RND', '21-APR-10', '30-APR-10', 150.00, 'WAEGNER', 'STANFORD', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(21553, 'RND', '01-MAY-10', '05-MAY-10', 172.50, 'MECHLING', 'KERRI', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(28465, 'RND', '05-MAY-10', '06-MAY-10', 150.00, 'SALLE', 'SANTANA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(62147, 'RND', '06-MAY-10', '13-MAY-10', 135.00, 'BURCHAM', 'JONATHON', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(38368, 'RND', '30-MAY-10', '06-JUN-10', 150.00, 'FOCKE', 'HONEY', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(48822, 'RND', '08-JUN-10', '12-JUN-10', 187.50, 'HULETTE', 'DARIUS', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(18822, 'RND', '12-JUN-10', '14-JUN-10', 150.00, 'NORN', 'GARLAND', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(34034, 'RND', '14-JUN-10', '20-JUN-10', 172.50, 'GABBETT', 'ALLEN', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(52470, 'RND', '20-JUN-10', '21-JUN-10', 127.50, 'BAIRAM', 'BRADLY', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(95709, 'RND', '21-JUN-10', '02-JUL-10', 135.00, 'TRUDEN', 'LEWIS', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(93984, 'RND', '02-JUL-10', '03-JUL-10', 187.50, 'DEBARDELABEN', 'NELL', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(76245, 'RND', '18-JUL-10', '19-JUL-10', 150.00, 'TOSTI', 'DAN', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(30300, 'RND', '19-JUL-10', '20-JUL-10', 135.00, 'PRIAL', 'MYLES', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(70440, 'RND', '23-JUL-10', '27-JUL-10', 150.00, 'DEVEY', 'GIUSEPPE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(44933, 'RND', '27-JUL-10', '29-JUL-10', 150.00, 'FURIA', 'ELWANDA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(63458, 'RND', '31-JUL-10', '01-AUG-10', 172.50, 'LELEUX', 'PORTER', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(78964, 'RND', '05-AUG-10', '06-AUG-10', 150.00, 'SPERAZZA', 'WILBUR', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(64503, 'RND', '06-AUG-10', '11-AUG-10', 172.50, 'MAURER', 'TEODORO', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(15534, 'RND', '28-AUG-10', '01-SEP-10', 150.00, 'GAUD', 'REINALDO', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(87560, 'RND', '01-SEP-10', '02-SEP-10', 150.00, 'SABALA', 'MORTON', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(59083, 'RND', '03-SEP-10', '10-SEP-10', 172.50, 'HARTFORD', 'NATHANAEL', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(91895, 'RND', '10-SEP-10', '11-SEP-10', 150.00, 'BLADE', 'RUBEN', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(65416, 'RND', '11-SEP-10', '13-SEP-10', 127.50, 'STRICK', 'NICHOLLE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(94375, 'RND', '13-SEP-10', '19-SEP-10', 150.00, 'WEGER', 'TOBY', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(69494, 'RND', '21-SEP-10', '22-SEP-10', 135.00, 'MINDEN', 'STACEY', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(14845, 'RND', '27-SEP-10', '30-SEP-10', 150.00, 'ROTCH', 'FLORIDA', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(10449, 'RND', '30-SEP-10', '01-OCT-10', 150.00, 'KLESS', 'NELSON', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(28494, 'RND', '01-OCT-10', '13-OCT-10', 135.00, 'DERKAS', 'GUS', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(67343, 'RND', '14-OCT-10', '25-OCT-10', 172.50, 'ALBROUGH', 'OLYMPIA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(63293, 'RND', '25-OCT-10', '01-NOV-10', 127.50, 'KUTA', 'HERMAN', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(55551, 'RND', '04-NOV-10', '08-NOV-10', 127.50, 'COOKUS', 'KASHA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(57705, 'RND', '12-NOV-10', '15-NOV-10', 172.50, 'ROTHMAN', 'GLENNIS', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(48532, 'RND', '20-NOV-10', '22-NOV-10', 135.00, 'VANDEBRINK', 'TRESSIE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(32049, 'RND', '22-NOV-10', '04-DEC-10', 135.00, 'PANOS', 'LESTER', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(54369, 'RND', '04-DEC-10', '06-DEC-10', 172.50, 'MULE', 'DIONNA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(24667, 'RND', '08-DEC-10', '09-DEC-10', 187.50, 'BAUGUESS', 'ERYN', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(68385, 'RND', '10-DEC-10', '12-DEC-10', 172.50, 'ALBERO', 'ABBEY', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(46577, 'RND', '12-DEC-10', '14-DEC-10', 135.00, 'MAURER', 'TEODORO', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(41783, 'RND', '19-DEC-10', '20-DEC-10', 187.50, 'LEDOUX', 'LENA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(30020, 'RND', '21-DEC-10', '28-DEC-10', 135.00, 'PORTO', 'MARIANO', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(26701, 'RND', '28-DEC-10', '30-DEC-10', 150.00, 'DEJAEGER', 'WELDON', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(69598, 'RND', '30-DEC-10', '31-DEC-10', 150.00, 'RENIER', 'MARCELLUS', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(97512, 'RND', '31-DEC-10', '02-JAN-11', 150.00, 'FRAILEY', 'JUANITA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(74548, 'IBS', '13-JAN-10', '16-JAN-10', 172.50, 'BORROMEO', 'EBONY', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(89123, 'IBS', '20-JAN-10', '30-JAN-10', 172.50, 'GISSLER', 'EFRAIN', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(77967, 'IBS', '30-JAN-10', '06-FEB-10', 172.50, 'MCNEELEY', 'ARTHUR', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(62333, 'IBS', '06-FEB-10', '07-FEB-10', 150.00, 'ABATIELL', 'CATHRYN', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(72456, 'IBS', '08-FEB-10', '10-FEB-10', 150.00, 'STARTIN', 'BRUNA', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(15733, 'IBS', '11-FEB-10', '13-FEB-10', 172.50, 'BEALLE', 'RASHAD', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(39602, 'IBS', '13-FEB-10', '23-FEB-10', 150.00, 'STUART', 'IVA', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(75477, 'IBS', '23-FEB-10', '24-FEB-10', 172.50, 'JAHR', 'JESSIE', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(41754, 'IBS', '24-FEB-10', '25-FEB-10', 172.50, 'ANA', 'ELLAN', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(12138, 'IBS', '28-FEB-10', '05-MAR-10', 150.00, 'SHARIAT', 'JARRED', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(95260, 'IBS', '07-MAR-10', '19-MAR-10', 187.50, 'PERRINO', 'DENNY', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(63558, 'IBS', '20-MAR-10', '22-MAR-10', 150.00, 'KEPKE', 'HAROLD', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(53535, 'IBS', '22-MAR-10', '25-MAR-10', 172.50, 'ALLENDE', 'VIRGIL', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(63746, 'IBS', '25-MAR-10', '27-MAR-10', 187.50, 'SCARLES', 'LANDON', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(58881, 'IBS', '27-MAR-10', '29-MAR-10', 150.00, 'RONFELDT', 'JERMAINE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(16933, 'IBS', '01-APR-10', '03-APR-10', 150.00, 'NORSWORTHY', 'AUBREY', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(18900, 'IBS', '03-APR-10', '05-APR-10', 172.50, 'LAURY', 'EMILY', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(58762, 'IBS', '05-APR-10', '07-APR-10', 127.50, 'REDEPENNING', 'FAITH', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(50223, 'IBS', '12-APR-10', '14-APR-10', 135.00, 'CALLICUTT', 'HONG', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(84715, 'IBS', '18-APR-10', '19-APR-10', 150.00, 'BECKUM', 'MISSY', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(55641, 'IBS', '23-APR-10', '25-APR-10', 150.00, 'ISHIBASHI', 'CAPRICE', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(99268, 'IBS', '30-APR-10', '03-MAY-10', 135.00, 'LEEHY', 'NENA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(14001, 'IBS', '11-MAY-10', '12-MAY-10', 187.50, 'COSTON', 'LANNY', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(63652, 'IBS', '14-MAY-10', '20-MAY-10', 172.50, 'COVERT', 'ADAM', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(28227, 'IBS', '30-MAY-10', '04-JUN-10', 172.50, 'STUART', 'IVA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(81780, 'IBS', '09-JUN-10', '10-JUN-10', 150.00, 'ENTWISLE', 'THOMAS', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(44426, 'IBS', '22-JUN-10', '23-JUN-10', 150.00, 'CHEESE', 'TRINIDAD', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(62816, 'IBS', '07-JUL-10', '11-JUL-10', 127.50, 'MAEWEATHER', 'AUGUST', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(94074, 'IBS', '15-JUL-10', '16-JUL-10', 135.00, 'TRIBBY', 'ADELIA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(22981, 'IBS', '21-JUL-10', '23-JUL-10', 172.50, 'KNERIEN', 'GRANT', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(72503, 'IBS', '23-JUL-10', '30-JUL-10', 150.00, 'VELZEBOER', 'HAN', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(44428, 'IBS', '04-AUG-10', '06-AUG-10', 172.50, 'ZAVADOSKI', 'CLAIR', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(26135, 'IBS', '09-AUG-10', '14-AUG-10', 150.00, 'STORDAHL', 'NATOSHA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(88795, 'IBS', '18-AUG-10', '26-AUG-10', 150.00, 'EURICH', 'ANTONE', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(78565, 'IBS', '28-AUG-10', '31-AUG-10', 187.50, 'WAGERS', 'HOUSTON', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(97220, 'IBS', '31-AUG-10', '04-SEP-10', 187.50, 'WIXOM', 'MARCIA', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(37585, 'IBS', '06-SEP-10', '08-SEP-10', 135.00, 'NOAH', 'DOROTHEA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(67689, 'IBS', '08-SEP-10', '11-SEP-10', 172.50, 'DELGUIDICE', 'DAN', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(53723, 'IBS', '11-SEP-10', '12-SEP-10', 135.00, 'KVETON', 'FREDRICK', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(11996, 'IBS', '14-SEP-10', '16-SEP-10', 187.50, 'BURBANK', 'ROBERT', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(55363, 'IBS', '20-SEP-10', '22-SEP-10', 135.00, 'VERDINE', 'ANTONINA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(53747, 'IBS', '22-SEP-10', '23-SEP-10', 135.00, 'SPEARIN', 'TOMMY', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(59610, 'IBS', '23-SEP-10', '30-SEP-10', 150.00, 'EGELSTON', 'EMANUEL', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(77319, 'IBS', '09-OCT-10', '11-OCT-10', 172.50, 'WIDOWSKI', 'EUSEBIO', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(58148, 'IBS', '11-OCT-10', '13-OCT-10', 172.50, 'VOLANTE', 'EMERY', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(62305, 'IBS', '15-OCT-10', '22-OCT-10', 150.00, 'KAMROWSKI', 'EVITA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(95100, 'IBS', '27-OCT-10', '05-NOV-10', 172.50, 'LABAT', 'JEANMARIE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(93407, 'IBS', '05-NOV-10', '07-NOV-10', 187.50, 'KOLP', 'PAMELIA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(35870, 'IBS', '09-NOV-10', '11-NOV-10', 135.00, 'DONIGAN', 'GLEN', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(42731, 'IBS', '11-NOV-10', '15-NOV-10', 172.50, 'HOTARD', 'ALYSIA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(22561, 'IBS', '15-NOV-10', '21-NOV-10', 127.50, 'VUTURO', 'DEVORAH', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(49482, 'IBS', '21-NOV-10', '02-DEC-10', 172.50, 'ATTEBURG', 'ELMIRA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(66331, 'IBS', '02-DEC-10', '06-DEC-10', 135.00, 'SEVILLANO', 'LILLI', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(27490, 'IBS', '06-DEC-10', '08-DEC-10', 150.00, 'PENDLEY', 'SCOTTIE', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(40675, 'IBS', '13-DEC-10', '14-DEC-10', 150.00, 'HANUS', 'THEOLA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(51097, 'IBS', '18-DEC-10', '20-DEC-10', 135.00, 'TOODLE', 'NOLA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(60749, 'IBS', '22-DEC-10', '24-DEC-10', 135.00, 'DONAHER', 'LAKIA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(58196, 'IBS', '24-DEC-10', '30-DEC-10', 150.00, 'ZELINSKI', 'ARTHUR', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(96658, 'IBS', '30-DEC-10', '01-JAN-11', 187.50, 'SCHLESSELMAN', 'NEVILLE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(17265, 'AOB', '01-JAN-10', '06-JAN-10', 175.00, 'HENLY', 'RUPERT', 4, 0)");
		adminI.add("INSERT INTO Reservations VALUES(52597, 'AOB', '11-JAN-10', '14-JAN-10', 175.00, 'CASMORE', 'MARINE', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(67200, 'AOB', '16-JAN-10', '23-JAN-10', 148.75, 'CHET', 'CHARLEEN', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(28406, 'AOB', '23-JAN-10', '29-JAN-10', 175.00, 'COOKUS', 'KASHA', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(10489, 'AOB', '02-FEB-10', '05-FEB-10', 218.75, 'CARISTO', 'MARKITA', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(31993, 'AOB', '08-FEB-10', '10-FEB-10', 201.25, 'ZIEBARTH', 'ADELAIDE', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(30937, 'AOB', '14-FEB-10', '15-FEB-10', 175.00, 'FITZGERREL', 'DENNY', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(15870, 'AOB', '15-FEB-10', '19-FEB-10', 175.00, 'CORIATY', 'BERTA', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(94545, 'AOB', '03-MAR-10', '13-MAR-10', 201.25, 'SHERRANGE', 'AUGUST', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(57527, 'AOB', '26-MAR-10', '28-MAR-10', 201.25, 'ABAJA', 'RHEA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(93341, 'AOB', '02-APR-10', '08-APR-10', 175.00, 'FROHMAN', 'SHAYNE', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(72945, 'AOB', '12-APR-10', '13-APR-10', 201.25, 'KOHS', 'BOB', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(65417, 'AOB', '13-APR-10', '15-APR-10', 175.00, 'ACHTER', 'GRETA', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(50207, 'AOB', '20-APR-10', '21-APR-10', 175.00, 'ROSENDO', 'TAREN', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(12258, 'AOB', '23-APR-10', '27-APR-10', 175.00, 'KANNEL', 'RODGER', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(58755, 'AOB', '30-APR-10', '01-MAY-10', 175.00, 'MALNAR', 'GROVER', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(17955, 'AOB', '05-MAY-10', '09-MAY-10', 201.25, 'PANARELLO', 'TODD', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(75853, 'AOB', '10-MAY-10', '12-MAY-10', 175.00, 'MELOT', 'JENNA', 4, 0)");
		adminI.add("INSERT INTO Reservations VALUES(58972, 'AOB', '17-MAY-10', '19-MAY-10', 157.50, 'BERS', 'BRENDA', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(48113, 'AOB', '24-MAY-10', '25-MAY-10', 175.00, 'BABU', 'SARAI', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(17344, 'AOB', '25-MAY-10', '26-MAY-10', 218.75, 'GALOW', 'RICKEY', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(33806, 'AOB', '27-MAY-10', '28-MAY-10', 175.00, 'OXFORD', 'KATRICE', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(28455, 'AOB', '29-MAY-10', '31-MAY-10', 175.00, 'RISHA', 'NORBERTO', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(14940, 'AOB', '01-JUN-10', '12-JUN-10', 175.00, 'BISHOFF', 'ISREAL', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(43225, 'AOB', '12-JUN-10', '13-JUN-10', 157.50, 'CAPRON', 'CASSAUNDRA', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(54831, 'AOB', '15-JUN-10', '21-JUN-10', 201.25, 'BAUGUESS', 'ERYN', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(59225, 'AOB', '26-JUN-10', '30-JUN-10', 201.25, 'DURAN', 'BO', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(18465, 'AOB', '30-JUN-10', '07-JUL-10', 175.00, 'KRIEGH', 'AMADO', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(38013, 'AOB', '08-JUL-10', '15-JUL-10', 175.00, 'RURY', 'SENA', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(47004, 'AOB', '15-JUL-10', '28-JUL-10', 175.00, 'STEBNER', 'MAXIE', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(56286, 'AOB', '29-JUL-10', '31-JUL-10', 201.25, 'BRICKEL', 'ROCKY', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(29253, 'AOB', '31-JUL-10', '03-AUG-10', 175.00, 'HILDRED', 'MARTY', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(76149, 'AOB', '07-AUG-10', '09-AUG-10', 201.25, 'HONEYWELL', 'JULIANA', 2, 2)");
		adminI.add("INSERT INTO Reservations VALUES(24300, 'AOB', '09-AUG-10', '15-AUG-10', 201.25, 'CANDON', 'PIERRE', 4, 0)");
		adminI.add("INSERT INTO Reservations VALUES(13058, 'AOB', '17-AUG-10', '18-AUG-10', 157.50, 'ABATIELL', 'CATHRYN', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(95605, 'AOB', '18-AUG-10', '20-AUG-10', 218.75, 'FIGLIOLI', 'NANCI', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(17270, 'AOB', '20-AUG-10', '23-AUG-10', 175.00, 'RELLIHAN', 'COURTNEY', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(67427, 'AOB', '02-SEP-10', '14-SEP-10', 175.00, 'STRACK', 'PORTER', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(33748, 'AOB', '14-SEP-10', '16-SEP-10', 175.00, 'PIGNONE', 'JOEL', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(47146, 'AOB', '16-SEP-10', '23-SEP-10', 201.25, 'GATTSHALL', 'REGAN', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(95204, 'AOB', '08-OCT-10', '14-OCT-10', 157.50, 'CARRUTH', 'SANDY', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(41619, 'AOB', '15-OCT-10', '22-OCT-10', 175.00, 'MASSEY', 'DORIAN', 4, 0)");
		adminI.add("INSERT INTO Reservations VALUES(80390, 'AOB', '22-OCT-10', '31-OCT-10', 157.50, 'PHILBERT', 'CHRISTIE', 2, 2)");
		adminI.add("INSERT INTO Reservations VALUES(44836, 'AOB', '31-OCT-10', '03-NOV-10', 148.75, 'DEFRANG', 'DWAIN', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(69035, 'AOB', '18-NOV-10', '26-NOV-10', 218.75, 'KU', 'MERIDETH', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(29287, 'AOB', '01-DEC-10', '08-DEC-10', 218.75, 'FRYDAY', 'HERBERT', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(18834, 'AOB', '08-DEC-10', '10-DEC-10', 148.75, 'DUCHARME', 'MIGUELINA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(58934, 'AOB', '15-DEC-10', '22-DEC-10', 201.25, 'RICHARD', 'ROBBY', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(82502, 'AOB', '25-DEC-10', '26-DEC-10', 175.00, 'KRULIK', 'JEFFRY', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(36890, 'AOB', '26-DEC-10', '28-DEC-10', 175.00, 'PHILBERT', 'CHRISTIE', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(10984, 'AOB', '28-DEC-10', '01-JAN-11', 201.25, 'ZULLO', 'WILLY', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(80192, 'MWC', '01-JAN-10', '03-JAN-10', 112.50, 'MUHLESTEIN', 'REINALDO', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(58266, 'MWC', '08-JAN-10', '14-JAN-10', 125.00, 'PICKARD', 'HORTENCIA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(53876, 'MWC', '14-JAN-10', '15-JAN-10', 125.00, 'KEBEDE', 'ARON', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(82409, 'MWC', '30-JAN-10', '01-FEB-10', 125.00, 'QUARTO', 'VANDA', 4, 0)");
		adminI.add("INSERT INTO Reservations VALUES(39640, 'MWC', '01-FEB-10', '05-FEB-10', 143.75, 'HERZING', 'DELPHIA', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(31371, 'MWC', '05-FEB-10', '06-FEB-10', 125.00, 'TARZIA', 'KAYLEE', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(31508, 'MWC', '10-FEB-10', '14-FEB-10', 112.50, 'FULK', 'GENE', 4, 0)");
		adminI.add("INSERT INTO Reservations VALUES(37985, 'MWC', '15-FEB-10', '18-FEB-10', 112.50, 'PEDERSON', 'DOTTIE', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(54966, 'MWC', '23-FEB-10', '24-FEB-10', 125.00, 'MAZUREK', 'LEIGHANN', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(64987, 'MWC', '28-FEB-10', '05-MAR-10', 125.00, 'BRESSE', 'BURTON', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(40678, 'MWC', '22-MAR-10', '29-MAR-10', 112.50, 'KENNETT', 'QUEEN', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(36356, 'MWC', '29-MAR-10', '04-APR-10', 125.00, 'SPECTOR', 'FRITZ', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(35180, 'MWC', '07-APR-10', '08-APR-10', 125.00, 'POWNELL', 'BRIDGET', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(96909, 'MWC', '09-APR-10', '15-APR-10', 125.00, 'WILLIBRAND', 'HEATHER', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(79056, 'MWC', '19-APR-10', '25-APR-10', 125.00, 'GANZER', 'HYMAN', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(90108, 'MWC', '30-APR-10', '01-MAY-10', 156.25, 'FANZO', 'TERRY', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(30479, 'MWC', '01-MAY-10', '10-MAY-10', 143.75, 'QUISPE', 'MARGARITO', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(60169, 'MWC', '10-MAY-10', '16-MAY-10', 125.00, 'ROSATI', 'LORENA', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(91415, 'MWC', '16-MAY-10', '18-MAY-10', 112.50, 'LEHRFELD', 'CHERLY', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(87943, 'MWC', '21-MAY-10', '28-MAY-10', 125.00, 'LUTFY', 'LIZETTE', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(14273, 'MWC', '28-MAY-10', '30-MAY-10', 125.00, 'STARE', 'ELIJAH', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(25134, 'MWC', '31-MAY-10', '02-JUN-10', 143.75, 'HARDYMAN', 'CLAYTON', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(34244, 'MWC', '04-JUN-10', '06-JUN-10', 112.50, 'SPIKE', 'ROSENDA', 4, 0)");
		adminI.add("INSERT INTO Reservations VALUES(84244, 'MWC', '06-JUN-10', '07-JUN-10', 125.00, 'TALAT', 'MEAGHAN', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(64456, 'MWC', '07-JUN-10', '09-JUN-10', 125.00, 'MCCLENNINGHAM', 'BRADLY', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(86375, 'MWC', '12-JUN-10', '22-JUN-10', 156.25, 'RANAUDO', 'SELINA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(84120, 'MWC', '29-JUN-10', '05-JUL-10', 143.75, 'LOTTO', 'MYRA', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(72817, 'MWC', '08-JUL-10', '09-JUL-10', 143.75, 'FINEFROCK', 'ALEXIS', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(16136, 'MWC', '09-JUL-10', '12-JUL-10', 143.75, 'FERENCE', 'MORGAN', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(44378, 'MWC', '12-JUL-10', '13-JUL-10', 125.00, 'COTMAN', 'JOLYNN', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(54323, 'MWC', '13-JUL-10', '14-JUL-10', 106.25, 'LUNNEY', 'YVETTE', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(18311, 'MWC', '27-JUL-10', '31-JUL-10', 125.00, 'LUEDKE', 'BRYNN', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(65909, 'MWC', '31-JUL-10', '01-AUG-10', 112.50, 'BENDICKSON', 'NEWTON', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(70287, 'MWC', '02-AUG-10', '11-AUG-10', 143.75, 'MAEDA', 'DUSTY', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(92348, 'MWC', '11-AUG-10', '17-AUG-10', 125.00, 'BARTHELL', 'RICARDA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(53295, 'MWC', '17-AUG-10', '22-AUG-10', 106.25, 'HANUS', 'THEOLA', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(97375, 'MWC', '26-AUG-10', '27-AUG-10', 112.50, 'DONNELLEY', 'GARNET', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(79219, 'MWC', '27-AUG-10', '30-AUG-10', 125.00, 'KLAASS', 'HYON', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(69840, 'MWC', '30-AUG-10', '03-SEP-10', 112.50, 'KNERIEN', 'GRANT', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(95620, 'MWC', '07-SEP-10', '10-SEP-10', 112.50, 'ZAHLER', 'GORDON', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(35136, 'MWC', '10-SEP-10', '12-SEP-10', 112.50, 'MALCHOW', 'RAMIRO', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(67464, 'MWC', '27-SEP-10', '30-SEP-10', 112.50, 'KVETON', 'FREDRICK', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(38677, 'MWC', '05-OCT-10', '11-OCT-10', 156.25, 'HORELICK', 'BYRON', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(32116, 'MWC', '15-OCT-10', '17-OCT-10', 143.75, 'RUSSO', 'MELODY', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(21848, 'MWC', '17-OCT-10', '18-OCT-10', 112.50, 'CLOFFI', 'CHAROLETTE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(73473, 'MWC', '18-OCT-10', '27-OCT-10', 125.00, 'ROTCHFORD', 'DWIGHT', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(98441, 'MWC', '30-OCT-10', '07-NOV-10', 106.25, 'RAMPLEY', 'HERMA', 4, 0)");
		adminI.add("INSERT INTO Reservations VALUES(54781, 'MWC', '07-NOV-10', '11-NOV-10', 125.00, 'KUTA', 'HERMAN', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(93587, 'MWC', '11-NOV-10', '12-NOV-10', 112.50, 'ZELAYA', 'PRINCE', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(33271, 'MWC', '29-NOV-10', '02-DEC-10', 143.75, 'YONKERS', 'RHETT', 2, 2)");
		adminI.add("INSERT INTO Reservations VALUES(40043, 'MWC', '04-DEC-10', '14-DEC-10', 125.00, 'LANSBERRY', 'JESUSITA', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(33155, 'MWC', '14-DEC-10', '16-DEC-10', 143.75, 'FOLGER', 'ELOISA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(88588, 'MWC', '16-DEC-10', '17-DEC-10', 125.00, 'RELFORD', 'MARYBELLE', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(57015, 'MWC', '19-DEC-10', '20-DEC-10', 106.25, 'BUSTER', 'TOM', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(87890, 'MWC', '20-DEC-10', '22-DEC-10', 143.75, 'HELKE', 'ISAAC', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(66189, 'MWC', '30-DEC-10', '04-JAN-11', 143.75, 'FROEHNER', 'LATRISHA', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(76801, 'HBB', '06-JAN-10', '18-JAN-10', 125.00, 'CRACE', 'HERB', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(24408, 'HBB', '18-JAN-10', '19-JAN-10', 100.00, 'RADIN', 'RUSTY', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(21980, 'HBB', '05-FEB-10', '08-FEB-10', 115.00, 'SIDDELL', 'MACY', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(87260, 'HBB', '13-FEB-10', '15-FEB-10', 90.00, 'REPKE', 'CHAE', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(78241, 'HBB', '20-FEB-10', '28-FEB-10', 85.00, 'CHAMBLEE', 'ALTHA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(64657, 'HBB', '01-MAR-10', '06-MAR-10', 115.00, 'VIPPERMAN', 'PABLO', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(25558, 'HBB', '17-MAR-10', '19-MAR-10', 125.00, 'SHOULDER', 'FLORENTINO', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(82874, 'HBB', '30-MAR-10', '09-APR-10', 125.00, 'ATTEBURG', 'ELMIRA', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(89780, 'HBB', '09-APR-10', '11-APR-10', 100.00, 'WAHR', 'CLINT', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(63523, 'HBB', '15-APR-10', '16-APR-10', 115.00, 'RATTANA', 'WINFRED', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(30723, 'HBB', '16-APR-10', '23-APR-10', 125.00, 'MASSER', 'TROY', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(88884, 'HBB', '27-APR-10', '30-APR-10', 85.00, 'KIRAKOSYAN', 'KAREEM', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(18019, 'HBB', '03-MAY-10', '05-MAY-10', 115.00, 'ARNDELL', 'JEFFEREY', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(77379, 'HBB', '06-MAY-10', '08-MAY-10', 115.00, 'TORNQUIST', 'JESS', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(45157, 'HBB', '11-MAY-10', '18-MAY-10', 115.00, 'WICKLIN', 'WYNELL', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(77574, 'HBB', '22-MAY-10', '03-JUN-10', 90.00, 'CASMORE', 'MARINE', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(69945, 'HBB', '07-JUN-10', '09-JUN-10', 115.00, 'FLEURILUS', 'KATTIE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(73154, 'HBB', '14-JUN-10', '19-JUN-10', 115.00, 'SAILORS', 'SANDA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(97000, 'HBB', '20-JUN-10', '26-JUN-10', 125.00, 'OCHS', 'ANGLA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(34855, 'HBB', '01-JUL-10', '04-JUL-10', 100.00, 'YURICK', 'SHARA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(86394, 'HBB', '07-JUL-10', '09-JUL-10', 100.00, 'WESTLING', 'DACIA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(49467, 'HBB', '09-JUL-10', '16-JUL-10', 115.00, 'SUSMILCH', 'RAYMUNDO', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(65206, 'HBB', '16-JUL-10', '21-JUL-10', 100.00, 'HIRONS', 'RANDAL', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(12686, 'HBB', '22-JUL-10', '25-JUL-10', 85.00, 'GROWNEY', 'MELVIN', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(92608, 'HBB', '30-JUL-10', '01-AUG-10', 125.00, 'PENCEK', 'SILVIA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(71443, 'HBB', '02-AUG-10', '05-AUG-10', 100.00, 'KLIMAVICIUS', 'ULRIKE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(52799, 'HBB', '05-AUG-10', '07-AUG-10', 85.00, 'WANDREI', 'ISSAC', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(41880, 'HBB', '07-AUG-10', '09-AUG-10', 100.00, 'PLAKE', 'KARMEN', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(25262, 'HBB', '09-AUG-10', '11-AUG-10', 115.00, 'YAPLE', 'CLAUDIE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(10500, 'HBB', '11-AUG-10', '12-AUG-10', 90.00, 'YESSIOS', 'ANNIS', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(97585, 'HBB', '15-AUG-10', '16-AUG-10', 85.00, 'PARISER', 'ELIJAH', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(84334, 'HBB', '19-AUG-10', '25-AUG-10', 125.00, 'NEUBECKER', 'GARY', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(28589, 'HBB', '25-AUG-10', '26-AUG-10', 115.00, 'RIMAR', 'KEELY', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(62874, 'HBB', '26-AUG-10', '27-AUG-10', 100.00, 'SKIBA', 'MITCHELL', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(46665, 'HBB', '31-AUG-10', '09-SEP-10', 100.00, 'MABRA', 'MARGET', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(40241, 'HBB', '13-SEP-10', '15-SEP-10', 85.00, 'CORSARO', 'SHELLA', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(48604, 'HBB', '16-SEP-10', '22-SEP-10', 115.00, 'GARCES', 'CRISTIE', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(48665, 'HBB', '26-SEP-10', '28-SEP-10', 90.00, 'STOUDYMIRE', 'COLUMBUS', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(20572, 'HBB', '28-SEP-10', '29-SEP-10', 115.00, 'SANDLER', 'JENISE', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(97269, 'HBB', '04-OCT-10', '05-OCT-10', 115.00, 'MELVE', 'PHIL', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(25616, 'HBB', '05-OCT-10', '06-OCT-10', 100.00, 'KOSANOVIC', 'EWA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(87713, 'HBB', '08-OCT-10', '10-OCT-10', 90.00, 'TANKER', 'JONE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(44040, 'HBB', '15-OCT-10', '16-OCT-10', 90.00, 'ZELAYA', 'PRINCE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(21202, 'HBB', '21-OCT-10', '23-OCT-10', 125.00, 'CASAGRANDA', 'BRAIN', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(10105, 'HBB', '23-OCT-10', '25-OCT-10', 100.00, 'SELBIG', 'CONRAD', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(19194, 'HBB', '29-OCT-10', '05-NOV-10', 125.00, 'WORKINGER', 'CLIFTON', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(35959, 'HBB', '07-NOV-10', '09-NOV-10', 100.00, 'BRAND', 'TROY', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(74141, 'HBB', '14-NOV-10', '16-NOV-10', 100.00, 'BRODOWSKI', 'RODERICK', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(56737, 'HBB', '17-NOV-10', '18-NOV-10', 100.00, 'PETTINE', 'NUMBERS', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(80302, 'HBB', '22-NOV-10', '24-NOV-10', 115.00, 'KLEIMAN', 'MAURICIO', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(55862, 'HBB', '28-NOV-10', '05-DEC-10', 85.00, 'BIERWAGEN', 'MARK', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(63078, 'HBB', '05-DEC-10', '06-DEC-10', 90.00, 'WHITTEN', 'MITZI', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(63043, 'HBB', '06-DEC-10', '07-DEC-10', 85.00, 'CIERPKE', 'DOT', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(73785, 'HBB', '07-DEC-10', '09-DEC-10', 100.00, 'ROGGE', 'MEREDITH', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(93165, 'HBB', '10-DEC-10', '14-DEC-10', 90.00, 'LEDOUX', 'LENA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(58668, 'HBB', '14-DEC-10', '19-DEC-10', 125.00, 'COASTER', 'MERLE', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(17672, 'HBB', '21-DEC-10', '22-DEC-10', 90.00, 'DECHELLIS', 'AARON', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(84695, 'HBB', '22-DEC-10', '23-DEC-10', 90.00, 'BRAND', 'TROY', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(38636, 'HBB', '29-DEC-10', '31-DEC-10', 115.00, 'FORESTA', 'JORDON', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(43157, 'HBB', '31-DEC-10', '06-JAN-11', 100.00, 'STURN', 'NEVADA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(96005, 'IBD', '01-JAN-10', '07-JAN-10', 150.00, 'BUTALA', 'YEVETTE', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(67066, 'IBD', '07-JAN-10', '11-JAN-10', 135.00, 'EDHOLM', 'ALFRED', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(14226, 'IBD', '11-JAN-10', '12-JAN-10', 150.00, 'SCHOENHUT', 'VERNICE', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(62078, 'IBD', '12-JAN-10', '25-JAN-10', 172.50, 'BRODOWSKI', 'RODERICK', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(43303, 'IBD', '27-JAN-10', '28-JAN-10', 150.00, 'MENINO', 'STEVEN', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(14678, 'IBD', '28-JAN-10', '30-JAN-10', 150.00, 'SATERFIELD', 'FRANCISCO', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(30363, 'IBD', '30-JAN-10', '09-FEB-10', 172.50, 'JAHALY', 'DELORSE', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(80613, 'IBD', '09-FEB-10', '12-FEB-10', 135.00, 'CREDIT', 'JANESSA', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(98216, 'IBD', '18-FEB-10', '25-FEB-10', 150.00, 'RAMSDEN', 'BILLIE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(43654, 'IBD', '27-FEB-10', '28-FEB-10', 172.50, 'BORROMEO', 'EBONY', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(56509, 'IBD', '08-MAR-10', '10-MAR-10', 150.00, 'VONDRA', 'HUMBERTO', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(95519, 'IBD', '21-MAR-10', '23-MAR-10', 172.50, 'ZINDEL', 'CHUNG', 2, 2)");
		adminI.add("INSERT INTO Reservations VALUES(57275, 'IBD', '28-MAR-10', '02-APR-10', 150.00, 'MABRA', 'MARGET', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(66089, 'IBD', '06-APR-10', '07-APR-10', 135.00, 'WINNEN', 'TYLER', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(70741, 'IBD', '09-APR-10', '14-APR-10', 150.00, 'EERKES', 'CODY', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(20107, 'IBD', '14-APR-10', '16-APR-10', 135.00, 'ARBUCKLE', 'LORENA', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(13136, 'IBD', '16-APR-10', '17-APR-10', 187.50, 'FEYLER', 'EMILIA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(33427, 'IBD', '17-APR-10', '21-APR-10', 135.00, 'BEALLE', 'RASHAD', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(80989, 'IBD', '24-APR-10', '06-MAY-10', 150.00, 'TIPPIN', 'ASUNCION', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(97996, 'IBD', '06-MAY-10', '07-MAY-10', 135.00, 'LUCIDO', 'AHMAD', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(55870, 'IBD', '08-MAY-10', '13-MAY-10', 135.00, 'BELONGIE', 'BIBI', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(11645, 'IBD', '13-MAY-10', '19-MAY-10', 135.00, 'SWAIT', 'DAN', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(19394, 'IBD', '23-MAY-10', '25-MAY-10', 150.00, 'HARPE', 'GERMAN', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(99845, 'IBD', '27-MAY-10', '28-MAY-10', 150.00, 'SEVILLANO', 'LILLI', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(32933, 'IBD', '05-JUN-10', '06-JUN-10', 135.00, 'MURASSO', 'JAMIE', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(83021, 'IBD', '06-JUN-10', '07-JUN-10', 172.50, 'KLAVETTER', 'DOUGLASS', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(65153, 'IBD', '12-JUN-10', '14-JUN-10', 150.00, 'VEGHER', 'ANGELA', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(96983, 'IBD', '18-JUN-10', '23-JUN-10', 127.50, 'CLOWERD', 'ARNULFO', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(86560, 'IBD', '23-JUN-10', '24-JUN-10', 187.50, 'SHUTTERS', 'CLARINDA', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(87086, 'IBD', '28-JUN-10', '07-JUL-10', 150.00, 'WAGERS', 'HOUSTON', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(98882, 'IBD', '07-JUL-10', '14-JUL-10', 127.50, 'GOON', 'JONE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(27983, 'IBD', '22-JUL-10', '24-JUL-10', 135.00, 'SWARTWOOD', 'JENI', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(37425, 'IBD', '26-JUL-10', '28-JUL-10', 150.00, 'FIERECK', 'ALBERTA', 4, 0)");
		adminI.add("INSERT INTO Reservations VALUES(17222, 'IBD', '28-JUL-10', '07-AUG-10', 150.00, 'BOHMAN', 'LAYNE', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(20269, 'IBD', '12-AUG-10', '19-AUG-10', 135.00, 'BILLICK', 'NIEVES', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(24326, 'IBD', '19-AUG-10', '21-AUG-10', 135.00, 'CULMER', 'LLOYD', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(63600, 'IBD', '21-AUG-10', '28-AUG-10', 150.00, 'WEARS', 'LIBBIE', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(37845, 'IBD', '29-AUG-10', '30-AUG-10', 127.50, 'PENEZ', 'AMIEE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(24986, 'IBD', '30-AUG-10', '01-SEP-10', 150.00, 'HEATH', 'PASQUALE', 4, 0)");
		adminI.add("INSERT INTO Reservations VALUES(12085, 'IBD', '04-SEP-10', '08-SEP-10', 135.00, 'GLASGLOW', 'EMMANUEL', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(29937, 'IBD', '08-SEP-10', '10-SEP-10', 150.00, 'OBERHAUSEN', 'JODY', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(11703, 'IBD', '10-SEP-10', '11-SEP-10', 172.50, 'HAVIS', 'SHERILYN', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(10183, 'IBD', '19-SEP-10', '20-SEP-10', 150.00, 'GABLER', 'DOLLIE', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(14149, 'IBD', '20-SEP-10', '22-SEP-10', 150.00, 'HOULIHAN', 'LEVI', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(17405, 'IBD', '02-OCT-10', '03-OCT-10', 127.50, 'KESTER', 'KIZZY', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(21625, 'IBD', '03-OCT-10', '14-OCT-10', 187.50, 'BOSE', 'FRANCISCO', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(43651, 'IBD', '19-OCT-10', '21-OCT-10', 150.00, 'LAGERMAN', 'LEOLA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(37060, 'IBD', '21-OCT-10', '23-OCT-10', 150.00, 'PASSANTINO', 'DALLAS', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(66548, 'IBD', '23-OCT-10', '26-OCT-10', 150.00, 'FEASTER', 'KRISTOPHER', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(11857, 'IBD', '27-OCT-10', '29-OCT-10', 187.50, 'HARDINA', 'LORITA', 4, 0)");
		adminI.add("INSERT INTO Reservations VALUES(33395, 'IBD', '31-OCT-10', '02-NOV-10', 150.00, 'METER', 'ROSA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(43584, 'IBD', '02-NOV-10', '04-NOV-10', 135.00, 'BRUSKI', 'MONTY', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(93736, 'IBD', '07-NOV-10', '10-NOV-10', 135.00, 'TREGRE', 'COLTON', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(44310, 'IBD', '15-NOV-10', '21-NOV-10', 135.00, 'DIEZ', 'NADIA', 4, 0)");
		adminI.add("INSERT INTO Reservations VALUES(47511, 'IBD', '21-NOV-10', '22-NOV-10', 172.50, 'VELIE', 'NIKKI', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(88892, 'IBD', '22-NOV-10', '26-NOV-10', 187.50, 'MARKWORTH', 'DESPINA', 4, 0)");
		adminI.add("INSERT INTO Reservations VALUES(26798, 'IBD', '30-NOV-10', '06-DEC-10', 135.00, 'BONJORNO', 'IGNACIO', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(33195, 'IBD', '06-DEC-10', '12-DEC-10', 150.00, 'DEHLINGER', 'VANCE', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(72506, 'IBD', '12-DEC-10', '25-DEC-10', 150.00, 'KNERIEN', 'GRANT', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(70889, 'IBD', '25-DEC-10', '26-DEC-10', 172.50, 'DINUNZIO', 'FELIPE', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(72890, 'IBD', '26-DEC-10', '27-DEC-10', 172.50, 'AKHTAR', 'BENTON', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(71679, 'IBD', '27-DEC-10', '31-DEC-10', 172.50, 'MIKEL', 'BRIGIDA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(43692, 'IBD', '31-DEC-10', '01-JAN-11', 172.50, 'BEAMON', 'HYACINTH', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(83481, 'TAA', '03-JAN-10', '07-JAN-10', 67.50, 'ENGELSON', 'MIKKI', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(38209, 'TAA', '08-JAN-10', '13-JAN-10', 86.25, 'ECKERT', 'EDIE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(89102, 'TAA', '14-JAN-10', '15-JAN-10', 67.50, 'CARBACK', 'ZOLA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(87915, 'TAA', '18-JAN-10', '24-JAN-10', 63.75, 'ACHENBACH', 'HERB', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(57627, 'TAA', '29-JAN-10', '05-FEB-10', 67.50, 'SWEAZY', 'ROY', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(77911, 'TAA', '05-FEB-10', '07-FEB-10', 75.00, 'WIDOWSKI', 'EUSEBIO', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(56475, 'TAA', '07-FEB-10', '08-FEB-10', 93.75, 'KLIGER', 'TRICIA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(89519, 'TAA', '09-FEB-10', '10-FEB-10', 75.00, 'STIMSON', 'NICOLA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(62313, 'TAA', '10-FEB-10', '12-FEB-10', 86.25, 'WAEGNER', 'BROOKS', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(68829, 'TAA', '12-FEB-10', '15-FEB-10', 67.50, 'POIRRIER', 'SUZETTE', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(84756, 'TAA', '19-FEB-10', '22-FEB-10', 75.00, 'CARLYLE', 'BILLYE', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(13972, 'TAA', '22-FEB-10', '25-FEB-10', 67.50, 'DAYA', 'COREY', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(55985, 'TAA', '25-FEB-10', '26-FEB-10', 86.25, 'BASSI', 'DUSTY', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(92508, 'TAA', '26-FEB-10', '27-FEB-10', 75.00, 'MULE', 'DIONNA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(56344, 'TAA', '02-MAR-10', '03-MAR-10', 86.25, 'INGUARDSEN', 'HARRISON', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(24420, 'TAA', '03-MAR-10', '12-MAR-10', 75.00, 'SCHLESSER', 'FLOYD', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(74927, 'TAA', '24-MAR-10', '26-MAR-10', 67.50, 'MACROSTIE', 'SABRINA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(69499, 'TAA', '27-MAR-10', '29-MAR-10', 75.00, 'AKHTAR', 'BENTON', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(44771, 'TAA', '29-MAR-10', '30-MAR-10', 67.50, 'NOBLIN', 'BELKIS', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(31636, 'TAA', '09-APR-10', '11-APR-10', 75.00, 'FAGERSTROM', 'ALLAN', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(36855, 'TAA', '13-APR-10', '14-APR-10', 67.50, 'TRAMM', 'SANG', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(92463, 'TAA', '14-APR-10', '15-APR-10', 75.00, 'DAOUD', 'STEPHEN', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(26423, 'TAA', '15-APR-10', '18-APR-10', 86.25, 'DEININGER', 'RICKIE', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(49886, 'TAA', '21-APR-10', '27-APR-10', 86.25, 'BRINAR', 'WOODROW', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(75081, 'TAA', '27-APR-10', '07-MAY-10', 75.00, 'HANSEN', 'LAREE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(29496, 'TAA', '07-MAY-10', '14-MAY-10', 75.00, 'HONIGSBERG', 'PEARLENE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(97924, 'TAA', '19-MAY-10', '21-MAY-10', 75.00, 'SPECTOR', 'FRITZ', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(61861, 'TAA', '30-MAY-10', '12-JUN-10', 86.25, 'EASTLING', 'AUNDREA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(27125, 'TAA', '13-JUN-10', '15-JUN-10', 75.00, 'BOESER', 'DIVINA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(26487, 'TAA', '15-JUN-10', '19-JUN-10', 63.75, 'JUHL', 'MILLARD', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(97491, 'TAA', '19-JUN-10', '23-JUN-10', 86.25, 'BALCOM', 'JOEY', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(49153, 'TAA', '23-JUN-10', '30-JUN-10', 86.25, 'ZEPEDA', 'ELANA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(68265, 'TAA', '30-JUN-10', '03-JUL-10', 67.50, 'BREGER', 'BOYD', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(94949, 'TAA', '03-JUL-10', '09-JUL-10', 75.00, 'FINEFROCK', 'ALEXIS', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(67917, 'TAA', '09-JUL-10', '13-JUL-10', 75.00, 'SALERNO', 'LOU', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(86151, 'TAA', '13-JUL-10', '15-JUL-10', 67.50, 'RIOPEL', 'TANA', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(32112, 'TAA', '20-JUL-10', '27-JUL-10', 75.00, 'SAMPLES', 'CLAIR', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(94735, 'TAA', '31-JUL-10', '05-AUG-10', 75.00, 'ALMGREN', 'CHANTAY', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(89433, 'TAA', '08-AUG-10', '18-AUG-10', 63.75, 'SHALHOUP', 'AMAL', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(19017, 'TAA', '18-AUG-10', '30-AUG-10', 86.25, 'EERKES', 'CODY', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(38017, 'TAA', '05-SEP-10', '07-SEP-10', 75.00, 'SHOULDER', 'FLORENTINO', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(85547, 'TAA', '11-SEP-10', '16-SEP-10', 93.75, 'EMIGHOLZ', 'ERWIN', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(35546, 'TAA', '19-SEP-10', '24-SEP-10', 67.50, 'YUK', 'TIM', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(32552, 'TAA', '11-OCT-10', '12-OCT-10', 75.00, 'MIDTHUN', 'EMMANUEL', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(16693, 'TAA', '17-OCT-10', '18-OCT-10', 67.50, 'ASHCROFT', 'RONALD', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(20463, 'TAA', '19-OCT-10', '22-OCT-10', 75.00, 'VANBLARICUM', 'CRISTIE', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(69654, 'TAA', '23-OCT-10', '29-OCT-10', 75.00, 'TOLLINCHI', 'CHRISTOPER', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(67381, 'TAA', '02-NOV-10', '04-NOV-10', 75.00, 'GUERETTE', 'CLARETTA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(58942, 'TAA', '04-NOV-10', '08-NOV-10', 75.00, 'HULETTE', 'DARIUS', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(76087, 'TAA', '18-NOV-10', '23-NOV-10', 67.50, 'SELIGA', 'LEOPOLDO', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(81825, 'TAA', '25-NOV-10', '27-NOV-10', 86.25, 'DEVEY', 'GIUSEPPE', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(90621, 'TAA', '28-NOV-10', '30-NOV-10', 86.25, 'RENDLEMAN', 'JULI', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(42000, 'TAA', '04-DEC-10', '11-DEC-10', 93.75, 'STRINGFELLOW', 'GEARLDINE', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(33013, 'TAA', '16-DEC-10', '18-DEC-10', 67.50, 'STYCH', 'DIA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(33339, 'TAA', '22-DEC-10', '27-DEC-10', 67.50, 'WISSINGER', 'JACQUES', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(54274, 'TAA', '27-DEC-10', '28-DEC-10', 75.00, 'PEYATT', 'SHERON', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(84497, 'TAA', '28-DEC-10', '29-DEC-10', 75.00, 'FRERICKS', 'RONNY', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(46908, 'TAA', '29-DEC-10', '30-DEC-10', 86.25, 'PULOS', 'ROBERTO', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(47108, 'TAA', '30-DEC-10', '12-JAN-11', 75.00, 'WILCUTT', 'BLAINE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(87722, 'CAS', '01-JAN-10', '07-JAN-10', 175.00, 'GIERLING', 'TRENT', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(24980, 'CAS', '07-JAN-10', '08-JAN-10', 201.25, 'GRONDAHL', 'ELVINA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(38140, 'CAS', '22-JAN-10', '23-JAN-10', 218.75, 'KUDRON', 'CATHERIN', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(75632, 'CAS', '26-JAN-10', '27-JAN-10', 148.75, 'DONIGAN', 'GLEN', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(97542, 'CAS', '27-JAN-10', '29-JAN-10', 175.00, 'GARZONE', 'EDISON', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(89101, 'CAS', '29-JAN-10', '30-JAN-10', 201.25, 'GIOVANINI', 'ROXANE', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(57298, 'CAS', '30-JAN-10', '03-FEB-10', 148.75, 'KNOP', 'JEFFRY', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(56797, 'CAS', '03-FEB-10', '05-FEB-10', 175.00, 'PFEUFFER', 'VALENTIN', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(32138, 'CAS', '05-FEB-10', '07-FEB-10', 175.00, 'KOEHNE', 'YUONNE', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(74036, 'CAS', '08-FEB-10', '14-FEB-10', 175.00, 'MANTERNACH', 'ALBERTA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(90483, 'CAS', '14-FEB-10', '25-FEB-10', 175.00, 'HUIZINGA', 'GILBERT', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(83347, 'CAS', '26-FEB-10', '28-FEB-10', 201.25, 'MCCULLARS', 'ALEIDA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(99340, 'CAS', '04-MAR-10', '05-MAR-10', 175.00, 'DONIGAN', 'GLEN', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(90569, 'CAS', '06-MAR-10', '07-MAR-10', 201.25, 'LIBBERTON', 'SAM', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(36156, 'CAS', '07-MAR-10', '12-MAR-10', 175.00, 'GUILFOIL', 'KOREY', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(66494, 'CAS', '14-MAR-10', '18-MAR-10', 157.50, 'KOLB', 'ENA', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(11718, 'CAS', '18-MAR-10', '19-MAR-10', 157.50, 'GLIWSKI', 'DAN', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(20991, 'CAS', '19-MAR-10', '01-APR-10', 201.25, 'PLYMEL', 'STEPHEN', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(92372, 'CAS', '01-APR-10', '03-APR-10', 175.00, 'GOODHUE', 'RUSSELL', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(46496, 'CAS', '08-APR-10', '12-APR-10', 175.00, 'ALWINE', 'SHAWANDA', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(31695, 'CAS', '16-APR-10', '18-APR-10', 148.75, 'KEPKE', 'HAROLD', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(14644, 'CAS', '23-APR-10', '24-APR-10', 148.75, 'VOLANTE', 'EMERY', 4, 0)");
		adminI.add("INSERT INTO Reservations VALUES(12631, 'CAS', '24-APR-10', '26-APR-10', 175.00, 'ONEEL', 'PASQUALE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(83133, 'CAS', '26-APR-10', '27-APR-10', 175.00, 'JEE', 'HERMAN', 2, 2)");
		adminI.add("INSERT INTO Reservations VALUES(93732, 'CAS', '30-APR-10', '02-MAY-10', 157.50, 'STARE', 'ELIJAH', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(82884, 'CAS', '07-MAY-10', '08-MAY-10', 157.50, 'LEVAR', 'NYLA', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(13192, 'CAS', '08-MAY-10', '15-MAY-10', 175.00, 'MCCORVEY', 'JESUS', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(46797, 'CAS', '15-MAY-10', '16-MAY-10', 175.00, 'VANBLARICUM', 'CRISTIE', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(93382, 'CAS', '16-MAY-10', '22-MAY-10', 175.00, 'PACHERO', 'MAGAN', 2, 2)");
		adminI.add("INSERT INTO Reservations VALUES(83866, 'CAS', '01-JUN-10', '05-JUN-10', 148.75, 'BARBER', 'CALEB', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(70937, 'CAS', '06-JUN-10', '07-JUN-10', 148.75, 'BERTINI', 'DYAN', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(92150, 'CAS', '13-JUN-10', '16-JUN-10', 157.50, 'BOBSEINE', 'HUMBERTO', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(87631, 'CAS', '16-JUN-10', '23-JUN-10', 175.00, 'EDEMANN', 'KACI', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(70863, 'CAS', '23-JUN-10', '05-JUL-10', 201.25, 'MUHLESTEIN', 'REINALDO', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(29003, 'CAS', '05-JUL-10', '07-JUL-10', 175.00, 'MASSER', 'TROY', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(21050, 'CAS', '11-JUL-10', '17-JUL-10', 175.00, 'CHATTERJEE', 'ALFRED', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(59593, 'CAS', '17-JUL-10', '18-JUL-10', 218.75, 'GRULKEY', 'JULIO', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(92515, 'CAS', '18-JUL-10', '25-JUL-10', 201.25, 'LIESTMAN', 'TOVA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(80282, 'CAS', '27-JUL-10', '29-JUL-10', 157.50, 'PETERSON', 'SHARRI', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(42424, 'CAS', '01-AUG-10', '14-AUG-10', 157.50, 'SCARPINO', 'BERNITA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(19812, 'CAS', '14-AUG-10', '15-AUG-10', 157.50, 'MCCARTER', 'IVAN', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(85102, 'CAS', '15-AUG-10', '16-AUG-10', 201.25, 'GIANOPULOS', 'LYLA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(90653, 'CAS', '16-AUG-10', '17-AUG-10', 175.00, 'SHIYOU', 'SYLVIE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(32565, 'CAS', '21-AUG-10', '26-AUG-10', 157.50, 'KALAFATIS', 'KEITH', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(28447, 'CAS', '26-AUG-10', '27-AUG-10', 175.00, 'GEOHAGAN', 'ISSAC', 2, 2)");
		adminI.add("INSERT INTO Reservations VALUES(75770, 'CAS', '27-AUG-10', '29-AUG-10', 175.00, 'TRIGLETH', 'REYES', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(26380, 'CAS', '30-AUG-10', '31-AUG-10', 201.25, 'KABZINSKI', 'MILLIE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(15059, 'CAS', '31-AUG-10', '07-SEP-10', 157.50, 'LERCH', 'NICKY', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(21863, 'CAS', '07-SEP-10', '08-SEP-10', 175.00, 'ARGOTE', 'ODELIA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(62161, 'CAS', '08-SEP-10', '09-SEP-10', 157.50, 'IKEDA', 'FREEMAN', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(32781, 'CAS', '09-SEP-10', '15-SEP-10', 175.00, 'DURAN', 'BO', 2, 2)");
		adminI.add("INSERT INTO Reservations VALUES(98805, 'CAS', '16-SEP-10', '18-SEP-10', 175.00, 'WITHFIELD', 'IVORY', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(37456, 'CAS', '18-SEP-10', '19-SEP-10', 175.00, 'BOWEN', 'NIDA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(10990, 'CAS', '21-SEP-10', '27-SEP-10', 175.00, 'TRACHSEL', 'DAMIEN', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(72813, 'CAS', '29-SEP-10', '03-OCT-10', 201.25, 'WEINLAND', 'BEV', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(21216, 'CAS', '03-OCT-10', '04-OCT-10', 201.25, 'VANNAMAN', 'ERICH', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(98711, 'CAS', '08-OCT-10', '09-OCT-10', 201.25, 'ROSSEY', 'ALLEN', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(88863, 'CAS', '22-OCT-10', '23-OCT-10', 175.00, 'TOAN', 'YONG', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(60313, 'CAS', '28-OCT-10', '30-OCT-10', 218.75, 'SLONE', 'LARITA', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(80582, 'CAS', '30-OCT-10', '09-NOV-10', 201.25, 'PFEUFFER', 'FREDRICK', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(98328, 'CAS', '14-NOV-10', '15-NOV-10', 201.25, 'MORAWSKI', 'KRIS', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(16234, 'CAS', '15-NOV-10', '16-NOV-10', 157.50, 'GRABILL', 'JULEE', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(32739, 'CAS', '20-NOV-10', '24-NOV-10', 148.75, 'SHAFE', 'TUAN', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(94073, 'CAS', '27-NOV-10', '03-DEC-10', 175.00, 'MANSELL', 'FLORENTINO', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(32015, 'CAS', '03-DEC-10', '05-DEC-10', 201.25, 'EADER', 'JACKIE', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(59986, 'CAS', '05-DEC-10', '09-DEC-10', 175.00, 'WEINAND', 'HARRIETT', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(36345, 'CAS', '10-DEC-10', '12-DEC-10', 157.50, 'TURVAVILLE', 'TYLER', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(99478, 'CAS', '29-DEC-10', '31-DEC-10', 157.50, 'WENRICH', 'ELWOOD', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(93389, 'CAS', '31-DEC-10', '08-JAN-11', 218.75, 'PELLOWSKI', 'ELKE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(32896, 'RTE', '06-JAN-10', '13-JAN-10', 157.50, 'LUTFY', 'LIZETTE', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(24412, 'RTE', '23-JAN-10', '30-JAN-10', 175.00, 'PENEZ', 'AMIEE', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(76432, 'RTE', '30-JAN-10', '03-FEB-10', 148.75, 'DIEUDONNE', 'KRYSTEN', 2, 2)");
		adminI.add("INSERT INTO Reservations VALUES(62024, 'RTE', '06-FEB-10', '12-FEB-10', 157.50, 'LUTTRELL', 'MONTY', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(25374, 'RTE', '15-FEB-10', '17-FEB-10', 157.50, 'JASPERS', 'LIBBIE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(45483, 'RTE', '17-FEB-10', '18-FEB-10', 157.50, 'JENQUIN', 'JAY', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(77094, 'RTE', '23-FEB-10', '27-FEB-10', 218.75, 'DISHAW', 'CODY', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(93672, 'RTE', '28-FEB-10', '04-MAR-10', 148.75, 'SOMO', 'FELICITAS', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(49606, 'RTE', '04-MAR-10', '05-MAR-10', 201.25, 'YOKUM', 'GARRY', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(23740, 'RTE', '07-MAR-10', '12-MAR-10', 218.75, 'NOWLEY', 'ROMEO', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(14447, 'RTE', '14-MAR-10', '18-MAR-10', 201.25, 'MAASCH', 'CHARISSA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(52353, 'RTE', '31-MAR-10', '07-APR-10', 201.25, 'WISWELL', 'JERROD', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(45279, 'RTE', '08-APR-10', '12-APR-10', 201.25, 'HALBERSTAM', 'SHERRILL', 4, 0)");
		adminI.add("INSERT INTO Reservations VALUES(50459, 'RTE', '14-APR-10', '15-APR-10', 218.75, 'LORENZANO', 'SON', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(50458, 'RTE', '18-APR-10', '20-APR-10', 175.00, 'VOLANTE', 'EMERY', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(98557, 'RTE', '20-APR-10', '21-APR-10', 218.75, 'MAIDENS', 'THOMASINE', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(25010, 'RTE', '22-APR-10', '01-MAY-10', 201.25, 'WESTRUM', 'TIMMY', 2, 2)");
		adminI.add("INSERT INTO Reservations VALUES(54061, 'RTE', '05-MAY-10', '12-MAY-10', 175.00, 'BARTHELL', 'RICARDA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(79213, 'RTE', '17-MAY-10', '29-MAY-10', 157.50, 'NEUZIL', 'MIREYA', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(34999, 'RTE', '15-JUN-10', '19-JUN-10', 175.00, 'DANIELLO', 'RUDOLF', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(37309, 'RTE', '19-JUN-10', '21-JUN-10', 175.00, 'PERRINO', 'DENNY', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(48854, 'RTE', '26-JUN-10', '27-JUN-10', 175.00, 'TROKEY', 'INGRID', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(22954, 'RTE', '27-JUN-10', '04-JUL-10', 157.50, 'ALLAIRE', 'RAMONA', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(14896, 'RTE', '04-JUL-10', '06-JUL-10', 148.75, 'KUDRON', 'CATHERIN', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(30186, 'RTE', '06-JUL-10', '07-JUL-10', 175.00, 'MAKI', 'ADAN', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(53186, 'RTE', '09-JUL-10', '14-JUL-10', 157.50, 'MANARD', 'ARDELIA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(43707, 'RTE', '14-JUL-10', '16-JUL-10', 175.00, 'GUT', 'ALEJANDRA', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(71343, 'RTE', '19-JUL-10', '21-JUL-10', 201.25, 'TOWBER', 'MODESTO', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(81785, 'RTE', '23-JUL-10', '24-JUL-10', 157.50, 'FALT', 'SHAWN', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(49802, 'RTE', '30-JUL-10', '04-AUG-10', 157.50, 'CONRAD', 'ELODIA', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(77535, 'RTE', '08-AUG-10', '12-AUG-10', 201.25, 'KEBEDE', 'ARON', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(12142, 'RTE', '13-AUG-10', '23-AUG-10', 175.00, 'JUNOR', 'LENNY', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(18398, 'RTE', '23-AUG-10', '24-AUG-10', 175.00, 'PARAGAS', 'ALVIN', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(79161, 'RTE', '24-AUG-10', '25-AUG-10', 175.00, 'RUBERTI', 'DOMINIC', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(82503, 'RTE', '27-AUG-10', '29-AUG-10', 175.00, 'RIINA', 'TATIANA', 2, 2)");
		adminI.add("INSERT INTO Reservations VALUES(80760, 'RTE', '03-SEP-10', '04-SEP-10', 175.00, 'DELISSER', 'COLEMAN', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(42688, 'RTE', '04-SEP-10', '08-SEP-10', 148.75, 'BRIERTON', 'MAJOR', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(73364, 'RTE', '09-SEP-10', '10-SEP-10', 157.50, 'MAROUN', 'MARTH', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(49609, 'RTE', '12-SEP-10', '17-SEP-10', 175.00, 'ZEPEDA', 'ELANA', 2, 2)");
		adminI.add("INSERT INTO Reservations VALUES(45022, 'RTE', '20-SEP-10', '21-SEP-10', 175.00, 'PAILET', 'GIUSEPPE', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(15192, 'RTE', '22-SEP-10', '23-SEP-10', 175.00, 'JEFFRYES', 'DANILO', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(15428, 'RTE', '25-SEP-10', '27-SEP-10', 175.00, 'BROOKSHEAR', 'NAPOLEON', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(57909, 'RTE', '02-OCT-10', '06-OCT-10', 201.25, 'OULETTE', 'ALDO', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(28353, 'RTE', '06-OCT-10', '08-OCT-10', 148.75, 'PINNELL', 'ANITA', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(71802, 'RTE', '23-OCT-10', '29-OCT-10', 201.25, 'GOODHUE', 'RUSSELL', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(87949, 'RTE', '02-NOV-10', '03-NOV-10', 201.25, 'SCHWEITZ', 'DANNETTE', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(19701, 'RTE', '14-NOV-10', '15-NOV-10', 201.25, 'HOTELLING', 'REGENIA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(79296, 'RTE', '15-NOV-10', '17-NOV-10', 175.00, 'BLACKMORE', 'APRYL', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(85166, 'RTE', '17-NOV-10', '19-NOV-10', 201.25, 'RAUGHT', 'DARON', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(21490, 'RTE', '23-NOV-10', '24-NOV-10', 175.00, 'PAVLOCK', 'MARCELO', 2, 2)");
		adminI.add("INSERT INTO Reservations VALUES(45745, 'RTE', '29-NOV-10', '30-NOV-10', 175.00, 'MEGGS', 'CARY', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(83487, 'RTE', '30-NOV-10', '02-DEC-10', 175.00, 'SHARRER', 'SHARA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(38293, 'RTE', '17-DEC-10', '19-DEC-10', 157.50, 'SCHOSSOW', 'BO', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(17480, 'RTE', '22-DEC-10', '28-DEC-10', 157.50, 'DEBARDELABEN', 'NELL', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(64785, 'RTE', '28-DEC-10', '04-JAN-11', 148.75, 'MAEWEATHER', 'AUGUST', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(56517, 'FNA', '01-JAN-10', '04-JAN-10', 225.00, 'TUPPEN', 'HANS', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(65186, 'FNA', '13-JAN-10', '15-JAN-10', 250.00, 'GOSSERAND', 'CURTIS', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(25966, 'FNA', '15-JAN-10', '17-JAN-10', 212.50, 'DUMAN', 'LUKE', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(66899, 'FNA', '18-JAN-10', '19-JAN-10', 225.00, 'TUNE', 'MARISSA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(58779, 'FNA', '19-JAN-10', '21-JAN-10', 250.00, 'ZENTNER', 'ROBBIE', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(93918, 'FNA', '26-JAN-10', '31-JAN-10', 250.00, 'HARDACRE', 'NEIL', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(51685, 'FNA', '03-FEB-10', '04-FEB-10', 250.00, 'EIMER', 'LYNELLE', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(28499, 'FNA', '04-FEB-10', '05-FEB-10', 212.50, 'DOMINGUEZ', 'STACEY', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(95600, 'FNA', '05-FEB-10', '07-FEB-10', 225.00, 'MOUTON', 'CASANDRA', 4, 0)");
		adminI.add("INSERT INTO Reservations VALUES(14834, 'FNA', '07-FEB-10', '13-FEB-10', 225.00, 'WITTROCK', 'DEBBIE', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(71009, 'FNA', '18-FEB-10', '20-FEB-10', 250.00, 'OXBORROW', 'CARLETTA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(63153, 'FNA', '20-FEB-10', '22-FEB-10', 312.50, 'TROKEY', 'INGRID', 4, 0)");
		adminI.add("INSERT INTO Reservations VALUES(28771, 'FNA', '26-FEB-10', '09-MAR-10', 250.00, 'MORRISSETTE', 'FAVIOLA', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(13205, 'FNA', '14-MAR-10', '18-MAR-10', 250.00, 'VANDOREN', 'MAJOR', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(72656, 'FNA', '18-MAR-10', '19-MAR-10', 250.00, 'PICHARD', 'HOLLIS', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(67292, 'FNA', '24-MAR-10', '28-MAR-10', 287.50, 'GUEDESSE', 'SOL', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(22169, 'FNA', '06-APR-10', '07-APR-10', 287.50, 'SCHUL', 'HOYT', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(65283, 'FNA', '08-APR-10', '09-APR-10', 287.50, 'ALLAIRE', 'RAMONA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(11631, 'FNA', '10-APR-10', '12-APR-10', 312.50, 'ESPINO', 'MARCELINA', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(83396, 'FNA', '12-APR-10', '13-APR-10', 250.00, 'DRDA', 'LESTER', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(99863, 'FNA', '17-APR-10', '19-APR-10', 250.00, 'ARBUCKLE', 'LORENA', 4, 0)");
		adminI.add("INSERT INTO Reservations VALUES(54811, 'FNA', '19-APR-10', '20-APR-10', 225.00, 'KLEVER', 'ERASMO', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(47352, 'FNA', '20-APR-10', '24-APR-10', 225.00, 'MARZETT', 'JOSEPH', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(28691, 'FNA', '24-APR-10', '26-APR-10', 225.00, 'SLUKA', 'AIKO', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(73732, 'FNA', '26-APR-10', '28-APR-10', 250.00, 'RODERICK', 'JOSLYN', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(26561, 'FNA', '29-APR-10', '03-MAY-10', 250.00, 'DYDA', 'DORIAN', 2, 2)");
		adminI.add("INSERT INTO Reservations VALUES(80964, 'FNA', '03-MAY-10', '10-MAY-10', 225.00, 'RHYME', 'QUINN', 4, 0)");
		adminI.add("INSERT INTO Reservations VALUES(85215, 'FNA', '14-MAY-10', '21-MAY-10', 250.00, 'TEHNEY', 'DARRYL', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(60205, 'FNA', '22-MAY-10', '31-MAY-10', 225.00, 'FRAINE', 'MANDA', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(37773, 'FNA', '31-MAY-10', '06-JUN-10', 312.50, 'BOUGIE', 'MONTE', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(69796, 'FNA', '09-JUN-10', '11-JUN-10', 250.00, 'LUANGSINGOTHA', 'WILHELMINA', 1, 3)");
		adminI.add("INSERT INTO Reservations VALUES(19494, 'FNA', '12-JUN-10', '19-JUN-10', 287.50, 'VORWERK', 'DORINE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(41037, 'FNA', '19-JUN-10', '02-JUL-10', 225.00, 'DUB', 'SUZANNE', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(74117, 'FNA', '02-JUL-10', '04-JUL-10', 225.00, 'HORTILLOSA', 'FREDDY', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(17338, 'FNA', '04-JUL-10', '05-JUL-10', 287.50, 'RUBY', 'QUENTIN', 2, 2)");
		adminI.add("INSERT INTO Reservations VALUES(71818, 'FNA', '05-JUL-10', '06-JUL-10', 250.00, 'SALOWITZ', 'QUEEN', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(99419, 'FNA', '11-JUL-10', '12-JUL-10', 250.00, 'DALGLEISH', 'RYAN', 4, 0)");
		adminI.add("INSERT INTO Reservations VALUES(87412, 'FNA', '16-JUL-10', '22-JUL-10', 250.00, 'HARE', 'ELSIE', 2, 2)");
		adminI.add("INSERT INTO Reservations VALUES(71902, 'FNA', '27-JUL-10', '31-JUL-10', 250.00, 'ANNABLE', 'IRA', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(94437, 'FNA', '31-JUL-10', '02-AUG-10', 250.00, 'SVATEK', 'KELLY', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(82515, 'FNA', '02-AUG-10', '03-AUG-10', 287.50, 'ALWINE', 'SHAWANDA', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(79615, 'FNA', '03-AUG-10', '05-AUG-10', 250.00, 'CONRAD', 'ELODIA', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(22615, 'FNA', '09-AUG-10', '10-AUG-10', 212.50, 'QUISPE', 'MARGARITO', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(59166, 'FNA', '20-AUG-10', '21-AUG-10', 250.00, 'TANI', 'DOMINICK', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(23302, 'FNA', '21-AUG-10', '22-AUG-10', 287.50, 'CONSTABLE', 'RASHAD', 1, 2)");
		adminI.add("INSERT INTO Reservations VALUES(53668, 'FNA', '22-AUG-10', '24-AUG-10', 287.50, 'FIGUROA', 'BEN', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(30671, 'FNA', '29-AUG-10', '01-SEP-10', 225.00, 'KON', 'DEWAYNE', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(35922, 'FNA', '01-SEP-10', '02-SEP-10', 287.50, 'SINGERMAN', 'YAN', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(75457, 'FNA', '05-SEP-10', '07-SEP-10', 225.00, 'SEGO', 'HUNG', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(16061, 'FNA', '11-SEP-10', '14-SEP-10', 312.50, 'ENDLER', 'ODIS', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(25389, 'FNA', '19-SEP-10', '21-SEP-10', 212.50, 'INTERRANTE', 'EMMITT', 3, 1)");
		adminI.add("INSERT INTO Reservations VALUES(60454, 'FNA', '06-OCT-10', '08-OCT-10', 225.00, 'GOLSTON', 'CONSUELA', 2, 2)");
		adminI.add("INSERT INTO Reservations VALUES(28198, 'FNA', '08-OCT-10', '18-OCT-10', 225.00, 'SAPORITO', 'ANDREAS', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(39910, 'FNA', '19-OCT-10', '21-OCT-10', 250.00, 'KAMINSKY', 'DANNY', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(92303, 'FNA', '04-NOV-10', '10-NOV-10', 287.50, 'BRISENDINE', 'JEWEL', 1, 0)");
		adminI.add("INSERT INTO Reservations VALUES(26345, 'FNA', '10-NOV-10', '12-NOV-10', 250.00, 'QUEROS', 'MAHALIA', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(99236, 'FNA', '16-NOV-10', '17-NOV-10', 287.50, 'SCHOENING', 'LEROY', 1, 1)");
		adminI.add("INSERT INTO Reservations VALUES(85417, 'FNA', '21-NOV-10', '26-NOV-10', 225.00, 'MULKEY', 'EMERY', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(10574, 'FNA', '26-NOV-10', '03-DEC-10', 287.50, 'SWEAZY', 'ROY', 2, 1)");
		adminI.add("INSERT INTO Reservations VALUES(29568, 'FNA', '03-DEC-10', '08-DEC-10', 225.00, 'BOETTNER', 'REIKO', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(28106, 'FNA', '11-DEC-10', '17-DEC-10', 312.50, 'VANDERSCHAEGE', 'MITZIE', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(56373, 'FNA', '18-DEC-10', '20-DEC-10', 225.00, 'BEGEN', 'ASHLYN', 3, 0)");
		adminI.add("INSERT INTO Reservations VALUES(94004, 'FNA', '21-DEC-10', '24-DEC-10', 250.00, 'JARVI', 'JOHNNIE', 4, 0)");
		adminI.add("INSERT INTO Reservations VALUES(28948, 'FNA', '24-DEC-10', '26-DEC-10', 287.50, 'RUPE', 'QUIANA', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(71987, 'FNA', '30-DEC-10', '31-DEC-10', 250.00, 'HARE', 'ELSIE', 2, 0)");
		adminI.add("INSERT INTO Reservations VALUES(67409, 'FNA', '31-DEC-10', '04-JAN-11', 250.00, 'MADRON', 'DONNIE', 2, 1)");
		
		// Get credentials from ServerSettings
		try {
			in = new BufferedReader(new FileReader("ServerSettings.txt"));
			url = in.readLine();
			username = in.readLine();
			password = in.readLine();
		}
		catch (IOException e) {
			System.err.println("Unable to get credentials from ServerSettings.txt.");
			System.exit(1);
		} 
		   
		// Set driver
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        }
        catch (Exception ex) {
            System.out.println("Driver not found");
        };

        // Connect to database server
        conn = null;
        try { 
           conn = DriverManager.getConnection(url, username, password);
        }
        catch (Exception ex)
        {
            System.out.println("Could not open connection");
        };
        System.out.println("Connected");

        // Ensure tables exist
        if (!tableExists("Rooms"))
        	createRoomsTable();
        if (!tableExists("Reservations"))
        	createReservationsTable();
        
        // Get number of entries in each table
        adminRoomCount = getRoomCount();
        adminReservationCount = getReservationCount();
     
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

	public String getPercentRevenue(String roomid) {
		String query =  "SELECT l / total " +
				"FROM (SELECT SUM(((CASE " +
				"                      WHEN CheckOut>TO_DATE('01-JAN-11') THEN TO_DATE('01-JAN-11') " +
				"                      ELSE CheckOut " +
				"                  END)-CheckIn) * rate) AS l " +
				"      FROM reservations " +
				"      WHERE Room = '" + roomid + "') a, " +
				"     (SELECT SUM(((CASE " +
				"                      WHEN CheckOut>TO_DATE('01-JAN-11') THEN TO_DATE('01-JAN-11') " +
				"                      ELSE CheckOut " +
				"                  END)-CheckIn) * rate) AS total " +
				"      FROM reservations) b";

		try {
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery(query);

			if (!rs.next())
				return "";

			float r = rs.getFloat(1);

			return String.format("%.2f%%", r * 100);
		} catch (SQLException e) {
			System.err.println(e);
			e.printStackTrace();
		}

		return "";
	}

	public String getTotalRevenue(String roomid) {
		String query = "SELECT SUM(((CASE " +
						"        WHEN CheckOut > TO_DATE('01-JAN-11') THEN TO_DATE('01-JAN-11') " +
						"        ELSE CheckOut " +
						"    END) - CheckIn) * Rate) " +
						"FROM Reservations " +
						"WHERE Room = '" + roomid + "'";

		try {
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery(query);

			if (!rs.next())
				return "";

			float r = rs.getFloat(1);

			return String.format("%.2f", r);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return "";
	}

	public String getPercentOccupied(String roomid) {
		String query = "SELECT SUM((CASE " +
						"        WHEN CheckOut > TO_DATE('01-JAN-11') THEN TO_DATE('01-JAN-11') " +
						"        ELSE CheckOut " +
						"    END) - CheckIn) / 365 " +
						"FROM Reservations " +
						"WHERE Room = '" + roomid + "'";

		try {
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery(query);

			if (!rs.next())
				return "";

			float r = rs.getFloat(1);
			
			return String.format("%.2f%%", r * 100);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return "";
	}

	public String getNightsOccupied(String roomid) {
		String query = "SELECT SUM((CASE " +
				        "        WHEN CheckOut > TO_DATE('01-JAN-11') THEN TO_DATE('01-JAN-11') " +
				        "        ELSE CheckOut " +
				        "    END) - CheckIn) " +
				        "FROM Reservations " +
				        "WHERE Room = '" + roomid + "'";
		
		try {
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery(query);
			
			if (!rs.next())
				return "";
			
			return rs.getString(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return "";
	}

	private static int getReservationCount() {
		String query = "SELECT COUNT(*) " +
	                   "FROM Reservations";
		int rows = 0;
		
		try {
			Statement s = conn.createStatement();
			s.executeQuery(query);
			ResultSet rs = s.getResultSet();
			rs.next();
			rows = rs.getInt(1);
		} catch (SQLException e) {
			return 0;
		}
		
		return rows;
	}

	private static int getRoomCount() {
		String query = "SELECT COUNT(*) " +
                       "FROM Rooms";
		int rows = 0;
		
		try {
			Statement s = conn.createStatement();
			s.executeQuery(query);
			ResultSet rs = s.getResultSet();
			rs.next();
			rows = rs.getInt(1);
		} catch (SQLException e) {
			return 0;
		}
		
		return rows;
	}

	private static void createReservationsTable() {
		String query = "CREATE TABLE Reservations ( " +
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
	
		try {
			Statement s = conn.createStatement();
			s.executeUpdate(query);
		} catch (SQLException e) {
			System.err.println("Unable to create Reservations table.");
			return;
		}	
	}

	private static void createRoomsTable() {
		String query = "CREATE TABLE Rooms ( " +
		        	   "RoomId VARCHAR(3) PRIMARY KEY, " +
		        	   "roomName VARCHAR(24), " +
		        	   "beds INTEGER, " +
		        	   "bedType VARCHAR(6), " +
		        	   "maxOccupancy INTEGER, " +
		        	   "basePrice INTEGER, " +
		        	   "decor VARCHAR(11), " +
		        	   "UNIQUE(roomName) " +
		        	   ")";
		
		try {
			Statement s = conn.createStatement();
			s.executeUpdate(query);
		} catch (SQLException e) {
			System.err.println("Unable to create Rooms table.");
			return;
		};
	}

	private static boolean tableExists(String string) {
		String query = "SELECT table_name " + 
				       "FROM user_tables " +
                       "WHERE table_name = UPPER(?)";
		
		try {
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, string);
			ResultSet result = ps.executeQuery();
			if (result.next()) {
				return true;
			}
		}
		catch (SQLException e) {
			return false;
		}
		
		return false;
	}

	/**
	 * Create the application.
	 */
	public InnReservations() {
		window = this;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmBedBreakfast = new JFrame();
		frmBedBreakfast.setTitle("Bed & Breakfast Inn Reservation System");
		frmBedBreakfast.setBounds(100, 100, 700, 600);
		frmBedBreakfast.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frmBedBreakfast.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JPanel guestPanel = new JPanel();
		guestPanel.setBorder(new EmptyBorder(5, 0, 5, 5));
		tabbedPane.addTab("Guest", null, guestPanel, null);
		guestPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel guestLeftPanel = new JPanel();
		guestPanel.add(guestLeftPanel, BorderLayout.WEST);
		guestLeftPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("100dlu:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.LINE_GAP_ROWSPEC,
				RowSpec.decode("23px"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),}));
		
		JButton btnGuestRR = new JButton("Rooms & Rates");
		btnGuestRR.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				displayRoomAndRates();
			}
		});
		guestLeftPanel.add(btnGuestRR, "2, 2, fill, top");
		
		JButton btnGuestReservations = new JButton("Reservations");
		btnGuestReservations.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				displayReservations();
			}
		});
		guestLeftPanel.add(btnGuestReservations, "2, 4");
		
		JLabel lblDetailsRoomInfo = new JLabel("Detailed room info:");
		guestLeftPanel.add(lblDetailsRoomInfo, "2, 6");
		
		JScrollPane spGuestDetailedRoomInfo = new JScrollPane();
		guestLeftPanel.add(spGuestDetailedRoomInfo, "2, 8, fill, fill");
		
		tblGuestDetailedRoomInfo = new JTable();
		tblGuestDetailedRoomInfo.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		spGuestDetailedRoomInfo.setViewportView(tblGuestDetailedRoomInfo);
		tblGuestDetailedRoomInfoTca = new TableColumnAdjuster(tblGuestDetailedRoomInfo);
		
		JPanel guestOutputPanel = new JPanel();
		guestPanel.add(guestOutputPanel, BorderLayout.CENTER);
		guestOutputPanel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane spGuestOutputPanel = new JScrollPane();
		guestOutputPanel.add(spGuestOutputPanel, BorderLayout.CENTER);
		
		tblGuestOutputPanel = new JTable();
		tblGuestOutputPanel.setSurrendersFocusOnKeystroke(true);
		guestOutputPanelTca = new TableColumnAdjuster(tblGuestOutputPanel);
		spGuestOutputPanel.setViewportView(tblGuestOutputPanel);
		
		tblGuestOutputPanel.getSelectionModel().addListSelectionListener(new RowListener());
		
		JPanel guestOutputPanelHeader = new JPanel();
		FlowLayout fl_guestOutputPanelHeader = (FlowLayout) guestOutputPanelHeader.getLayout();
		fl_guestOutputPanelHeader.setAlignment(FlowLayout.LEFT);
		guestOutputPanel.add(guestOutputPanelHeader, BorderLayout.NORTH);
		
		JLabel lblGuestOutputPanelHeader = new JLabel("Enter dates of stay:");
		guestOutputPanelHeader.add(lblGuestOutputPanelHeader);
		
		textFieldGuestOutputPanelHeaderCheckin = new JTextField();
		textFieldGuestOutputPanelHeaderCheckin.setText("01-JAN-10");
		textFieldGuestOutputPanelHeaderCheckin.setToolTipText("CheckIn");
		guestOutputPanelHeader.add(textFieldGuestOutputPanelHeaderCheckin);
		textFieldGuestOutputPanelHeaderCheckin.setColumns(10);
		
		textFieldGuestOutputPanelHeaderCheckOut = new JTextField();
		textFieldGuestOutputPanelHeaderCheckOut.setText("20-JAN-10");
		textFieldGuestOutputPanelHeaderCheckOut.setToolTipText("CheckOut");
		guestOutputPanelHeader.add(textFieldGuestOutputPanelHeaderCheckOut);
		textFieldGuestOutputPanelHeaderCheckOut.setColumns(10);
		
		JLabel lblDateFormat = new JLabel("DD-MON-YY");
		guestOutputPanelHeader.add(lblDateFormat);
		
		JPanel guestOutputPanelFooter = new JPanel();
		FlowLayout flowLayout = (FlowLayout) guestOutputPanelFooter.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		guestOutputPanel.add(guestOutputPanelFooter, BorderLayout.SOUTH);
		
		JButton btnCheckOutputFooterCA = new JButton("Check Availability");
		btnCheckOutputFooterCA.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String start = textFieldGuestOutputPanelHeaderCheckin.getText();
				String end = textFieldGuestOutputPanelHeaderCheckOut.getText();
				int j = tblGuestOutputPanel.getSelectionModel().getLeadSelectionIndex();
				if (j < 0)
					return;
				String rm = (String) tblGuestOutputPanel.getValueAt(j, 0);
				String query = String.format("( " +
						"SELECT TO_CHAR(d.curdate, 'DD-MON-YY') AS Day, 'Not occupied' AS Status, " +
						"      CASE " +
						"         WHEN TO_CHAR(d.curdate) = '01-JAN-10' THEN rm.basePrice * 1.25 " +
						"         WHEN TO_CHAR(d.curdate) = '04-JUL-10' THEN rm.basePrice * 1.25 " +
						"         WHEN TO_CHAR(d.curdate) = '06-SEP-10' THEN rm.basePrice * 1.25 " +
						"         WHEN TO_CHAR(d.curdate) = '30-OCT-10' THEN rm.basePrice * 1.25 " +
						"         WHEN TO_CHAR(d.curdate, 'FMDAY') = 'MONDAY' THEN rm.basePrice " +
						"         WHEN TO_CHAR(d.curdate, 'FMDAY') = 'TUESDAY' THEN rm.basePrice   " +
						"         WHEN TO_CHAR(d.curdate, 'FMDAY') = 'WEDNESDAY' THEN rm.basePrice   " +
						"         WHEN TO_CHAR(d.curdate, 'FMDAY') = 'THURSDAY' THEN rm.basePrice   " +
						"         WHEN TO_CHAR(d.curdate, 'FMDAY') = 'FRIDAY' THEN rm.basePrice   " +
						"         WHEN TO_CHAR(d.curdate, 'FMDAY') = 'SATURDAY' THEN rm.basePrice * 1.10 " +
						"         WHEN TO_CHAR(d.curdate, 'FMDAY') = 'SUNDAY' THEN rm.basePrice * 1.10  " +
						"      END AS Price " +
						"FROM (SELECT to_date('%s') + rownum - 1 AS CurDate " +
						"      FROM reservations  " +
						"      WHERE rownum < to_date('%s') - to_date('%s') + 1) d, " +
						"     reservations r, " +
						"     rooms rm " +
						"WHERE r.room = '%s' and rm.roomid = r.room and " +
						"      d.curdate not in (SELECT d.curdate " +
						"                        FROM (SELECT to_date('%s') + rownum - 1 AS CurDate " +
						"                              FROM reservations " +
						"                              WHERE rownum < to_date('%s') - to_date('%s') + 1) d, " +
						"                             reservations r " +
						"                        WHERE r.room = '%s' and " +
						"                              d.curdate between r.checkin and r.checkout-1) " +
						") " +
						"UNION " +
						"( " +
						"SELECT TO_CHAR(d.curdate, 'DD-MON-YY'), 'Occupied', NULL " +
						"FROM (SELECT to_date('%s') + rownum - 1 AS CurDate " +
						"      FROM reservations " +
						"      WHERE rownum < to_date('%s') - to_date('%s') + 1) d, " +
						"     reservations r " +
						"WHERE r.room = '%s' and " +
						"      d.curdate between r.checkin and r.checkout-1 " +
						") " +
						"ORDER BY 1", start, end, start, rm, start, end, start, rm, start, end, start, rm);
				
				Vector<String> dataColumns = new Vector<String>();
				Vector<Vector<String>> data = new Vector<Vector<String>>();
				
				try {
					Statement s = conn.createStatement();
					ResultSet rs = s.executeQuery(query);
					ResultSetMetaData md = rs.getMetaData();
			        int columns = md.getColumnCount();
			        
			        for (int i = 1; i <= columns; i++)
			        	dataColumns.addElement(md.getColumnName(i));
			        
					while (rs.next()) {
						Vector<String> row = new Vector<String>(columns);

		                for (int i = 1; i <= columns; i++)
		                    row.addElement(rs.getString(i));

		                data.addElement(row);
					}
					
				} catch (SQLException e1) {
					return;
				}
				
				
				TableDialog dialog = new TableDialog(dataColumns, data);
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.setVisible(true);
			}
		});
		guestOutputPanelFooter.add(btnCheckOutputFooterCA);
		
		JButton btnCheckOutputFooterPR = new JButton("Place a Reservation");
		btnCheckOutputFooterPR.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (reservationIsValid()) {
					ReservationCompletionForm dialog = new ReservationCompletionForm(window);
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
				}
				else {
					JOptionPane.showMessageDialog(null, "Atleast one of the days in the specified date range is occupied.");
				}
			}
		});
		guestOutputPanelFooter.add(btnCheckOutputFooterPR);
		
		JPanel ownerPanel = new JPanel();
		ownerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		tabbedPane.addTab("Owner", null, ownerPanel, null);
		ownerPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel panelOwnerLeft = new JPanel();
		ownerPanel.add(panelOwnerLeft, BorderLayout.WEST);
		panelOwnerLeft.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				FormFactory.MIN_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.LINE_GAP_ROWSPEC,
				RowSpec.decode("14px"),
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.MIN_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.MIN_ROWSPEC,
				RowSpec.decode("min:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.MIN_ROWSPEC,
				RowSpec.decode("min:grow"),}));
		
		JLabel lblOwnerStart = new JLabel("Start DD-MON-YY");
		panelOwnerLeft.add(lblOwnerStart, "2, 2, left, top");
		
		textFieldOwnerStart = new JTextField();
		textFieldOwnerStart.setText("01-JAN-10");
		panelOwnerLeft.add(textFieldOwnerStart, "2, 3, fill, default");
		textFieldOwnerStart.setColumns(10);
		
		JLabel lblOwnerEnd = new JLabel("End DD-MON-YY");
		panelOwnerLeft.add(lblOwnerEnd, "2, 5");
		
		textFieldOwnerEnd = new JTextField();
		textFieldOwnerEnd.setText("02-JAN-10");
		panelOwnerLeft.add(textFieldOwnerEnd, "2, 6, fill, default");
		textFieldOwnerEnd.setColumns(10);
		
		JLabel lblOwnerRoom = new JLabel("Room ###");
		panelOwnerLeft.add(lblOwnerRoom, "2, 8");
		
		textFieldOwnerRoom = new JTextField();
		panelOwnerLeft.add(textFieldOwnerRoom, "2, 9, fill, default");
		textFieldOwnerRoom.setColumns(10);
		
		JButton btnOccupancy = new JButton("Occupancy");
		btnOccupancy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ownerMode = OwnerMode.OCCUPANCY;
				
				String start = textFieldOwnerStart.getText();
				String end = textFieldOwnerEnd.getText();
				
				viewOccupancy(start, end);
			}
		});
		panelOwnerLeft.add(btnOccupancy, "2, 11");
		
		JPanel panel = new JPanel();
		panelOwnerLeft.add(panel, "2, 13, fill, fill");
		panel.setLayout(new BorderLayout(0, 0));
		
		JButton btnOwnerReservations = new JButton("Reservations");
		btnOwnerReservations.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				displayReservationStats();
			}
		});
		btnOwnerReservations.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(btnOwnerReservations, BorderLayout.WEST);
		
		JButton btnOwnerDays = new JButton("Days");
		btnOwnerDays.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				displayDayStats();
			}
		});
		panel.add(btnOwnerDays, BorderLayout.CENTER);
		
		JButton btnOwnerRevenue = new JButton("Revenue");
		btnOwnerRevenue.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				displayRevenueStats();
			}
		});
		panel.add(btnOwnerRevenue, BorderLayout.EAST);
		
		JButton btnOwnerViewReservations = new JButton("View Reservations");
		btnOwnerViewReservations.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ownerMode = OwnerMode.RESERVATIONS;
				
				String startDate = textFieldOwnerStart.getText();
				String endDate = textFieldOwnerEnd.getText();
				String room = textFieldOwnerRoom.getText();
				
				startDate = startDate.length() > 0 ? startDate : null;
				endDate = endDate.length() > 0 ? endDate : null;
				room = room.length() > 0 ? room : null;
				
				browseReservationsQuery(startDate, endDate, room);
			}
		});
		panelOwnerLeft.add(btnOwnerViewReservations, "2, 15");
		
		JButton btnOwnerViewRooms = new JButton("View Rooms");
		btnOwnerViewRooms.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ownerMode = OwnerMode.ROOMS;
				
				browseRoomsQuery();
			}
		});
		panelOwnerLeft.add(btnOwnerViewRooms, "2, 17");
		
		JLabel lblOwnerDetailedInformation = new JLabel("Detailed Information");
		panelOwnerLeft.add(lblOwnerDetailedInformation, "2, 20");
		
		JScrollPane scrollPaneOwnerDetailed = new JScrollPane();
		panelOwnerLeft.add(scrollPaneOwnerDetailed, "2, 21, fill, fill");
		
		tableOwnerDetailed = new JTable();
		tableOwnerDetailedTca = new TableColumnAdjuster(tableOwnerDetailed);
		scrollPaneOwnerDetailed.setViewportView(tableOwnerDetailed);
		
		JLabel lblOwnerReservations = new JLabel("Reservations");
		panelOwnerLeft.add(lblOwnerReservations, "2, 23");
		
		JScrollPane scrollPaneOwnerReservations = new JScrollPane();
		panelOwnerLeft.add(scrollPaneOwnerReservations, "2, 24, fill, fill");
		
		tableOwnerReservations = new JTable();
		tableOwnerReservationsTca = new TableColumnAdjuster(tableOwnerReservations);
		scrollPaneOwnerReservations.setViewportView(tableOwnerReservations);
		
		tableOwnerReservations.getSelectionModel().addListSelectionListener(new OwnerReservationRowListener());
		
		JPanel panelOwnerRight = new JPanel();
		ownerPanel.add(panelOwnerRight, BorderLayout.CENTER);
		panelOwnerRight.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPaneOwnerRight = new JScrollPane();
		panelOwnerRight.add(scrollPaneOwnerRight, BorderLayout.CENTER);
		
		tableOwnerRight = new JTable();
		tableOwnerRight.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableOwnerRightTca = new TableColumnAdjuster(tableOwnerRight);
		scrollPaneOwnerRight.setViewportView(tableOwnerRight);
		
		tableOwnerRight.getSelectionModel().addListSelectionListener(new OwnerDetailedRowListenerSingle());
		
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
		
		lblAdminStatus = new JLabel("Database status: " + (adminRoomCount + adminReservationCount != 0 ? "full" : "empty"));
		statusPanel.add(lblAdminStatus, "2, 2, left, default");
		
		lblAdminReservations = new JLabel("Reservations: " + adminReservationCount);
		statusPanel.add(lblAdminReservations, "2, 4, left, center");
		
		lblAdminRooms = new JLabel("Rooms: " + adminRoomCount);
		statusPanel.add(lblAdminRooms, "2, 6, left, center");
		
		lblAdminDiagnostics = new JLabel("Diagnostics: success");
		statusPanel.add(lblAdminDiagnostics, "2, 8, left, center");
		
		JButton btnViewRooms = new JButton("View Rooms");
		btnViewRooms.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				viewRoomsQuery();
			}
		});
		statusPanel.add(btnViewRooms, "2, 10, fill, top");
		
		JButton btnViewReservations = new JButton("View Reservations");
		btnViewReservations.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				viewReservationsQuery();
			}
		});
		statusPanel.add(btnViewReservations, "2, 12, fill, top");
		
		btnAdminLoadDB = new JButton(adminRoomCount + adminReservationCount == 0 ? "Load DB" : "Reload DB");
		btnAdminLoadDB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadDB();
			}
		});
		statusPanel.add(btnAdminLoadDB, "2, 14, fill, top");
		
		JButton btnClearDB = new JButton("Clear DB");
		btnClearDB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ClearDB();
			}
		});
		statusPanel.add(btnClearDB, "2, 16, fill, top");
		
		JButton btnRemoveDB = new JButton("Remove DB");
		btnRemoveDB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeDB();
			}
		});
		statusPanel.add(btnRemoveDB, "2, 18, fill, top");
		
		JPanel outputPanel = new JPanel();
		adminPanel.add(outputPanel, BorderLayout.CENTER);
		outputPanel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		outputPanel.add(scrollPane, BorderLayout.CENTER);
		
		adminTable = new JTable();
		adminTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		adminTca = new TableColumnAdjuster(adminTable);
		scrollPane.setViewportView(adminTable);
	}

	protected void browseRoomsQuery() {
		if (!tableExists("Rooms"))
			return;
		
		Vector<String> tableColumns = new Vector<String>();
		Vector<Vector<String>> tableData = new Vector<Vector<String>>();
		
		String query = "SELECT RoomId, roomName FROM Rooms";
		
		Statement s;
		try {
			s = conn.createStatement();
			ResultSet rs = s.executeQuery(query);
			ResultSetMetaData md = rs.getMetaData();
	        int columns = md.getColumnCount();
	        
	        for (int i = 1; i <= columns; i++)
	        	tableColumns.addElement(md.getColumnName(i));
	        
			while (rs.next()) {
				Vector<String> row = new Vector<String>(columns);

                for (int i = 1; i <= columns; i++)
                    row.addElement(rs.getString(i));

                tableData.addElement(row);
			}
			s.close();
		} catch (SQLException e) {
			return;
		}
		
		tableOwnerRight.setModel(new DefaultTableModel(
				tableData,
				tableColumns
			));
		
		tableOwnerRightTca.adjustColumns();
	}

	protected void displayReservationStats() {
		reservationMonthByMonthQuery("counts");
	}
	
	protected void displayDayStats() {
		reservationMonthByMonthQuery("days");
	}
	
	protected void displayRevenueStats() {
		reservationMonthByMonthQuery("revenue");
	}

	protected void displayReservations() {
		if (tableExists("Rooms") && tableExists("Reservations")) {
			Vector<String> tableColumns = new Vector<String>();
			 Vector<Vector<String>> tableData = new Vector<Vector<String>>();
			
			 String start = textFieldGuestOutputPanelHeaderCheckin.getText();
			 String end = textFieldGuestOutputPanelHeaderCheckOut.getText();
			 
			 if (start.length() == 0 || end.length() == 0)
				 return;
			 
			 String query = String.format("SELECT k.roomid, k.roomName, MAX(k.Price) AS Price " +
						"FROM (SELECT rm.roomid, " +
						"             rm.roomName, " +
						"             CASE " +
						"                WHEN TO_CHAR(d.curdate) = '01-JAN-10' THEN rm.basePrice * 1.25 " +
						"                WHEN TO_CHAR(d.curdate) = '04-JUL-10' THEN rm.basePrice * 1.25 " +
						"                WHEN TO_CHAR(d.curdate) = '06-SEP-10' THEN rm.basePrice * 1.25 " +
						"                WHEN TO_CHAR(d.curdate) = '30-OCT-10' THEN rm.basePrice * 1.25 " +
						"                WHEN TO_CHAR(d.curdate, 'FMDAY') = 'MONDAY' THEN rm.basePrice " +
						"                WHEN TO_CHAR(d.curdate, 'FMDAY') = 'TUESDAY' THEN rm.basePrice " +
						"                WHEN TO_CHAR(d.curdate, 'FMDAY') = 'WEDNESDAY' THEN rm.basePrice " +
						"                WHEN TO_CHAR(d.curdate, 'FMDAY') = 'THURSDAY' THEN rm.basePrice " +
						"                WHEN TO_CHAR(d.curdate, 'FMDAY') = 'FRIDAY' THEN rm.basePrice " +
						"                WHEN TO_CHAR(d.curdate, 'FMDAY') = 'SATURDAY' THEN rm.basePrice * 1.10 " +
						"                WHEN TO_CHAR(d.curdate, 'FMDAY') = 'SUNDAY' THEN rm.basePrice * 1.10 " +
						"             END AS Price " +
						"      FROM (SELECT to_date('%s') + rownum - 1 AS CurDate " +
						"            FROM reservations  " +
						"            WHERE rownum < to_date('%s') - to_date('%s') + 1) d, " +
						"           reservations r,  " +
						"           rooms rm  " +
						"      WHERE rm.roomid = r.room and  " +
						"            d.curdate not in (SELECT d.curdate  " +
						"                              FROM (SELECT to_date('%s') + rownum - 1 AS CurDate  " +
						"                                    FROM reservations  " +
						"                                    WHERE rownum < to_date('%s') - to_date('%s') + 1) d,  " +
						"                                   reservations r  " +
						"                              WHERE d.curdate between r.checkin and r.checkout-1 AND r.room = rm.roomid)) k " +
						"GROUP BY k.roomid, k.roomName " +
						"HAVING COUNT(k.roomid) = (SELECT COUNT(*) * (TO_DATE('%s') - TO_DATE('%s')) " +
						"                          FROM Reservations " +
						"                          WHERE Room = k.roomid)", start, end, start, start, end, start, end, start);
			
			Statement s;
			try {
				s = conn.createStatement();
				ResultSet rs = s.executeQuery(query);
				ResultSetMetaData md = rs.getMetaData();
		        int columns = md.getColumnCount();
		        
		        for (int i = 1; i <= columns; i++)
		        	tableColumns.addElement(md.getColumnName(i));
		        
				while (rs.next()) {
					Vector<String> row = new Vector<String>(columns);

	                for (int i = 1; i <= columns; i++)
	                    row.addElement(rs.getString(i));

	                tableData.addElement(row);
				}
				s.close();
			} catch (SQLException e) {
				return;
			}
			
			tblGuestOutputPanel.setModel(new DefaultTableModel(
					tableData,
					tableColumns
				));
			
			guestOutputPanelTca.adjustColumns();
		}
		
		
	}

	// Determines whether date range is valid for reservation
	protected boolean reservationIsValid() {
		String start = textFieldGuestOutputPanelHeaderCheckin.getText();
		String end = textFieldGuestOutputPanelHeaderCheckOut.getText();
		int j = tblGuestOutputPanel.getSelectionModel().getLeadSelectionIndex();
		if (j < 0)
			return false;
		String rm = (String) tblGuestOutputPanel.getValueAt(
				j,
				0);
		String query = String.format(
				"SELECT 'Occupied' " +
				"FROM (SELECT to_date('%s') + rownum - 1 AS CurDate " +
				"      FROM reservations " +
				"      WHERE rownum < to_date('%s') - to_date('%s') + 1) d, " +
				"     reservations r " +
				"WHERE r.room = '%s' and " +
				"      d.curdate between r.checkin and r.checkout-1 ", start, end, start, rm);
	
		try {
			Statement s = conn.createStatement();
			ResultSet result = s.executeQuery(query);
			if (result.next()) {
				return false;
			}
		}
		catch (SQLException e) {
			return false;
		}
	
		return true;
	}

	protected void displayRoomAndRates() {
		
		if (tableExists("Rooms")) {
			Vector<String> tableColumns = new Vector<String>();
			 Vector<Vector<String>> tableData = new Vector<Vector<String>>();
			
			String query = "SELECT roomid, roomname " +
		                   "FROM Rooms";
			
			Statement s;
			try {
				s = conn.createStatement();
				ResultSet rs = s.executeQuery(query);
				ResultSetMetaData md = rs.getMetaData();
		        int columns = md.getColumnCount();
		        
		        for (int i = 1; i <= columns; i++)
		        	tableColumns.addElement(md.getColumnName(i));
		        
				while (rs.next()) {
					Vector<String> row = new Vector<String>(columns);

	                for (int i = 1; i <= columns; i++)
	                    row.addElement(rs.getString(i));

	                tableData.addElement(row);
				}
				s.close();
			} catch (SQLException e) {
				return;
			}
			
			tblGuestOutputPanel.setModel(new DefaultTableModel(
					tableData,
					tableColumns
				));
			
			guestOutputPanelTca.adjustColumns();
		}
	}

	protected void removeDB() {
		boolean oneExists = false;

		try {
			if (tableExists("Reservations")) {
				oneExists = true;
				Statement s = conn.createStatement();
				s.executeUpdate("DROP TABLE Reservations CASCADE CONSTRAINTS");
				s.close();
			}

			if (tableExists("Rooms")) {
				oneExists = true;
				Statement s = conn.createStatement();
				s.executeUpdate("DROP TABLE Rooms CASCADE CONSTRAINTS");
				s.close();
			}
		}
		catch (SQLException e) {
			return;
		}
		
		lblAdminReservations.setText("Reservations: 0");
		lblAdminRooms.setText("Rooms: 0");
		lblAdminDiagnostics.setText("Diagnostics: " + (oneExists ? "success" : "no tables"));
		lblAdminStatus.setText("Database status: no database");
		btnAdminLoadDB.setText("Load DB");
	}

	protected void ClearDB() {
		boolean oneExists = false;
		
		try {
			if (tableExists("Reservations")) {
				oneExists = true;
				Statement s = conn.createStatement();
				s.executeUpdate("DELETE FROM Reservations");
			}
			
			if (tableExists("Rooms")) {
				oneExists = true;
				Statement s = conn.createStatement();
				s.executeUpdate("DELETE FROM Rooms");
			}
		}
		catch (SQLException e) {
			return;
		}
		
		adminRoomCount = getRoomCount();
		adminReservationCount = getReservationCount();
		lblAdminReservations.setText("Reservations: " + adminReservationCount);
		lblAdminRooms.setText("Rooms: " + adminRoomCount);
		lblAdminDiagnostics.setText("Diagnostics: " + (oneExists ? "success" : "failure"));
		lblAdminStatus.setText("Database status: " + (oneExists ? "empty" : "no database"));
		btnAdminLoadDB.setText("Load DB");
	}

	protected void loadDB() {
		if (!tableExists("Rooms"))
        	createRoomsTable();
		
        if (!tableExists("Reservations"))
        	createReservationsTable();
		
		if (adminRoomCount + adminReservationCount == 0) {
			try {
				Statement s = conn.createStatement();
				for (String e : adminI) {
					s.executeUpdate(e);
				}
				s.close();
			} catch (SQLException e1) {
				return;
			}
			adminRoomCount = getRoomCount();
			adminReservationCount = getReservationCount();
			lblAdminReservations.setText("Reservations: " + adminReservationCount);
			lblAdminRooms.setText("Rooms: " + adminRoomCount);
			lblAdminDiagnostics.setText("Diagnostics: success");
			lblAdminStatus.setText("Database status: full");
			btnAdminLoadDB.setText("Reload DB");
		}
		else {
			lblAdminDiagnostics.setText("Diagnostics: already full");
		}
	}

	protected void viewRoomsQuery() {
		if (!tableExists("Rooms")) {
			lblAdminDiagnostics.setText("Diagnostics: no database");
			return;
		}
		
		String query = "SELECT * " +
	                   "FROM Rooms";
		adminColumns = new Vector<String>();
		adminData = new Vector<Vector<String>>();
		
		try {
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery(query);
			ResultSetMetaData md = rs.getMetaData();
	        int columns = md.getColumnCount();
	        
	        for (int i = 1; i <= columns; i++)
                adminColumns.addElement(md.getColumnName(i));
	        
			while (rs.next()) {
				Vector<String> row = new Vector<String>(columns);

                for (int i = 1; i <= columns; i++)
                    row.addElement(rs.getString(i));

                adminData.addElement(row);
			}
			
		} catch (SQLException e) {
			System.err.println("Unable to get rooms.");
			return;
		}
		
		adminTable.setModel(new DefaultTableModel(
				adminData,
				adminColumns
			));
		
		adminTca.adjustColumns();
	}

	protected void viewReservationsQuery() {
		if (!tableExists("Reservations")) {
			lblAdminDiagnostics.setText("Diagnostics: no database");
			return;
		}
		
		String query = "SELECT  CODE, ROOM, " +
				       "TO_CHAR(CHECKIN, 'DD-MON-YY') AS CHECKIN, " +
				       "TO_CHAR(CHECKOUT, 'DD-MON-YY') AS CHECKOUT, " +
				       "RATE, LASTNAME, FIRSTNAME, ADULTS, KIDS " +
	                   "FROM Reservations";
		adminColumns = new Vector<String>();
		adminData = new Vector<Vector<String>>();
		
		try {
			Statement s = conn.createStatement();
			ResultSet rs = s.executeQuery(query);
			ResultSetMetaData md = rs.getMetaData();
	        int columns = md.getColumnCount();
	        
	        for (int i = 1; i <= columns; i++)
                adminColumns.addElement(md.getColumnName(i));
	        
			while (rs.next()) {
				Vector<String> row = new Vector<String>(columns);

                for (int i = 1; i <= columns; i++)
                    row.addElement(rs.getString(i));

                adminData.addElement(row);
			}
			
		} catch (SQLException e) {
			System.err.println("Unable to get reservations.");
			return;
		}
		
		adminTable.setModel(new DefaultTableModel(
				adminData,
				adminColumns
			));
		
		adminTca.adjustColumns();
	}
	
	/* Used to achieve OR-1 Requirement */
	private void viewOccupancy(String startDate, String endDate) {
	    if(!startDate.contains("10") && !startDate.contains("11")) {
	        startDate += "-10";
	    }

	    if(endDate.length() == 0) {
	        oneDateOccupancyQuery(startDate);
	    }
	    else {
	        if(!endDate.contains("10") && !endDate.contains("11")) {
	            endDate += "-10";
	        }
	        twoDateOccupancyQuery(startDate, endDate);
	    }

	}

	private void oneDateOccupancyQuery(String inputDate) {
	    String queryToExecute = "select distinct roomname, r1.roomid, " +
	    		"	        case when exists (select * from reservations) then 'Occupied' else 'Empty' end Occupied " +
	    		"	        from rooms r1, reservations re1 " +
	    		"	        where r1.roomid = re1.room and " +
	    		"	        EXISTS " +
	    		"	            (select * from rooms r, reservations re " +
	    		"	             where " +
	    		"	              r.roomid = re.room and " +
	    		"	              r1.roomid = r.roomid and " +
	    		"	              checkin <= to_date(?, 'DD-MON-YY') and " +
	    		"	              checkout > to_date(?, 'DD-MON-YY')) " +
	    		"	        UNION " +
	    		"	        select distinct roomname, r1.roomid, " +
	    		"	        case when not exists (select * from reservations) then 'Occupied' else 'Empty' end Occupied " +
	    		"	        from rooms r1, reservations re1 " +
	    		"	        where r1.roomid = re1.room and " +
	    		"	        NOT EXISTS " +
	    		"	            (select * from rooms r, reservations re " +
	    		"	             where " +
	    		"	              r.roomid = re.room and " +
	    		"	              r1.roomid = r.roomid and " +
	    		"	              checkin <= to_date(?, 'DD-MON-YY') and " +
	    		"	              checkout > to_date(?, 'DD-MON-YY')) ";

	    Vector<String> dataColumns = new Vector<String>();
		Vector<Vector<String>> data = new Vector<Vector<String>>();
	    
	    try {
	        PreparedStatement ps = conn.prepareStatement(queryToExecute);
	        ps.setString(1, inputDate);
	        ps.setString(2, inputDate);
	        ps.setString(3, inputDate);
	        ps.setString(4, inputDate);
	        ResultSet results = ps.executeQuery();
	        
	        ResultSetMetaData md = results.getMetaData();
	        int columns = md.getColumnCount();
	        
	        for (int i = 1; i <= columns; i++)
	        	dataColumns.addElement(md.getColumnName(i));
	        
			while (results.next()) {
				Vector<String> row = new Vector<String>(columns);

                for (int i = 1; i <= columns; i++)
                    row.addElement(results.getString(i));

                data.addElement(row);
			}

			
	    }
	    catch (SQLException e) {
	    	return;
	    }
	    
	    tableOwnerRight.setModel(new DefaultTableModel(
	    		data,
				dataColumns
			));
		
	    tableOwnerRightTca.adjustColumns();
	}

	/* Handles OR-1 case where range of dates is given. */
	private void twoDateOccupancyQuery(String startDate, String endDate){
	    List<String> emptyRooms = findEmptyRoomsInRange(startDate, endDate);
	    List<String> fullyOccupiedRooms = findOccupiedRoomsInRange(startDate, endDate, emptyRooms);
	    List<String> partiallyOccupiedRooms = generateListOfAllRoomIDS();
	    partiallyOccupiedRooms.removeAll(emptyRooms);
	    partiallyOccupiedRooms.removeAll(fullyOccupiedRooms);
	    
	    Vector<String> occupancyColumns = new Vector<String>();
	    Vector<Vector<String>> occupancyData = new Vector<Vector<String>>();
	    occupancyColumns.addElement("RoomId");
	    occupancyColumns.addElement("Occupancy Status");

	    for(String room: emptyRooms) {
	        Vector<String> row = new Vector<String>();
	        row.addElement(room);
	        row.addElement("Empty");
	        occupancyData.addElement(row);
	    }
	    for(String room: fullyOccupiedRooms) {
	        Vector<String> row = new Vector<String>();
	        row.addElement(room);
	        row.addElement("Fully Occupied");
	        occupancyData.addElement(row);
	    }
	    for(String room: partiallyOccupiedRooms) {
	        Vector<String> row = new Vector<String>();
	        row.addElement(room);
	        row.addElement("Partially Occupied");
	        occupancyData.addElement(row);
	    }
	   
	    tableOwnerRight.setModel(new DefaultTableModel(
	    		occupancyData,
	    		occupancyColumns
			));
		
	    tableOwnerRightTca.adjustColumns();
	    
	    return;
	}

	/* Finds and returns the list of rooms that are completely empty. */
	private List<String> findEmptyRoomsInRange(String startDate, String endDate) {
	    /* startDate indices = 1,3,6 
	    endDate indices = 2,4,5  */
	    String emptyRoomsQuery = "select r1.roomid " +
	                            "from rooms r1 " +
	                            "where r1.roomid NOT IN (" +
	                            "select room " +
	                            "from reservations " + 
	                            "where room = r1.roomid and ((checkin <= to_date(?, 'DD-MON-YY') and " +
	                            "checkout > to_date(?, 'DD-MON-YY')) or " +
	                            "(checkin >= to_date(?, 'DD-MON-YY') and " +
	                            "checkin < to_date(?, 'DD-MON-YY')) or " +
	                            "(checkout < to_date(?, 'DD-MON-YY') and " +
	                            "checkout > to_date(?, 'DD-MON-YY'))))";

	    try {
	        PreparedStatement erq = conn.prepareStatement(emptyRoomsQuery);
	        erq.setString(1, startDate);
	        erq.setString(3, startDate);
	        erq.setString(6, startDate);
	        erq.setString(2, endDate);
	        erq.setString(4, endDate);
	        erq.setString(5, endDate);
	        ResultSet emptyRoomsQueryResult = erq.executeQuery();
	        List<String> emptyRooms = getEmptyRoomsFromResultSet(emptyRoomsQueryResult);
	        return emptyRooms;
	    } catch (SQLException e) {
	        System.out.println("Empty rooms query failed.");
	        return new ArrayList<String>();
	    }
	}

	/* Returns a list of strings. This list contains the RoomIds of all completely empty rooms. */
	private List<String> getEmptyRoomsFromResultSet(ResultSet emptyQueryResults) {
	    ArrayList<String> rooms = new ArrayList<String>();
	    boolean hasNext;
		try {
			hasNext = emptyQueryResults.next();
			while (hasNext) {
		        String room = emptyQueryResults.getString(1);
		        rooms.add(room);
		        hasNext = emptyQueryResults.next();
		    }
		} catch (SQLException e) {
			return rooms;
		}
	     
	    return rooms;
	}

	private ArrayList<String> generateListOfAllRoomIDS() {
	    String[] rooms = {"AOB", "CAS", "FNA", "HBB", "IBD", "IBS", "MWC", "RND", "RTE", "TAA"};
	    return new ArrayList<String>(Arrays.asList(rooms));
	}

	private List<String> findOccupiedRoomsInRange(String startDate, String endDate, List<String> emptyRooms) {
	    ArrayList<String> fullyOccupiedRooms = new ArrayList<String>();
	    String occupiedRoomsQuery = "SELECT room " +
	    		"FROM   reservations  " +
	    		"    WHERE ( checkin <= To_date(?, 'DD-MON-YY') AND checkout > To_date(?, 'DD-MON-YY')) OR " +
	    		"         ( checkin >= To_date(?, 'DD-MON-YY') AND checkin < To_date(?, 'DD-MON-YY')) OR " +
	    		"         ( checkout < To_date(?, 'DD-MON-YY') AND checkout > To_date(?, 'DD-MON-YY')) " +
	    		"GROUP BY room " +
	    		"HAVING SUM((CASE WHEN CheckOut > TO_DATE(?) THEN TO_DATE(?) " +
	    		"                       ELSE CheckOut END) - (CASE WHEN CheckIn < TO_DATE(?) THEN TO_DATE(?) " +
	    		"                                                  ELSE CheckIn END)) = TO_DATE(?) - TO_DATE(?)";
	    try {
	        PreparedStatement oq = conn.prepareStatement(occupiedRoomsQuery);
            oq.setString(1, startDate);
            oq.setString(3, startDate);
            oq.setString(6, startDate);
            oq.setString(9, startDate);
            oq.setString(10, startDate);
            oq.setString(12, startDate);
            oq.setString(2, endDate);
            oq.setString(4, endDate);
            oq.setString(5, endDate);
            oq.setString(7, endDate);
            oq.setString(8, endDate);
            oq.setString(11, endDate);
            ResultSet result = oq.executeQuery();
            while (result.next())
            	fullyOccupiedRooms.add(result.getString(1));
	    } catch (SQLException e) {
	        System.out.println("Occupied Query Failed.");
	        return fullyOccupiedRooms;
	    }
	    return fullyOccupiedRooms;
	}

	/* Handles OR-2 */
	private void reservationMonthByMonthQuery(String query) {
	    String daysCountsQuery = 
	        "select *  " +
	        "from " +
	        "((select roomname, sum(checkout - checkin) as JAN " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JAN' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(checkout - checkin) as FEB " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'FEB' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(checkout - checkin) as MAR " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'MAR' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(checkout - checkin) as APR " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'APR' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(checkout - checkin) as MAY " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'MAY' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(checkout - checkin) as JUN " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JUN' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(checkout - checkin) as JUL " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JUL' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(checkout - checkin) as AUG " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'AUG' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(checkout - checkin) as SEP " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'SEP' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(checkout - checkin) as OCT " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'OCT' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(checkout - checkin) as NOV " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'NOV' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(checkout - checkin) as DEC " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'DEC' " +
	        "group by roomname) " +
	        "NATURAL JOIN  " +
	        "(select roomname, sum(checkout - checkin) as Total " +
	        "from reservations, rooms " +
	        "where rooms.roomid = reservations.room " +
	        "group by roomname))";
	    String reservationCountsQuery = "select *  " +
	        "from " +
	        "((select roomname, count(*) as JAN " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JAN' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, count(*) as FEB " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'FEB' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, count(*) as MAR " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'MAR' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, count(*) as APR " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'APR' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, count(*) as MAY " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'MAY' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, count(*) as JUN " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JUN' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, count(*) as JUL " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JUL' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, count(*) as AUG " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'AUG' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, count(*) as SEP " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'SEP' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, count(*) as OCT " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'OCT' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, count(*) as NOV " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'NOV' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, count(*) as DEC " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'DEC' " +
	        "group by roomname) " +
	        "NATURAL JOIN  " +
	        "(select roomname, count(*) as Total " +
	        "from reservations, rooms " +
	        "where rooms.roomid = reservations.room " +
	        "group by roomname))";

	    String revenuesQuery = "select *  " +
	        "from " +
	        "((select roomname, sum(rate * (checkout - checkin)) as JAN " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JAN' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(rate * (checkout - checkin)) as FEB " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'FEB' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(rate * (checkout - checkin)) as MAR " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'MAR' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(rate * (checkout - checkin)) as APR " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'APR' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(rate * (checkout - checkin)) as MAY " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'MAY' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(rate * (checkout - checkin)) as JUN " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JUN' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(rate * (checkout - checkin)) as JUL " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'JUL' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(rate * (checkout - checkin)) as AUG " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'AUG' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(rate * (checkout - checkin)) as SEP " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'SEP' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(rate * (checkout - checkin)) as OCT " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'OCT' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(rate * (checkout - checkin)) as NOV " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'NOV' " +
	        "group by roomname) " +
	        "NATURAL JOIN " +
	        "(select roomname, sum(rate * (checkout - checkin)) as DEC " +
	        "from reservations, rooms  " +
	        "where rooms.roomid = reservations.room and to_char(checkout, 'MON') = 'DEC' " +
	        "group by roomname) " +
	        "NATURAL JOIN  " +
	        "(select roomname, sum(rate * (checkout - checkin)) as Total " +
	        "from reservations, rooms " +
	        "where rooms.roomid = reservations.room " +
	        "group by roomname))";
	    
	    Vector<Vector<String>> table = new Vector<Vector<String>>();
	    Vector<String> columnHeaders = new Vector<String>();
	    
	    try {
	        String queryToExecute;
	        switch(query) {
	            case "counts":
	                queryToExecute = reservationCountsQuery;
	                break;
	            case "days":
	                queryToExecute = daysCountsQuery;
	                break;
	            default:
	                queryToExecute = revenuesQuery;
	                break;

	        }
	        Statement s = conn.createStatement();
	        ResultSet results = s.executeQuery(queryToExecute);
	       
	        String[] headers = {"Roomname", "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL",
	                    "AUG", "SEP", "OCT", "NOV", "DEC", "TOTAL"};
	        
	        columnHeaders.addAll(Arrays.asList(headers));



	        boolean hasNext = results.next();
	        while(hasNext) {
	            Vector<String> row = new Vector<String>();
	            for(int column = 1; column < 15; column++) {
	                row.addElement(results.getString(column));
	            }
	            table.addElement(row);
	            hasNext = results.next();
	        }        
	        
	        Vector<String> totals = new Vector<String>();
	        totals.addElement("Totals");
	        for(int column = 1; column < 14; column++) {
	        	Double sum = 0.0;
	        	for (int row = 0; row < table.size(); row++)
	        		sum += Double.parseDouble(table.get(row).get(column));
	        	totals.addElement(sum.toString());
	        }
	        table.addElement(totals);
	    } catch (SQLException e) {
	        System.out.println("Reservation counts query failed.");
	        return;
	    }
	    
	    tableOwnerRight.setModel(new DefaultTableModel(
	    		table,
	    		columnHeaders
			));
		
	    tableOwnerRightTca.adjustColumns();

	}


	/* Handles OR-3 */
	private void browseReservationsQuery(String startDate, String endDate, String room) {
	    String queryToExecute;
	    
	    if (room == null && startDate == null)
	    	return;
	    
	    if (startDate != null && endDate == null)
	    	return;
	    
	    if(room == null) {
	        queryToExecute = "select code, lastname, to_char(checkin, 'DD-MON-YY'), to_char(checkout, 'DD-MON-YY') " +
	            "from reservations " +
	            "where checkin >= to_date(? ,'DD-MON-YY') and " +
	            "checkin < to_date(?, 'DD-MON-YY')";
	    } else {
	        if(startDate == null) {
	            queryToExecute = "select code, lastname, to_char(checkin, 'DD-MON-YY'), to_char(checkout, 'DD-MON-YY') " +
	                "from reservations " +
	                "where room = ?";
	        }
	        else {
	            queryToExecute = "select code, lastname, to_char(checkin, 'DD-MON-YY'), to_char(checkout, 'DD-MON-YY') " +
	                "from reservations " +
	                "where checkin >= to_date(? ,'DD-MON-YY') and " + 
	                "checkin < to_date(?, 'DD-MON-YY') and " + 
	                "room = ?";
	        }
	    }

	    Vector<String> columnHeaders = new Vector<String>();
	    Vector<Vector<String>> table = new Vector<Vector<String>>();
	    
	    try {
	        PreparedStatement ps = conn.prepareStatement(queryToExecute);
	        if(room == null) {
	            ps.setString(1, startDate);
	            ps.setString(2, endDate);
	        } else if(startDate == null) {
	            ps.setString(1, room);
	        } else {
	            ps.setString(1, startDate);
	            ps.setString(2, endDate);
	            ps.setString(3, room);
	        }
	        ResultSet results = ps.executeQuery();
	        String[] headers = {"Room", "LastName", "CheckIn", "CheckOut"};
	        
	        columnHeaders.addAll(Arrays.asList(headers));
	        
	        boolean hasNext = results.next();
	        while (hasNext) {
	            Vector<String> row = new Vector<String>();
	            row.addElement(results.getString(1));
	            row.addElement(results.getString(2));
	            row.addElement(results.getString(3));
	            row.addElement(results.getString(4));
	            table.addElement(row);
	            hasNext = results.next();
	        }
	    } catch (SQLException e) {
	        System.out.println("Reservations query failed.");
	        return;
	    }
	    
	    tableOwnerRight.setModel(new DefaultTableModel(
	    		table,
				columnHeaders
			));
		
		tableOwnerRightTca.adjustColumns();
	}
}

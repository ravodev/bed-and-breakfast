import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import javax.swing.ButtonModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.PatternSyntaxException;


public class ReservationCompletionForm extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField tfName;
	private JTextField tfNumAdults;
	private JTextField tfNumKids;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private InnReservations parent;
	private JRadioButton rdbtnAarp, rdbtnNone, rdbtnAAA;

	/**
	 * Create the dialog.
	 * @param window 
	 */
	public ReservationCompletionForm(InnReservations window) {
		super(window.frmBedBreakfast);
		this.parent = window;
		this.setModal(true);
		setResizable(false);
		setTitle("Reservation Completion Form");
		setBounds(100, 100, 406, 243);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.MIN_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.MIN_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.MIN_COLSPEC,},
			new RowSpec[] {
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
		{
			JLabel lblNamefirstLast = new JLabel("Name (first, last):");
			contentPanel.add(lblNamefirstLast, "10, 6, right, default");
		}
		{
			tfName = new JTextField();
			contentPanel.add(tfName, "12, 6, 5, 1, fill, default");
			tfName.setColumns(10);
		}
		{
			JLabel lblNewLabel = new JLabel("# of Adults:");
			contentPanel.add(lblNewLabel, "10, 8, right, default");
		}
		{
			tfNumAdults = new JTextField();
			contentPanel.add(tfNumAdults, "12, 8, 5, 1, fill, default");
			tfNumAdults.setColumns(10);
		}
		{
			JLabel lblNewLabel_1 = new JLabel("# of Kids:");
			contentPanel.add(lblNewLabel_1, "10, 10, right, default");
		}
		{
			tfNumKids = new JTextField();
			contentPanel.add(tfNumKids, "12, 10, 5, 1, fill, default");
			tfNumKids.setColumns(10);
		}
		{
			JLabel lblDiscounts = new JLabel("Discounts");
			contentPanel.add(lblDiscounts, "10, 12");
		}
		{
			rdbtnNone = new JRadioButton("None");
			buttonGroup.add(rdbtnNone);
			rdbtnNone.setSelected(true);
			contentPanel.add(rdbtnNone, "12, 12");
		}
		{
			rdbtnAAA = new JRadioButton("AAA");
			buttonGroup.add(rdbtnAAA);
			contentPanel.add(rdbtnAAA, "14, 12");
		}
		{
			rdbtnAarp = new JRadioButton("AARP");
			buttonGroup.add(rdbtnAarp);
			contentPanel.add(rdbtnAarp, "16, 12");
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton btnPlaceReservation = new JButton("Place Reservation");
				btnPlaceReservation.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int numKids, numAdults;
						String rm = (String) parent.tblGuestOutputPanel.getValueAt(
								parent.tblGuestOutputPanel.getSelectionModel().getLeadSelectionIndex(),
								0);
						String ci = parent.textFieldGuestOutputPanelHeaderCheckin.getText();
						String co = parent.textFieldGuestOutputPanelHeaderCheckOut.getText();
						
						if (tfName.getText().length() == 0) {
							JOptionPane.showMessageDialog(null, "Name cannot be empty.");
							return;
						}
						
						String[] firstLast;
						try {
							firstLast = tfName.getText().split("\\s+");
							if (firstLast.length != 2) {
								JOptionPane.showMessageDialog(null, "Must provide first and last name.");
								return;
							}
						}
						catch (PatternSyntaxException e1) {
							JOptionPane.showMessageDialog(null, "Must provide first and last name.");
							return;
						}
						
						try {
							numKids = Integer.parseInt(tfNumKids.getText());
						}
						catch (NumberFormatException e1) {
							JOptionPane.showMessageDialog(null, "Invalid number of kids.");
							return;
						}
						
						try {
							numAdults = Integer.parseInt(tfNumAdults.getText());
						}
						catch (NumberFormatException e1) {
							JOptionPane.showMessageDialog(null, "Invalid number of adults.");
							return;
						}
						
						if (numAdults < 0 || numKids < 0) {
							JOptionPane.showMessageDialog(null, "Cannot use negative numbers.");
							return;
						}
						
						int maxOccupancy = getMaxOccupancy();
						
						if (numKids + numAdults > maxOccupancy) {
							JOptionPane.showMessageDialog(null, "Room doesn't support that many people.");
							return;
						}
						
						double discount;
						if (rdbtnNone.isSelected())
							discount = 0.0;
						else if (rdbtnAarp.isSelected())
							discount = 0.15;
						else
							discount = 0.10;
						
						placeReservation(firstLast[0], firstLast[1], numAdults, numKids, rm, ci, co, discount);
					}
				});
				btnPlaceReservation.setActionCommand("OK");
				buttonPane.add(btnPlaceReservation);
				getRootPane().setDefaultButton(btnPlaceReservation);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						closeWindow();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	protected void placeReservation(String first, String last, int numAdults, int numKids,
			                        String rm, String ci, String co, double discount) {
		int reservationCode = generateReservationCode();
		double maxRate = getMaxRate();
		String q = "INSERT INTO Reservations " +
	               "VALUES (?, ?, TO_DATE(?), TO_DATE(?), ?, ?, ?, ?, ?)";
		
		try {
			PreparedStatement ps = parent.conn.prepareStatement(q);
			ps.setInt(1, reservationCode);
			ps.setString(2, rm);
			ps.setString(3, ci);
			ps.setString(4, co);
			ps.setFloat(5, (float) (maxRate - (maxRate * discount)));
			ps.setString(6, last);
			ps.setString(7, first);
			ps.setInt(8, numAdults);
			ps.setInt(9, numKids);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.err.println(e);
			e.printStackTrace();
		}
		
		
		JOptionPane.showMessageDialog(null, "Your reservation is complete!");
		closeWindow();
	}

	private double getMaxRate() {
		String start = parent.textFieldGuestOutputPanelHeaderCheckin.getText();
		String end = parent.textFieldGuestOutputPanelHeaderCheckOut.getText();
		String rm = (String) parent.tblGuestOutputPanel.getValueAt(
				parent.tblGuestOutputPanel.getSelectionModel().getLeadSelectionIndex(),
				0);
		String query = String.format(
				"SELECT MAX(CASE " +
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
				"      END) " +
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
				"                              d.curdate between r.checkin and r.checkout-1) ", start, end, start, rm, start, end, start, rm);
		
		try {
			Statement s = parent.conn.createStatement();
			ResultSet rs = s.executeQuery(query);
			if (!rs.next()) throw new SQLException();
			return rs.getFloat(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}

	private int generateReservationCode() {
		String q = "SELECT x.num " +
		           "FROM (SELECT TRUNC(dbms_random.value(100000, 999999)) num " + 
				   "FROM dual) x " +
		           "WHERE x.num NOT IN (SELECT code " +
				                       "FROM reservations)";
		
		Statement s;
		try {
			s = parent.conn.createStatement();
			ResultSet rs = s.executeQuery(q);
			if (!rs.next()) throw new SQLException();
			return rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}

	protected int getMaxOccupancy() {
		String rm = (String) parent.tblGuestOutputPanel.getValueAt(
				parent.tblGuestOutputPanel.getSelectionModel().getLeadSelectionIndex(),
				0);
		
		String q = "SELECT maxOccupancy FROM Rooms WHERE RoomId = ?";
		
		try {
			PreparedStatement ps = parent.conn.prepareStatement(q);
			ps.setString(1, rm);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				return rs.getInt(1);
			else
				throw new SQLException();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return 0;
	}

	protected void closeWindow() {
		this.dispose();
	}

}

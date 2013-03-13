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

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class ReservationCompletionForm extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private final ButtonGroup buttonGroup = new ButtonGroup();

	/**
	 * Create the dialog.
	 * @param frmBedBreakfast 
	 */
	public ReservationCompletionForm(JFrame frmBedBreakfast) {
		super(frmBedBreakfast);
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
			textField = new JTextField();
			contentPanel.add(textField, "12, 6, 5, 1, fill, default");
			textField.setColumns(10);
		}
		{
			JLabel lblNewLabel = new JLabel("# of Adults:");
			contentPanel.add(lblNewLabel, "10, 8, right, default");
		}
		{
			textField_1 = new JTextField();
			contentPanel.add(textField_1, "12, 8, 5, 1, fill, default");
			textField_1.setColumns(10);
		}
		{
			JLabel lblNewLabel_1 = new JLabel("# of Kids:");
			contentPanel.add(lblNewLabel_1, "10, 10, right, default");
		}
		{
			textField_2 = new JTextField();
			contentPanel.add(textField_2, "12, 10, 5, 1, fill, default");
			textField_2.setColumns(10);
		}
		{
			JLabel lblDiscounts = new JLabel("Discounts");
			contentPanel.add(lblDiscounts, "10, 12");
		}
		{
			JRadioButton rdbtnNone = new JRadioButton("None");
			buttonGroup.add(rdbtnNone);
			rdbtnNone.setSelected(true);
			contentPanel.add(rdbtnNone, "12, 12");
		}
		{
			JRadioButton rdbtnNewRadioButton = new JRadioButton("AAA");
			buttonGroup.add(rdbtnNewRadioButton);
			contentPanel.add(rdbtnNewRadioButton, "14, 12");
		}
		{
			JRadioButton rdbtnAarp = new JRadioButton("AARP");
			buttonGroup.add(rdbtnAarp);
			contentPanel.add(rdbtnAarp, "16, 12");
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Place Reservation");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

}

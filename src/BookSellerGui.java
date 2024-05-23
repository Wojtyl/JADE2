package jadelab2;

import jade.core.AID;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class BookSellerGui extends JFrame {	
	private BookSellerAgent myAgent;
	
	private JTextField titleField, priceField, shippingPriceField;
	
	BookSellerGui(BookSellerAgent a) {
		super(a.getLocalName());
		
		myAgent = a;

		//[2]: Extended Gtui with one more row
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(3, 2));

		p.add(new JLabel("Title:"));
		titleField = new JTextField(15);
		p.add(titleField);

		p.add(new JLabel("Price:"));
		priceField = new JTextField(15);
		p.add(priceField);

		// [3]: Added new shipping price field
		p.add(new JLabel("Shipping price:"));
		shippingPriceField = new JTextField(15);
		p.add(shippingPriceField);

		getContentPane().add(p, BorderLayout.CENTER);
		
		JButton addButton = new JButton("Add");
		addButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					String title = titleField.getText().trim();
					String price = priceField.getText().trim();
					// [4]: Read shipping price field value and add it to catalogue update method
					String shippingPrice = shippingPriceField.getText().trim();
					myAgent.updateCatalogue(title, Integer.parseInt(price), Integer.parseInt(shippingPrice));
					titleField.setText("");
					priceField.setText("");
					shippingPriceField.setText("");
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(BookSellerGui.this, "Invalid values. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
				}
			}
		} );
		p = new JPanel();
		p.add(addButton);
		getContentPane().add(p, BorderLayout.SOUTH);
		
		addWindowListener(new	WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();
			}
		} );
		
		setResizable(false);
	}
	
	public void display() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		setVisible(true);
	}	
}

package jadelab2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class BookBuyerBudgetGui extends JFrame {
    private BookBuyerAgent myAgent;

    private JTextField moneyField;

    BookBuyerBudgetGui(BookBuyerAgent a) {

        super(a.getLocalName());

        myAgent = a;

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(2, 2));
        p.add(new JLabel("Amount:"));
        moneyField = new JTextField(15);
        p.add(moneyField);
        getContentPane().add(p, BorderLayout.CENTER);

        JButton addButton = new JButton("Add money");
        addButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    String amount = moneyField.getText().trim();
                    myAgent.addMoney(Integer.parseInt(amount));
                    closeWindow();
                    moneyField.setText("");
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(BookBuyerBudgetGui.this, "Invalid values. " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

    public void closeWindow() {
        setVisible(false);
    }
}


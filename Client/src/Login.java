import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;


import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Login extends JDialog{
	private JTextField IDField;
	public String player_ID;
	
	public Login(){
		
		setTitle("게임 로그인");
		setBounds(100,100, 270, 150);
		getContentPane().setLayout(null);
		
		IDField = new JTextField();
		IDField.setBounds(99, 37, 116, 21);
		getContentPane().add(IDField);
		IDField.setColumns(10);
		
		JLabel IDLabel = new JLabel(" I D : ");
		IDLabel.setBounds(30,40,70,15);
		getContentPane().add(IDLabel);

		JButton newInfoBtn = new JButton("LOGIN");
		newInfoBtn.addActionListener(new IDListener());
		newInfoBtn.setBounds(46, 70, 158, 23);
		getContentPane().add(newInfoBtn);
	}
	
	private class IDListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			try{
				String ID = IDField.getText();
				if(ID.length()>6)
					throw new Exception("ID는 6자 이내로 입력해주세요");
				if(ID.length() ==0)
					throw new Exception("ID를 입력해주세요");
		
				//player_ID = new String(ID);
				Game.player_ID = new String(ID);
			
				if(ID != null){
					JOptionPane.showMessageDialog(null, "등록 되었습니다.");
					setVisible(false);
				}
			}
			catch(Exception err) {
				JOptionPane.showMessageDialog(null, err.getMessage());
			}	
		}
	}
}

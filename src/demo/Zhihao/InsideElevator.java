package demo.Zhihao;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

class InsideElevator extends JFrame implements ActionListener{

	private JButton[] eleTasks = new JButton[ElevatorGUI.MAX_FLOOR];
	private JLabel eleName = new JLabel();
	private JLabel currFloor = new JLabel("Current floor is 1");
	private String eleNumber = null;
	private ElevatorDispatcher delegate = null;
	
		
	public InsideElevator(String eleNo, ElevatorDispatcher deleg)
	{
		
		delegate = deleg;
		eleNumber = eleNo;
		this.setLayout(new GridLayout(2, 1, 5, 5));
		JPanel information = new JPanel(new GridLayout(1, 4));
		JButton emergency = new JButton("Emergency!");
		JButton open = new JButton("Open");
		JButton close = new JButton("Close");
		open.addActionListener(this);
		close.addActionListener(this);
		emergency.addActionListener(this);
		information.add(emergency);
		eleName.setText("This is Elevator No." + eleNo);
		eleName.setHorizontalAlignment(SwingConstants.CENTER);
		currFloor.setHorizontalAlignment(SwingConstants.CENTER);
		information.add(eleName);
		//currFloor.setHorizontalAlignment(SwingConstants.RIGHT);
		information.add(currFloor);
		JPanel oc = new JPanel(new GridLayout(2, 1, 5, 5));
		oc.add(open);
		oc.add(close);
		information.add(oc);
		
		this.add(information);
		
		JPanel tasks = new JPanel(new GridLayout(2, 10, 2, 2));
		for (int i = 0; i < ElevatorGUI.MAX_FLOOR; i++)
		{
			eleTasks[i] = new JButton(Integer.toString(i+1));
			eleTasks[i].addActionListener(this);
			tasks.add(eleTasks[i]);
		}
		this.add(tasks);
		
		super.setTitle("Inside Elevator Panel No." + eleNumber);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setLocation(100, 50);
		this.setSize(600, 200);
		this.setResizable(false);
		//this.setVisible(true);
		toFront();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals("Emergency!"))
		{
			int pos = delegate.getElevCurrPos(eleNumber);
			delegate.emergency(eleNumber);
			for (int i = 1; i < pos; i++)
			{
				delegate.addInsideTask(eleNumber, Integer.toString(i));
				// Here to use the absolute floor number instead of relative floor number
				// because in the delegate.addinsideTask() the floor number is decreased.
				eleTasks[i - 1].setEnabled(false);
			}
			return;
		}
		if (e.getActionCommand().equals("Open"))
		{
			delegate.openDoor(Integer.parseInt(eleNumber));
			return;
		}
		if (e.getActionCommand().equals("Close"))
		{
			delegate.closeDoor(Integer.parseInt(eleNumber));
			return;
		}
		
		delegate.addInsideTask(eleNumber, e.getActionCommand());
		eleTasks[Integer.parseInt(e.getActionCommand()) - 1].setEnabled(false);
		ElevatorDispatcher.getGUI().appendStatus("[Elevator NO." + eleNumber +"]:inside selected " + e.getActionCommand() + "\n");
	}
	
	public void rmvTask(String tsk)
	{
		eleTasks[Integer.parseInt(tsk)].setEnabled(true);
		// NOTICE: Here we don't need to use (tsk-1) because in the Elevator.checkCurrentPosSync()
		// we pass the (currentPos - 1).
		//System.out.println("tsk - 1 = " + (Integer.parseInt(tsk)-1));
	}
	
	public void setFloor(int floor)
	{
		currFloor.setText("Current floor is " + floor);
	}
	
}

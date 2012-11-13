package demo.Zhihao;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ElevatorGUI extends JFrame implements ActionListener{

	public final static int MAX_FLOOR = 20;
	public final static int ELEV_NUM = 5;
	/**
	 * @param args
	 */
	
	private JTextArea statusArea = new JTextArea("==Elevator Dispatcher==\n");
	private JButton[] elevators = new JButton[ELEV_NUM];
	private JButton[] floorUp = new JButton[MAX_FLOOR - 1];
	private JButton[] floorDown = new JButton[MAX_FLOOR - 1];
	private JMenuItem[] recover = new JMenuItem[ELEV_NUM];
	private Hashtable<String ,InsideElevator> reuseableInsideWindows = 
			new Hashtable<String ,InsideElevator>();
	private ElevatorDispatcher delegate = null; // delegate to deal with the elevator issue.
	

	
	public ElevatorGUI(ElevatorDispatcher deleg)
	{
		super("Elevator Dispatcher");
		this.delegate = deleg;
		
		statusArea.setSize(200, 0);
		Calendar cal = Calendar.getInstance();
		Date date = cal.getTime();
		statusArea.append(date.toString() + "\n");
		statusArea.setEditable(false);
		
		//Menu Bar
		JMenuBar menuBar = new JMenuBar();
		JMenu aboutMenu = new JMenu("About");
		JMenuItem helpItem = new JMenuItem("Help");
		JMenuItem aboutItem = new JMenuItem("About");
		JLabel title = new JLabel("ElevatorDispatcher");
		
		helpItem.addActionListener(this);
		aboutItem.addActionListener(this);
		
		JMenu controlMenu = new JMenu("Control");
		for (int i = 0; i < ELEV_NUM; i++)
		{
			recover[i] = new JMenuItem("Recover Elevator No."+i);
			recover[i].setActionCommand("R"+i);
			recover[i].addActionListener(this);
			recover[i].setEnabled(false);
			controlMenu.add(recover[i]);
		}
		
		aboutMenu.add(helpItem);
		aboutMenu.add(aboutItem);
		menuBar.add(title);
		menuBar.add(controlMenu);
		menuBar.add(aboutMenu);
		this.add(menuBar, BorderLayout.NORTH);
		
		//Function Pane
		JPanel funcPanel = new JPanel();
		funcPanel.setLayout(null);
		JPanel dispPanel = new JPanel();
		dispPanel.setLayout(null);
		dispPanel.setBounds(0, 0, 850, 597);
		funcPanel.add(dispPanel);
		JScrollPane scrollPane = new JScrollPane(statusArea);
		scrollPane.setBounds(850, 0, 150, 597);
		scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		funcPanel.add(scrollPane);

		this.add(funcPanel);
		
		//Elevator Animation with Button
		JPanel animPanel = new JPanel();
		animPanel.setLayout(null);
		for (int i = 0; i < ELEV_NUM; i++)
		{			
			elevators[i] = new JButton(Integer.toString(i));
			elevators[i].setBounds(170*i, 0, 170, 450);
			elevators[i].addActionListener(this);
			animPanel.add(elevators[i]);
		}
		animPanel.setBounds(0, 0, 850, 450);
		dispPanel.add(animPanel);
		
		
		//Floor Buttons
		JPanel floorButtonPanel = new JPanel();
		floorButtonPanel.setLayout(null);
		for (int i = 0; i < MAX_FLOOR; i++)
		{
			JLabel lab = new JLabel(Integer.toString(i + 1));
			lab.setBounds(5 + 42*i, 0, 42, 20); //5 px indentation
			lab.setHorizontalAlignment(SwingConstants.CENTER);
			floorButtonPanel.add(lab);
		}
		JLabel downLabel = new JLabel("Down");
		downLabel.setBounds(5, 20, 42, 60);
		downLabel.setHorizontalAlignment(SwingConstants.CENTER);
		floorButtonPanel.add(downLabel);
		for (int i = 0; i < MAX_FLOOR - 1; i++)
		{
			floorDown[i] = new JButton();
			floorDown[i].setBounds(47 + 42 * i, 20, 42, 60);
			// 47 stands for 5 + 42, 5 px is the indentation and 42 is the width of a button.
			// Notice: Down button begins to appear from the 2nd floor,
			// but the array begins at index 0.
			floorDown[i].setActionCommand("D"+i);
			floorButtonPanel.add(floorDown[i]);
			floorDown[i].addActionListener(delegate);
		}
		for (int i = 0; i < MAX_FLOOR - 1; i++)
		{
			floorUp[i] = new JButton();
			floorUp[i].setBounds(5 + 42 * i, 82, 42, 60);
			// Notice: Up button ends appearing at the last but one floor.
			floorUp[i].setActionCommand("U"+i);
			floorButtonPanel.add(floorUp[i]);
			floorUp[i].addActionListener(delegate);
		}
		JLabel upLabel = new JLabel("Up");
		upLabel.setBounds(850 - 42 - 5, 82, 42, 60);
		upLabel.setHorizontalAlignment(SwingConstants.CENTER);
		floorButtonPanel.add(upLabel);
		floorButtonPanel.setBounds(0, 450, 850, 200);
		dispPanel.add(floorButtonPanel);
		
				
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocation(100, 50);
		this.setSize(1000, 650);
		this.setResizable(false);
		this.setVisible(true);
		toFront();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		//System.out.println(cmd);
		if (cmd.equals("About"))
		{
			JOptionPane.showMessageDialog(null, "Elevator Dispatcher\n      Copyleft Li Zhihao No.1152691\n" +
					"      Source code can be distributed freely but not for commercial use.", "About Elevator Dispatcher", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (cmd.equals("Help"))
		{
			JOptionPane.showMessageDialog(null, "By clicking each elevator you can get into it and add tasks inside.\n" +
					"If an emergency occurred, press Emergency! button inside the elevators.\n" +
					"To recover an elevator from emergency status, choose from the Control Menu.\n" +
					"For other help, please refer to the documentation.", "What to say in Help?", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (cmd.charAt(0) == 'R')
		{
			enableElevator(true, Integer.parseInt(cmd.substring(1)));
			delegate.setEmergencyState(false, cmd.substring(1));
			return;
		}
		if (reuseableInsideWindows.containsKey(cmd))
		{
			reuseableInsideWindows.get(cmd).setVisible(true);
		}
		else 
		{
			InsideElevator ele = new InsideElevator(cmd, delegate);
			reuseableInsideWindows.put(cmd, ele);
			ele.setVisible(true);
		}
	}
	
	public void rmvInsideTask(String eleNo, String tsk)
	{
		if (reuseableInsideWindows.containsKey(eleNo))
		{
			reuseableInsideWindows.get(eleNo).rmvTask(tsk);
			// Never use uncreated windows.
			//System.out.println("reuseable " + eleNo + " " + tsk);
		}
	}
	
	public void setInsideFloor(String eleNo, int floor)
	{
		if (reuseableInsideWindows.containsKey(eleNo))
		{
			reuseableInsideWindows.get(eleNo).setFloor(floor);
			// Never use uncreated windows.
		}

	}
	
	public void setEleAnimation(Elevator anim, int eleNo)
	{
		elevators[eleNo].add(anim);
	}
	
	public void appendStatus(String status)
	{
		statusArea.append(status);
	}
	
	public void setEnableFloorUp(int i, boolean en)
	{
		floorUp[i].setEnabled(en);
	}
	
	public void setEnableFloorDown(int i, boolean en)
	{
		floorDown[i].setEnabled(en);
	}

	public void enableUpDownButton(Direction dir, int index)
	// index begins with 0
	{
		if (dir == Direction.UP)
			floorUp[index].setEnabled(true);
		else if (dir == Direction.DOWN)
			floorDown[index].setEnabled(true);
	}
	
	public void enableElevator(boolean en, int num)
	{
		elevators[num].setEnabled(en);
		recover[num].setEnabled(en?false:true);
		if (reuseableInsideWindows.containsKey(Integer.toString(num)))
		{
			reuseableInsideWindows.get(Integer.toString(num)).setVisible(false);
			// Never use uncreated windows.
		}
	}
}

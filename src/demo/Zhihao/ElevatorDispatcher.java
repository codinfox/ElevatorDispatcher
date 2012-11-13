package demo.Zhihao;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ElevatorDispatcher implements ActionListener{
	
	private static Elevator[] elevators = new Elevator[ElevatorGUI.ELEV_NUM];
	private static ElevatorDispatcher dispatcher = new ElevatorDispatcher();
	private static ElevatorGUI eleGUI = new ElevatorGUI(ElevatorDispatcher.getInstance());
	private static ArrayDeque<String> taskList = new ArrayDeque<String>();
	private ReadWriteLock taskLock = new ReadWriteLock();
	private Hashtable<String, Direction> outsideTask = new Hashtable<String, Direction>(); 
	
//	private class IndexElev
//	{
//		public Elevator ele = null;;
//		public int floor = 0;
//		
//		public IndexElev(Elevator elev, int flr)
//		{
//			ele = elev;
//			floor = flr;
//		}
//	}
	
	private ElevatorDispatcher()
	{
		for (int i = 0; i < ElevatorGUI.ELEV_NUM; i++)
		{
			elevators[i] = new Elevator(Integer.toString(i));
			new Thread(elevators[i]).start();
		}
		new Thread(new TaskDispatcher()).start();
	}
	
	public static ElevatorDispatcher getInstance()
	{
		return dispatcher;
	}
	
	public static ElevatorGUI getGUI()
	{
		return eleGUI;
	}
	
	public void addInsideTask(String eleNo, String pos)
	{
		elevators[Integer.parseInt(eleNo)].addTask(Integer.parseInt(pos) - 1);
	}
	
	public void emergency(String eleNo)
	{
		if (elevators[Integer.parseInt(eleNo)].getPos() != 1) 
			elevators[Integer.parseInt(eleNo)].setHeadForDown();
		elevators[Integer.parseInt(eleNo)].clearTasks();
		elevators[Integer.parseInt(eleNo)].setEmergency(true);
		eleGUI.enableElevator(false, Integer.parseInt(eleNo));
	}
	
	public int getElevCurrPos(String eleNo)
	{
		return elevators[Integer.parseInt(eleNo)].getPos();
	}
	
	public void closeDoor(int i)
	{
		elevators[i].closeDoor();
	}
	
	public void openDoor(int i)
	{
		elevators[i].openDoor();
	}
		
	public static void main(String[] args) 
	{
		for (int i = 0; i < ElevatorGUI.ELEV_NUM; i++)
			eleGUI.setEleAnimation(elevators[i], i);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// NOTICE: the floorDown buttons are <Ux> (x is for the floor number), vice versa
		//         floorDown buttons begins at 0 (which represent 2nd floor)
		//         floorUp buttons begins at 0 (which represent 1st floor)
		String cmd = e.getActionCommand();
		if (cmd.charAt(0) == 'U')
			eleGUI.setEnableFloorUp(Integer.parseInt(cmd.substring(1)), false);
		else if (cmd.charAt(0) == 'D')
			eleGUI.setEnableFloorDown(Integer.parseInt(cmd.substring(1)), false);

		if (!performTask(cmd))
		{
			try {
				taskLock.writeLock();
				taskList.addLast(cmd);
				System.out.println("addLast");
				taskLock.writeUnlock();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		e.getActionCommand().substring(1);
		System.out.println(e.getActionCommand());
	}
	
	private boolean performTask(String cmd)
	{
		Elevator choice = null;
		int steps = 1000; // represent infinity.
		if (cmd.charAt(0) == 'U')
		{
			//eleGUI.setEnableFloorUp(Integer.parseInt(cmd.substring(1)), false);
			for (Elevator ele : elevators)
			{
				if (ele.isEmergency()) continue;
				//else System.out.println("Not emer");
				int tmp = ele.stepsTo(Integer.parseInt(cmd.substring(1)) + 1, Direction.UP);
				// System.out.println(tmp);
				if (tmp  != -1 )
				{
					if (tmp < steps) 
					{
						steps = tmp;
						choice = ele;
					}
				}
			}
			if (choice == null)
			{
				return false;
			}
			else
			{
				choice.addTask(Integer.parseInt(cmd.substring(1)));
				outsideTask.put(choice.getEleName()+(Integer.parseInt(cmd.substring(1)) + 1), Direction.UP);
				System.out.println("UP " + (Integer.parseInt(cmd.substring(1)) + 1));
				return true;
			}
		}
		else if (cmd.charAt(0) == 'D')
		{
			//eleGUI.setEnableFloorDown(Integer.parseInt(cmd.substring(1)), false);
			for (Elevator ele : elevators)
			{
				if (ele.isEmergency()) continue;
				int tmp = 0;
				if ((tmp = ele.stepsTo(Integer.parseInt(cmd.substring(1)) + 2, Direction.DOWN)) != -1 )
				{
					if (tmp < steps) 
					{
						steps = tmp;
						choice = ele;
					}
				}
			}
			if (choice == null)
			{
				return false;
			}
			else
			{
				choice.addTask(Integer.parseInt(cmd.substring(1)) + 1);
				outsideTask.put(choice.getEleName()+(Integer.parseInt(cmd.substring(1)) + 2), Direction.DOWN);
				System.out.println("DOWN " + (Integer.parseInt(cmd.substring(1)) + 2));
				System.out.println(choice.getEleName() + " " + outsideTask.get(choice.getEleName()+(Integer.parseInt(cmd.substring(1)) + 2)));
				
				return true;
			}

		}
		return false;
	}
	
	public void rmvOutsideTask(Elevator elev, int floor)
	// floor is the absolute floor.
	{
		System.out.println("rmvOutsideTask ---- " + elev.getEleName() + " " + floor);
		System.out.println(outsideTask.get(elev.getEleName()+floor));
		if (outsideTask.get(elev.getEleName()+floor) == Direction.UP)
		{
			System.out.println("UP" + floor + " floor-1 = " + (floor-1));
			eleGUI.enableUpDownButton(Direction.UP, floor - 1);
			outsideTask.remove(elev.getEleName()+floor);
		}
		else if (outsideTask.get(elev.getEleName()+floor) == Direction.DOWN)
		{
			System.out.println("DOWN" + floor + " floor-2 = " + (floor-2));
			eleGUI.enableUpDownButton(Direction.DOWN, floor - 2);
			outsideTask.remove(elev.getEleName()+floor);
		}
		System.gc();
	}
	
	public void setEmergencyState(boolean eme, String eleNo)
	{
		elevators[Integer.parseInt(eleNo)].setEmergency(eme);
	}

	
	private class TaskDispatcher implements Runnable
	{

		@Override
		public void run() {
			while (true)
			{
				try {
					taskLock.writeLock();
					String tmp = null;
					int count = taskList.size();
					while (count > 0 && !taskList.isEmpty())
					{
						tmp = taskList.getFirst();
						if (performTask(tmp))
							taskList.removeFirst();
						else
						{
							taskList.removeFirst();
							taskList.addLast(tmp);
						}
						count--;
					}
					taskLock.writeUnlock();
					
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		}
		
	}
}

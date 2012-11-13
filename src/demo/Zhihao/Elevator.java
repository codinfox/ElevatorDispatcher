package demo.Zhihao;

import java.util.Scanner;
import java.awt.*;

import javax.swing.*;

enum Direction
{
	UP, DOWN, IDLE;
}

class Elevator extends JPanel implements Runnable
{
	public static final int SLEEP_TIME = 2000;
	
	private class DrawAnimation implements Runnable
	{
		//private Elevator delegate = null;
		private ReadWriteLock destLock = new ReadWriteLock();
		public int y = 418;
		private int dest = 418;
		
		private DrawAnimation(){}

		@Override
		public void run() {
			while (true)
			{
				repaint();
				if (dest > y)
					y++;
				else if (dest < y)
					y--;

				//System.out.println("I am working.");
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void setDest(int floor)
		// this is the absolute floor, begins with 1
		{
			try {
				destLock.writeLock();
				dest = 418 - (floor-1)*22; // each floor is 22px height.
				destLock.writeUnlock();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public boolean isDone()
		{
			boolean rtrn = false;
			
			try {
				destLock.readLock();
				rtrn = (dest == y);
				destLock.readUnlock();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			return rtrn;
		}
		
		public synchronized void sleepFor(long millis)
		{
//			elevColor = Color.RED;
//			repaint();
//			int i = 1000;
			//while (i-- > 0);
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//elevColor = Color.BLACK;
		}
	}
	
	private Direction headFor = Direction.IDLE;
	private int currentPos = 1;
	private String eleNo = null;
	private DrawAnimation draw = null;
	private boolean tasks[] = new boolean[ElevatorGUI.MAX_FLOOR];
	private Color elevColor = Color.BLACK;
	private int open = 0;
	private boolean emergency = false;
	private Direction outSideStatus = Direction.IDLE;
	// tasks[0] is the execute panel, tasks[1] is the direction panel.

	// Read and write locks of the private variables
	public ReadWriteLock dirLock = new ReadWriteLock(); // lock of direction
	public ReadWriteLock posLock = new ReadWriteLock(); // lock of position
	public ReadWriteLock taskLock = new ReadWriteLock(); // lock of task table
	
	public Elevator(String no)
	{
		for (int i = 0; i < ElevatorGUI.MAX_FLOOR; i++)
		{
			tasks[i] = false;
		}
		eleNo = no;
		//tasks[EXE][5] = tasks[EXE][8] = true;
		this.setBackground(Color.WHITE);
		new Thread(draw = new DrawAnimation()).start();
	}
	
	public String getEleName()
	{
		return eleNo;
	}
	
	public void setHeadForDown()
	{
		try {
			dirLock.writeLock();
			headFor = Direction.DOWN;
			dirLock.writeUnlock();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public int getPos()
	{
		int rtrn = 1;
		try {
			posLock.readLock();
			rtrn = currentPos;
			posLock.readUnlock();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return rtrn;
	}
	
	public void clearTasks()
	{
		try {
			taskLock.writeLock();
			for (int i = 0; i < ElevatorGUI.MAX_FLOOR; i++)
			{
				tasks[i] = false;
				ElevatorDispatcher.getGUI().rmvInsideTask(eleNo, Integer.toString(i));
				ElevatorDispatcher.getInstance().rmvOutsideTask(this, i+1);
			}
			taskLock.writeUnlock();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	synchronized boolean checkCurrentPosSync()
	// this method is thread safe
	// WARNING: there is a taskLock.writeLock() here, ensure that no other taskLock
	{
		ElevatorDispatcher.getGUI().setInsideFloor(eleNo, currentPos);
		boolean retrn = false;
		try {
			System.out.print(eleNo + " -- ");
			System.out.println("this is "+currentPos);
			taskLock.writeLock(); //System.out.println("taskLock.writeLock ***checkCurentPosSync");
			posLock.readLock(); //System.out.println("posLock.readLock ***checkCurentPosSync");
			if (tasks[currentPos-1])
			{
				tasks[currentPos-1] = false;
				retrn = true;
			}
			posLock.readUnlock(); //System.out.println("readLock.readUnlock ***checkCurentPosSync");
			taskLock.writeUnlock(); //System.out.println("taskLock.writeUnlock ***checkCurentPosSync");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (retrn)
		{
			System.out.println("I am Here at " + currentPos);
			ElevatorDispatcher.getGUI().appendStatus("[Elevator NO." + eleNo +"]:arrived at " + currentPos + "\n");
			ElevatorDispatcher.getGUI().rmvInsideTask(eleNo, Integer.toString(currentPos - 1));
			ElevatorDispatcher.getInstance().rmvOutsideTask(this, currentPos);
			//System.out.println(eleNo + " " + (currentPos-1));
			//elevColor = Color.RED; System.out.println("elevColor changed to RED");
			//this.repaint(); System.out.println("Color changed to RED and repaint()");
			//elevColor = Color.BLACK;

//			draw.sleepFor(1000);
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			elevColor = Color.RED;
			repaint();
		}
		return retrn;
	}
	
	@Override
	public void run() {

		while (true)
		{
			boolean flag = false;
			boolean go = false;
			try {
				// dirLock.writeLock();
				// Here we don't need a dirLock because this is the only thread which could
				// write the headFor variable.
				switch (headFor)
				{
				case UP: 
					flag = checkCurrentPosSync(); // TODO: Animation to be added.
					go = false;
					if (flag)
					{
						draw.sleepFor(SLEEP_TIME);
						while (open > 0)
						{
							elevColor = Color.RED;
							System.out.println("while open-- > 0");
							draw.sleepFor(1000);
							Thread.sleep(1000);
							open--;
						}

						elevColor = Color.BLACK;

						posLock.readLock(); //System.out.println("posLock.readLock ***switch UP");
						taskLock.readLock(); //System.out.println("taskLock.readLock ***switch UP");
						for (int i = currentPos; i < ElevatorGUI.MAX_FLOOR; i++)
							if (tasks[i]) go = true;
						taskLock.readUnlock(); //System.out.println("taskLock.readUnlock ***switch UP");
						posLock.readUnlock(); //System.out.println("posLock.readUnlock ***switch UP");
						if (!go)
						{
							dirLock.writeLock(); //System.out.println("dirLock.writeLock ***switch UP");
							headFor = Direction.IDLE;
							dirLock.writeUnlock(); //System.out.println("dirLock.writeUnlock ***switch UP");
							break; // get out of the switch block
						}
					}
					//Thread.sleep(2000);
					posLock.readLock();
					draw.setDest(currentPos + 1);
					posLock.readUnlock();
					while (!draw.isDone());// System.out.println("Blocked");
					posLock.writeLock(); //System.out.println("posLock.writeLock ***switch UP");
					currentPos++; // Therefore, the currentPos is the beginning position.
					posLock.writeUnlock(); //System.out.println("posLock.writeUnlock ***switch UP");
					break;
					
				case DOWN:
					flag = checkCurrentPosSync(); // TODO: Animation to be added.
					go = false;
					if (flag)
					{
						draw.sleepFor(SLEEP_TIME);
						while (open > 0)
						{
							elevColor = Color.RED;
							System.out.println("while open-- > 0");
							draw.sleepFor(1000);
							Thread.sleep(1000);
							open--;
						}

						elevColor = Color.BLACK;

						posLock.readLock(); //System.out.println("posLock.readLock ***switch DOWN");
						taskLock.readLock(); //System.out.println("taskLock.readLock ***switch DOWN");
						for (int i = currentPos - 2; i >= 0; i--)
							if (tasks[i]) go = true;
						taskLock.readUnlock(); //System.out.println("taskLock.readUnlock ***switch DOWN");
						posLock.readUnlock(); //System.out.println("posLock.readUnlock ***switch DOWN");
						if (!go)
						{
							dirLock.writeLock(); //System.out.println("dirLock.writeLock ***switch DOWN");
							headFor = Direction.IDLE;
							dirLock.writeUnlock(); //System.out.println("dirLock.writeUnlock ***switch DOWN");
							break;
						}
					}
					//Thread.sleep(2000);
					posLock.readLock();
					draw.setDest(currentPos - 1);
					posLock.readUnlock();
					while (!draw.isDone());// System.out.println("Blocked");
					//Thread.sleep(440);
					posLock.writeLock(); //System.out.println("posLock.writeLock ***switch DOWN");
					currentPos--; // Therefore, the currentPos is the beginning position.
					posLock.writeUnlock(); //System.out.println("posLock.writeUnlock ***switch DOWN");
					break;
				case IDLE:
					taskLock.readLock(); //System.out.println("taskLock.readLock ***switch IDLE");
					for (int i = ElevatorGUI.MAX_FLOOR - 1; i >= 0; i-- )
					{
						
						if (tasks[i])
						{
							posLock.readLock(); //System.out.println("posLock.readLock ***switch IDLE");
							dirLock.writeLock(); //System.out.println("dirLock.writeLock ***switch IDLE");
							//System.out.println(headFor.toString() + " " + currentPos);
							if (i > currentPos - 1)
								headFor = Direction.UP;
							else if (i < currentPos - 1)
								headFor = Direction.DOWN;
							else 
							{
								//System.out.println("checkCurrentPosSync()");
								taskLock.readUnlock();
								if (checkCurrentPosSync())
								{
									draw.sleepFor(SLEEP_TIME);
									while (open > 0)
									{
										elevColor = Color.RED;
										System.out.println("while open-- > 0");
										draw.sleepFor(1000);
										Thread.sleep(1000);
										open--;
									}
									elevColor = Color.BLACK;
								}
								taskLock.readLock();
							}
							dirLock.writeUnlock(); //System.out.println("dirLock.writeUnlock ***switch IDLE");
							posLock.readUnlock(); // although checkCurrentPosSync locked read
							                         //System.out.println("posLock.readUnlock ***switch IDLE");
							break;
						}		
					}
					taskLock.readUnlock(); //System.out.println("taskLock.readUnlock ***switch IDLE");
					Thread.sleep(20); // to relieve the pressure of cpu
					break;
				}
				//dirLock.writeUnlock();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}
	
	public void addTask(int i)
	{
		try {
			taskLock.writeLock();
			tasks[i] = true;
			taskLock.writeUnlock();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public synchronized void paint(Graphics g)
	{
		// Paint area 136*440
		super.paint(g);
		
		//if (elevColor == Color.RED) System.out.println("RED");
		g.setColor(elevColor);
		g.fillRect(0, draw.y, 136, 22);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 15));
		g.drawString(Integer.toString(currentPos), 5, draw.y + 17);
		g.setFont(new Font("Arial", Font.PLAIN, 12));
		g.drawString("Elev."+eleNo, 100, draw.y + 16);
	}
	
	public int stepsTo(int dest, Direction dir)
	// ALGORITHM: search all the tasks to see how many steps it will take to get to the dest
	// Meanwhile the dir of elevator should be the same of the param dir
	{
		try {
			dirLock.readLock();
			posLock.readLock();
			if (dir != headFor && headFor != Direction.IDLE)
			{
				posLock.readUnlock();
				dirLock.readUnlock();

				return -1;
			}
			if (dir == headFor)
			{
				if (headFor == Direction.DOWN)
					if (dest >= currentPos)
					{
						posLock.readUnlock();
						dirLock.readUnlock();

						return -1;
					}
					else 
					{
						posLock.readUnlock();
						dirLock.readUnlock();

						return (currentPos - dest);
					}
				else if (headFor == Direction.UP)
					if (dest <= currentPos)
					{
						posLock.readUnlock();
						dirLock.readUnlock();

						return -1;
					}
					else
					{
						posLock.readUnlock();
						dirLock.readUnlock();

						return (dest - currentPos);
					}
			}
			else if (headFor == Direction.IDLE)
			{
				posLock.readUnlock();
				dirLock.readUnlock();

				return Math.abs(dest - currentPos);
			}
			posLock.readUnlock();
			dirLock.readUnlock();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public void closeDoor()
	{
		elevColor = Color.BLACK;
	}
	
	public void openDoor()
	{
		if (draw.isDone())
		{
			open++;			
			System.out.println("open " + open);
			if (headFor == Direction.IDLE)
			{
				this.addTask(currentPos - 1);
				open--;
			}
		}
	}
	
	public boolean isEmergency()
	{
		return emergency;
	}
	
	public void setEmergency(boolean eme)
	{
		emergency = eme;
	}
	
	public static void main(String[] args)
	{
//		Scanner scan = new Scanner(System.in);
//		Elevator ele = new Elevator("Test");
//		new Thread(ele).start();
//		while (true)
//		{
//			int num = scan.nextInt();
//			ele.addTask(num, true);
//		}
		
		JFrame frame = new JFrame();
		frame.add(new Elevator("Test"));
		frame.setVisible(true);
		frame.setSize(170, 450);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}

package demo.Zhihao;

import java.util.Hashtable;

class Tst implements Runnable
{
	private int aa = 2000;
	private ReadWriteLock lock = new ReadWriteLock();
	public void run()
	{
		while (true)
		{
			try {
				lock.writeLock();
				aa--;
				System.out.printf("%s aa=%d\n", Thread.currentThread().getName(), aa);
				Thread.sleep(2000);
				lock.writeUnlock();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

public class Test {

	public static void main(String[] args)
	{
		Hashtable<String, Integer> a = new Hashtable<String, Integer>();
		a.put("Hello", 2);
		a.get("Hello");
		if (a.get("Hello") != null) System.out.println("Ha");
	}
}

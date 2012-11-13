package demo.Zhihao;

public class ReadWriteLock {
	
	private int readingReaders = 0;
	private int writingWriters = 0;
	private int waitingWriters = 0;

	public synchronized void readLock() throws InterruptedException
	{
	//	System.out.println("ReadLock");
		while (writingWriters >0 || waitingWriters > 0)
			wait();
		readingReaders++;
	}
	
	public synchronized void readUnlock() throws InterruptedException
	{
	//	System.out.println("ReadUnLock");

		readingReaders--;
		if (readingReaders == 0)
			notifyAll();
	}
	
	public synchronized void writeLock() throws InterruptedException
	{
	//	System.out.println("WriteLock");

		waitingWriters++;
		while (writingWriters > 0 || readingReaders > 0)
			wait();
		waitingWriters--;
		writingWriters++;
	}
	
	public synchronized void writeUnlock() throws InterruptedException
	{
	//	System.out.println("WriteUnLock");

		writingWriters--;
		notifyAll();
	}
	
}

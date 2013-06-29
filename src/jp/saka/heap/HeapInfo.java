package jp.saka.heap;

import android.app.ActivityManager;
import android.os.Debug;

public class HeapInfo
{
	public long LinuxHeapSize; // システム全体のメモリ容量
	public long LinuxHeapFreeSize; //空き容量
	public long LowMemoryThreshold; //システムがLowMemory状態となる閾値（LinuxHeapFreeSizeがLowMemoryThresholdを下回るとシステムはlowMemory状態となる）
	public boolean LowMemory; //システムがLowMemory状態かどうかを示すフラグ

	public long NativeHeapSize;
	public long NativeHeapAllocatedSize;
	public long NativeHeapFreeSize;

	public long JavaHeapSize;
	public long JavaHeapAllocatedSize;
	public long JavaHeapFreeSize;

	public long ApplicationHeapSize;
	public long ApplicationHeapAllocatedSize;
	public long ApplicationHeapFreeSize;

	public long ApplicationHeapMaxSize0, ApplicationHeapMaxSize1, ApplicationHeapMaxSize2;

	private ActivityManager.MemoryInfo mMemoryInfo;

	public HeapInfo(ActivityManager am)
	{
		mMemoryInfo = new ActivityManager.MemoryInfo();
		reload(am);
	}

	public static String bytes2str(long num)
	{
		float n = (float)num;
		String str = "???";

		if (n < 0) {
			;
		} else if (n >= 1073741824.0) {
			n /= 1073741824.0;
			str = String.valueOf(n) + " GB";
		} else if (n >= 1048576) {
			n /= 1048576.0;
			str = String.valueOf(n) + " MB";
		} else if (n >= 1024) {
			n /= 1024.0;
			str = String.valueOf(n) + " KB";
		} else {
			str = String.valueOf(n) + " B";
		}

		return str;
	}

	public String toString()
	{
		synchronized (this) {
			return (
					"========================================\n" + 
					"Linux Heap Size                  : " + bytes2str(LinuxHeapSize) + "\n" + 
					"Linux Heap Free Size             : " + bytes2str(LinuxHeapFreeSize) + "\n" + 
					"Low Memory Threshold             : " + bytes2str(LowMemoryThreshold) + "\n" + 
					"Low Memory                       : " + LowMemory + "\n" + 
					"----------------------------------------\n" +
					"Application Heap Max Size        : " + "\n" +
					" Runtime.maxMemory               : " + bytes2str(ApplicationHeapMaxSize0) + "\n" + 
					" ActivityManager.getMemoryClass  : " + bytes2str(ApplicationHeapMaxSize1) + "\n" + 
					"  (large heap)                   : " + bytes2str(ApplicationHeapMaxSize2) + "\n" + 
					"Application Heap Size            : " + bytes2str(ApplicationHeapSize) + "\n" + 
					"Application Heap Allocated Size  : " + bytes2str(ApplicationHeapAllocatedSize) + "\n" + 
					"Application Heap Free Size       : " + bytes2str(ApplicationHeapFreeSize) + "\n" + 
					"----------------------------------------\n" +
					"Native Heap Size                 : " + bytes2str(NativeHeapSize) + "\n" + 
					"Native Heap Allocated Size       : " + bytes2str(NativeHeapAllocatedSize) + "\n" + 
					"Native Heap Free Size            : " + bytes2str(NativeHeapFreeSize) + "\n" + 
					"----------------------------------------\n" +
					"Java Heap Size                   : " + bytes2str(JavaHeapSize) + "\n" + 
					"Java Heap Allocated Size         : " + bytes2str(JavaHeapAllocatedSize) + "\n" + 
					"Java Heap Free Size              : " + bytes2str(JavaHeapFreeSize) + "\n" + 
					"========================================\n"
					);
		}
	}

	public void reload(ActivityManager am)
	{
		synchronized (this) {
			Runtime runtime = Runtime.getRuntime();
			am.getMemoryInfo(mMemoryInfo);	//note that polling is not recommended

			//LinuxHeapSize = mMemoryInfo.totalMem; /*API level 16*/
			LinuxHeapSize = -1;
			LinuxHeapFreeSize = mMemoryInfo.availMem;
			LowMemoryThreshold = mMemoryInfo.threshold;
			LowMemory = mMemoryInfo.lowMemory;

			NativeHeapSize = Debug.getNativeHeapSize();
			NativeHeapAllocatedSize = Debug.getNativeHeapAllocatedSize();
			NativeHeapFreeSize = Debug.getNativeHeapFreeSize();

			JavaHeapSize = runtime.totalMemory();
			JavaHeapAllocatedSize = runtime.totalMemory() - runtime.freeMemory();
			JavaHeapFreeSize = runtime.freeMemory();

			ApplicationHeapSize = JavaHeapSize + NativeHeapSize;
			ApplicationHeapAllocatedSize = JavaHeapAllocatedSize + NativeHeapAllocatedSize;
			ApplicationHeapFreeSize = JavaHeapFreeSize + NativeHeapFreeSize;

			ApplicationHeapMaxSize0 = runtime.maxMemory();
			ApplicationHeapMaxSize1 = (long)am.getMemoryClass() * 1024 * 1024;
			//ApplicationHeapMaxSize2 = (long)am.getLargeMemoryClass() * 1024 * 1024;
			ApplicationHeapMaxSize2 = -1;
		}
	}
}

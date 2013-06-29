package jp.saka.heap;

import android.app.ActivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.CheckBox;

import jp.saka.heap.HeapInfo;

public class HeapMoniter
{
	private Thread mHeapMoniterThread = null;
	private boolean mHeapMoniterRunning = false;
	private int mHeapMoniterInterval = 0;
	private HeapMoniter.Listener mListener = null;
	private Handler mHandler;
	private HeapInfo mHeapInfo;
	private ActivityManager mActivityManager;

	public HeapMoniter(ActivityManager activityManager, Handler handler, int interval)
	{
		mHandler = handler;
		mActivityManager = activityManager;
		mHeapMoniterInterval = interval;
		mHeapInfo = new HeapInfo(mActivityManager);
	}

	public void setListener(HeapMoniter.Listener listener)
	{
		synchronized (this) {
			mListener = listener;
		}
	}

	public boolean isRunning()
	{
		synchronized (this) {
			return mHeapMoniterRunning;
		}
	}

	public boolean start()
	{
		synchronized (this) {

			if (mHeapMoniterRunning) {
				// 実行中
				return mHeapMoniterRunning;
			}

			if (mHeapMoniterThread != null) {
				// 停止中
				return mHeapMoniterRunning;
			} 

			mHeapMoniterThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (mHeapMoniterRunning && mHeapInfo != null && mHandler != null && mActivityManager != null) {
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								synchronized (this) {
									if (mListener != null) {
										mHeapInfo.reload(mActivityManager);
										mListener.onReloadHeapInfo(mHeapInfo);
									}
								}
							}
						});
						if (mHeapMoniterInterval > 0) {
							try {
								Thread.sleep(mHeapMoniterInterval*1000, 0);
							} catch (Exception e) {
								mHeapMoniterRunning = false;
							}
						}
					}
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							synchronized (this) {
								mHeapMoniterThread = null;
								if (mListener != null) {
									mListener.onStopHeapMoniter();
								}
							}
						}
					});
				}
			});

			mHeapMoniterRunning = true;
			mHeapMoniterThread.start();

			return mHeapMoniterRunning;
		}
	}

	public void stop()
	{
		synchronized (this) {
			mHeapMoniterRunning = false;
		}
	}

	public interface Listener
	{
		public void onReloadHeapInfo(HeapInfo heapInfo);
		public void onStopHeapMoniter();
	}

	public void forceReload()
	{
		synchronized (this) {
			if (mHandler != null) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						synchronized (this) {
							if (mListener != null && mHeapInfo != null && mActivityManager != null) {
								mHeapInfo.reload(mActivityManager);
								mListener.onReloadHeapInfo(mHeapInfo);
							}
						}
					}
				});
			}
		}
	}

}


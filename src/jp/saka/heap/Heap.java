package jp.saka.heap;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;

import java.nio.ByteBuffer;

import jp.saka.heap.HeapInfo;
import jp.saka.heap.HeapMoniter;

public class Heap extends Activity implements HeapMoniter.Listener
{
	static {
		System.loadLibrary("malloc");
	}

	public native int alloc(int size);
	public native void free();

	private ActivityManager mActivityManager = null;
	private Handler mHandler = null;
	private HeapMoniter mHeapMoniter = null;
	private CheckBox mEnableHeapMoniterCheckBox = null;
	private TextView mHeapInfoTextView = null;
	private TextView mJavaUsageTextView = null, mNativeUsageTextView = null;
	private Button mJavaNew10MBButton = null, mJavaNew1MBButton = null, mJavaUnrefButton = null, mJavaGCButton = null;
	private Button mNativeNew100MBButton = null, mNativeNew10MBButton = null, mNativeNew1MBButton = null, mNativeFreeButton = null;
	private JavaMemoryBlock mJavaMemoryBlockTop = null, mJavaMemoryBlockButtom = null;

	private int mJavaAllocatedSize = 0;
	private int mNativeAllocatedSize = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mActivityManager = ((ActivityManager)getSystemService(Activity.ACTIVITY_SERVICE));
		mHandler = new Handler();
		mHeapMoniter = new HeapMoniter(mActivityManager, mHandler, 3);
		mHeapMoniter.setListener(this);

		mHeapInfoTextView = (TextView) findViewById(R.id.HeapInfoTextView);
		mEnableHeapMoniterCheckBox = (CheckBox) findViewById(R.id.EnableHeapMoniterCheckBox);
		mJavaUsageTextView = (TextView) findViewById(R.id.JavaUsageTextView);
		mJavaNew10MBButton = (Button)findViewById(R.id.JavaNew10MByteButton);
		mJavaNew1MBButton = (Button)findViewById(R.id.JavaNew1MByteButton);
		mJavaUnrefButton = (Button)findViewById(R.id.JavaUnrefButton);
		mJavaGCButton = (Button)findViewById(R.id.JavaGCButton);
		mNativeUsageTextView = (TextView) findViewById(R.id.NativeUsageTextView);
		mNativeNew100MBButton = (Button)findViewById(R.id.NativeNew100MByteButton);
		mNativeNew10MBButton = (Button)findViewById(R.id.NativeNew10MByteButton);
		mNativeNew1MBButton = (Button)findViewById(R.id.NativeNew1MByteButton);
		mNativeFreeButton = (Button)findViewById(R.id.NativeFreeButton);

		mEnableHeapMoniterCheckBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CheckBox checkBox = (CheckBox) v;
				if (checkBox.isChecked()) {
					startHeapMoniter();
				} else {
					stopHeapMoniter();
				}
			}
		});

		updateEnableHeapMoniterCheckBox();

		updateMemoryUsageTextView();

		mJavaNew10MBButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				allocJavaMemory(10*1024*1024);
				updateMemoryUsageTextView();
			}
		});

		mJavaNew1MBButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				allocJavaMemory(1024*1024);
				updateMemoryUsageTextView();
			}
		});

		mJavaUnrefButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				unrefJavaMemory();
				updateMemoryUsageTextView();
			}
		});

		mJavaGCButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				System.gc();
				updateMemoryUsageTextView();
			}
		});

		mNativeNew100MBButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mNativeAllocatedSize += alloc(100*1024*1024);
				updateMemoryUsageTextView();
			}
		});

		mNativeNew10MBButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mNativeAllocatedSize += alloc(10*1024*1024);
				updateMemoryUsageTextView();
			}
		});

		mNativeNew1MBButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mNativeAllocatedSize += alloc(1024*1024);
				updateMemoryUsageTextView();
			}
		});

		mNativeFreeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mNativeAllocatedSize = 0;
				free();
				updateMemoryUsageTextView();
			}
		});

	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (mHeapMoniter != null) {
			stopHeapMoniter();
			mHeapMoniter.setListener(null);
			mHeapMoniter = null;
		}
	}

	private void updateMemoryUsageTextView()
	{
		mJavaUsageTextView.setText("Java Heap: " + HeapInfo.bytes2str(mJavaAllocatedSize));
		mNativeUsageTextView.setText("Native Heap: " + HeapInfo.bytes2str(mNativeAllocatedSize));
		if (mHeapMoniter != null) {
			mHeapMoniter.forceReload();
		}
	}

	private void updateEnableHeapMoniterCheckBox()
	{
		if (mHeapMoniter != null) {
			if (mHeapMoniter.isRunning()) {
				mEnableHeapMoniterCheckBox.setChecked(true);
			} else {
				mEnableHeapMoniterCheckBox.setChecked(false);
			}
		} else {
			mEnableHeapMoniterCheckBox.setChecked(false);
		}
	}

	private void startHeapMoniter()
	{
		if (mHeapMoniter != null) {
			if (mHeapMoniter.start()) {
				Log.i("sakalog", "Heap Moniter is runnnig...");
			}
		}
		updateEnableHeapMoniterCheckBox();
	}

	private void stopHeapMoniter()
	{
		if (mHeapMoniter != null) {
			mHeapMoniter.stop();
		}
		updateEnableHeapMoniterCheckBox();
	}

	public void onReloadHeapInfo(HeapInfo heapInfo)
	{
		Log.i("sakalog", heapInfo.toString());
		if (mHeapInfoTextView != null) {
			mHeapInfoTextView.setText(heapInfo.toString());
		}
	}

	public void onStopHeapMoniter()
	{
		Log.i("sakalog", "Heap Moniter has been stopped.");
		updateEnableHeapMoniterCheckBox();
	}

	private void allocJavaMemory(int size)
	{
		JavaMemoryBlock jmb = new JavaMemoryBlock(size);
		if (mJavaMemoryBlockTop == null) {
			mJavaMemoryBlockTop = mJavaMemoryBlockButtom = jmb;
		} else {
			mJavaMemoryBlockButtom.link(jmb);
			mJavaMemoryBlockButtom = jmb;
		}
	}

	private void unrefJavaMemory()
	{
		mJavaMemoryBlockTop = mJavaMemoryBlockButtom = null;
		mJavaAllocatedSize = 0;
	}

	private class JavaMemoryBlock
	{
		private ByteBuffer mBuf = null;
		private JavaMemoryBlock mNext = null;

		public JavaMemoryBlock(int size)
		{
			mBuf = ByteBuffer.allocate(size);
			mJavaAllocatedSize += size;
		}

		public void link(JavaMemoryBlock jmb)
		{
			mNext = jmb;
		}
	}

}

android-app-heap
================

#概要

OutOfMemoryErrorを発生させるテストアプリ。

#UI説明
* Enable Heap Moniter チェックボックス
	* チェック時は**Heap使用状況表示**を3秒間隔で更新
* Java Heap 操作ボタン
	* Java Heapから10MB単位または1MB単位で獲得
	* 獲得したメモリへの参照を消すunref
	* GCを強制実行
* Native Heap 操作ボタン
	* Native Heapから100MB単位、10MB単位、1MB単位で獲得
	* 獲得したメモリを解放
* Heap使用状況表示
	* Linux Heap Size
		* システムのHeap Sizeの総量 (未対応)
	* Linux Heap Free Size
		* システムのHeap Sizeの残量
	* Low Memory Threshold
		* Linux Heap Free SizeがLow Memory Thresholdを下回るとLowMemoryKillerが発動
	* Low Memory
		* Linux Heap Free SizeがLow Memory Thresholdを下回っている場合はtrue
	* Application Heap Max Size
		* Runtime.maxMemory
			* このアプリケーションが利用可能なJava Heapの上限
		* ActivityManager.getMemoryClass
			* 同上
	* Application Heap Size
		* `Native Heap Size + Java Heap Size`
	* Application Heap Allocated Size
		* `Native Heap Allocated Size + Java Heap Allocated Size`
	* Application Heap Free Size
		* `Native Heap Free Size + Java Heap Free Size`
	* Native Heap Size
		* このアプリケーションのNative Heap Sizeの現在の上限
> Native Heap Allocated Size増加に合わせて、この上限は増加する。
> Java HeapのRuntime.maxMemoryに相当する**本当の上限**は無い。
> このアプリケーションはLinux Heap Free Sizeに残量がある限りNative Heapからメモリを獲得できる。
> ただしLinux Heap Free SizeがLow Memory Thresholdを下回ったLowMemory状態では、LowMemoryKillerによってこのアプリケーションがKillされる可能性がある。
> LowMemory状態では、LowMemoryKillerは、各アプリケーションのOOM値(adjとminfreeから決まる値)を見て、どのアプリケーションをKillするかを決める。
	* Native Heap Allocated Size
		* このアプリケーションが使用中のNative Heap Size  
	* Native Heap Free Size
		* Native Heap Sizeの残量
		すなわち、`Native Heap Size - Native Heap Allocated Size`
	* Java Heap Size
		* このアプリケーションのJava Heap Sizeの現在の上限  
> Java Heap Allocated Size増加に合わせてJava Heap Sizeは増加する。
> このアプリケーションのJava Heap Sizeの本当の上限はRuntime.maxMemoryである。
	* Java Heap Allocated Size
		* このアプリケーションが使用中のJava Heap Size
	* Java Heap Free Size
		* Java Heap Sizeの残量
		すなわち、`Java Heap Size - Java Heap Allocated Size`


#使用方法
##OutOfMemoryErrorをわざと起こす
Java Heap 操作ボタンでJava Heapからメモリを獲得して、Java Heap Allocated Sizeを増加させる。
Java Heap Allocated Sizeの使用量がRuntime.maxMemoryを超えるとOutOfMemoryErrorが発生して、このアプリケーションは強制終了する。

##LowMemoryKillerをわざと発動させる
Native Heap 操作ボタンでNative Heapからメモリを獲得して、Linux Heap Free Sizeを低下させる。

Linux Heap Free SizeがLow Memory Thresholdを下回るとLow Memoryがtureに変化する。

LowMemoryKillerが発動する。

このアプリケーションはフォアグラウンドにあるため最もKillされ難い。LowMemoryKillerは他のアプリケーションをKillするだろう。

他のアプリケーションがKillされると、他のアプリケーションが獲得していはJava Heap及びNative Heapが解放されるため、Linux Heap Free Sizeが増加して、Low Memoryがfalseになるだろう。

更にNative Heap 操作ボタンでNative Heapからメモリを獲得 -> LowMemoryKillerが他のアプリケーションがKill -> Linux Heap Free Size が増加 -> ・・・
しばらくはこれが繰り返されるだろう。

そして最後には、このアプリケーションがLowMemoryKillerによってKillされるだろう。


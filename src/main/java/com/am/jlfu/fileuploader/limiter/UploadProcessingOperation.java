package com.am.jlfu.fileuploader.limiter;


public class UploadProcessingOperation {

	/**
	 * 指定已写入的字节数量。
	 * */
	private long bytesWritten;
	private Object bytesWrittenLock = new Object();

	/**
	 * 指定可用于再填充过程的迭代的字节量。
	 * of {@link RateLimiter}
	 * */
	private long downloadAllowanceForIteration;
	private Object downloadAllowanceForIterationLock = new Object();



	public long getDownloadAllowanceForIteration() {
		synchronized (downloadAllowanceForIterationLock) {
			return downloadAllowanceForIteration;
		}
	}


	void setDownloadAllowanceForIteration(long downloadAllowanceForIteration) {
		synchronized (downloadAllowanceForIterationLock) {
			this.downloadAllowanceForIteration = downloadAllowanceForIteration;
		}
	}


	public long getAndResetBytesWritten() {
		synchronized (bytesWrittenLock) {
			final long temp = bytesWritten;
			bytesWritten = 0;
			return temp;
		}
	}


	/**
	 * 指定从文件中读取的字节。
	 * 
	 * @param bytesConsumed
	 */
	public void bytesConsumedFromAllowance(long bytesConsumed) {
		synchronized (bytesWrittenLock) {
			synchronized (downloadAllowanceForIterationLock) {
				bytesWritten += bytesConsumed;
				downloadAllowanceForIteration -= bytesConsumed;
			}
		}
	}


}

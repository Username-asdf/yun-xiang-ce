package com.umi.utils;

/**
 * 进度展示工具类
 */
public class ProcessBar {

	private int totatlNum;
	private int currentNum;
	private String lock = "lock";
	private String msg;
	private boolean isPrintMsg = true;
	
	public ProcessBar() {
	}
	public ProcessBar(String msg) {
		this.msg = msg;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public int getTotatlNum() {
		return totatlNum;
	}
	public void setTotatlNum(int totatlNum) {
		this.totatlNum = totatlNum;
	}
	public int getCurrentNum() {
		return currentNum;
	}
	public void setCurrentNum(int currentNum) {
		synchronized (lock) {
			this.currentNum = currentNum;
		}
	}
	
	public void printCurrentProcess() {
		System.out.print(this.msg+":"+this.currentNum+"/"+this.totatlNum
				+" -- "+(this.currentNum*10000/this.totatlNum)/100f+"%\r");
	}
	
	public void println() {
		System.out.println();
	}
}

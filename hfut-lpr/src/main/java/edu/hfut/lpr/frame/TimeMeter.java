package edu.hfut.lpr.frame;

import java.util.Calendar;

/**
 * 时间度量器
 *
 * @author wanggang
 *
 */
public class TimeMeter {

	private long startTime;

	/**
	 * 初始化当前时间
	 */
	public TimeMeter() {
		this.startTime = Calendar.getInstance().getTimeInMillis();
	}

	/**
	 * 获取时间差
	 */
	public long getTime() {
		return Calendar.getInstance().getTimeInMillis() - this.startTime;
	}

}
package org.processmining.plugins.inductiveVisualMiner.animation.renderingthread;

public class TimeManager {

	private double timeRequested = -1;
	private long timeRequestedAt = -1;
	private double minTimeRequested = -1;
	private double maxTimeRequested = -1;

	private long now; //in ms
	private long lastUpdated; //in ms
	private double time; //in s

	private double minTime;
	private double maxTime;

	public TimeManager(double minTime, double maxTime) {
		now = System.currentTimeMillis();
		lastUpdated = System.currentTimeMillis();
		time = minTime;

		this.minTime = minTime;
		this.maxTime = maxTime;
	}

	public void seek(double timeRequested) {
		timeRequestedAt = System.currentTimeMillis();
		this.timeRequested = timeRequested;
	}
	
	public void resume() {
		lastUpdated = System.currentTimeMillis();
	}

	public void setExtremeTimes(double minTime, double maxTime) {
		minTimeRequested = minTime;
		maxTimeRequested = maxTime;
	}

	public double getTimeToBeRendered(boolean running) {
		if (timeRequested != -1) {
			lastUpdated = timeRequestedAt;
			time = timeRequested;
			timeRequestedAt = -1;
			timeRequested = -1;
		}
		if (minTimeRequested != -1) {
			minTime = minTimeRequested;
			maxTime = maxTimeRequested;
			minTimeRequested = -1;
			maxTimeRequested = -1;
		}

		if (running) {
			now = System.currentTimeMillis();
			time = time + ((now - lastUpdated) / 1000.0);
		}
		if (minTime < maxTime) {
			while (time < minTime) {
				time += (maxTime - minTime);
			}
			while (time > maxTime) {
				time -= (maxTime - minTime);
			}
		}
		lastUpdated = now;
		return time;
	}

	public double getMinTime() {
		return minTime;
	}

	public double getMaxTime() {
		return maxTime;
	}
}

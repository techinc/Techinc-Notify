package nl.techinc.notify;

import android.app.Application;

public class NotifyApp extends Application {
	private boolean spaceState;
	private boolean updating;
	private long lastUpdated;

	public boolean getSpaceState() {
		return spaceState;
	}

	public void setSpaceState(boolean spaceState) {
		this.spaceState = spaceState;
	}

	public boolean isUpdating() {
		return updating;
	}

	public void setUpdating(boolean updating) {
		this.updating = updating;
	}

	public long getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(long lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}

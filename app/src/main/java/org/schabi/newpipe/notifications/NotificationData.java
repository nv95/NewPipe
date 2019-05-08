package org.schabi.newpipe.notifications;

import android.content.Intent;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

final class NotificationData {

	private int id;
	private String title;
	private String iconUrl;
	private long channelId;
	private final ArrayList<CharSequence> items = new ArrayList<>();
	private Intent intent;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<CharSequence> getItems() {
		return items;
	}

	public void addItem(CharSequence item) {
		this.items.add(item);
	}

	public String getText() {
		return TextUtils.join(", ", items);
	}

	public int getId() {
		return id;
	}

	public int getCount() {
		return items.size();
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public Intent getIntent() {
		return intent;
	}

	public void setIntent(Intent intent) {
		this.intent = intent;
		this.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	}
}

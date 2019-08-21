package org.schabi.newpipe.notifications;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import org.schabi.newpipe.extractor.channel.ChannelInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.util.NavigationHelper;

import java.util.ArrayList;
import java.util.List;

public final class ChannelUpdates {

	private final int serviceId;
	private final String url;
	private final String avatarUrl;
	private final String name;
	private final List<StreamInfoItem> streams;


	public ChannelUpdates(int serviceId, String url, String avatarUrl, String name, List<StreamInfoItem> streams) {
		this.serviceId = serviceId;
		this.url = url;
		this.avatarUrl = avatarUrl;
		this.name = name;
		this.streams = streams;
	}

	static ChannelUpdates from(ChannelInfo channel, List<StreamInfoItem> streams) {
		return new ChannelUpdates(
				channel.getServiceId(),
				channel.getUrl(),
				channel.getAvatarUrl(),
				channel.getName(),
				streams
		);
	}

	public int getServiceId() {
		return serviceId;
	}

	public String getUrl() {
		return url;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public String getName() {
		return name;
	}

	public List<StreamInfoItem> getStreams() {
		return streams;
	}

	public boolean isNotEmpty() {
		return !streams.isEmpty();
	}

	public int getCount() {
		return streams.size();
	}

	public String getText() {
		final ArrayList<String> titles = new ArrayList<>(streams.size());
		for (StreamInfoItem stream : streams) {
			titles.add(stream.getName());
		}
		return TextUtils.join(", ", titles);
	}

	public Intent createOpenChannelIntent(Context context) {
		return NavigationHelper.getChannelIntent(context, serviceId, url,name)
				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	}

	public int getId() {
		return url.hashCode();
	}
}

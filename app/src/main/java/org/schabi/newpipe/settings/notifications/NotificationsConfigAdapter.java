package org.schabi.newpipe.settings.notifications;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.Animatable2Compat;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.schabi.newpipe.R;
import org.schabi.newpipe.database.subscription.NotificationMode;
import org.schabi.newpipe.database.subscription.SubscriptionEntity;
import org.schabi.newpipe.util.ThemeHelper;

import java.util.ArrayList;
import java.util.List;

final class NotificationsConfigAdapter extends RecyclerView.Adapter<NotificationsConfigAdapter.SubscriptionHolder> {

	private final ArrayList<SubscriptionEntity> dataSet;
	private final ModeToggleListener modeListener;

	NotificationsConfigAdapter(ModeToggleListener listener) {
		dataSet = new ArrayList<>();
		modeListener = listener;
	}

	@NonNull
	@Override
	public SubscriptionHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		final View view = LayoutInflater.from(viewGroup.getContext())
				.inflate(R.layout.item_notification_config, viewGroup, false);
		return new SubscriptionHolder(view, modeListener);
	}

	@Override
	public void onBindViewHolder(@NonNull SubscriptionHolder subscriptionHolder, int i) {
		final SubscriptionEntity item = dataSet.get(i);
		subscriptionHolder.bind(item);
	}

	public SubscriptionEntity getItem(int position) {
		return dataSet.get(position);
	}

	@Override
	public int getItemCount() {
		return dataSet.size();
	}

	void update(List<SubscriptionEntity> newData) {
		dataSet.clear();
		dataSet.addAll(newData);
		notifyDataSetChanged();
	}

	final static class SubscriptionHolder extends RecyclerView.ViewHolder implements View.OnClickListener, ModeToggleListener {

		private final ModeToggleListener listener;
		private final TextView titleTextView;
		private final ImageView stateImageView;

		SubscriptionHolder(@NonNull View itemView, ModeToggleListener listener) {
			super(itemView);
			this.listener = listener;
			titleTextView = itemView.findViewById(R.id.text_title);
			stateImageView = itemView.findViewById(R.id.icon_state);
			stateImageView.setOnClickListener(this);
		}

		void bind(SubscriptionEntity data) {
			titleTextView.setText(data.getName());
			stateImageView.setImageDrawable(getIcon(data.getNotificationMode()));
		}

		@Override
		public void onClick(View v) {
			final Animatable icon = (Animatable) stateImageView.getDrawable();
			if (!icon.isRunning()) {
				icon.start();
			}
		}

		@Override
		public void onModeToggle(int position, int mode) {
			listener.onModeToggle(getAdapterPosition(), mode);
			stateImageView.setImageDrawable(getIcon(mode));
		}

		@SuppressWarnings("ConstantConditions")
		private Drawable getIcon(@NotificationMode int mode) {
			final AnimatedVectorDrawableCompat icon = AnimatedVectorDrawableCompat.create(
					itemView.getContext(),
					mode == NotificationMode.DISABLED ? R.drawable.av_notification_off : R.drawable.av_notification_on
			);
			icon.setTint(ThemeHelper.resolveColorFromAttr(itemView.getContext(), android.R.attr.textColorPrimary));
			icon.registerAnimationCallback(new ModeToggleCallback(this,
					mode == NotificationMode.DISABLED ? NotificationMode.ENABLED_DEFAULT : NotificationMode.DISABLED));
			return icon;
		}
	}

	private final static class ModeToggleCallback extends Animatable2Compat.AnimationCallback {

		@NotificationMode
		private final int newMode;
		private final ModeToggleListener listener;

		private ModeToggleCallback(ModeToggleListener listener, int newMode) {
			this.listener = listener;
			this.newMode = newMode;
		}

		@Override
		public void onAnimationEnd(Drawable drawable) {
			listener.onModeToggle(-1, newMode);
		}
	}

	interface ModeToggleListener {

		void onModeToggle(int position, @NotificationMode int mode);
	}
}

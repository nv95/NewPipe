package org.schabi.newpipe.settings.notifications;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.schabi.newpipe.R;
import org.schabi.newpipe.database.subscription.NotificationMode;
import org.schabi.newpipe.database.subscription.SubscriptionEntity;
import org.schabi.newpipe.local.subscription.SubscriptionService;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public final class NotificationsChannelsConfigFragment extends Fragment implements NotificationsConfigAdapter.ModeToggleListener {

	private NotificationsConfigAdapter adapter;
	@Nullable
	private Disposable loader = null;
	private CompositeDisposable updaters;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new NotificationsConfigAdapter(this);
		updaters = new CompositeDisposable();
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_channels_notifications, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
		recyclerView.setAdapter(adapter);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (loader != null) {
			loader.dispose();
		}
		loader = SubscriptionService.getInstance(requireContext())
				.getSubscription()
				.firstElement()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(adapter::update);
	}

	@Override
	public void onDestroy() {
		if (loader != null) {
			loader.dispose();
		}
		updaters.dispose();
		super.onDestroy();
	}

	@Override
	public void onModeToggle(int position, @NotificationMode int mode) {
		final SubscriptionEntity subscription = adapter.getItem(position);
		updaters.add(
				SubscriptionService.getInstance(requireContext())
						.updateNotificationMode(subscription.getServiceId(), subscription.getUrl(), mode)
						.subscribe()
		);
	}
}

package org.schabi.newpipe.notifications;

import android.content.Context;
import android.support.annotation.NonNull;

import org.schabi.newpipe.NewPipeDatabase;
import org.schabi.newpipe.database.stream.dao.StreamDAO;
import org.schabi.newpipe.database.subscription.NotificationMode;
import org.schabi.newpipe.database.subscription.SubscriptionEntity;
import org.schabi.newpipe.extractor.channel.ChannelInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;
import org.schabi.newpipe.local.subscription.SubscriptionService;
import org.schabi.newpipe.util.ExtractorHelper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;

final class SubscriptionUpdates implements FlowableOnSubscribe<ChannelUpdates> {

    private final SubscriptionService subscriptionService;
    private final StreamDAO streamTable;

    SubscriptionUpdates(Context context) {
        this.subscriptionService = SubscriptionService.getInstance(context);
        this.streamTable = NewPipeDatabase.getInstance(context).streamDAO();
    }

    @Override
    public void subscribe(FlowableEmitter<ChannelUpdates> emitter) {
        try {
            final List<SubscriptionEntity> subscriptions = subscriptionService.getSubscription().blockingFirst();
            for (SubscriptionEntity subscription : subscriptions) {
                if (subscription.getNotificationMode() != NotificationMode.DISABLED) {
                    final ChannelInfo channel = ExtractorHelper.getChannelInfo(subscription.getServiceId(), subscription.getUrl(), true).blockingGet();
                    final ChannelUpdates updates = ChannelUpdates.from(channel, filterStreams(channel.getRelatedItems()));
                    if (updates.isNotEmpty()) {
                        emitter.onNext(updates);
                    }
                }
            }
            emitter.onComplete();
        } catch (Exception e) {
            emitter.onError(e);
        }
    }

    @NonNull
    private List<StreamInfoItem> filterStreams(@NonNull List<?> list) {
        final ArrayList<StreamInfoItem> streams = new ArrayList<>(list.size());
        for (Object o : list) {
            if (o instanceof StreamInfoItem) {
                final StreamInfoItem item = (StreamInfoItem) o;
                if (streamTable.exists(item.getServiceId(), item.getUrl())) {
                    break;
                }
                streams.add(item);
            }
        }
        streams.trimToSize();
        return streams;
    }
}

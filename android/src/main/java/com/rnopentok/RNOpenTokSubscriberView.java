package com.rnopentok;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.ThemedReactContext;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

public class RNOpenTokSubscriberView extends RNOpenTokView implements SubscriberKit.SubscriberListener, SubscriberKit.VideoListener, SubscriberKit.StreamListener {
    private Subscriber mSubscriber;
    private Boolean mAudioEnabled;
    private Boolean mVideoEnabled;

    public RNOpenTokSubscriberView(ThemedReactContext context) {
        super(context);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        RNOpenTokSessionManager.getSessionManager().setSubscriberListener(mSessionId, this);
    }

    public void setAudio(Boolean enabled) {
        if (mSubscriber != null) {
            mSubscriber.setSubscribeToAudio(enabled);
        }

        mAudioEnabled = enabled;
    }

    public void setVideo(Boolean enabled) {
        if (mSubscriber != null) {
            mSubscriber.setSubscribeToVideo(enabled);
        }

        mVideoEnabled = enabled;
    }

    public void onVideoEnabled(WritableMap payload){
        sendEvent(Events.ON_VIDEO_ENABLED, payload);
    }

    public void onVideoDisabled(WritableMap payload){
        sendEvent(Events.ON_VIDEO_DISABLED, payload);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        RNOpenTokSessionManager.getSessionManager().removeSubscriberListener(mSessionId);
    }

    private void startSubscribing(Stream stream) {
        mSubscriber = new Subscriber(getContext(), stream);

        mSubscriber.setSubscriberListener(this);
        mSubscriber.setVideoListener(this);
        mSubscriber.setStreamListener(this);

        mSubscriber.setSubscribeToAudio(mAudioEnabled);
        mSubscriber.setSubscribeToVideo(mVideoEnabled);

        mSubscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);

        Session session = RNOpenTokSessionManager.getSessionManager().getSession(mSessionId);
        if(session != null) {
            session.subscribe(mSubscriber);

            session.setStreamPropertiesListener(new Session.StreamPropertiesListener() {
                @Override
                public void onStreamHasAudioChanged(Session session, Stream stream, boolean b) {

                }

                @Override
                public void onStreamHasVideoChanged(Session session, Stream stream, boolean b) {
                  
                }

                @Override
                public void onStreamHasVideoChanged(Session session, Stream stream, boolean b) {

                }
                
                @Override
                public void onStreamVideoDimensionsChanged(Session session, Stream stream, int i, int i1) {

                }

                @Override
                public void onStreamVideoTypeChanged(Session session, Stream stream, Stream.StreamVideoType streamVideoType) {

                }
            });
        }

        attachSubscriberView();
    }

    private void attachSubscriberView() {
        addView(mSubscriber.getView(), new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        requestLayout();
//        onVideoEnabled();
    }

    private void cleanUpSubscriber() {
        if( mSubscriber != null) {
            removeView(mSubscriber.getView());
            mSubscriber = null;
        }
    }

    public void onStreamReceived(Session session, Stream stream) {
        if (mSubscriber == null) {
            startSubscribing(stream);
//            sendEvent(Events.EVENT_SUBSCRIBE_START, Arguments.createMap());
        }
    }

    public void onStreamDropped(Session session, Stream stream) {
//        sendEvent(Events.EVENT_SUBSCRIBE_STOP, Arguments.createMap());
//         cleanUpSubscriber();
    }

    /** Subscribe listener **/

    @Override
    public void onConnected(SubscriberKit subscriberKit) {
        sendEvent(Events.EVENT_SUBSCRIBE_START, Arguments.createMap());
    }

    @Override
    public void onReconnected(SubscriberKit subscriberKit) {

    }

    @Override
    public void onDisconnected(SubscriberKit subscriberKit) {
        sendEvent(Events.EVENT_SUBSCRIBE_STOP, Arguments.createMap());
        cleanUpSubscriber();
    }

    @Override
    public void onError(SubscriberKit subscriberKit, OpentokError opentokError) {
                                                    WritableMap payload = Arguments.createMap();
        payload.putString("connectionId", opentokError.toString());

        sendEvent(Events.EVENT_SUBSCRIBE_ERROR, payload);

//        Check it
        cleanUpSubscriber();

    }

    @Override
    public void onVideoDataReceived(SubscriberKit subscriberKit) {
        sendEvent(Events.ON_VIDEO_ENABLED, Arguments.createMap());
    }

    @Override
    public void onVideoDisabled(SubscriberKit subscriberKit, String s) {
        sendEvent(Events.ON_VIDEO_DISABLED, Arguments.createMap());
    }

    @Override
    public void onVideoEnabled(SubscriberKit subscriberKit, String s) {
        sendEvent(Events.ON_VIDEO_ENABLED, Arguments.createMap());
    }

    @Override
    public void onVideoDisableWarning(SubscriberKit subscriberKit) {

    }

    @Override
    public void onVideoDisableWarningLifted(SubscriberKit subscriberKit) {

    }
}

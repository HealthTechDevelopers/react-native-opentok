#import <Foundation/Foundation.h>
#import "RNOpenTokSubscriberView.h"

@interface RNOpenTokSubscriberView () <OTSubscriberDelegate>
@end

@implementation RNOpenTokSubscriberView {
    OTSubscriber *_subscriber;
}

@synthesize sessionId = _sessionId;
@synthesize session = _session;

- (void)didMoveToWindow {
    [super didMoveToSuperview];
    [self mount];
}

- (void)dealloc {
    [self stopObserveSession];
    [self stopObserveStream];
    [self cleanupSubscriber];
}

- (void)didSetProps:(NSArray<NSString *> *)changedProps {
    if (_subscriber == nil) {
        return;
    }

    if ([changedProps containsObject:@"mute"]) {
        _subscriber.subscribeToAudio = !_mute;
    }

    if ([changedProps containsObject:@"video"]) {
        _subscriber.subscribeToVideo = _video;
    }
}


#pragma mark - Private methods


- (void)mount {
    [self observeSession];
    [self observeStream];
    if (!_session) {
        [self connectToSession];
    }
}

- (void)doSubscribe:(OTStream*)stream {
    [self unsubscribe];
    _subscriber = [[OTSubscriber alloc] initWithStream:stream delegate:self];
    _subscriber.subscribeToAudio = !_mute;
    _subscriber.subscribeToVideo = _video;

    OTError *error = nil;
    [_session subscribe:_subscriber error:&error];

    if (error) {
        [self subscriber:_subscriber didFailWithError:error];
        return;
    }

    [self attachSubscriberView];
}

- (void)unsubscribe {
    OTError *error = nil;
    [_session unsubscribe:_subscriber error:&error];

    if (error) {
        NSLog(@"%@", error);
    }
}

- (void)attachSubscriberView {
    [_subscriber.view setFrame:CGRectMake(0, 0, self.bounds.size.width, self.bounds.size.height)];
    [self addSubview:_subscriber.view];
}

- (void)cleanupSubscriber {
    [_subscriber.view removeFromSuperview];
    [self unsubscribe];
    _subscriber.delegate = nil;
    _subscriber = nil;
}

- (void)onStreamCreated:(NSNotification *)notification {
    OTStream *stream = notification.userInfo[@"stream"];
    if (_subscriber == nil) {
        [self doSubscribe:stream];
    }
}

- (void)onStreamDestroyed:(NSNotification *)notification {
    OTStream *stream = notification.userInfo[@"stream"];

    if ([_subscriber.stream.streamId isEqualToString:stream.streamId]) {
        [self cleanupSubscriber];
    }
}

- (void)observeStream {
    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(onStreamCreated:)
     name:[@"stream-created:" stringByAppendingString:_sessionId]
     object:nil];

    [[NSNotificationCenter defaultCenter]
     addObserver:self
     selector:@selector(onStreamDestroyed:)
     name:[@"stream-destroyed:" stringByAppendingString:_sessionId]
     object:nil];
}

- (void)stopObserveStream {
    [[NSNotificationCenter defaultCenter]
     removeObserver:self
     name:[@"stream-created:" stringByAppendingString:_sessionId]
     object:nil];

    [[NSNotificationCenter defaultCenter]
     removeObserver:self
     name:[@"stream-destroyed:" stringByAppendingString:_sessionId]
     object:nil];
}

#pragma mark - OTSubscriber delegate callbacks

- (void)subscriber:(OTSubscriberKit*)subscriber didFailWithError:(OTError*)error {
    [[NSNotificationCenter defaultCenter]
     postNotificationName:@"onSubscribeError"
     object:nil
     userInfo:@{@"sessionId": _sessionId, @"error": [error description]}];
    [self cleanupSubscriber];
}

- (void)subscriberDidConnectToStream:(OTSubscriberKit*)subscriber {
    [[NSNotificationCenter defaultCenter]
     postNotificationName:@"onSubscribeStart"
     object:nil
     userInfo:@{@"sessionId": _sessionId}];
}

- (void)subscriberDidDisconnectFromStream:(OTSubscriberKit*)subscriber {
    [[NSNotificationCenter defaultCenter]
     postNotificationName:@"onSubscribeStop"
     object:nil
     userInfo:@{@"sessionId": _sessionId}];
    [self cleanupSubscriber];
}

- (void)subscriberDidReconnectToStream:(OTSubscriberKit*)subscriber {
    [self subscriberDidConnectToStream:subscriber];
}

- (void)subscriberVideoEnabled:(OTSubscriberKit *)subscriber reason:(OTSubscriberVideoEventReason)reason {
    [[NSNotificationCenter defaultCenter]
     postNotificationName:@"onVideoEnabled"
     object:nil
     userInfo:@{@"sessionId": _sessionId, @"reason": @(reason)}];
}

- (void)subscriberVideoDisabled:(OTSubscriberKit *)subscriber reason:(OTSubscriberVideoEventReason)reason {
    [[NSNotificationCenter defaultCenter]
     postNotificationName:@"onVideoDisabled"
     object:nil
     userInfo:@{@"sessionId": _sessionId, @"reason": @(reason)}];
}

@end


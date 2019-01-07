package com.orange.server.ws;

import com.orange.server.codec.MyWebSocketDecoder;
import com.orange.server.codec.MyWebSocketEncoder;

import io.netty.handler.codec.http.websocketx.WebSocketFrameDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketFrameEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker13;

public class WebSocketHandshaker13 extends WebSocketServerHandshaker13 {

	private final boolean allowExtensions0;

	public WebSocketHandshaker13(String webSocketURL, String subprotocols, boolean allowExtensions, int maxFramePayloadLength) {
		super(webSocketURL, subprotocols, allowExtensions, maxFramePayloadLength);
		this.allowExtensions0 = allowExtensions;
	}

	@Override
	protected WebSocketFrameDecoder newWebsocketDecoder() {
		return new MyWebSocketDecoder(true, allowExtensions0, maxFramePayloadLength());
	}

	@Override
	protected WebSocketFrameEncoder newWebSocketEncoder() {
		return new MyWebSocketEncoder();
	}
}

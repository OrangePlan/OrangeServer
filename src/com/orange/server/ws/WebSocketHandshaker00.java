package com.orange.server.ws;

import com.orange.server.codec.MyWebSocketDecoder00;
import com.orange.server.codec.MyWebSocketEncoder00;

import io.netty.handler.codec.http.websocketx.WebSocketFrameDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketFrameEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker00;

public class WebSocketHandshaker00 extends WebSocketServerHandshaker00 {

	public WebSocketHandshaker00(String webSocketURL, String subprotocols, int maxFramePayloadLength) {
		super(webSocketURL, subprotocols, maxFramePayloadLength);
	}

	@Override
	protected WebSocketFrameDecoder newWebsocketDecoder() {
		return new MyWebSocketDecoder00(maxFramePayloadLength());
	}

	@Override
	protected WebSocketFrameEncoder newWebSocketEncoder() {
		return new MyWebSocketEncoder00();
	}
}

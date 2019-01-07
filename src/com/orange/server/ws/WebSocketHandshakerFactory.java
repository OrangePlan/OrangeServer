package com.orange.server.ws;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

public class WebSocketHandshakerFactory {

	private final String webSocketURL;

	private final String subprotocols;

	private final boolean allowExtensions;

	private final int maxFramePayloadLength;

	public WebSocketHandshakerFactory(String webSocketURL, String subprotocols, boolean allowExtensions) {
		this(webSocketURL, subprotocols, allowExtensions, 65536);
	}

	public WebSocketHandshakerFactory(String webSocketURL, String subprotocols, boolean allowExtensions, int maxFramePayloadLength) {
		this.webSocketURL = webSocketURL;
		this.subprotocols = subprotocols;
		this.allowExtensions = allowExtensions;
		this.maxFramePayloadLength = maxFramePayloadLength;
	}

	public WebSocketServerHandshaker newHandshaker(HttpRequest req) {
		String version = req.headers().get(Names.SEC_WEBSOCKET_VERSION);
		if (version != null) {
			if (version.equals(WebSocketVersion.V13.toHttpHeaderValue())) {
				// Version 13 of the wire protocol - RFC 6455 (version 17 of the draft hybi specification).
				// return new WebSocketServerHandshaker13(webSocketURL, subprotocols, allowExtensions,
				// maxFramePayloadLength);
				return new WebSocketHandshaker13(version, version, allowExtensions, maxFramePayloadLength);
			} else if (version.equals(WebSocketVersion.V08.toHttpHeaderValue())) {
				// Version 8 of the wire protocol - version 10 of the draft hybi specification.
				// return new WebSocketServerHandshaker08(webSocketURL, subprotocols, allowExtensions,
				// maxFramePayloadLength);
				return new WebSocketHandshaker08(version, version, allowExtensions, maxFramePayloadLength);
			} else if (version.equals(WebSocketVersion.V07.toHttpHeaderValue())) {
				// Version 8 of the wire protocol - version 07 of the draft hybi specification.
				// return new WebSocketServerHandshaker07(webSocketURL, subprotocols, allowExtensions,
				// maxFramePayloadLength);
				return new WebSocketHandshaker07(version, version, allowExtensions, maxFramePayloadLength);
			} else {
                System.out.println("WebSocket handshaker not find");
				return null;
			}
		} else {
			// Assume version 00 where version header was not specified
			// return new WebSocketServerHandshaker00(webSocketURL, subprotocols, maxFramePayloadLength);
			return new WebSocketHandshaker00(webSocketURL, subprotocols, maxFramePayloadLength);
		}
	}

	/**
	 * @deprecated use {@link #sendUnsupportedVersionResponse(Channel)}
	 */
	@Deprecated
	public static void sendUnsupportedWebSocketVersionResponse(Channel channel) {
		sendUnsupportedVersionResponse(channel);
	}

	/**
	 * Return that we need cannot not support the web socket version
	 */
	public static ChannelFuture sendUnsupportedVersionResponse(Channel channel) {
		return sendUnsupportedVersionResponse(channel, channel.newPromise());
	}

	/**
	 * Return that we need cannot not support the web socket version
	 */
	public static ChannelFuture sendUnsupportedVersionResponse(Channel channel, ChannelPromise promise) {
		HttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UPGRADE_REQUIRED);
		res.headers().set(Names.SEC_WEBSOCKET_VERSION, WebSocketVersion.V13.toHttpHeaderValue());
		HttpHeaders.setContentLength(res, 0);
		return channel.writeAndFlush(res, promise);
	}

}

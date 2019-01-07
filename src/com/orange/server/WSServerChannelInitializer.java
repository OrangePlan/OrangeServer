package com.orange.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;

public class WSServerChannelInitializer extends ChannelInitializer<SocketChannel>{

	private final int maxContentLength = 65536;//64 * 1024;
	
	private SslContext sslCtx;
	
	public WSServerChannelInitializer(SslContext sslCtx) {
		this.sslCtx = sslCtx;
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		
		ChannelPipeline p = ch.pipeline();
		
		if (sslCtx != null) {
			p.addLast(sslCtx.newHandler(ch.alloc()));
		}
		
		p.addLast(new HttpServerCodec());
		p.addLast(new HttpObjectAggregator(maxContentLength));
		
		//p.addLast(new DiscardServerHandler());
		
		p.addLast(new WSChannelInboundHandlerAdapter());
		//p.addLast(new WebSocketServerProtocolHandler("/ws"));
		
	}

}

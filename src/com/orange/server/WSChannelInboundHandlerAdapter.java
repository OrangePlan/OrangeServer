package com.orange.server;


import java.util.Date;

import com.orange.server.ws.WebSocketHandshakerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.AsciiString;
import io.netty.util.ReferenceCountUtil;

public class WSChannelInboundHandlerAdapter extends SimpleChannelInboundHandler<Object> {

	private WebSocketServerHandshaker handshaker;

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		
		//添加到全局
		GlobalChannel.group.add(ctx.channel());
		System.out.println("客户端与服务端连接开启：" + ctx.channel().remoteAddress().toString());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		
		//从全局中移除
		GlobalChannel.group.remove(ctx.channel());
		System.out.println("客户端与服务端连接关闭：" + ctx.channel().remoteAddress().toString());
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object frame) throws Exception {
		if (frame instanceof FullHttpRequest) {//如果是HTTP请求，进行HTTP操作
			processHttpRequest(ctx, (FullHttpRequest) frame);
		}
		else if (frame instanceof BinaryWebSocketFrame) {//如果是Websocket请求，则进行websocket操作
			processBinaryWebSocketRequest(ctx, (BinaryWebSocketFrame) frame);
		}
		else if(frame instanceof TextWebSocketFrame)
		{
			processTextWebSocketRequest(ctx, (TextWebSocketFrame) frame);
		}
		else if (frame instanceof CloseWebSocketFrame) // 判断是否关闭链路的指令
		{
			System.out.println("CloseWebSocketFrame");
			handshaker.close(ctx.channel(), ((CloseWebSocketFrame) frame).retain());
		}
		else if (frame instanceof PingWebSocketFrame)// 判断是否ping消息
		{
			System.out.println("PingWebSocketFrame");
			ctx.channel().write(new PongWebSocketFrame(((PingWebSocketFrame) frame).content().retain()));
		}
	}

	protected void processHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
		if (!HttpMethod.GET.equals(request.getMethod()) || !"websocket".equalsIgnoreCase(request.headers().get("Upgrade"))) {//如果不是WS握手的HTTP请求，将进行处理

			//			DefaultHttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
			//			ctx.channel().write(resp);
			//			ctx.channel().close();

			handleHttpRequest(ctx, request);
		}
		else
		{
			String uri=request.uri();
			System.out.println("ws url:"+uri);
			WebSocketHandshakerFactory wsShakerFactory = new WebSocketHandshakerFactory("ws://" + request.headers().get(HttpHeaders.Names.HOST), null, false);
			handshaker = wsShakerFactory.newHandshaker(request);
			if (handshaker == null) {
				WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
			} else {
				handshaker.handshake(ctx.channel(), request);
			}
		}
	}

	private static final byte[] CONTENT = { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd' };
	private static final AsciiString CONTENT_TYPE = AsciiString.cached("Content-Type");
	private static final AsciiString CONTENT_LENGTH = AsciiString.cached("Content-Length");
	private static final AsciiString CONNECTION = AsciiString.cached("Connection");
	private static final AsciiString KEEP_ALIVE = AsciiString.cached("keep-alive");
	//处理HTTP的代码
	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
		HttpMethod method=req.method();
		String uri=req.uri();

		System.out.println(uri);

		if(uri == "/favicon.ico")
		{
			DefaultHttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
			ctx.channel().write(resp);
			ctx.channel().close();
			return;
		}
		else
		{
			boolean keepAlive = HttpUtil.isKeepAlive(req);
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(CONTENT));
			response.headers().set(CONTENT_TYPE, "text/plain");
			response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

			if (!keepAlive) {
				ctx.write(response).addListener(ChannelFutureListener.CLOSE);
			} else {
				response.headers().set(CONNECTION, KEEP_ALIVE);
				ctx.write(response);
			}
			ctx.flush();
			ctx.close();

			if(method==HttpMethod.GET&&"/login".equals(uri))
			{
				//....处理 
			}else if(method==HttpMethod.POST&&"/register".equals(uri)){
				//...处理
			}
		}

	}

	protected void processBinaryWebSocketRequest(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) {
		ByteBuf copy = frame.content().copy();

		byte[] data = new byte[copy.capacity()];
		copy.readBytes(data);

		StringBuilder a = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			int v = data[i] & 0xFF;
			a.append(Integer.toHexString(v));
		}
	}

	private void processTextWebSocketRequest(ChannelHandlerContext ctx, TextWebSocketFrame frame) 
	{
		// 返回应答消息
		String request = ((TextWebSocketFrame) frame).text();
		System.out.println("服务端收到：" + request);

		TextWebSocketFrame tws = new TextWebSocketFrame(new Date().toString() + ctx.channel().id() + "：" + request);
		// 群发
		//GlobalChannel.group.writeAndFlush(tws);
		// 返回【谁发的发给谁】
		// ctx.channel().writeAndFlush(tws);
	}


//	@Override
//	public void channelRead(ChannelHandlerContext ctx, Object msg) {
//
//	System.out.println("ctx:"+ctx);
//		System.out.println("channelRead:"+msg);
//		//静默丢弃接收到的数据
//		//((ByteBuf) msg).release();
//
//		//    	ByteBuf in = (ByteBuf) msg;
//		//        try {
//		//            while (in.isReadable()) {
//		//                System.out.print((char) in.readByte());
//		//                System.out.flush();
//		//            }
//		//        } finally {
//		//            ReferenceCountUtil.release(msg);
//		//        }
//
//		ByteBuf in = (ByteBuf) msg;
//		String rect = in.toString(io.netty.util.CharsetUtil.US_ASCII);
//		System.out.println("接收:"+rect);
//
//		//    	String send = "返回:"+rect;
//		//    	final ByteBuf time = ctx.alloc().buffer(4); // (2)
//		//        time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));
//		//        time.writeChar("s".toCharArray()[0]);
//		//        time.writeChar("s".toCharArray()[0]);
//		//        time.writeChar("s".toCharArray()[0]);
//		//        time.writeChar("s".toCharArray()[0]);
//		//    	//ctx.write(msg);
//		//        //ctx.flush();
//		//    	ctx.writeAndFlush(time);
//	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);

		//引发异常时关闭连接
		cause.printStackTrace();
		ctx.close();
	}
}

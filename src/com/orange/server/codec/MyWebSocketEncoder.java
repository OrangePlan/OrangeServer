package com.orange.server.codec;

import java.util.List;

import com.google.protobuf.Message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketFrameEncoder;

public class MyWebSocketEncoder extends MessageToMessageEncoder<PBMessage> implements WebSocketFrameEncoder {

	public MyWebSocketEncoder() {

	}

	@Override
	protected void encode(ChannelHandlerContext ctx, PBMessage msg, List<Object> out) throws Exception {
		if (msg.getMyMsg() instanceof MessageBuffer) {
			MessageBuffer messageBuffer = (MessageBuffer) msg.getMyMsg();
			for (PBMessage message : messageBuffer) {
				encode0(ctx, message, out);
			}
		} else {
			encode0(ctx, msg, out);
		}
	}

	private void encode0(ChannelHandlerContext ctx, PBMessage msg, List<Object> out) throws Exception {
		int dataSize = PBMessage.HDR_SIZE;
		Message message = msg.getMessage();
		if (message != null) {
			dataSize += message.getSerializedSize();
		} else if (msg.getBytes() != null) {
			dataSize += msg.getBytes().length;
		}

		boolean release = true;
		ByteBuf buf = null;
		try {
			if (dataSize <= 125) {
				int size = 2 + dataSize;
				buf = ctx.alloc().buffer(size);
				buf.writeByte(130);
				byte b = (byte) (dataSize);
				buf.writeByte(b);
				msg.writeClientHeader(dataSize, buf);
			} else if (dataSize <= 0xFFFF) {
				int size = 4 + dataSize;
				buf = ctx.alloc().buffer(size);
				buf.writeByte(130);
				buf.writeByte(126);
				buf.writeByte(dataSize >>> 8 & 0xFF);
				buf.writeByte(dataSize & 0xFF);
				msg.writeClientHeader(dataSize, buf);
			} else {
				System.out.println("max length code = " + msg.getCode());
				return;
			}

			out.add(buf);

			release = false;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (release && buf != null) {
				buf.release();
			}
		}
	}

}

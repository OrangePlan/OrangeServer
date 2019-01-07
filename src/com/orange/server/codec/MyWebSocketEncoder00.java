package com.orange.server.codec;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.WebSocketFrameEncoder;

public class MyWebSocketEncoder00 extends MessageToMessageEncoder<PBMessage> implements WebSocketFrameEncoder {

	public static final byte OPCODE_CONT = 0x0;
	public static final byte OPCODE_TEXT = 0x1;
	public static final byte OPCODE_BINARY = 0x2;
	public static final byte OPCODE_CLOSE = 0x8;
	public static final byte OPCODE_PING = 0x9;
	public static final byte OPCODE_PONG = 0xA;

	private static final ByteBuf _0XFF_0X00 = Unpooled.unreleasableBuffer(Unpooled.directBuffer(2, 2).writeByte((byte) 0xFF).writeByte((byte) 0x00));

	@Override
	protected void encode(ChannelHandlerContext ctx, PBMessage msg, List<Object> out) throws Exception {
		if (msg.getOpCode() == OPCODE_CLOSE) {
			// Close frame, needs to call duplicate to allow multiple writes.
			// See https://github.com/netty/netty/issues/2768
			out.add(_0XFF_0X00.duplicate());
		}

		else {
			// Binary frame
			int dataSize = PBMessage.HDR_SIZE;
			if (msg.getBytes() != null) {
				dataSize += msg.getBytes().length;
			}

			final ByteBuf data = ctx.alloc().buffer(dataSize);;
			msg.writeClientHeader(dataSize, data);

			int dataLen = data.readableBytes();

			ByteBuf buf = ctx.alloc().buffer(5);
			boolean release = true;
			try {
				// Encode type.
				buf.writeByte((byte) 0x80);

				// Encode length.
				int b1 = dataLen >>> 28 & 0x7F;
				int b2 = dataLen >>> 14 & 0x7F;
				int b3 = dataLen >>> 7 & 0x7F;
				int b4 = dataLen & 0x7F;
				if (b1 == 0) {
					if (b2 == 0) {
						if (b3 == 0) {
							buf.writeByte(b4);
						} else {
							buf.writeByte(b3 | 0x80);
							buf.writeByte(b4);
						}
					} else {
						buf.writeByte(b2 | 0x80);
						buf.writeByte(b3 | 0x80);
						buf.writeByte(b4);
					}
				} else {
					buf.writeByte(b1 | 0x80);
					buf.writeByte(b2 | 0x80);
					buf.writeByte(b3 | 0x80);
					buf.writeByte(b4);
				}

				// Encode binary data.
				out.add(buf);
				out.add(data.retain());
				release = false;
			} finally {
				if (release) {
					buf.release();
				}
			}
		}
	}
}

package com.orange.server.codec;

import java.io.Serializable;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;

public class PBMessage implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	public static final short HDR_SIZE = 14;
	public static final short HEADER = 0x71ab;

	private short header = HEADER;
	private short len;
	private short code;
	private long playerId;
	private boolean isFromClient;
	private short param1 = -1;

	private int byteLength;
	private byte[] bytes;
	private Message message;

	private byte opCode = 0x2;
	private Object myMsg;

	public PBMessage() {

	}

	public PBMessage(Object myMsg) {
		this.myMsg = myMsg;
	}

	public PBMessage(short code, byte[] bytes) {
		this(code, -1);
		this.bytes = bytes;
	}

	public PBMessage(short code) {
		this(code, -1);
	}

	public PBMessage(short code, long playerId) {
		this.code = code;
		this.playerId = playerId;
	}

	public PBMessage(short code, Object myMsg) {
		this.code = code;
		this.myMsg = myMsg;
	}

	public PBMessage(short code, long playerId, Object myMsg) {
		this.code = code;
		this.playerId = playerId;
		this.myMsg = myMsg;
	}

	public short getHeader() {
		return header;
	}

	public short getLen() {
		return len;
	}

	public void setLen(short len) {
		this.len = len;
	}

	public short getCode() {
		return code;
	}

	public void setCode(short code) {
		this.code = code;
	}

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public boolean isFromClient() {
		return isFromClient;
	}

	public void setFromClient(boolean isFromClient) {
		this.isFromClient = isFromClient;
	}

	public short getParam1() {
		return param1;
	}

	public void setParam1(short param1) {
		this.param1 = param1;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public void readFromClient(ByteBuf in) {
		in.readShort();
		len = in.readShort();
		code = in.readShort();

		if (in.isReadable()) {
			bytes = new byte[in.capacity() - 6];
			in.readBytes(bytes);
		}
	}

	public void readFromRobot(ByteBuf in) {
		in.readShort();
		len = in.readShort();
		code = in.readShort();
		playerId = in.readLong();
		if (in.isReadable()) {
			bytes = new byte[in.capacity() - 14];
			in.readBytes(bytes);
		}
	}

	public void writeClientData(ByteBuf out) {
		out.writeShort(HEADER);
		if (message != null) {
			out.writeShort(message.getSerializedSize() + 6);
		} else {
			out.writeShort(6);
		}
		out.writeShort(code);

		if (message != null) {
			out.writeBytes(message.toByteArray());
		}
	}

	public void writeClientHeader(int len, ByteBuf out) {
		out.writeShort(HEADER);
		out.writeShort((short) len);
		out.writeShort(code);
		out.writeLong(playerId);
		if (message != null) {
			out.writeBytes(message.toByteArray());
		} else if (bytes != null) {
			out.writeBytes(bytes);
		}
	}

	public byte[] toByteArray() {
		return bytes;
	}

	public int getByteLength() {
		return byteLength;
	}

	public void setByteLength(int byteLength) {
		this.byteLength = byteLength;
	}

	public byte getOpCode() {
		return opCode;
	}

	public void setOpCode(byte opCode) {
		this.opCode = opCode;
	}

	public Object getMyMsg() {
		return myMsg;
	}

	public void setMyMsg(Object myMsg) {
		this.myMsg = myMsg;
	}

	@Override
	public PBMessage clone() {
		PBMessage clone = null;
		try {
			clone = (PBMessage) super.clone();
			clone.setMessage(message);
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String headerToStr() {
		StringBuilder sb = new StringBuilder();
		sb.append("playerId : ").append(playerId);
		sb.append(", code : ").append(Integer.toHexString(code));
		sb.append(", len : ").append(len);
		return sb.toString();
	}

	public String detailToStr() {
		if (bytes == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(b + ", ");
		}
		return headerToStr() + ", content : [" + sb.toString() + "]";
	}
}

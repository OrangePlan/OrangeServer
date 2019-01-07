package com.orange.server.codec;

import java.util.ArrayList;

/**
 * 客户端消息数据对象<br>
 * 服务器下发客户端的数据结构对象, 这里实际上只是个消息数组.
 * 
 */
public class MessageBuffer extends ArrayList<PBMessage> {
	private static final long serialVersionUID = 1L;
}
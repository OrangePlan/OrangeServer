package com.orange.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

/**
 * WebSocket服务器
 * @author quan
 * DEMO https://github.com/netty/netty/tree/4.1/example/src/main/java/io/netty/example
 */
public class WSServer {

	private String host;
	private int port;
	private boolean isSSL;
	
	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;
	private ChannelFuture channelFuture;

	public WSServer(String host, int port, boolean isSSL) {
		this.host = host;
		this.port = port;
		this.isSSL = isSSL;
	}

	
	/**
	 * @return true启动成功  false启动失败
	 * @throws SSLException
	 * @throws CertificateException
	 */
	public boolean start() throws SSLException, CertificateException {

		boolean bool;
		final SslContext sslCtx;
		if (this.isSSL) {
			SelfSignedCertificate ssc = new SelfSignedCertificate();
			sslCtx = SslContextBuilder.forServer(ssc.certificate(),
					ssc.privateKey()).build();
		} else {
			sslCtx = null;
		}

		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup);
			b.channel(NioServerSocketChannel.class);
			b.handler(new LoggingHandler(LogLevel.INFO));
			b.childHandler(new WSServerChannelInitializer(sslCtx));

			//option主要是针对boss线程组，childOption主要是针对worker线程组
			
			//一个端口释放后会等待两分钟之后才能再被使用，SO_REUSEADDR是让端口释放后立即就可以被再次使用。
			b.option(ChannelOption.SO_REUSEADDR, true);
			
			// 请求连接的最大队列长度，如果backlog参数的值大于操作系统限定的队列的最大长度，那么backlog参数无效 Linux: ulimit -n
			b.option(ChannelOption.SO_BACKLOG, 20 * 1024);
			
			//Socket参数，连接保活，默认值为False。启用该功能时，TCP会主动探测空闲连接的有效性。可以将此功能视为TCP的心跳机制，需要注意的是：默认的心跳间隔是7200s即2小时。Netty默认关闭该功能。
			b.option(ChannelOption.SO_KEEPALIVE, true);
			
			// 在TCP/IP协议中，无论发送多少数据，总是要在数据前面加上协议头，同时，对方接收到数据，也需要发送ACK表示确认。为了尽可能的利用网络带宽，TCP总是希望尽可能的发送足够大的数据。这里就涉及到一个名为Nagle的算法，该算法的目的就是为了尽可能发送大块数据，避免网络中充斥着许多小数据块。
			// TCP_NODELAY就是用于启用或关于Nagle算法。如果要求高实时性，有数据发送时就马上发送，就将该选项设置为true关闭Nagle算法；如果要减少发送次数减少网络交互，就设置为false等累积一定大小后再发送。默认为false。
			b.option(ChannelOption.TCP_NODELAY, true);
			
			// 在调用close方法后，将阻塞n秒，让未完成发送的数据尽量发出
			b.option(ChannelOption.SO_LINGER, 5);
						
			// 设置了ServerSocket类的SO_RCVBUF选项，就相当于设置了Socket对象的接收缓冲区大小，4KB
			b.option(ChannelOption.SO_RCVBUF, 32 * 1024);// 这是接收缓冲大小
			b.option(ChannelOption.SO_SNDBUF, 32 * 1024);//设置发送缓冲大小
			
			// 使用内存池的缓冲区重用机制
			b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
			
			
			//
			b.childOption(ChannelOption.SO_KEEPALIVE, true);
			b.childOption(ChannelOption.SO_LINGER, 5);
			b.childOption(ChannelOption.TCP_NODELAY, true);
			b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
			b.childOption(ChannelOption.SO_RCVBUF, 32 * 1024);// 这是接收缓冲大小
			b.childOption(ChannelOption.SO_SNDBUF, 32 * 1024);//设置发送缓冲大小

			//绑定端口，并开始接收进来的连接
			channelFuture = b.bind(port).sync();

			System.out.println("WSServer Start Successfully!	"+host+":"+port+"");

			bool = true;
			
			//等待连接关闭
			channelFuture.channel().closeFuture().sync();
		}
		catch(Exception e)
		{
			System.out.println("WSServer Start Error!");
			bool = false;
		}
		return bool;
	}
	
	
	/**
	 * 关闭服务器
	 */
	public void close() {
		try {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
			channelFuture.channel().close().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("WSServer Closed! Host:" + host + ",Port:" + port);
		}
	}
}

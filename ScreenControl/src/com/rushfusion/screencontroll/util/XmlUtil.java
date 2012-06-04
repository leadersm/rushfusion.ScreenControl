package com.rushfusion.screencontroll.util;


public class XmlUtil {

	public static final int stbPort = 6802; 

	public static byte[] SearchReq(String taskno, String mip) {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
					+ "<Package> "
					// <!— 命令名称 -->
					+ "<Property  name = 'cmd' vaule = 'stbresp' /> "
					// <!— 将请求任务序号回传给客户端 -->
					+ "<Property  name = 'taskno' vaule = '" + taskno + "' /> "
					// <!— 客户端内网IP，可以用于机顶盒区分多个客户端的请求 -->
					+ "<Property  name = 'IP' vaule = '" + mip + "' /> "
					// <!— 客户端侦听端口号 -->
					+ "<Property  name = 'port' vaule = '"+ stbPort +"' /> " 
					+ "</Package>";
		byte[] xml_bytes = xml.getBytes();
		byte[] headlen_bytes = Tools.intToByteArray(12);
		byte[] bodylen_bytes = Tools.intToByteArray(xml_bytes.length);
		byte[] version_bytes = Tools.intToByteArray(1);

		byte[] data = new byte[12 + xml_bytes.length];

		System.arraycopy(headlen_bytes, 0, data, 0, 4);
		System.arraycopy(bodylen_bytes, 0, data, 4, 4);
		System.arraycopy(version_bytes, 0, data, 8, 4);
		System.arraycopy(xml_bytes, 0, data, 12, xml_bytes.length);

		return data;
	}

//	public static byte[] PlayReq(String taskno,String mip,String title,String duration,String url) {
//		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
//				+ "<Package> "
//				// <!— 命令名称 -->
//				+ "<Property  name = 'cmd' vaule = 'playreq' /> "
//				// <!— 将请求任务序号回传给客户端 -->
//				+ "<Property  name = 'taskno' vaule = '" + taskno + "' /> "
//				// <!— 客户端内网IP，可以用于机顶盒区分多个客户端的请求 -->
//				+ "<Property  name = 'IP' vaule = '" + mip + "' /> "
//				// <!— 客户端侦听端口号 -->
//				+ "<Property  name = 'port' vaule = '"+ stbPort +"' /> " 
//				// <!— 播放url -->
//				+ "<Property  name = 'url' vaule = '" + url + "' /> "
//				// <!— 节目名称 -->
//				+ "<Property  name = 'title' vaule = '" + title + "' /> "
//				// <!— 节目时长 -->
//				+ "<Property  name = 'duration' vaule = '" + duration + "' /> "
//				+ "</Package>";
//		byte[] xml_bytes = xml.getBytes();
//		byte[] headlen_bytes = Tools.intToByteArray(12);
//		byte[] bodylen_bytes = Tools.intToByteArray(xml_bytes.length);
//		byte[] version_bytes = Tools.intToByteArray(1);
//
//		byte[] data = new byte[12 + xml_bytes.length];
//
//		System.arraycopy(headlen_bytes, 0, data, 0, 4);
//		System.arraycopy(bodylen_bytes, 0, data, 4, 4);
//		System.arraycopy(version_bytes, 0, data, 8, 4);
//		System.arraycopy(xml_bytes, 0, data, 12, xml_bytes.length);
//
//		return data;
//	}
}

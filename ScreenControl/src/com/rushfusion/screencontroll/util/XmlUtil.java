package com.rushfusion.screencontroll.util;


public class XmlUtil {

	public static final int stbPort = 6801; 

	public static byte[] SearchReq(String taskno, String mip) {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
				+ "<Package> "
				// <!�� �������� -->
				+ "<Property  name = 'cmd' vaule = 'stbresp' /> "
				// <!�� ������������Żش����ͻ��� -->
				+ "<Property  name = 'taskno' vaule = '" + taskno + "' /> "
				// <!�� �ͻ�������IP���������ڻ��������ֶ���ͻ��˵����� -->
				+ "<Property  name = 'IP' vaule = '" + mip + "' /> "
				// <!�� �ͻ��������˿ں� -->
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

	public static byte[] PlayReq(String taskno,String mip,String title,String duration,String url) {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>"
				+ "<Package> "
				// <!�� �������� -->
				+ "<Property  name = 'cmd' vaule = 'playreq' /> "
				// <!�� ������������Żش����ͻ��� -->
				+ "<Property  name = 'taskno' vaule = '" + taskno + "' /> "
				// <!�� �ͻ�������IP���������ڻ��������ֶ���ͻ��˵����� -->
				+ "<Property  name = 'IP' vaule = '" + mip + "' /> "
				// <!�� �ͻ��������˿ں� -->
				+ "<Property  name = 'port' vaule = '"+ stbPort +"' /> " 
				// <!�� ����url -->
				+ "<Property  name = 'url' vaule = '" + url + "' /> "
				// <!�� ��Ŀ���� -->
				+ "<Property  name = 'title' vaule = '" + title + "' /> "
				// <!�� ��Ŀʱ�� -->
				+ "<Property  name = 'duration' vaule = '" + duration + "' /> "
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
}

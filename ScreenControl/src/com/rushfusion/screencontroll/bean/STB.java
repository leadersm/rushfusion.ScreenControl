package com.rushfusion.screencontroll.bean;

public class STB {
	String ip;
	String taskno;
	String username;
	String password;
	String mcid;
	
	
	public STB(String ip, String taskno, String username, String password,
			String mcid) {
		super();
		this.ip = ip;
		this.taskno = taskno;
		this.username = username;
		this.password = password;
		this.mcid = mcid;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getTaskno() {
		return taskno;
	}
	public void setTaskno(String taskno) {
		this.taskno = taskno;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getMcid() {
		return mcid;
	}
	public void setMcid(String mcid) {
		this.mcid = mcid;
	}
	public STB() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
}

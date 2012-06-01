package com.rushfusion.screencontroll.bean;

public class STB {
	String ip;
	String mcid;
	String password;
	PlayStatus playStatus;
	String taskno;
	String username;

	public STB() {
	}

	public STB(String paramString1, String paramString2, String paramString3,
			String paramString4, String paramString5) {
		this.ip = paramString1;
		this.taskno = paramString2;
		this.username = paramString3;
		this.password = paramString4;
		this.mcid = paramString5;
	}

	public STB(String paramString1, String paramString2, String paramString3,
			String paramString4, String paramString5, PlayStatus paramPlayStatus) {
		this.ip = paramString1;
		this.taskno = paramString2;
		this.username = paramString3;
		this.password = paramString4;
		this.mcid = paramString5;
		this.playStatus = paramPlayStatus;
	}

	public String getIp() {
		return this.ip;
	}

	public String getMcid() {
		return this.mcid;
	}

	public String getPassword() {
		return this.password;
	}

	public PlayStatus getPlayStatus() {
		return this.playStatus;
	}

	public String getTaskno() {
		return this.taskno;
	}

	public String getUsername() {
		return this.username;
	}

	public void setIp(String paramString) {
		this.ip = paramString;
	}

	public void setMcid(String paramString) {
		this.mcid = paramString;
	}

	public void setPassword(String paramString) {
		this.password = paramString;
	}

	public void setPlayStatus(PlayStatus paramPlayStatus) {
		this.playStatus = paramPlayStatus;
	}

	public void setTaskno(String paramString) {
		this.taskno = paramString;
	}

	public void setUsername(String paramString) {
		this.username = paramString;
	}

	@Override
	public String toString() {
		return "STB [ip=" + ip + ", mcid=" + mcid + ", password=" + password
				+ ", playStatus=" + playStatus + ", taskno=" + taskno
				+ ", username=" + username + "]";
	}

}
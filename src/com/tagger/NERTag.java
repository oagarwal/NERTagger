package com.tagger;

public class NERTag {
	
	String token;
	String tag;
	
	public NERTag(String token, String tag){
		this.token = token;
		this.tag = tag;
	}
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public String getTag() {
		return tag;
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}
	
}

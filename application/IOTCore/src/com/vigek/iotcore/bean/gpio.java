package com.vigek.iotcore.bean;

public class gpio {

	private String gpio_name;
	private int    gpio_number;
	
	public gpio(){
		
	}
	
	public gpio(String name, int num)
	{
		gpio_name = name;
		gpio_number = num;
	}
	
	public String getGpio_name() {
		return gpio_name;
	}
	public void setGpio_name(String gpio_name) {
		this.gpio_name = gpio_name;
	}
	public int getGpio_number() {
		return gpio_number;
	}
	public void setGpio_number(int gpio_number) {
		this.gpio_number = gpio_number;
	}
	
	
}

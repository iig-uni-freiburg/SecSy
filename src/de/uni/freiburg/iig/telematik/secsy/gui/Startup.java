package de.uni.freiburg.iig.telematik.secsy.gui;

public class Startup {
	
	public static void main(String[] args) throws Exception{
		System.setProperty( "com.apple.mrj.application.apple.menu.about.name", "SecSy" );
		System.setProperty( "com.apple.macos.useScreenMenuBar", "true" );
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		new Simulator();
	}

}

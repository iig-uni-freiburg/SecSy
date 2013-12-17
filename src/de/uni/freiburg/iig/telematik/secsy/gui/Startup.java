package de.uni.freiburg.iig.telematik.secsy.gui;

public class Startup {
	
	public static void main(String[] args) {
		String osType = System.getProperty("os.name");
		if(osType.equals("Mac OS") || osType.equals("Mac OS X")){
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SecSy");
			System.setProperty("com.apple.macos.useScreenMenuBar", "true");
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SecSy");
		}
		new Simulator();
	}

}

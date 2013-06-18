package gui;

public class Startup {
	
	public static void main(String[] args) throws Exception{
		System.setProperty( "com.apple.mrj.application.apple.menu.about.name", "Ted" );
		System.setProperty( "com.apple.macos.useScreenMenuBar", "true" );
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		new Simulator();
	}

}

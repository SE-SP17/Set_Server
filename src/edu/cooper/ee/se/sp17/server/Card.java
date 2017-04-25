package edu.cooper.ee.se.sp17.server;

import java.util.Random;

public class Card {
	public static int S_OVAL = 1;
	public static int S_SQUG = 2;
	public static int S_DIAM = 3;

	public static int C_RED = 1;
	public static int C_PUR = 2;
	public static int C_GRN = 3;
	
	//public static int N_ONE = 1;
	
	public static int SH_SOL = 1;
	public static int SH_STR = 2;
	public static int SH_OTL = 3;
	
	public int shape, color, num, shade;
	Random rn = new Random();
	
	public Card(int shp, int c, int n, int shd){
		shape = shp;
		color = c;
		num = n;
		shade = shd;
	}
	
	public Card(){
		random();
	}
	
	public void random(){
		shape = rn.nextInt(3)+1;
		color = rn.nextInt(3)+1;
		num = rn.nextInt(3)+1;
		shade = rn.nextInt(3)+1;
	}
	
	public String toString(){
		return shape + " " + color + " " + num + " " + shade;
	}
}

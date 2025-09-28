package me.mxngo.ui;

public enum ProfileTheme {
	DEFAULT(0xff9984f9, 0xff050114, 0xffab99ff, 0xff0d0529),
	RED(0xfff25a5a, 0xff571414, 0xfff56c6c, 0xff8a1717),
	GREEN(0xff6edb94, 0xff10381e, 0xff78de9b, 0xff174f2b),
	BLUE(0xff69a7e0, 0xff06121f, 0xff75b0e6, 0xff0b1e33),
	PINK(0xffdc78de, 0xff1e0a1f, 0xffe878eb, 0xff341136),
	YELLOW(0xffd6a33a, 0xff000000, 0xfffabe43, 0xff3d2e07),
	GREY(0xffcfcfcf, 0xff000000, 0xffdbdbdb, 0xff3b3b3b),
	BRONZE(0xffdb8858, 0xff471e06, 0xffeb9654, 0xff59280b);
	
	private int bgStart, bgEnd, borderStart, borderEnd;
	
	private ProfileTheme(int bgStart, int bgEnd, int borderStart, int borderEnd) {
		this.bgStart = bgStart;
		this.bgEnd = bgEnd;
		this.borderStart = borderStart;
		this.borderEnd = borderEnd;
	}
	
	public int getBackgroundStart() {
		return this.bgStart;
	}
	
	public int getBackgroundEnd() {
		return this.bgEnd;
	}
	
	public int getBorderStart() {
		return this.borderStart;
	}
	
	public int getBorderEnd() {
		return this.borderEnd;
	}
}

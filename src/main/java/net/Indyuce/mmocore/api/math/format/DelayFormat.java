package net.Indyuce.mmocore.api.math.format;

public class DelayFormat {
	private int display = charArray.length;

	private static final long[] millisArray = { 31557081600l, 2629756800l, 86400000l, 3600000l, 60000l, 1000l };
	private static final String[] charArray = { "y", "M", "d", "h", "m", "s" };

	public DelayFormat() {
		this(charArray.length);
	}

	public DelayFormat(int display) {
		this.display = Math.min(display, charArray.length);
	}

	public String format(long ms) {
		String format = "";

		for (int j = 0; j < charArray.length && display > 0; j++)
			if (ms > millisArray[j]) {
				format += (ms / millisArray[j]) + charArray[j] + " ";
				ms = ms % millisArray[j];
				display--;
			}

		return format.equals("") ? "Now!" : format.substring(0, format.length() - 1);
	}
}

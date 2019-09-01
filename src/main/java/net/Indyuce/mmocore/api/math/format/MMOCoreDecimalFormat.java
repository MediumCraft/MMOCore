package net.Indyuce.mmocore.api.math.format;

import java.text.DecimalFormat;

import net.Indyuce.mmocore.MMOCore;

public class MMOCoreDecimalFormat extends DecimalFormat {
	private static final long serialVersionUID = -2794789471349113852L;

	public MMOCoreDecimalFormat(String str) {
		super(str);
		
		setDecimalFormatSymbols(MMOCore.plugin.configManager.formatSymbols);
	}
}

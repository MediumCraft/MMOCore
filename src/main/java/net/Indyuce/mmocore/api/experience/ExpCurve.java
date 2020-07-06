package net.Indyuce.mmocore.api.experience;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

public class ExpCurve {
	private final String id;

	/*
	 * experience needed to level up. different professions or classes can have
	 * different exp curves so that it is easier to balance.
	 */
	private final List<Integer> experience = new ArrayList<>();

	/*
	 * purely arbitrary but MMOCore needs a default exp curve for everything
	 * otherwise there might be divisions by 0 when trying to update the vanilla
	 * exp bar which requires a 0.0 -> 1.0 float as parameter
	 */
	public static final ExpCurve DEFAULT = new ExpCurve("default", 100, 200, 300, 400, 500);

	/*
	 * read exp curve from a file, one line after the other
	 */
	public ExpCurve(File file) throws IOException {
		this.id = file.getName().replace(".txt", "").toLowerCase().replace("_", "-").replace(" ", "-");

		BufferedReader reader = new BufferedReader(new FileReader(file));
		String readLine;
		while ((readLine = reader.readLine()) != null)
			experience.add(Integer.valueOf(readLine));
		reader.close();

		Validate.isTrue(!experience.isEmpty(), "There must be at least one exp value in your exp curve");
	}

	/*
	 * can be used by external plugins to register curves and it used by MMOCore
	 * to create the default exp curve if none is selected
	 */
	public ExpCurve(String id, int... values) {
		this.id = id;
		for (int value : values)
			experience.add(value);
		Validate.isTrue(!experience.isEmpty(), "There must be at least one exp value in your exp curve");
	}

	public String getId() {
		return id;
	}

	/*
	 * retrieves the experience needed. the level serves as index for the list
	 * checkup. if the level is higher than the amount of exp inputs, just
	 * return the last list value
	 */
	public int getExperience(int level) {
		Validate.isTrue(level > 0, "Level must be stricly positive");
		return experience.get(Math.min(level, experience.size()) - 1);
	}
}

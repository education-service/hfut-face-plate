package edu.hfut.fr.image.processing.morphology;

/**
 * 结构化组件的格雷字母
 *
 * @author Jimbo
 */
public class GolayAlphabet {

	public final static StructuringElement[] H;

	public final static StructuringElement[] I;

	public final static StructuringElement[] E;

	public final static StructuringElement[] L;

	static {
		H = new StructuringElement[] { StructuringElement.parseElement("***\n***\n***", 1, 1) };

		I = new StructuringElement[] { StructuringElement.parseElement("ooo\nooo\nooo", 1, 1) };

		E = new StructuringElement[] { StructuringElement.parseElement("...\no*o\nooo", 1, 1),
				StructuringElement.parseElement("oo.\no*.\noo.", 1, 1),
				StructuringElement.parseElement("ooo\no*o\n...", 1, 1),
				StructuringElement.parseElement(".oo\n.*o\n.oo", 1, 1) };

		L = new StructuringElement[] { StructuringElement.parseElement("ooo\n.*.\n***", 1, 1),
				StructuringElement.parseElement(".oo\n**o\n.*.", 1, 1),

				StructuringElement.parseElement("*.o\n**o\n*.o", 1, 1),
				StructuringElement.parseElement(".*.\n**o\n.oo", 1, 1),

				StructuringElement.parseElement("***\n.*.\nooo", 1, 1),
				StructuringElement.parseElement(".*.\no**\noo.", 1, 1),

				StructuringElement.parseElement("o.*\no**\no.*", 1, 1),
				StructuringElement.parseElement("oo.\no**\n.*.", 1, 1), };
	}

}

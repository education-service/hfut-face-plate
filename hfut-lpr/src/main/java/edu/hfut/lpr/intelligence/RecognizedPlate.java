package edu.hfut.lpr.intelligence;

import java.util.Vector;

import edu.hfut.lpr.recognizer.CharacterRecognizer.RecognizedChar;

/**
 * 已经识别的车牌类
 *
 * @author wanggang
 *
 */
public class RecognizedPlate {

	Vector<RecognizedChar> chars;

	public RecognizedPlate() {
		this.chars = new Vector<RecognizedChar>();
	}

	public void addChar(RecognizedChar chr) {
		this.chars.add(chr);
	}

	public RecognizedChar getChar(int i) {
		return this.chars.elementAt(i);
	}

	public String getString() {
		String ret = "";
		for (int i = 0; i < this.chars.size(); i++) {
			ret = ret + this.chars.elementAt(i).getPattern(0).getChar();
		}
		return ret;
	}

}
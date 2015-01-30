package edu.hfut.lpr.tackle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


//import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.hfut.lpr.core.CharRecognizer.RecognizedChar;
import edu.hfut.lpr.run.SimpleLPR;
import edu.hfut.lpr.utils.ConfigUtil;

/**
 * 解析器
 *
 * @author wanggang
 *
 */
public class XMLParser {

	/**
	 * 车牌表单
	 */
	public class PlateForm {

		/**
		 * 车牌位置
		 */
		public class Position {

			public char[] allowedChars;

			public Position(String data) {
				this.allowedChars = data.toCharArray();
			}

			public boolean isAllowed(char chr) {
				boolean ret = false;
				for (int i = 0; i < this.allowedChars.length; i++) {
					if (this.allowedChars[i] == chr) {
						ret = true;
					}
				}
				return ret;
			}

		}

		Vector<Position> positions;
		String name;
		public boolean flagged = false;

		public PlateForm(String name) {
			this.name = name;
			this.positions = new Vector<>();
		}

		public void addPosition(Position p) {
			this.positions.add(p);
		}

		public Position getPosition(int index) {
			return this.positions.elementAt(index);
		}

		public int length() {
			return this.positions.size();
		}

	}

	/**
	 * 最终车牌
	 */
	public class FinalPlate {

		public String plate;
		public float requiredChanges = 0;

		FinalPlate() {
			this.plate = "";
		}

		public void addChar(char chr) {
			this.plate = this.plate + chr;
		}

	}

	Vector<PlateForm> plateForms;

	/**
	 * 创建解析器示例
	 */
	public XMLParser() throws ParserConfigurationException, SAXException, IOException {

		this.plateForms = new Vector<>();

		String fileName = ConfigUtil.getConfigurator().getPathProperty("intelligence_syntaxDescriptionFile");

		if (fileName == null || fileName.isEmpty()) {
			throw new IOException("Failed to get syntax description file from Configurator");
		}

		InputStream inStream = ConfigUtil.getConfigurator().getResourceAsStream(fileName);

		if (inStream == null) {
			throw new IOException("Couldn't find parser syntax description file");
		}

		try {
			this.plateForms = this.loadFromXml(inStream);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			System.err.println("Failed to load from parser syntax description file");
			throw e;
		}

	}

	/**
	 * @deprecated use {@link XMLParser#loadFromXml(InputStream)}
	 */
	@Deprecated
	public Vector<PlateForm> loadFromXml(String fileName) throws ParserConfigurationException, SAXException,
			IOException {
		InputStream inStream = ConfigUtil.getConfigurator().getResourceAsStream(fileName);
		return this.loadFromXml(inStream);
	}

	/**
	 * 加载XML文件
	 */
	public Vector<PlateForm> loadFromXml(InputStream inStream) throws ParserConfigurationException, SAXException,
			IOException {

		Vector<PlateForm> plateForms = new Vector<>();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		DocumentBuilder parser = factory.newDocumentBuilder();
		Document doc = parser.parse(inStream);

		Node structureNode = doc.getDocumentElement();
		NodeList structureNodeContent = structureNode.getChildNodes();
		for (int i = 0; i < structureNodeContent.getLength(); i++) {
			Node typeNode = structureNodeContent.item(i);
			if (!typeNode.getNodeName().equals("type")) {
				continue;
			}
			PlateForm form = new PlateForm(((Element) typeNode).getAttribute("name"));
			NodeList typeNodeContent = typeNode.getChildNodes();
			for (int ii = 0; ii < typeNodeContent.getLength(); ii++) {
				Node charNode = typeNodeContent.item(ii);
				if (!charNode.getNodeName().equals("char")) {
					continue;
				}
				String content = ((Element) charNode).getAttribute("content");

				form.addPosition(form.new Position(content.toUpperCase()));
			}
			plateForms.add(form);
		}

		return plateForms;
	}

	/**
	 * 设置所有标识为false
	 */
	public void unFlagAll() {
		for (PlateForm form : this.plateForms) {
			form.flagged = false;
		}
	}

	/**
	 * 对不大于指定长度的表单进行true设置
	 */
	public void flagEqualOrShorterLength(int length) {
		boolean found = false;
		for (int i = length; (i >= 1) && !found; i--) {
			for (PlateForm form : this.plateForms) {
				if (form.length() == i) {
					form.flagged = true;
					found = true;
				}
			}
		}
	}

	/**
	 * 对等于指定长度的表单进行true设置
	 */
	public void flagEqualLength(int length) {
		for (PlateForm form : this.plateForms) {
			if (form.length() == length) {
				form.flagged = true;
			}
		}
	}

	/**
	 * 反转标识
	 */
	public void invertFlags() {
		for (PlateForm form : this.plateForms) {
			form.flagged = !form.flagged;
		}
	}

	/**
	 * 解析操作
	 *
	 * 符号分析三个模式：
	 *    0--------不进行解析
	 *    1--------只对相等的长度进行解析
	 *    2--------只对不大于的长度进行解析
	 * @param recognizedPlate 已经识别的车牌
	 * @param syntaxAnalysisMode 符号分析模式
	 */
	public String parse(TackledPlate recognizedPlate, int syntaxAnalysisMode) {

		if (syntaxAnalysisMode == 0) {
			SimpleLPR.rg.insertText(" result : " + recognizedPlate.getString() + " --> <font size=15>"
					+ recognizedPlate.getString() + "</font><hr><br>");
			return recognizedPlate.getString();
		}

		int length = recognizedPlate.chars.size();
		this.unFlagAll();
		if (syntaxAnalysisMode == 1) {
			this.flagEqualLength(length);
		} else {
			this.flagEqualOrShorterLength(length);
		}

		Vector<FinalPlate> finalPlates = new Vector<>();

		for (PlateForm form : this.plateForms) {
			if (!form.flagged) {
				continue;
			}
			for (int i = 0; i <= (length - form.length()); i++) {
				//				System.out.println("comparing " + recognizedPlate.getString() + " with form " + form.name
				//						+ " and offset " + i);
				FinalPlate finalPlate = new FinalPlate();
				for (int ii = 0; ii < form.length(); ii++) {
					//					 form.getPosition(ii).allowedChars
					RecognizedChar rc = recognizedPlate.getChar(ii + i);
					if (form.getPosition(ii).isAllowed(rc.getPattern(0).getChar())) {
						finalPlate.addChar(rc.getPattern(0).getChar());
					} else {
						finalPlate.requiredChanges++;
						for (int x = 0; x < rc.getPatterns().size(); x++) {
							if (form.getPosition(ii).isAllowed(rc.getPattern(x).getChar())) {
								RecognizedChar.RecognizedPattern rp = rc.getPattern(x);
								finalPlate.requiredChanges += (rp.getCost() / 100);
								finalPlate.addChar(rp.getChar());
								break;
							}
						}
					}
				}
				//				System.out.println("adding " + finalPlate.plate + " with required changes "
				//						+ finalPlate.requiredChanges);
				finalPlates.add(finalPlate);
			}
		}

		if (finalPlates.size() == 0) {
			return recognizedPlate.getString();
		}

		float minimalChanges = Float.POSITIVE_INFINITY;
		int minimalIndex = 0;

		for (int i = 0; i < finalPlates.size(); i++) {
			//			System.out.println("::" + finalPlates.elementAt(i).plate + " " + finalPlates.elementAt(i).requiredChanges);
			if (finalPlates.elementAt(i).requiredChanges <= minimalChanges) {
				minimalChanges = finalPlates.elementAt(i).requiredChanges;
				minimalIndex = i;
			}
		}

		String toReturn = recognizedPlate.getString();
		if (finalPlates.elementAt(minimalIndex).requiredChanges <= 2) {
			toReturn = finalPlates.elementAt(minimalIndex).plate;
		}

		return toReturn;
	}

}
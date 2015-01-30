package edu.hfut.lpr.utils;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

/**
 * 配置文件类
 *
 * @author wanggang
 *
 */
public class ConfUtil {

	private static ConfUtil configurator;

	/* 默认的配置文件名 */
	private String fileName = "config.xml";

	/* 配置文件解释 */
	private String comment = "分布式车牌识别系统的全局配置文件";

	/* 包含配置文件中的所有配置属性 */
	private Properties list;

	public ConfUtil() throws IOException {
		this.list = new Properties();

		/********* Start：默认属性值定义 ********/

		// 自适应阈值半径 (0代表不使用自适应调整策略)，推荐使用7
		this.setIntProperty("photo_adaptivethresholdingradius", 7);

		this.setDoubleProperty("bandgraph_peakfootconstant", 0.55); // 0.75
		this.setDoubleProperty("bandgraph_peakDiffMultiplicationConstant", 0.2);

		this.setIntProperty("carsnapshot_distributormargins", 25);
		this.setIntProperty("carsnapshot_graphrankfilter", 9);

		this.setDoubleProperty("carsnapshotgraph_peakfootconstant", 0.55); // 0.55
		this.setDoubleProperty("carsnapshotgraph_peakDiffMultiplicationConstant", 0.1);

		this.setIntProperty("intelligence_skewdetection", 0);

		// this.setDoubleProperty("char_contrastnormalizationconstant", 0.5); // 1.0

		this.setIntProperty("char_normalizeddimensions_x", 8); // 8
		this.setIntProperty("char_normalizeddimensions_y", 13); // 13
		this.setIntProperty("char_resizeMethod", 1); // 0=linear 1=average
		this.setIntProperty("char_featuresExtractionMethod", 0); // 0=map, 1=edge
		this.setStrProperty("char_neuralNetworkPath", "/neuralnetworks/network_avgres_813_map.xml");
		this.setStrProperty("char_learnAlphabetPath", "/alphabets/alphabet_8x13");
		this.setIntProperty("intelligence_classification_method", 0); // 0=pattern match,1=ann

		this.setDoubleProperty("plategraph_peakfootconstant", 0.7);

		this.setDoubleProperty("plategraph_rel_minpeaksize", 0.86); // 0.85

		this.setDoubleProperty("platehorizontalgraph_peakfootconstant", 0.05);
		this.setIntProperty("platehorizontalgraph_detectionType", 1); // 1=edgedetection, 0=magnitudederivate

		this.setDoubleProperty("plateverticalgraph_peakfootconstant", 0.42);

		this.setIntProperty("intelligence_numberOfBands", 3);
		this.setIntProperty("intelligence_numberOfPlates", 3);
		this.setIntProperty("intelligence_numberOfChars", 20);

		this.setIntProperty("intelligence_minimumChars", 5);
		this.setIntProperty("intelligence_maximumChars", 15);

		// 车牌启发式处理参数
		this.setDoubleProperty("intelligence_maxCharWidthDispersion", 0.5); // in plate
		this.setDoubleProperty("intelligence_minPlateWidthHeightRatio", 0.5);
		this.setDoubleProperty("intelligence_maxPlateWidthHeightRatio", 15.0);

		// 字符启发式处理参数
		this.setDoubleProperty("intelligence_minCharWidthHeightRatio", 0.1);
		this.setDoubleProperty("intelligence_maxCharWidthHeightRatio", 0.92);
		this.setDoubleProperty("intelligence_maxBrightnessCostDispersion", 0.161);
		this.setDoubleProperty("intelligence_maxContrastCostDispersion", 0.1);
		this.setDoubleProperty("intelligence_maxHueCostDispersion", 0.145);
		this.setDoubleProperty("intelligence_maxSaturationCostDispersion", 0.24); // 0.15
		this.setDoubleProperty("intelligence_maxHeightCostDispersion", 0.2);
		this.setDoubleProperty("intelligence_maxSimilarityCostDispersion", 100);

		// 识别参数
		this.setIntProperty("intelligence_syntaxanalysis", 2);
		this.setStrProperty("intelligence_syntaxDescriptionFile", "/syntax/syntax.xml");

		// int maxK, double eps, double lambda, double micro
		this.setIntProperty("neural_maxk", 8000); // 最大迭代次数
		this.setDoubleProperty("neural_eps", 0.07); // 终止条件
		this.setDoubleProperty("neural_lambda", 0.05); // 学习率
		this.setDoubleProperty("neural_micro", 0.5); // 微调因子
		this.setIntProperty("neural_topology", 20); // 神经网络拓扑结构，即层数

		/********* End：默认属性值定义 ********/

		// 帮助信息和报告生成信息
		this.setStrProperty("help_file_help", "/help/help.html");
		this.setStrProperty("help_file_about", "/help/about.html");
		this.setStrProperty("reportgeneratorcss", "/reportgenerator/style.css");

		InputStream is = this.getResourceAsStream(this.fileName);

		if (is != null) {
			this.loadConfiguration(is);
			is.close();
		}

		ConfUtil.configurator = this;
	}

	public void setConfigurationFileName(String name) {
		this.fileName = name;
	}

	public String getConfigurationFileName() {
		return this.fileName;
	}

	public String getStrProperty(String name) {
		return this.list.getProperty(name).toString();
	}

	public String getPathProperty(String name) {
		return this.getStrProperty(name).replace('/', File.separatorChar);

	}

	public void setStrProperty(String name, String value) {
		this.list.setProperty(name, value);
	}

	public int getIntProperty(String name) throws NumberFormatException {
		return Integer.decode(this.list.getProperty(name));
	}

	public void setIntProperty(String name, int value) {
		this.list.setProperty(name, String.valueOf(value));
	}

	public double getDoubleProperty(String name) throws NumberFormatException {
		return Double.parseDouble(this.list.getProperty(name));
	}

	public void setDoubleProperty(String name, double value) {
		this.list.setProperty(name, String.valueOf(value));
	}

	public Color getColorProperty(String name) {
		return new Color(Integer.decode(this.list.getProperty(name)));
	}

	public void setColorProperty(String name, Color value) {
		this.list.setProperty(name, String.valueOf(value.getRGB()));
	}

	public void saveConfiguration() throws IOException {
		this.saveConfiguration(this.fileName);
	}

	public void saveConfiguration(String arg_file) throws IOException {
		FileOutputStream os = new FileOutputStream(arg_file);
		this.list.storeToXML(os, this.comment);
		os.close();
	}

	public void loadConfiguration() throws InvalidPropertiesFormatException, IOException {
		this.loadConfiguration(this.fileName);
	}

	public void loadConfiguration(String arg_file) throws InvalidPropertiesFormatException, IOException {
		InputStream is = this.getResourceAsStream(arg_file);

		this.loadConfiguration(is);
		is.close();
	}

	public void loadConfiguration(InputStream arg_stream) throws InvalidPropertiesFormatException, IOException {
		if (arg_stream == null) {
			this.list = null;
			return;
		}

		this.list.loadFromXML(arg_stream);
	}

	public InputStream getResourceAsStream(String filename) {

		String corrected = filename;

		URL f = this.getClass().getResource(corrected);
		//		URL f = this.getClass().getClassLoader().getResource(corrected);
		if (f != null) {
			return this.getClass().getResourceAsStream(corrected);
			//			return this.getClass().getResourceAsStream(corrected);
		}

		if (filename.startsWith("/")) {
			corrected = filename.substring(1);
		} else if (filename.startsWith("./")) {
			corrected = filename.substring(2);
		} else {
			corrected = "/" + filename;
		}

		f = this.getClass().getResource(corrected);
		//		f = this.getClass().getClassLoader().getResource(corrected);

		if (f != null) {
			return this.getClass().getResourceAsStream(corrected);
			//			return this.getClass().getClassLoader().getResourceAsStream(corrected);
		}

		File file = new File(filename);
		if (file.exists()) {
			FileInputStream fis = null;

			try {
				fis = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			return fis;
		}

		return null;
	}

	public static ConfUtil getConfigurator() {
		if (configurator == null) {
			try {
				configurator = new ConfUtil();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		return configurator;
	}

}
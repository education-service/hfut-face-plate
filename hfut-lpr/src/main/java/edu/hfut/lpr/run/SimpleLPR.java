package edu.hfut.lpr.run;

import java.io.File;
import java.io.IOException;

import javax.swing.UIManager;

import edu.hfut.lpr.core.ANNClassificator;
import edu.hfut.lpr.frame.FrameCompInit;
import edu.hfut.lpr.frame.FrameCore;
import edu.hfut.lpr.frame.Reporter;
import edu.hfut.lpr.images.CarSnapshot;
import edu.hfut.lpr.images.Char;
import edu.hfut.lpr.tackle.TackleCore;
import edu.hfut.lpr.utils.ConfigUtil;

/**
 * <p>简单的车牌识别，通过命令行输入参数</p>
 * <p/>
 * 可选参数如下：
 * <ul>
 * <li>-help  帮助信息。
 * <li>-gui   运行可视化界面（默认）。
 * <li>-recognize -i "snapshot"  识别单个图片。
 * <li>-recognize -i "snapshot" -o "dstdir"   识别单个图片并保存报告信息到指定目录。
 * <li>-newconfig -o "file"    生成默认配置文件
 * <li>-newnetwork -o "file"   根据选取的特征提取方法和学习参数（在config.xml中）来训练神经网络，并将训练好的模型数据保存到输出文件中。
 * <li>-newalphabet -i "srcdir" -o "dstdir"  标准化目录srcdir中的所有图片，并保存到目录dstdir中。
 * </ul>
 *
 * @author wanggang
 *
 */
public class SimpleLPR {

	// 报告生成器
	public static Reporter rg = new Reporter();

	// 智能信息处理
	public static TackleCore systemLogic;

	/**
	 * 主函数
	 */
	public static void main(String[] args) throws Exception {

		if ((args.length == 0) || ((args.length == 1) && args[0].equals("-gui"))) {
			// DONE run gui
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			FrameCompInit frameComponentInit = new FrameCompInit(); // 显示等待
			SimpleLPR.systemLogic = new TackleCore();
			frameComponentInit.dispose(); // 隐藏等待
			new FrameCore();
		} else if ((args.length == 3) && args[0].equals("-recognize") && args[1].equals("-i")) {
			// 加载arg[2]中的车辆图片，并进行车牌识别
			try {
				SimpleLPR.systemLogic = new TackleCore();
				System.out.println(SimpleLPR.systemLogic.recognize(new CarSnapshot(args[2])));
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		} else if ((args.length == 5) && args[0].equals("-recognize") && args[1].equals("-i") && args[3].equals("-o")) {
			// 加载arg[2]中的车辆图片，生成报告信息到arg[4]中
			try {
				SimpleLPR.rg = new Reporter(args[4]);
				SimpleLPR.systemLogic = new TackleCore();
				SimpleLPR.systemLogic.recognizeWithReport(new CarSnapshot(args[2]));
				SimpleLPR.rg.finish();
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}

		} else if ((args.length == 3) && args[0].equals("-newconfig") && args[1].equals("-o")) {
			// 保存配置文件信息到args[2]中
			ConfigUtil configurator = new ConfigUtil();
			try {
				configurator.saveConfiguration(args[2]);
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		} else if ((args.length == 3) && args[0].equals("-newnetwork") && args[1].equals("-o")) {
			// 训练新的神经网络，并保存网络模型信息到args[2]中
			try {
				SimpleLPR.learnAlphabet(args[2]);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		} else if ((args.length == 5) && args[0].equals("-newalphabet") && args[1].equals("-i") && args[3].equals("-o")) {
			// 标将args[2]中的图片准化到args[4]中
			try {
				SimpleLPR.newAlphabet(args[2], args[4]);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		} else {
			// 打印帮助信息
			System.out.println(SimpleLPR.helpText);
		}

	}

	// 帮助信息
	public static String helpText = "" + "-----------------------------------------------------------\n"
			+ "分布式车牌识别系统\n\n" //
			+ "使用方法 : java -jar LPR.jar [-options]\n\n" //
			+ "options包括:\n\n" //
			+ "    -help       提示帮助信息。\n" //
			+ "    -gui        运行可视化界面 (默认)。\n" //
			+ "    -recognize -i <snapshot>\n         识别单个图片。\n" //
			+ "    -recognize -i <snapshot> -o <dstdir>\n         识别单个图片，并保存报告信息到指定目录。\n" //
			+ "    -newconfig -o <file>\n          生成默认的配置文件。\n" //
			+ "    -newnetwork -o <file>\n         根据指定的特征提取方法和学习参数训练神经网络（配置文为config.xml），并保存到输出文件中。\n" //
			+ "    -newalphabet -i <srcdir> -o <dstdir>\n         标准化目录<srcdir>中的所有图片，并保存到目录<dstdir>中。";

	/**
	 * 标准化目录srcdir中的所有图片，并保存到目录dstdir中。
	 */
	public static void newAlphabet(String srcdir, String dstdir) throws IOException {

		int x = ConfigUtil.getConfigurator().getIntProperty("char_normalizeddimensions_x");
		int y = ConfigUtil.getConfigurator().getIntProperty("char_normalizeddimensions_y");
		System.out.println("\nCreating new alphabet (" + x + " x " + y + " px)... \n");

		for (String fileName : Char.getAlphabetList(srcdir)) {
			Char c = new Char(fileName);
			c.normalize();
			c.saveImage(dstdir + File.separator + fileName);
			System.out.println(fileName + " done");
			c.close();
		}
	}

	/**
	 * 根据选取的特征提取方法和学习参数（在config.xml中）来训练神经网络，并将训练好的模型数据保存到输出文件中。
	 */
	public static void learnAlphabet(String destinationFile) throws Exception {
		try {
			File f = new File(destinationFile);
			f.createNewFile();
		} catch (Exception e) {
			throw new IOException("Can't find the path specified");
		}
		System.out.println();
		ANNClassificator npc = new ANNClassificator(true);
		npc.network.saveToXml(destinationFile);
	}

}

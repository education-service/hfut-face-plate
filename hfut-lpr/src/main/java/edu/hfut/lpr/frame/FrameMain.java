package edu.hfut.lpr.frame;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import edu.hfut.lpr.images.CarSnapshot;
import edu.hfut.lpr.images.Photo;
import edu.hfut.lpr.run.SimpleLPR;

/**
 * Frame主控函数
 *
 * @author wanggang
 *
 */
public class FrameMain extends javax.swing.JFrame {

	private static final long serialVersionUID = 1283761798262612123L;

	/**
	 * 车牌识别处理线程类
	 */
	public class RecognizeThread extends Thread {

		FrameMain parentFrame = null;

		public RecognizeThread(FrameMain parentFrame) {
			this.parentFrame = parentFrame;
		}

		@Override
		public void run() {
			String recognizedText = "";
			this.parentFrame.recognitionLabel.setText("处理中 ...");
			int index = this.parentFrame.selectedIndex;
			try {
				recognizedText = SimpleLPR.systemLogic.recognize(this.parentFrame.car);
			} catch (Exception ex) {
				this.parentFrame.recognitionLabel.setText("图像像素太低");
				return;
			}
			this.parentFrame.recognitionLabel.setText(recognizedText);
			this.parentFrame.fileListModel.fileList.elementAt(index).recognizedPlate = recognizedText;
		}

	}

	/**
	 * 加载图像线程类
	 */
	public class LoadImageThread extends Thread {

		FrameMain parentFrame = null;
		String url = null;

		public LoadImageThread(FrameMain parentFrame, String url) {
			this.parentFrame = parentFrame;
			this.url = url;
		}

		@Override
		public void run() {
			try {
				this.parentFrame.car = new CarSnapshot(this.url);
				this.parentFrame.panelCarContent = this.parentFrame.car.duplicate().getBi();
				this.parentFrame.panelCarContent = Photo.linearResizeBi(this.parentFrame.panelCarContent,
						this.parentFrame.panelCar.getWidth(), this.parentFrame.panelCar.getHeight());
				this.parentFrame.panelCar.paint(this.parentFrame.panelCar.getGraphics());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	// 车辆快照图
	CarSnapshot car;
	// 缓冲图
	BufferedImage panelCarContent;

	// Java文件选择器
	JFileChooser fileChooser;
	// 文件列表模型
	private FileListModel fileListModel;
	// 选择图像索引
	int selectedIndex = -1;

	// 创建Frame主函数
	public FrameMain() {
		this.initComponents();

		// 初始化：文件选择
		this.fileChooser = new JFileChooser();
		this.fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		this.fileChooser.setMultiSelectionEnabled(true);
		this.fileChooser.setDialogTitle("加载图片");
		// this.fileChooser.setFileFilter(new ImageFileFilter());

		// 初始化: 窗口维度和可视化
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = this.getWidth();
		int height = this.getHeight();
		this.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
		this.setVisible(true);
	}

	/**
	 * 初始化组件：在表单编辑器中用到
	 */
	private void initComponents() {
		this.recognitionLabel = new javax.swing.JLabel();
		this.panelCar = new JPanel() {
			static final long serialVersionUID = 0;

			@Override
			public void paint(Graphics g) {
				super.paint(g);
				g.drawImage(FrameMain.this.panelCarContent, 0, 0, null);
			}
		};
		this.fileListScrollPane = new javax.swing.JScrollPane();
		this.fileList = new javax.swing.JList<Object>();
		this.recognizeButton = new javax.swing.JButton();
		this.bottomLine = new javax.swing.JLabel();
		this.menuBar = new javax.swing.JMenuBar();
		this.imageMenu = new javax.swing.JMenu();
		this.openItem = new javax.swing.JMenuItem();
		this.exitItem = new javax.swing.JMenuItem();
		this.helpMenu = new javax.swing.JMenu();
		this.aboutItem = new javax.swing.JMenuItem();
		this.helpItem = new javax.swing.JMenuItem();

		this.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		this.setTitle("分布式车牌识别");
		this.setResizable(false);
		this.recognitionLabel.setBackground(new java.awt.Color(0, 0, 0));
		this.recognitionLabel.setFont(new java.awt.Font("Arial", 0, 24));
		this.recognitionLabel.setForeground(new java.awt.Color(255, 204, 51));
		this.recognitionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		this.recognitionLabel.setText(null);
		this.recognitionLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		this.recognitionLabel.setOpaque(true);

		this.panelCar.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		org.jdesktop.layout.GroupLayout panelCarLayout = new org.jdesktop.layout.GroupLayout(this.panelCar);
		this.panelCar.setLayout(panelCarLayout);
		panelCarLayout.setHorizontalGroup(panelCarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(0, 585, Short.MAX_VALUE));
		panelCarLayout.setVerticalGroup(panelCarLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
				.add(0, 477, Short.MAX_VALUE));

		this.fileListScrollPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
		this.fileListScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		this.fileList.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
		this.fileList.setFont(new java.awt.Font("Arial", 0, 11));
		this.fileList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			@Override
			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
				FrameMain.this.fileListValueChanged(evt);
			}
		});

		this.fileListScrollPane.setViewportView(this.fileList);

		this.recognizeButton.setFont(new java.awt.Font("Arial", 0, 11));
		this.recognizeButton.setText("识别车牌");
		this.recognizeButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				FrameMain.this.recognizeButtonActionPerformed(evt);
			}
		});

		this.bottomLine.setFont(new java.awt.Font("Arial", 0, 11));
		this.bottomLine.setText("版权 (c) 2015 王刚");

		this.menuBar.setFont(new java.awt.Font("Arial", 0, 11));
		this.imageMenu.setText("输入图片");
		this.imageMenu.setFont(new java.awt.Font("Arial", 0, 11));
		this.openItem.setFont(new java.awt.Font("Arial", 0, 11));
		this.openItem.setText("加载图片");
		this.openItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				FrameMain.this.openItemActionPerformed(evt);
			}
		});

		this.imageMenu.add(this.openItem);

		this.exitItem.setFont(new java.awt.Font("Arial", 0, 11));
		this.exitItem.setText("退出识别");
		this.exitItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				FrameMain.this.exitItemActionPerformed(evt);
			}
		});

		this.imageMenu.add(this.exitItem);

		this.menuBar.add(this.imageMenu);

		this.helpMenu.setText("帮助信息");
		this.helpMenu.setFont(new java.awt.Font("Arial", 0, 11));
		this.helpMenu.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				FrameMain.this.helpMenuActionPerformed(evt);
			}
		});

		this.aboutItem.setFont(new java.awt.Font("Arial", 0, 11));
		this.aboutItem.setText("关于项目");
		this.aboutItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				FrameMain.this.aboutItemActionPerformed(evt);
			}
		});

		this.helpMenu.add(this.aboutItem);

		this.helpItem.setFont(new java.awt.Font("Arial", 0, 11));
		this.helpItem.setText("如何操作");
		this.helpItem.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				FrameMain.this.helpItemActionPerformed(evt);
			}
		});

		this.helpMenu.add(this.helpItem);

		this.menuBar.add(this.helpMenu);

		this.setJMenuBar(this.menuBar);

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this.getContentPane());
		this.getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
				layout.createSequentialGroup()
						.addContainerGap()
						.add(layout
								.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
								.add(org.jdesktop.layout.GroupLayout.LEADING, this.bottomLine,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
								.add(org.jdesktop.layout.GroupLayout.LEADING, this.panelCar,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
						.add(layout
								.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
								.add(this.fileListScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190,
										Short.MAX_VALUE)
								.add(org.jdesktop.layout.GroupLayout.LEADING, this.recognitionLabel,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
								.add(this.recognizeButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190,
										Short.MAX_VALUE)).addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
				layout.createSequentialGroup()
						.addContainerGap()
						.add(layout
								.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
								.add(layout
										.createSequentialGroup()
										.add(this.fileListScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
												402, Short.MAX_VALUE)
										.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
										.add(this.recognizeButton)
										.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
										.add(this.recognitionLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 44,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
								.add(this.panelCar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(this.bottomLine)));
		this.pack();
	}

	private void helpMenuActionPerformed(java.awt.event.ActionEvent evt) {
		//
	}

	private void helpItemActionPerformed(java.awt.event.ActionEvent evt) {
		new FrameHelp(FrameHelp.SHOW_HELP);
	}

	private void aboutItemActionPerformed(java.awt.event.ActionEvent evt) {
		new FrameHelp(FrameHelp.SHOW_ABOUT);
	}

	private void recognizeButtonActionPerformed(java.awt.event.ActionEvent evt) {
		//		String plate = null;
		// 启动车牌识别线程
		new RecognizeThread(this).start();
		//		this.fileListModel.fileList.elementAt(this.selectedIndex).recognizedPlate = plate;
		//		this.label.setText(plate);
	}

	/**
	 * 文件列表值更改
	 */
	private void fileListValueChanged(javax.swing.event.ListSelectionEvent evt) {
		int selectedNow = this.fileList.getSelectedIndex();

		if ((selectedNow != -1)) {
			this.recognitionLabel.setText(this.fileListModel.fileList.elementAt(selectedNow).recognizedPlate);
			this.selectedIndex = selectedNow;
			String path = ((FileListModel.FileListModelEntry) this.fileListModel.getElementAt(selectedNow)).fullPath;
			//			this.showImage(path);
			new LoadImageThread(this, path).start();
		}
	}

	/**
	 * 退出操作
	 */
	private void exitItemActionPerformed(java.awt.event.ActionEvent evt) {
		System.exit(0);
	}

	/**
	 * 打开操作
	 */
	private void openItemActionPerformed(java.awt.event.ActionEvent evt) {
		int returnValue;

		// this.fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		// this.fileChooser.setDialogTitle("Load snapshots");
		returnValue = this.fileChooser.showOpenDialog((Component) evt.getSource());

		if (returnValue != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File[] selectedFiles = this.fileChooser.getSelectedFiles();
		this.fileListModel = new FileListModel();

		for (File selectedFile : selectedFiles) {

			if (selectedFile.isFile()) {
				this.fileListModel.addFileListModelEntry(selectedFile.getName(), selectedFile.getAbsolutePath());
			}

			else if (selectedFile.isDirectory()) {
				for (String fileName : selectedFile.list()) {
					if (ImageFileFilter.accept(fileName)) {
						this.fileListModel.addFileListModelEntry(fileName, selectedFile + File.separator + fileName);
					}
				}
			}
		}

		this.fileList.setModel(this.fileListModel);
	}

	// 声明所有用到的表量
	private javax.swing.JMenuItem aboutItem;
	private javax.swing.JLabel bottomLine;
	private javax.swing.JMenuItem exitItem;
	private javax.swing.JList<Object> fileList;
	private javax.swing.JScrollPane fileListScrollPane;
	private javax.swing.JMenuItem helpItem;
	private javax.swing.JMenu helpMenu;
	private javax.swing.JMenu imageMenu;
	private javax.swing.JMenuBar menuBar;
	private javax.swing.JMenuItem openItem;
	private javax.swing.JPanel panelCar;
	private javax.swing.JLabel recognitionLabel;
	private javax.swing.JButton recognizeButton;

}
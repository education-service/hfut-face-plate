package edu.hfut.lpr.gui.windows;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URL;

import edu.hfut.lpr.utils.Configurator;

/**
 * Frame帮助类
 *
 * @author wanggang
 *
 */
public class FrameHelp extends javax.swing.JFrame {

	private static final long serialVersionUID = -999025095118371586L;

	public static int SHOW_HELP = 0;
	public static int SHOW_ABOUT = 1;
	public int mode;

	public FrameHelp(int mode) {
		this.initComponents();
		this.mode = mode;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = this.getWidth();
		int height = this.getHeight();
		this.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
		try {
			if (mode == FrameHelp.SHOW_ABOUT) {
				URL url = getClass().getResource(Configurator.getConfigurator().getPathProperty("help_file_about"));
				System.out.println(url);
				this.editorPane.setPage(url);
			} else {
				URL url = getClass().getResource(Configurator.getConfigurator().getPathProperty("help_file_help"));
				System.out.println(url);
				this.editorPane.setPage(url);
			}
		} catch (Exception e) {
			this.dispose();
		}
		this.setVisible(true);
	}

	/**
	 * 初始化组件
	 */
	private void initComponents() {
		this.jScrollPane1 = new javax.swing.JScrollPane();
		this.editorPane = new javax.swing.JEditorPane();
		this.helpWindowClose = new javax.swing.JButton();

		this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		this.setTitle("分布式车牌识别");
		this.setResizable(false);
		this.jScrollPane1.setViewportView(this.editorPane);

		this.helpWindowClose.setFont(new java.awt.Font("Arial", 0, 11));
		this.helpWindowClose.setText("关闭");
		this.helpWindowClose.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				FrameHelp.this.helpWindowCloseActionPerformed(evt);
			}
		});

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this.getContentPane());
		this.getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
				layout.createSequentialGroup()
						.addContainerGap()
						.add(layout
								.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
								.add(org.jdesktop.layout.GroupLayout.TRAILING, this.helpWindowClose)
								.add(this.jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 514,
										Short.MAX_VALUE)).addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(
				org.jdesktop.layout.GroupLayout.TRAILING,
				layout.createSequentialGroup().addContainerGap()
						.add(this.jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE)
						.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(this.helpWindowClose)
						.addContainerGap()));
		this.pack();
	}

	/**
	 * 关闭帮助窗口
	 */
	private void helpWindowCloseActionPerformed(java.awt.event.ActionEvent evt) {
		this.dispose();
	}

	// 声明所有用到的变量
	private javax.swing.JEditorPane editorPane;
	private javax.swing.JButton helpWindowClose;
	private javax.swing.JScrollPane jScrollPane1;

}
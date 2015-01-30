package edu.hfut.lpr.frame;

import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * Frame组件初始化
 *
 * @author wanggang
 *
 */
public class FrameComponentInit extends javax.swing.JFrame {

	private static final long serialVersionUID = 1476389227096293590L;

	/**
	 * 创建新的表单FrameComponentInit
	 */
	public FrameComponentInit() {
		this.initComponents();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = this.getWidth();
		int height = this.getHeight();
		this.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
		this.setVisible(true);
	}

	/**
	 * 组件初始化，该方法在表单编辑器中调用的
	 */
	private void initComponents() {
		this.label = new javax.swing.JLabel();

		this.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		this.setTitle("初始化系统逻辑");
		this.setAlwaysOnTop(true);
		this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
		this.setFocusable(false);
		this.setResizable(false);
		this.setUndecorated(true);
		this.label.setFont(new java.awt.Font("Arial", 0, 14));
		this.label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		this.label.setText("启动应用, 稍等...");
		this.label.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this.getContentPane());
		this.getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(this.label,
				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(this.label,
				org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE));
		this.pack();
	}

	private javax.swing.JLabel label;

}
package com.uinv.gis.tileProject;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class GUI {
	public void setupUI() {
		JFrame f = new JFrame("地图瓦片生成工具v1.1");
		f.setLayout(null);
		f.setLocation(300, 300);
		f.setSize(600, 500);
		f.setBackground(Color.DARK_GRAY);
		f.setResizable(false);
		f.setVisible(true);
		JPanel panel = new JPanel();
		panel.setSize(600, 500);
		f.add(panel);
		JLabel lab = new JLabel("请选择下载好的切片存放路径", 0);
		panel.add(lab);
		JButton bu = new JButton("选择路径");
		bu.setSize(100, 30);
		panel.add(bu);

		final JButton bu1 = new JButton("打开路径");
		bu1.setSize(100, 30);
		bu1.setEnabled(false);
		panel.add(bu1);

		final JTextArea textArea = new JTextArea(20, 50);

		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		JScrollPane scrollPane = new JScrollPane(textArea);// 添加滚动条
		scrollPane.setBounds(30, 50, 535, 380);
		panel.add(scrollPane);
		textArea.setEditable(false);

		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		bu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(1);

				int returnVal = chooser.showOpenDialog(new JPanel());
				if (returnVal == 0) {
					// 在事件处理函数里面，重开一个线程，在这个新开的线程里面，执行比较耗时的计算与相应的打印内容
					new Thread(new Runnable() {
						@Override
						public void run() {
							// 比较耗时的计算与相应的打印内容代码写在这里
							// 这样可以实时输出信息
							final String path = chooser.getSelectedFile().getAbsolutePath();
							textArea.append("你打开的文件是: " + path);
							textArea.append("\r\n");
							textArea.setCaretPosition(textArea.getText().length());
							TextAreaOutputStream taOutputStream = new TextAreaOutputStream(textArea, "");
                            try {
								PrintStream ps = new PrintStream(taOutputStream, true, "utf-8");
								System.setOut(ps);
								System.setErr(ps);
                            } catch (UnsupportedEncodingException ex) {
                                throw new RuntimeException(ex);
                            }
							try {
								GISUtil.generateBundleFile(path);
								bu1.setEnabled(true);
								bu1.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										try {
											Desktop.getDesktop().open(new File(path + File.separator + "bundle"));
										} catch (IOException e2) {
											e2.printStackTrace();
											textArea.append(e2.toString());
											textArea.append("\r\n");
											textArea.setCaretPosition(textArea.getText().length());
										}
									}
								});
							} catch (Exception e1) {
								e1.printStackTrace();
								textArea.append(e1.toString());
								textArea.append("\r\n");
								textArea.setCaretPosition(textArea.getText().length());
							}
						}
					}).start();

				}
			}
		});
	}

	public static void main(String[] args) {
		new GUI().setupUI();
	}
}

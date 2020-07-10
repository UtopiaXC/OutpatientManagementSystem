package com.dlnu.oms.login;

import com.dlnu.oms.constant.Values;
import com.dlnu.oms.functions.AES;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;

public class DBSettings {
    private JPanel panel;
    private JTextField textFieldHost;
    private JPasswordField passwordField;
    private JTextField textFieldPort;
    private JTextField textFieldDBName;
    private JTextField textFieldUserName;
    private JButton ButtonSubmit;
    private static JFrame frame;

    public DBSettings(){
        setButtonSubmit();
    }

    public static void main(String[] args) {
        frame = new JFrame("数据库设置");
        frame.setContentPane(new DBSettings().panel);
        frame.setIconImage(new ImageIcon("src/com/dlnu/oms/source/logo.png").getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(500,400));
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
    }

    private void setButtonSubmit() {
        ButtonSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String Host=textFieldHost.getText();
                String Port=textFieldPort.getText();
                String DBName=textFieldDBName.getText();
                String DBUser=textFieldUserName.getText();
                //noinspection deprecation
                String DBPassword=passwordField.getText();
                try{
                    Class.forName(Values.ClassName);
                    Connection connection= DriverManager.getConnection("jdbc:mysql://"+Host+":"+Port+"/"+DBName+"?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",DBUser,DBPassword);
                }catch (Exception e1){
                    JOptionPane.showMessageDialog(frame,"无法连接数据库！","警告！", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Values.Host=Host;
                Values.Port=Port;
                Values.DB=DBName;
                Values.DBUserName=DBUser;
                Values.DBPassword=DBPassword;
                String[] settings =new String[5];
                settings[0]=Host;
                settings[1]=Port;
                settings[2]=DBName;
                settings[3]=DBUser;
                settings[4]=DBPassword;

                File file = new File(Values.Config);
                if (file.exists()) {
                    boolean succeedDeleted = file.delete();
                }
                try {
                    if (file.createNewFile()){
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
                        outputStreamWriter.append(AES.encrypt(Host,Values.AESPassword));
                        outputStreamWriter.append("\n");
                        outputStreamWriter.append(AES.encrypt(Port,Values.AESPassword));
                        outputStreamWriter.append("\n");
                        outputStreamWriter.append(AES.encrypt(DBName,Values.AESPassword));
                        outputStreamWriter.append("\n");
                        outputStreamWriter.append(AES.encrypt(DBUser,Values.AESPassword));
                        outputStreamWriter.append("\n");
                        outputStreamWriter.append(AES.encrypt(DBPassword,Values.AESPassword));
                        outputStreamWriter.append("\n");
                        outputStreamWriter.close();
                        fileOutputStream.close();
                        JOptionPane.showMessageDialog(frame,"数据库连接、配置文件写入成功！","成功！", JOptionPane.PLAIN_MESSAGE);
                        frame.dispose();
                        Login.main(settings);
                    }else{
                        JOptionPane.showMessageDialog(frame,"配置文件写入失败！","失败！", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    JOptionPane.showMessageDialog(frame,"配置文件写入失败！","失败！", JOptionPane.ERROR_MESSAGE);
                }

            }
        });
    }
}

package com.dlnu.oms.login;

import com.dlnu.oms.constant.Keys;
import com.dlnu.oms.constant.Values;
import com.dlnu.oms.functions.AES;
import com.dlnu.oms.ui.DoctorMainUI;
import com.dlnu.oms.functions.MD5;
import com.dlnu.oms.ui.PatientMainUI;
import com.dlnu.oms.ui.PharmacyMainUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Login {
    static JFrame frame;
    private JTabbedPane tabbedPane;
    private JPanel panel;
    private JTextField DoctorNum;
    private JPasswordField DoctorPass;
    private JButton DoctorLoginButton;
    private JTextField PatientName;
    private JPasswordField PatientPassword;
    private JTextField PharmacyNum;
    private JPasswordField PharmacyPassword;
    private JButton PharmacyLoginButton;
    private JButton UserRegisterButton;
    private JButton UserLoginButton;

    public Login() {
        setDoctor();
        setPatient();
        setPharmacy();
    }

    public static void main(String[] args) {
        File file = new File(Values.Config);
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            StringBuilder stringBuffer = new StringBuilder();
            while (true) {
                try {
                    if (!inputStreamReader.ready()) break;
                    stringBuffer.append((char) inputStreamReader.read());
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(frame, "配置文件存在错误，请重新添加配置！", "失败！", JOptionPane.ERROR_MESSAGE);
                    DBSettings.main(null);
                    e.printStackTrace();
                    return;
                }
            }
            try {
                Values.Host = AES.decrypt(stringBuffer.toString().split("\n")[0], Values.AESPassword);
                Values.Port = AES.decrypt(stringBuffer.toString().split("\n")[1], Values.AESPassword);
                Values.DB = AES.decrypt(stringBuffer.toString().split("\n")[2], Values.AESPassword);
                Values.DBUserName = AES.decrypt(stringBuffer.toString().split("\n")[3], Values.AESPassword);
                Values.DBPassword = AES.decrypt(stringBuffer.toString().split("\n")[4], Values.AESPassword);
                try{
                    Values.Host=args[0];
                    Values.Port=args[1];
                    Values.DB=args[2];
                    Values.DBUserName=args[3];
                    Values.DBPassword=args[4];
                }catch (Exception ignored){
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "配置文件已被损坏，请重新添加配置！"+e.toString(), "失败！", JOptionPane.ERROR_MESSAGE);
                DBSettings.main(null);
                return;
            }
        } catch (FileNotFoundException e) {
            DBSettings.main(null);
            return;
        }


        frame = new JFrame("门诊管理系统");
        frame.setContentPane(new Login().panel);
        frame.setIconImage(new ImageIcon("src/com/dlnu/oms/source/logo.png").getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(500, 300));
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
        try {
            Class.forName(Values.ClassName);
            Keys.connection = DriverManager.getConnection(Values.getURL(), Values.DBUserName, Values.DBPassword);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "数据库错误，请重新添加配置（如果是首次添加配置信息后出现该提示请尝试重启）！", "失败！", JOptionPane.ERROR_MESSAGE);
            DBSettings.main(null);
            e.printStackTrace();
            frame.dispose();
        }
    }

    public void setDoctor() {
        DoctorNum.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                DoctorEnter(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        DoctorPass.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                DoctorEnter(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        DoctorLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DoctorLogin();
            }
        });
    }

    public void setPatient() {
        PatientName.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                PatientEnter(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        PatientPassword.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                PatientEnter(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        UserLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PatientLogin();
            }
        });
    }

    public void setPharmacy() {
        PharmacyNum.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                PharmacyEnter(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        PharmacyPassword.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                PharmacyEnter(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        PharmacyLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PharmacyLogin();
            }
        });
    }

    public void DoctorEnter(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
            DoctorLogin();
    }

    public void DoctorLogin() {
        //noinspection deprecation
        if (DoctorPass.getText().equals("") || DoctorNum.getText().equals(""))
            JOptionPane.showMessageDialog(frame, "请完整填写登录信息！", "警告", JOptionPane.ERROR_MESSAGE);
        else {
            try {
                PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                        "SELECT Password FROM User WHERE UserType='医师' AND UID=?");
                preparedStatement.setString(1, DoctorNum.getText());
                ResultSet resultSet = preparedStatement.executeQuery();
                resultSet.next();
                String Password = resultSet.getString("Password");
                //noinspection deprecation
                if (MD5.md5(DoctorPass.getText()).equals(Password)) {
                    Keys.UID = DoctorNum.getText();
                    frame.dispose();
                    DoctorMainUI.main(null);
                } else {
                    JOptionPane.showMessageDialog(frame, "工号或密码错误！", "警告", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "工号不存在！", "警告", JOptionPane.ERROR_MESSAGE);

            }
        }
    }

    public void PatientEnter(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
            PatientLogin();
    }

    public void PatientLogin() {
        //noinspection deprecation
        if (PatientName.getText().equals("") || PatientPassword.getText().equals(""))
            JOptionPane.showMessageDialog(frame, "请完整填写登录信息！", "警告", JOptionPane.ERROR_MESSAGE);
        else {
            try {
                PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                        "SELECT Password,UID FROM User WHERE UserType='患者' AND Username=?");
                preparedStatement.setString(1, PatientName.getText());
                ResultSet resultSet = preparedStatement.executeQuery();
                resultSet.next();
                String Password = resultSet.getString("Password");
                //noinspection deprecation
                if (MD5.md5(PatientPassword.getText()).equals(Password)) {
                    Keys.UID = resultSet.getString("UID");
                    frame.dispose();
                    PatientMainUI.main(null);
                } else {
                    JOptionPane.showMessageDialog(frame, "用户名或密码错误！", "警告", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "用户名不存在！", "警告", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void PharmacyEnter(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
            PharmacyLogin();
    }

    public void PharmacyLogin() {
        //noinspection deprecation
        if (PharmacyNum.getText().equals("") || PharmacyPassword.getText().equals(""))
            JOptionPane.showMessageDialog(frame, "请完整填写登录信息！", "警告", JOptionPane.ERROR_MESSAGE);
        else {
            try {
                PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                        "SELECT Password,UID FROM User WHERE UserType='药师' AND UID=?");
                preparedStatement.setString(1, PharmacyNum.getText());
                ResultSet resultSet = preparedStatement.executeQuery();
                resultSet.next();
                String Password = resultSet.getString("Password");
                //noinspection deprecation
                if (MD5.md5(PharmacyPassword.getText()).equals(Password)) {
                    Keys.UID = resultSet.getString("UID");
                    frame.dispose();
                    PharmacyMainUI.main(null);
                } else {
                    JOptionPane.showMessageDialog(frame, "工号或密码错误！", "警告", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "工号不存在！", "警告", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

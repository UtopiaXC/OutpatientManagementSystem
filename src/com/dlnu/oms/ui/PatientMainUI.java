package com.dlnu.oms.ui;

import com.dlnu.oms.constant.Keys;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

public class PatientMainUI {
    private JPanel panel;
    private JTabbedPane tabbedPane1;
    private JTextField textFieldName;
    private JButton ButtonSubmitUpdate;
    private JTextField textFieldIDNumber;
    private JTextField textFieldAge;
    private JTextField textFieldSex;
    private JTextField textFieldAllergies;
    private JTextField textFieldDiseases;
    private JComboBox<String> comboBoxDepartment;
    private JButton ButtonDepartmentSubmit;
    private JComboBox<String> comboBoxDoctor;
    private JButton ButtonRegisterSubmit;
    private JComboBox<String> comboBoxHistory;
    private JButton ButtonHistorySubmit;
    private JTextArea textAreaDiagnosis;
    private JTextArea textAreaMedicine;
    private JTextArea textAreaAdvice;
    private JComboBox<String> comboBoxTicket;
    private JButton ButtonTicketSubmit;
    private JLabel LabelCost;
    private JLabel LabelArrears;
    private JLabel LabelLatestTime;
    private JButton ButtonPayTicket;
    private JButton ButtonReleaseCSV;
    private static JFrame frame;

    public PatientMainUI() {
        setMessages();
        setComboBoxDepartment();
        setComboBoxHistory();
        setComboBoxTicket();
        setButtonRegisterSubmit();
        setButtonHistorySubmit();
        setButtonTicketSubmit();
        setButtonReleaseCSV();
    }

    public static void main(String[] args) {
        frame = new JFrame("患者客户端");
        frame.setIconImage(new ImageIcon("src/com/dlnu/oms/source/logo.png").getImage());
        frame.setContentPane(new PatientMainUI().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1000, 600));
        frame.pack();
        frame.setVisible(true);
    }

    public void setMessages() {
        try {
            PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                    "SELECT * FROM record WHERE RID=(SELECT ID FROM user WHERE UID=?)");
            preparedStatement.setString(1, Keys.UID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                textFieldName.setText(resultSet.getString("Name"));
                textFieldAge.setText(resultSet.getString("Age"));
                textFieldSex.setText(resultSet.getString("Sex"));
                textFieldIDNumber.setText(resultSet.getString("IDNumber"));
                textFieldAllergies.setText(resultSet.getString("Allergies"));
                textFieldDiseases.setText(resultSet.getString("MajorDiseases"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        ButtonSubmitUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (textFieldName.getText().equals("") ||
                        textFieldAge.getText().equals("") ||
                        textFieldSex.getText().equals("") ||
                        textFieldIDNumber.getText().equals("")) {
                    JOptionPane.showMessageDialog(frame, "身份证号，姓名，年龄和性别为必填项！", "警告", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!textFieldSex.getText().equals("男") && !
                        textFieldSex.getText().equals("女")) {
                    JOptionPane.showMessageDialog(frame, "性别必须为男或女！", "警告", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                            "UPDATE record SET Name=?,Age=?,Sex=?,IDNumber=?,Allergies=?,MajorDiseases=? WHERE RID=(SELECT ID FROM user WHERE UID=?)");
                    preparedStatement.setString(1, textFieldName.getText());
                    preparedStatement.setString(2, textFieldAge.getText());
                    preparedStatement.setString(3, textFieldSex.getText());
                    preparedStatement.setString(4, textFieldIDNumber.getText());
                    preparedStatement.setString(5, textFieldAllergies.getText());
                    preparedStatement.setString(6, textFieldDiseases.getText());
                    preparedStatement.setString(7, Keys.UID);
                    preparedStatement.execute();
                    JOptionPane.showMessageDialog(frame, "修改完成！", "成功", JOptionPane.PLAIN_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "存在数字格式错误！", "警告", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }

            }
        });
    }

    public void setComboBoxDepartment() {
        try {
            PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                    "SELECT Department FROM doctor GROUP BY Department");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                comboBoxDepartment.addItem(resultSet.getString("Department"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ButtonDepartmentSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                            "SELECT * FROM doctor WHERE Department=?");
                    preparedStatement.setString(1, Objects.requireNonNull(comboBoxDepartment.getSelectedItem()).toString());
                    ResultSet resultSet = preparedStatement.executeQuery();
                    comboBoxDoctor.removeAllItems();
                    while (resultSet.next()) {
                        comboBoxDoctor.addItem(resultSet.getString("DID") + "-" + resultSet.getString("Name"));
                    }
                    ButtonRegisterSubmit.setEnabled(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void setComboBoxHistory() {
        try {
            PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                    "SELECT RGID FROM registered WHERE RID=(SELECT ID FROM user WHERE UID=?)");
            preparedStatement.setString(1, Keys.UID);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                comboBoxHistory.addItem(resultSet.getString("RGID"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        comboBoxHistory.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                comboBoxHistory.removeAllItems();
                try {
                    PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                            "SELECT RGID FROM registered WHERE RID=(SELECT ID FROM user WHERE UID=?)");
                    preparedStatement.setString(1, Keys.UID);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        comboBoxHistory.addItem(resultSet.getString("RGID"));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {

            }
        });
    }

    public void setComboBoxTicket() {
        try {
            PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                    "SELECT RGID FROM registered WHERE RID=(SELECT ID FROM user WHERE UID=?)");
            preparedStatement.setString(1, Keys.UID);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                comboBoxTicket.addItem(resultSet.getString("RGID"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        comboBoxTicket.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                comboBoxTicket.removeAllItems();
                try {
                    PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                            "SELECT RGID FROM registered WHERE RID=(SELECT ID FROM user WHERE UID=?)");
                    preparedStatement.setString(1, Keys.UID);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        comboBoxTicket.addItem(resultSet.getString("RGID"));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {

            }
        });
    }

    public void setButtonRegisterSubmit() {
        ButtonRegisterSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                            "SELECT ID FROM user WHERE UID=?");
                    preparedStatement.setString(1, Keys.UID);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    resultSet.next();
                    String RID = resultSet.getString("ID");

                    preparedStatement = Keys.connection.prepareStatement(
                            "SELECT * FROM registered WHERE RID=? AND IsOver='否'");
                    preparedStatement.setString(1, RID);
                    resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        JOptionPane.showMessageDialog(frame, "您有未完成的诊号，请完成后再进行挂号！", "警告", JOptionPane.ERROR_MESSAGE);
                        return;
                    }


                    preparedStatement = Keys.connection.prepareStatement(
                            "INSERT INTO Registered(RID,Department,Doctor)values(?,?,?)");
                    preparedStatement.setString(1, RID);
                    preparedStatement.setString(2, Objects.requireNonNull(comboBoxDepartment.getSelectedItem()).toString());
                    preparedStatement.setString(3, Objects.requireNonNull(comboBoxDoctor.getSelectedItem()).toString().split("-")[0]);
                    preparedStatement.execute();
                    JOptionPane.showMessageDialog(frame, "挂号完成！", "成功", JOptionPane.PLAIN_MESSAGE);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void setButtonHistorySubmit() {
        ButtonHistorySubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                            "SELECT * From diagnosis WHERE RGID=?");
                    preparedStatement.setString(1, Objects.requireNonNull(comboBoxHistory.getSelectedItem()).toString());
                    ResultSet resultSet = preparedStatement.executeQuery();
                    resultSet.next();
                    textAreaAdvice.setText(resultSet.getString("Advice"));
                    textAreaDiagnosis.setText(resultSet.getString("Diagnosis"));
                } catch (Exception ez) {
                    ez.printStackTrace();
                }
                try {
                    textAreaMedicine.setText("");
                    PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                            "SELECT prescription.UnitPrice,prescription.Count,pharmacy.Name,pharmacy.MedicineType FROM pharmacy,prescription WHERE prescription.RGID=? AND pharmacy.MID=prescription.MID");
                    preparedStatement.setString(1, Objects.requireNonNull(comboBoxHistory.getSelectedItem()).toString());
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        textAreaMedicine.append(resultSet.getString("MedicineType") + " : " + resultSet.getInt("UnitPrice") + "元*" + resultSet.getInt("Count") + "份 : " + resultSet.getString("Name") + "\n");
                    }
                } catch (Exception ez) {
                    ez.printStackTrace();
                }
            }
        });
    }

    public void setButtonTicketSubmit() {
        ButtonTicketSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                            "SELECT * From toll WHERE RGID=?");
                    preparedStatement.setString(1, Objects.requireNonNull(comboBoxTicket.getSelectedItem()).toString());
                    ResultSet resultSet = preparedStatement.executeQuery();
                    resultSet.next();
                    LabelCost.setText("总金额：" + resultSet.getString("Cost"));
                    LabelArrears.setText("未缴纳：" + resultSet.getString("Arrears"));
                    LabelLatestTime.setText("缴纳期限：" + resultSet.getString("LatestTime"));
                    ButtonPayTicket.setEnabled(true);
                } catch (Exception ez) {
                    ez.printStackTrace();
                }
            }
        });

        ButtonPayTicket.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                            "UPDATE toll SET Paid=Cost,Arrears=0 WHERE RGID=?");
                    preparedStatement.setString(1, Objects.requireNonNull(comboBoxTicket.getSelectedItem()).toString());
                    preparedStatement.execute();
                    JOptionPane.showMessageDialog(frame, "您的账单已缴纳！", "成功", JOptionPane.PLAIN_MESSAGE);
                    ButtonTicketSubmit.doClick();
                } catch (Exception ez) {
                    ez.printStackTrace();
                }
            }
        });
    }

    public void setButtonReleaseCSV(){
        ButtonReleaseCSV.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    File file = new File("患者导出.csv");
                    if (file.exists()) {
                        boolean succeedDeleted = file.delete();
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "GBK");
                    PreparedStatement preparedStatement=Keys.connection.prepareStatement(
                            "SELECT * FROM registered,diagnosis,record WHERE record.RID=registered.RID AND registered.RGID=diagnosis.RGID AND registered.RID=(SELECT ID From user WHERE UID=?)");
                    preparedStatement.setString(1,Keys.UID);
                    ResultSet resultSet=preparedStatement.executeQuery();
                    outputStreamWriter.append("病历单号,诊断,医嘱,是否诊断完成,患者ID,患者姓名,患者年龄,患者性别,患者身份证号,患者过敏史,患者重大疾病史\n");
                    while (resultSet.next()){
                        outputStreamWriter.append(resultSet.getString("RGID")).append(",")
                                .append(resultSet.getString("Diagnosis")).append(",")
                                .append(resultSet.getString("Advice")).append(",")
                                .append(resultSet.getString("IsOver")).append(",")
                                .append(resultSet.getString("RID")).append(",")
                                .append(resultSet.getString("Name")).append(",")
                                .append(resultSet.getString("Age")).append(",")
                                .append(resultSet.getString("Sex")).append(",")
                                .append(resultSet.getString("IDNumber")).append(",")
                                .append(resultSet.getString("Allergies")).append(",")
                                .append(resultSet.getString("MajorDiseases")).append("\n");
                    }

                    preparedStatement=Keys.connection.prepareStatement(
                            "SELECT * FROM registered,prescription,pharmacy WHERE registered.RGID=prescription.RGID AND pharmacy.MID=prescription.MID AND registered.RID=(SELECT ID From user WHERE UID=?)");
                    preparedStatement.setString(1,Keys.UID);
                    resultSet=preparedStatement.executeQuery();
                    outputStreamWriter.append("\n\n病历单号,处方品号,处方品名,类型,制造商,单价,份数,总价\n");
                    while (resultSet.next()){
                        outputStreamWriter.append(resultSet.getString("RGID")).append(",")
                                .append(resultSet.getString("MID")).append(",")
                                .append(resultSet.getString("Name")).append(",")
                                .append(resultSet.getString("MedicineType")).append(",")
                                .append(resultSet.getString("Manufacturer")).append(",")
                                .append(resultSet.getString("UnitPrice")).append(",")
                                .append(resultSet.getString("Count")).append(",")
                                .append(resultSet.getString("Price")).append("\n");
                    }

                    outputStreamWriter.close();
                    fileOutputStream.close();
                    JOptionPane.showMessageDialog(frame,"导出成功！","成功", JOptionPane.PLAIN_MESSAGE);


                }catch (Exception e1){
                    JOptionPane.showMessageDialog(frame,"导出失败！","警告", JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                }
            }
        });
    }
}

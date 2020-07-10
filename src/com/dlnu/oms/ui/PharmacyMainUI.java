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

public class PharmacyMainUI {
    private JPanel panel;
    private JTextArea textAreaIncome;
    private JTextField textFieldName;
    private JButton ButtonInsertSubmit;
    private JTextField textFieldManufacturer;
    private JTextField textFieldMedicineType;
    private JTextField textFieldUnitPrice;
    private JTextField textFieldInStock;
    private JComboBox<String> comboBoxMedicine;
    private JButton ButtonSelectMedicine;
    private JButton ButtonUpdateUnitPrice;
    private JTextField textFieldUpdateInStock;
    private JTextField textFieldUpdateUnitPrice;
    private JButton ButtonUpdateInStock;
    private JTextField textFieldUpdateName;
    private JTextField textFieldUpdateManufacturer;
    private JTextField textFieldUpdateMedicineType;
    private JButton ButtonRefresh;
    private JButton ButtonReleaseCSV;
    private static JFrame frame;

    public PharmacyMainUI(){
        setTextAreaIncome();
        setButtonInsertSubmit();
        setManage();
        setButtonReleaseCSV();
        ButtonRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setTextAreaIncome();
            }
        });
    }

    public static void main(String[] args) {
        frame = new JFrame("药局客户端");
        frame.setIconImage(new ImageIcon("src/com/dlnu/oms/source/logo.png").getImage());
        frame.setContentPane(new PharmacyMainUI().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1000, 800));
        frame.pack();
        frame.setVisible(true);
    }

    public void setTextAreaIncome(){
        textAreaIncome.setText("");
        try {
            PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                    "SELECT SUM(Cost),SUM(Paid),SUM(Arrears) From toll");
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            textAreaIncome.append("总应收："+resultSet.getString("SUM(Cost)")+
                    "元\n总实收："+resultSet.getString("SUM(Paid)")+
                    "元\n欠缴："+resultSet.getString("SUM(Arrears)")+"元\n\n药品开出情况：\n");

            preparedStatement = Keys.connection.prepareStatement(
                    "SELECT prescription.UnitPrice,prescription.Count,pharmacy.Name,pharmacy.MedicineType FROM pharmacy,prescription WHERE pharmacy.MID=prescription.MID");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                textAreaIncome.append(resultSet.getString("MedicineType") + " : " + resultSet.getInt("UnitPrice") + "元*" + resultSet.getInt("Count") + "份 : " + resultSet.getString("Name") + "\n");
            }
        } catch (Exception ez) {
            ez.printStackTrace();
        }
    }

    public void setButtonInsertSubmit(){
        ButtonInsertSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (textFieldName.getText().equals("") ||
                        textFieldManufacturer.getText().equals("") ||
                        textFieldMedicineType.getText().equals("") ||
                        textFieldUnitPrice.getText().equals("")||
                        textFieldInStock.getText().equals("")) {
                    JOptionPane.showMessageDialog(frame, "有项目未填写！", "警告", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!textFieldMedicineType.getText().equals("处方药") && !
                        textFieldMedicineType.getText().equals("非处方药")&& !
                        textFieldMedicineType.getText().equals("保健品")&& !
                        textFieldMedicineType.getText().equals("化验")&& !
                        textFieldMedicineType.getText().equals("手术器械")) {
                    JOptionPane.showMessageDialog(frame, "类型请从处方药、非处方药、保健品、化验、手术器械中选取", "警告", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                            "INSERT INTO pharmacy(MedicineType,Name,Manufacturer,UnitPrice,InStock)VALUES(?,?,?,?,?)");
                    preparedStatement.setString(1, textFieldMedicineType.getText());
                    preparedStatement.setString(2, textFieldName.getText());
                    preparedStatement.setString(3, textFieldManufacturer.getText());
                    preparedStatement.setString(4, textFieldUnitPrice.getText());
                    preparedStatement.setString(5, textFieldInStock.getText());
                    preparedStatement.execute();
                    JOptionPane.showMessageDialog(frame, "添加完成！", "成功", JOptionPane.PLAIN_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "存在数字格式错误！", "警告", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });
    }

    public void setManage(){
        try {
            PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                    "SELECT MID,Name From pharmacy");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                comboBoxMedicine.addItem(resultSet.getString("MID")+"-"+resultSet.getString("Name"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        ButtonSelectMedicine.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                            "SELECT * From pharmacy WHERE MID=?");
                    preparedStatement.setString(1, Objects.requireNonNull(comboBoxMedicine.getSelectedItem()).toString().split("-")[0]);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    resultSet.next();
                    textFieldUpdateName.setText(resultSet.getString("Name"));
                    textFieldUpdateManufacturer.setText(resultSet.getString("Manufacturer"));
                    textFieldUpdateMedicineType.setText(resultSet.getString("MedicineType"));
                    textFieldUpdateUnitPrice.setText(resultSet.getString("UnitPrice"));
                    textFieldUpdateInStock.setText(resultSet.getString("InStock"));
                    ButtonUpdateUnitPrice.setEnabled(true);
                    ButtonUpdateInStock.setEnabled(true);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
        ButtonUpdateUnitPrice.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (textFieldUpdateUnitPrice.getText().equals("")){
                    JOptionPane.showMessageDialog(frame, "未填写内容！", "警告", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                            "UPDATE pharmacy SET UnitPrice=? WHERE MID=?");
                    preparedStatement.setString(1, textFieldUpdateUnitPrice.getText());
                    preparedStatement.setString(2, Objects.requireNonNull(comboBoxMedicine.getSelectedItem()).toString().split("-")[0]);
                    preparedStatement.execute();
                    JOptionPane.showMessageDialog(frame, "单价已修改！", "成功", JOptionPane.PLAIN_MESSAGE);

                }catch (Exception ex){
                    JOptionPane.showMessageDialog(frame, "数字格式错误！", "警告", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        ButtonUpdateInStock.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (textFieldUpdateInStock.getText().equals("")){
                    JOptionPane.showMessageDialog(frame, "未填写内容！", "警告", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                            "UPDATE pharmacy SET InStock=? WHERE MID=?");
                    preparedStatement.setString(1, textFieldUpdateInStock.getText());
                    preparedStatement.setString(2, Objects.requireNonNull(comboBoxMedicine.getSelectedItem()).toString().split("-")[0]);
                    preparedStatement.execute();
                    JOptionPane.showMessageDialog(frame, "库存已修改！", "成功", JOptionPane.PLAIN_MESSAGE);

                }catch (Exception ex){
                    JOptionPane.showMessageDialog(frame, "数字格式错误！", "警告", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        comboBoxMedicine.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                comboBoxMedicine.removeAllItems();
                try {
                    PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                            "SELECT MID,Name From pharmacy");
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()){
                        comboBoxMedicine.addItem(resultSet.getString("MID")+"-"+resultSet.getString("Name"));
                    }
                }catch (Exception ex){
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

    public void setButtonReleaseCSV(){
        ButtonReleaseCSV.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    File file = new File("药局导出.csv");
                    if (file.exists()) {
                        boolean succeedDeleted = file.delete();
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "GBK");
                    PreparedStatement preparedStatement=Keys.connection.prepareStatement(
                            "SELECT * FROM pharmacy");
                    ResultSet resultSet=preparedStatement.executeQuery();
                    outputStreamWriter.append("处方品号,处方品名,处方品类型,制造商,单价,当前库存\n");
                    while (resultSet.next()){
                        outputStreamWriter.append(resultSet.getString("MID")).append(",")
                                .append(resultSet.getString("Name")).append(",")
                                .append(resultSet.getString("MedicineType")).append(",")
                                .append(resultSet.getString("Manufacturer")).append(",")
                                .append(resultSet.getString("UnitPrice")).append(",")
                                .append(resultSet.getString("InStock")).append("\n");
                    }

                    preparedStatement=Keys.connection.prepareStatement(
                            "SELECT * FROM registered,prescription,pharmacy WHERE registered.RGID=prescription.RGID AND pharmacy.MID=prescription.MID");
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

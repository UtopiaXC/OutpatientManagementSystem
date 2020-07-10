package com.dlnu.oms.ui;

import com.dlnu.oms.constant.Keys;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

public class DoctorMainUI {
    private JPanel panel;
    private JComboBox<String> comboBoxHistory;
    private JComboBox<String> comboBoxPatient;
    private JLabel LabelRGID;
    private JLabel LabelName;
    private JLabel LabelAge;
    private JLabel LabelSex;
    private JLabel LabelID;
    private JLabel LabelAllergies;
    private JLabel LabelDiseases;
    private JButton buttonSelectPatient;
    private JButton buttonSelectDiagnosis;
    private JTextArea textAreaDiagnosis;
    private JTextArea textAreaAdvice;
    private JButton ButtonDiagnosisSubmit;
    private JTextField textFieldSearch;
    private JButton ButtonSearch;
    private JComboBox<String> comboBoxMedicine;
    private JTextField textFieldCount;
    private JButton ButtonMedicineSubmit;
    private JTextArea textAreaMedicine;
    private JButton ButtonReleaseCSV;
    private static JFrame frame;
    private String RGID;

    public DoctorMainUI() {
        LabelRGID.setText("未选择病人");
        LabelName.setText("");
        LabelAge.setText("");
        LabelSex.setText("");
        LabelID.setText("");
        LabelAllergies.setText("");
        LabelDiseases.setText("");
        ButtonMedicineSubmit.setEnabled(false);
        ButtonDiagnosisSubmit.setEnabled(false);

        setPatients();
        setButtonSelectDiagnosis();
        setButtonReleaseCSV();

        buttonSelectPatient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectPatient();
            }
        });

        ButtonSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchMedicine();
            }
        });

        ButtonMedicineSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitMedicine();
            }
        });

        ButtonDiagnosisSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitDiagnosis();
            }
        });

        comboBoxPatient.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

                comboBoxPatient.removeAllItems();
                try {
                    PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                            "SELECT registered.RGID,record.Name FROM registered,record WHERE record.RID=registered.RID AND registered.IsOver='否' AND registered.Doctor=(SELECT ID FROM user WHERE UID=?)");
                    preparedStatement.setString(1, Keys.UID);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        comboBoxPatient.addItem(resultSet.getString("RGID") + "-" + resultSet.getString("Name"));
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

    public static void main(String[] args) {
        frame = new JFrame("医师客户端");
        frame.setIconImage(new ImageIcon("src/com/dlnu/oms/source/logo.png").getImage());
        frame.setContentPane(new DoctorMainUI().panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1000, 800));
        frame.pack();
        frame.setVisible(true);
    }

    public void setPatients(){
        comboBoxPatient.removeAllItems();
        try {
            PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                    "SELECT registered.RGID,record.Name FROM registered,record WHERE record.RID=registered.RID AND registered.IsOver='否' AND registered.Doctor=(SELECT ID FROM user WHERE UID=?)");
            preparedStatement.setString(1, Keys.UID);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                comboBoxPatient.addItem(resultSet.getString("RGID") + "-" + resultSet.getString("Name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void selectPatient() {
        RGID = Objects.requireNonNull(comboBoxPatient.getSelectedItem()).toString().split("-")[0];
        ButtonMedicineSubmit.setEnabled(true);
        ButtonDiagnosisSubmit.setEnabled(true);
        textAreaAdvice.setText("");
        textAreaAdvice.setEnabled(true);
        textAreaDiagnosis.setText("");
        textAreaDiagnosis.setEnabled(true);
        comboBoxHistory.removeAllItems();
        try {
            PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                    "SELECT * FROM record WHERE RID=(SELECT RID FROM registered WHERE RGID=?)");
            preparedStatement.setString(1, RGID);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            LabelRGID.setText(RGID);
            LabelName.setText(resultSet.getString("Name"));
            LabelAge.setText(resultSet.getString("Age"));
            LabelSex.setText(resultSet.getString("Sex"));
            LabelID.setText(resultSet.getString("IDNumber"));
            String Allergies=resultSet.getString("Allergies");
            String Diseases=resultSet.getString("MajorDiseases");
            LabelAllergies.setText(Allergies);
            LabelDiseases.setText(Diseases);
            if (Allergies==null)
                LabelAllergies.setText("无");
            if (Diseases==null)
                LabelDiseases.setText("无");

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                    "SELECT * FROM registered WHERE IsOver='是' AND RID=(SELECT RID FROM registered WHERE RGID=?)");
            preparedStatement.setString(1, RGID);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                comboBoxHistory.addItem(resultSet.getString("RGID"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            textAreaMedicine.setText("");
            PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                    "SELECT prescription.UnitPrice,prescription.Count,pharmacy.Name,pharmacy.MedicineType FROM pharmacy,prescription WHERE prescription.RGID=? AND pharmacy.MID=prescription.MID");
            preparedStatement.setString(1, RGID);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                textAreaMedicine.append(resultSet.getString("MedicineType")+" : "+resultSet.getInt("UnitPrice")+"元*"+resultSet.getInt("Count")+"份 : "+resultSet.getString("Name")+"\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void searchMedicine(){
        if (textFieldSearch.getText().equals(""))
            return;
        try {
            comboBoxMedicine.removeAllItems();
            PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                    "SELECT MID,Name FROM pharmacy WHERE MID LIKE ? OR Name LIKE ? OR Manufacturer LIKE ?");
            preparedStatement.setString(1, "%"+textFieldSearch.getText()+"%");
            preparedStatement.setString(2, "%"+textFieldSearch.getText()+"%");
            preparedStatement.setString(3, "%"+textFieldSearch.getText()+"%");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                comboBoxMedicine.addItem(resultSet.getString("MID") + "-" + resultSet.getString("Name"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void submitMedicine(){
        if(Objects.requireNonNull(comboBoxMedicine.getSelectedItem()).toString().equals("")|| textFieldCount.getText().equals("")){
            return;
        }
        String MID=Objects.requireNonNull(comboBoxMedicine.getSelectedItem()).toString().split("-")[0];
        int count = 0;
        try {
            count = Integer.parseInt(textFieldCount.getText());
        }catch (Exception e){
            JOptionPane.showMessageDialog(frame,"存在错误输入！","警告！", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            comboBoxMedicine.removeAllItems();
            PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                    "INSERT INTO Prescription(RGID,MID,Count)VALUES(?,?,?)");
            preparedStatement.setString(1,RGID);
            preparedStatement.setString(2, MID);
            preparedStatement.setInt(3, count);
            preparedStatement.execute();
            selectPatient();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void submitDiagnosis(){
        try {
            PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                    "UPDATE diagnosis SET Diagnosis=? , Advice=? WHERE RGID=?");
            preparedStatement.setString(1, textAreaDiagnosis.getText());
            preparedStatement.setString(2, textAreaAdvice.getText());
            preparedStatement.setString(3, RGID);
            preparedStatement.execute();
        }catch (Exception e){
            e.printStackTrace();
        }

        LabelRGID.setText("未选择病人");
        LabelName.setText("");
        LabelAge.setText("");
        LabelSex.setText("");
        LabelID.setText("");
        LabelAllergies.setText("");
        LabelDiseases.setText("");
        ButtonMedicineSubmit.setEnabled(false);
        ButtonDiagnosisSubmit.setEnabled(false);
        textAreaDiagnosis.setText("");
        textAreaAdvice.setText("");
        textAreaMedicine.setText("");
        RGID=null;
        setPatients();
    }

    public void setButtonSelectDiagnosis(){
        buttonSelectDiagnosis.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RGID = Objects.requireNonNull(comboBoxHistory.getSelectedItem()).toString();
                ButtonMedicineSubmit.setEnabled(false);
                ButtonDiagnosisSubmit.setEnabled(false);
                try {
                    textAreaMedicine.setText("");
                    PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                            "SELECT prescription.UnitPrice,prescription.Count,pharmacy.Name,pharmacy.MedicineType FROM pharmacy,prescription WHERE prescription.RGID=? AND pharmacy.MID=prescription.MID");
                    preparedStatement.setString(1, RGID);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        textAreaMedicine.append(resultSet.getString("MedicineType")+" : "+resultSet.getInt("UnitPrice")+"元*"+resultSet.getInt("Count")+"份 : "+resultSet.getString("Name")+"\n");
                    }
                } catch (Exception ez) {
                    ez.printStackTrace();
                }
                try {
                    PreparedStatement preparedStatement = Keys.connection.prepareStatement(
                            "SELECT * From diagnosis WHERE RGID=?");
                    preparedStatement.setString(1, RGID);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    resultSet.next();
                    textAreaAdvice.setText(resultSet.getString("Advice"));
                    textAreaAdvice.setEnabled(false);
                    textAreaDiagnosis.setText(resultSet.getString("Diagnosis"));
                    textAreaDiagnosis.setEnabled(false);
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
                    File file = new File("医师导出.csv");
                    if (file.exists()) {
                        boolean succeedDeleted = file.delete();
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "GBK");
                    PreparedStatement preparedStatement=Keys.connection.prepareStatement(
                            "SELECT * FROM registered,diagnosis,record WHERE record.RID=registered.RID AND registered.RGID=diagnosis.RGID AND registered.Doctor=(SELECT ID From user WHERE UID=?)");
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
                            "SELECT * FROM registered,prescription,pharmacy WHERE registered.RGID=prescription.RGID AND pharmacy.MID=prescription.MID AND registered.Doctor=(SELECT ID From user WHERE UID=?)");
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

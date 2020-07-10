package com.dlnu.oms.constant;

public class Values {
    public static String DBUserName="";
    public static String DBPassword="";
    public static String DB="";
    public static String Host="";
    public static String Port="";
    public static final String ClassName="com.mysql.cj.jdbc.Driver";
    public static final String AESPassword="OMS";
    public static final String Config="outpatient.conf";
    public static String getURL(){
        return "jdbc:mysql://"+Host+":"+Port+"/"+DB+"?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }
    public static String getURL(String Host,String Port,String DB){
        return "jdbc:mysql://"+Host+":"+Port+"/"+DB+"?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }
}

//  gtTsRaw.java
//
//  extract total station raw data from Topcon *.mjf file
//  model : Topcon GT-1000

import java.sql.*;
import java.util.*;
import java.io.*;

public class gtTsRaw{
    protected Connection con;  //  database connection
    protected String outputString = "";
    protected double prismConstant=0;  //  prism constant

    //  constructor
    public gtTsRaw(){
        try{
            Properties pro = new Properties();
            pro.load(new FileReader("gtRawConfig.txt"));

            //  set the prism constant
            if(pro.getProperty("prismConstant")!=null)
                prismConstant = Double.parseDouble(pro.getProperty("prismConstant"));

            //  db parameters
            String url = "jdbc:sqlite:";
            url += pro.getProperty("totalstationLogFile");

            //  Topcon *.mjf file is virtually a SQLite database file
            //  create a connection to the database
            con = DriverManager.getConnection(url);
            //  Connection to SQLite has been established.

        }catch(Exception e){
            System.out.println(e);
        }
    }

    public void extract(){
        Vector<setupHeader> occupiedStations = new Vector<setupHeader>();

        String sqlString = "SELECT keyTsOccupations AS setup, Name, InstrumentHt FROM tblTsOccups LEFT JOIN tblSoPoints ON keySoPoint=fkeySoPoint";
        try{
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sqlString);
            while(rs.next()){
                occupiedStations.add(new setupHeader(rs.getInt("setup"), rs.getString("Name"), rs.getDouble("InstrumentHt")));
            }
            rs.close();
            stmt.close();

            sqlString = "SELECT Name, HAngle, VAngle, SDistance, TargetAntHeight, nameCode FROM tblTsShots LEFT JOIN tblSoPoints ON keySoPoint=fkeyTargetStation LEFT JOIN tblCodesToPts ON fkeyPoint=keySoPoint LEFT JOIN tblCodeDefs ON fkeyCodeDef=keyCodeDef WHERE fkeyTsOccupations=?";
            PreparedStatement pstmt = con.prepareStatement(sqlString);
            int blockNum = 1;

            //  loop for each occupied station
            for(int index=0; index<occupiedStations.size(); index++){
            	setupHeader occupiedStation = occupiedStations.get(index);
                occupiedStation.setBlockNumber(blockNum);

                //  add an station header before each setup
                outputString += occupiedStation + System.lineSeparator();
                blockNum++;

                pstmt.setInt(1, occupiedStation.getSetupIndex());
                rs = pstmt.executeQuery();

                //  export to GSI8 format
                //  loop for each observation
                while(rs.next()){
                	outputString += gsi8_util.getPtIDWord(blockNum, rs.getString("Name")) + " 21.10";
                	blockNum++;

                	double angleValue = rs.getDouble("HAngle");
                	angleValue = gsi8_util.toHMS(angleValue);
                	outputString += (gsi8_util.getDataWord('4', angleValue)).substring(5) + " 22.10";

                	angleValue = rs.getDouble("VAngle");
                	angleValue = gsi8_util.toHMS(angleValue);
                	outputString += (gsi8_util.getDataWord('4', angleValue)).substring(5) + " 31..0";

                	double length = rs.getDouble("SDistance") + prismConstant;
                	outputString += (gsi8_util.getDataWord('0', length)).substring(5) + " 87...";

                	length = rs.getDouble("TargetAntHeight");
                	outputString += (gsi8_util.getDataWord('0', length)).substring(5) + " 71....";

                	outputString += (gsi8_util.getPtIDWord(blockNum, rs.getString("nameCode"))).substring(6) + " ";

                	outputString += System.lineSeparator();
                }

            }  //  end of for loop

            rs.close();
            pstmt.close();

        }catch(Exception e){
            System.out.println(e);
        }
    }

    public String toString(){
    	return new String(outputString);
    }

    public static void main(String[] args) {
        gtTsRaw g = new gtTsRaw();
        g.extract();
        System.out.println(g);
    }
}  //  end of class

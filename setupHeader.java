import java.io.Serializable;

//  setupHeader.java

public class setupHeader implements Cloneable, Serializable{
    protected int index;
    protected String StationName;
    protected double instrumentHeight;
    protected int blockNum = 1;

    //  constructor
    public setupHeader(int i, String name, double hi){
        index = i;
        StationName = new String(name);
        instrumentHeight =  hi;
    }

    public void setBlockNumber(int b){
        if(b<10000)
            blockNum = b;
    }

    public int getSetupIndex(){
        return index;
    }

    public String getStationName(){
        return new String(StationName);
    }

    public double getHI(){
        return instrumentHeight;
    }

    public String toString(){
        //  generate Station header in GSI8 format
        String sResult = "41" + (gsi8_util.getPtIDWord(blockNum, "1")).substring(2) + " 42....";
        sResult += (gsi8_util.getPtIDWord(1, StationName)).substring(6);
        sResult += " 43..1";
        sResult += (gsi8_util.getDataWord('0', instrumentHeight)).substring(5);

        return sResult;
    }

    public Object clone()throws CloneNotSupportedException{
        return (setupHeader)(new setupHeader(index, StationName, instrumentHeight));
    }
}

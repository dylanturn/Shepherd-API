package dylanturn.shepherdapi.messaging;

import org.jgroups.Global;
import org.jgroups.Header;
import org.jgroups.util.Util;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.function.Supplier;

/**
 * Package: dylanturn.shepherdapi
 * Date:    11/13/2016
 * Author:  Dylan
 */
public class ShepherdHeader extends Header {
    private long systemTime;
    private String xmlPayload;

    public ShepherdHeader(){}

    public ShepherdHeader(String xmlPayload){
        this.systemTime = System.currentTimeMillis();
        this.xmlPayload = xmlPayload;
    }

    public long getSystemTime(){
        return systemTime;
    }
    public String getXMLPayload(){
        return xmlPayload;
    }

    @Override
    public short getMagicId() {
        return 3000;
    }

    @Override
    public int size() {
        return Global.BYTE_SIZE + Global.LONG_SIZE + Global.LONG_SIZE + Util.size(xmlPayload);
    }

    @Override
    public void writeTo(DataOutput dataOutput) throws Exception {
        dataOutput.writeLong(systemTime);
        dataOutput.writeUTF(xmlPayload);
    }

    @Override
    public void readFrom(DataInput dataInput) throws Exception {
        systemTime = dataInput.readLong();
        xmlPayload = dataInput.readUTF();
    }

    @Override
    public Supplier<? extends Header> create() {
        return () -> new ShepherdHeader();
    }
}

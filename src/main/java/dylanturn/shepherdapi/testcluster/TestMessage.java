package dylanturn.shepherdapi.testcluster;

import org.jgroups.Global;
import org.jgroups.Header;
import org.jgroups.util.Util;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.function.Supplier;

/**
 * Package: dylanturn.shepherdapi.testcluster
 * Date:    11/13/2016
 * Author:  Dylan
 */
public class TestMessage extends Header {

    private long systemTime;
    private String ipAddress;

    public TestMessage(){}

    public TestMessage(String ipAddress){
        this.systemTime = System.currentTimeMillis();
        this.ipAddress = ipAddress;
    }

    public long getSystemTime(){
        return systemTime;
    }
    public String getIpAddress(){
        return ipAddress;
    }

    @Override
    public short getMagicId() {
        return 3000;
    }

    @Override
    public int size() {
        return Global.BYTE_SIZE + Global.LONG_SIZE + Global.LONG_SIZE + Util.size(ipAddress);
    }

    @Override
    public void writeTo(DataOutput dataOutput) throws Exception {
        dataOutput.writeLong(systemTime);
        dataOutput.writeUTF(ipAddress);
    }

    @Override
    public void readFrom(DataInput dataInput) throws Exception {
        systemTime = dataInput.readLong();
        ipAddress = dataInput.readUTF();
    }

    @Override
    public Supplier<? extends Header> create() {
        return () -> new TestMessage();
    }
}

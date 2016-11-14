package dylanturn.shepherdapi.members;

import org.jgroups.Address;

/**
 * Package: dylanturn.shepherdapi.members
 * Date:    11/13/2016
 * Author:  Dylan
 */
public class MemberRecord {
    public final Address memberAddress;
    public final String memberIpAddress;
    public final long memberPing;
    public MemberRecord(Address memberAddress,String memberIpAddress, long memberPing){
        this.memberAddress = memberAddress;
        this.memberIpAddress = memberIpAddress;
        this.memberPing = memberPing;
    }
}

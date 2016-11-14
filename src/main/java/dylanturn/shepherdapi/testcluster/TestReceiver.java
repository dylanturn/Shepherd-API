package dylanturn.shepherdapi.testcluster;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.Message;
import org.jgroups.Receiver;

/**
 * Package: dylanturn.shepherdapi.testcluster
 * Date:    11/13/2016
 * Author:  Dylan
 */
public class TestReceiver implements Receiver {
    private static final Logger LOG = LogManager.getLogger(TestReceiver.class);
    private TestReceiver messenger;

    @Override
    public void receive(Message message) {
        if(LOG.isTraceEnabled())
            LOG.trace("Received message. - TEST");
    }
}

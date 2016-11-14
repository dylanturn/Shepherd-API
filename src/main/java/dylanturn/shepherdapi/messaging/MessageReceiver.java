package dylanturn.shepherdapi.messaging;

import dylanturn.shepherdapi.ShepherdAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.Message;
import org.jgroups.Receiver;

/**
 * Package: dylanturn.shepherdapi.messaging
 * Date:    11/13/2016
 * Author:  Dylan
 */
public class MessageReceiver implements Receiver {
    private static final Logger LOG = LogManager.getLogger(MessageReceiver.class);
    private ShepherdAPI shepherdAPI;

    public MessageReceiver(ShepherdAPI shepherdAPI) {
        this.shepherdAPI = shepherdAPI;
    }

    @Override
    public void receive(Message message) {
        if(message.getHeader(new ShepherdHeader().getMagicId()) != null){
            shepherdAPI.updateClusterNodeDetail(message);
        }
        if(LOG.isTraceEnabled())
            LOG.trace("Received message. - SAPI");
    }
}

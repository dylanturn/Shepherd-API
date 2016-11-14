package dylanturn.shepherdapi;

import dylanturn.shepherdapi.members.MemberCollector;
import dylanturn.shepherdapi.messaging.MessageReceiver;
import dylanturn.shepherdapi.messaging.ShepherdHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.conf.ClassConfigurator;
import org.jgroups.fork.ForkChannel;
import org.jgroups.protocols.CENTRAL_LOCK;
import org.jgroups.protocols.STATS;
import spark.Request;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;

import static spark.Spark.*;

/**
 * Package: dylanturn.shepherdapi
 * Date:    11/13/2016
 * Author:  Dylan
 */

public class ShepherdAPI {
    private static final Logger LOG = LogManager.getLogger(ShepherdAPI.class);

    private int apiPort;
    private int apiIntervalMS;
    private String responseXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Cluster>Empty</Cluster>";
    private JChannel channel;
    private ForkChannel shepherdChannel;
    private String clusterIP;
    private String clusterPort;
    private String clusterVersion;

    private Map<Address,String> clusterNodeDetail = new ConcurrentHashMap<Address,String>();
    private Map<Address,String> clusterEventList = new ConcurrentHashMap<Address,String>();

    public ShepherdAPI(JChannel channel, String clusterIP, String clusterPort, String clusterVersion, int apiPort, int apiIntervalMS) {
        this.channel = channel;
        this.clusterIP = clusterIP;
        this.clusterPort = clusterPort;
        this.clusterVersion = clusterVersion;
        this.apiPort = apiPort;
        this.apiIntervalMS = apiIntervalMS;

        System.out.println("Starting API Server...");
        if(LOG.isInfoEnabled())
            LOG.info("Starting API Server...");

        try {
            ClassConfigurator.add(new ShepherdHeader().getMagicId(), ShepherdHeader.class);
            shepherdChannel = new ForkChannel(channel, "shepherdfork", "fork-shepherd", new CENTRAL_LOCK(), new STATS());
            shepherdChannel.setReceiver(new MessageReceiver(this));
            shepherdChannel.connect("Shepherd");
        } catch (Exception error){
            if(LOG.isErrorEnabled())
                LOG.error("Failed to create Shepherd fork channel.",error);
        }
        startDataCollector();

        port(apiPort);
        before((request, response) -> response.type("application/xml"));
        after((request, response) -> { response.header("Access-Control-Allow-Origin", "*"); });
        get("/sdk", (request, response) -> getResponseXML(request));

        System.out.println("API Server Started!");

    }

    private void startDataCollector(){
        final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleWithFixedDelay(()->{ collectData(channel, this); }, 0, apiIntervalMS, TimeUnit.MILLISECONDS);
    }

    public synchronized void setResponseXML(String responseXML){
        if(LOG.isTraceEnabled())
            LOG.trace("Received cluster detail update.");
        this.responseXML = responseXML;
    }

    private String getResponseXML(Request request){
        if(LOG.isDebugEnabled())
            LOG.debug("Responding to API request from " + request.ip());
        return responseXML;
    }

    private void collectData(JChannel channel, ShepherdAPI scatterAPI){
        Logger collectionLogger = LogManager.getLogger(ShepherdAPI.class);
        if(collectionLogger.isDebugEnabled())
            collectionLogger.debug("Collecting member data...");

        try {
            ShepherdHeader shepherdHeader = new ShepherdHeader(new MemberCollector(channel).getLocalNodeXML());
            shepherdChannel.send(new Message().putHeader(shepherdHeader.getMagicId(), shepherdHeader));
            pruneClusterNodeDetail();
        } catch (Exception error){
            if(LOG.isErrorEnabled())
                LOG.error("Failed to send status update to cluster!",error);
        }

        try {
            StringBuilder responseBuilder = new StringBuilder();
            String responseDate = String.valueOf(System.currentTimeMillis());
            responseBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            responseBuilder.append(String.format("<Cluster time=\"%s\">", responseDate));
            responseBuilder.append(getClusterXML());
            responseBuilder.append(buildMembershipXML());
            responseBuilder.append("</Cluster>");
            scatterAPI.setResponseXML(responseBuilder.toString());
        } catch (Exception error){
            if(collectionLogger.isDebugEnabled())
                collectionLogger.debug(String.format("Error getting data: %s",error.getMessage()),error);
        }
    }

    private String getClusterXML(){
        String secondaryCoord = "";
        if(channel.getView().getMembersRaw().length > 1)
            secondaryCoord = channel.getView().getMembersRaw()[1].toString();

        return String.format( "<ClusterDetail name=\"%s\" primaryNode=\"%s\" secondaryNode=\"%s\" clusterIp=\"%s\" clusterPort=\"%s\" version=\"%s\" />",
                channel.clusterName(),
                channel.getView().getCoord().toString(),
                secondaryCoord,
                clusterIP,
                clusterPort,
                clusterVersion);
    }

    private String buildMembershipXML(){
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append(String.format("<Members size=\"%s\">",getMemberCount()));
        for(Address memberAddress : shepherdChannel.getView().getMembers()){
            xmlBuilder.append(clusterNodeDetail.get(memberAddress));
        }
        xmlBuilder.append("</Members>");
        return xmlBuilder.toString();
    }

    private int getMemberCount(){
        return channel.getView().getMembersRaw().length;
    }

    private void pruneClusterNodeDetail() {
        for(Address memberAddress : shepherdChannel.getView().getMembers()){
            if(!clusterNodeDetail.containsKey(memberAddress)){
                clusterNodeDetail.remove(memberAddress);
            }
        }
    }

    public void updateClusterNodeDetail(Message message){
        ShepherdHeader shepherdHeader = message.getHeader(new ShepherdHeader().getMagicId());
        if(clusterNodeDetail.containsKey(message.getSrc())){
            clusterNodeDetail.replace(message.getSrc(),shepherdHeader.getXMLPayload());
        } else {
            clusterNodeDetail.put(message.getSrc(),shepherdHeader.getXMLPayload());
        }
    }
}

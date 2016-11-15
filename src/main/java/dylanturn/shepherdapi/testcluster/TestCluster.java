package dylanturn.shepherdapi.testcluster;

import dylanturn.shepherdapi.ShepherdAPI;
import org.jgroups.JChannel;
import org.jgroups.conf.ClassConfigurator;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

/**
 * Package: dylanturn.shepherdapi.testcluster
 * Date:    11/13/2016
 * Author:  Dylan
 */
public class TestCluster {

    private final static String CLUSTER_NAME = "TestCluster";
    private final static String CLUSTER_ADDR = "228.8.8.8";
    private final static int CLUSTER_PORT = 45566;
    private final static int API_PORT = 5001;
    private final static int IPVER = 4;
    private final static int COLLECTION_INTERVAL = 1000;

    public static void main(String[] args) throws Exception {
        System.out.println("*******************************************");
        System.out.println("*        Shepherd-API Test Cluster        *");
        System.out.println("*******************************************");
        System.out.println(String.format("Build Timestamp: %s", getCompileTimeStamp().toString()));
        System.out.println(String.format("Cluster Name:    %s", CLUSTER_NAME));
        System.out.println(String.format("Cluster Address: %s", CLUSTER_ADDR));
        System.out.println(String.format("Cluster Port:    %s", CLUSTER_PORT));
        System.out.println(String.format("API Port:        %s", API_PORT));

        JChannel jChannel = new JChannel();
        jChannel.setReceiver(new TestReceiver());
        jChannel.stats(true);

        ShepherdAPI shepherdAPI = new ShepherdAPI(jChannel,CLUSTER_ADDR,String.valueOf(CLUSTER_PORT),String.valueOf(IPVER),API_PORT,COLLECTION_INTERVAL);

        jChannel.connect(CLUSTER_NAME);
        shepherdAPI.connect();


        System.out.println("Press Ctl+C to Quit...");
        while(true){
            Thread.sleep(5000);
        }
    }

    /**
     * get date a class was compiled by looking at the corresponding class file in the jar.
     * @author Zig
     */

    public static Date getCompileTimeStamp( ) throws IOException {
        Class<?> cls = TestCluster.class;
        ClassLoader loader = cls.getClassLoader();
        String filename = cls.getName().replace('.', '/') + ".class";
        // get the corresponding class file as a Resource.
        URL resource=( loader!=null ) ?
                loader.getResource( filename ) :
                ClassLoader.getSystemResource( filename );
        URLConnection connection = resource.openConnection();
        // Note, we are using Connection.getLastModified not File.lastModifed.
        // This will then work both or members of jars or standalone class files.
        long time = connection.getLastModified();
        return( time != 0L ) ? new Date( time ) : null;
    }
}

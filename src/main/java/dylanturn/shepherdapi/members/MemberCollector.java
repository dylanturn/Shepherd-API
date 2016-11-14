package dylanturn.shepherdapi.members;

import org.jgroups.JChannel;

import java.io.File;
import java.lang.management.*;

/**
 * Package: dylanturn.shepherdapi
 * Date:    11/13/2016
 * Author:  Dylan
 */

public class MemberCollector {

    private JChannel channel;
    private Runtime runtime;
    private RuntimeMXBean runBean;
    private OperatingSystemMXBean operBean;
    private MemoryMXBean memBean;

    public MemberCollector(JChannel channel){
        this.channel = channel;
        runtime = Runtime.getRuntime();
        operBean = ManagementFactory.getOperatingSystemMXBean();
        runBean = ManagementFactory.getRuntimeMXBean();
        memBean = ManagementFactory.getMemoryMXBean();
    }

    public String getLocalNodeXML(){

        StringBuilder xmlBuilder = new StringBuilder();

        xmlBuilder.append(String.format("<Member name=\"%s\">",channel.getAddressAsString()));

        xmlBuilder.append(createNodeDetail());

        xmlBuilder.append(createStatistics());

        xmlBuilder.append("</Member>");

        return xmlBuilder.toString();
    }

    private String createNodeDetail() {
        StringBuilder xmlBuilder = new StringBuilder();

        xmlBuilder.append("<MemberDetail>");

        xmlBuilder.append(getSystemStats());

        xmlBuilder.append(getJVMStats());

        xmlBuilder.append("</MemberDetail>");

        return xmlBuilder.toString();
    }

    private String getSystemStats(){
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append(String.format("<System loadaverage=\"%s\">", operBean.getSystemLoadAverage()));
        xmlBuilder.append(getOperatingSystemStats());
        xmlBuilder.append(getCPUStats());
        xmlBuilder.append(getMemoryStats());
        xmlBuilder.append(getFileSystemStats());
        xmlBuilder.append("</System>");
        return xmlBuilder.toString();
    }

    private String getOperatingSystemStats(){
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append(String.format("<OperatingSystem  name=\"%s\" version=\"%s\" arch=\"%s\" />",
                operBean.getName(),
                operBean.getVersion(),
                operBean.getArch()));
        return xmlBuilder.toString();
    }

    private String getCPUStats(){
        com.sun.management.OperatingSystemMXBean mxbean =
                (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        Double value  =  mxbean.getSystemCpuLoad();
        Double cpuUtil;

        if (value == -1.0)
            cpuUtil = Double.NaN;
        else
            cpuUtil = ((int)(value * 1000) / 10.0);

        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append(String.format("<CPU totalcores=\"%s\" utilization=\"\" >", runtime.availableProcessors(), cpuUtil));
        String id = "";
        String model = "";
        String cores = "";
        xmlBuilder.append(String.format("<Socket id=\"%s\" model=\"%s\" cores=\"%s\">",
                id,
                model,
                cores));
        xmlBuilder.append("</Socket>");
        xmlBuilder.append("</CPU>");
        return xmlBuilder.toString();
    }

    private String getMemoryStats(){
        long maxMemory = runtime.maxMemory();
        long allocMemory = runtime.totalMemory() - runtime.freeMemory();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append(String.format("<Memory max=\"%s\" allocated=\"%s\" free=\"%s\" totalfree=\"%s\" />",
                maxMemory,
                allocMemory,
                freeMemory,
                totalMemory));
        return xmlBuilder.toString();
    }

    private String getFileSystemStats(){
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<FileSystems>");
        File[] roots = File.listRoots();
        for (File root : roots) {
            xmlBuilder.append(String.format("<FileSystem root=\"%s\" totalspace=\"%s\" freespace=\"%s\" useablespace=\"%s\" />",
                    root.getAbsolutePath(),
                    root.getTotalSpace(),
                    root.getFreeSpace(),
                    root.getUsableSpace()));
        }
        xmlBuilder.append("</FileSystems>");
        return xmlBuilder.toString();
    }

    private String getJVMStats(){
        MemoryUsage heapMem = memBean.getHeapMemoryUsage();
        MemoryUsage nonHead = memBean.getNonHeapMemoryUsage();
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append(String.format("<JVM name=\"%s\" starttime=\"%s\" uptime=\"%s\" version=\"%s\" >",
                runBean.getName(),
                runBean.getStartTime(),
                runBean.getUptime(),
                runBean.getVmVersion()));
        xmlBuilder.append(String.format("<HeapMemoryUsage init=\"%s\" used=\"%s\" committed=\"%s\" max=\"%s\" />",
                heapMem.getInit(),
                heapMem.getUsed(),
                heapMem.getCommitted(),
                heapMem.getMax()));
        xmlBuilder.append(String.format("<NonHeapMemoryUsage  init=\"%s\" used=\"%s\" committed=\"%s\" max=\"%s\" />",
                nonHead.getInit(),
                nonHead.getUsed(),
                nonHead.getCommitted(),
                nonHead.getMax()));
        xmlBuilder.append("</JVM>");
        return xmlBuilder.toString();
    }

    private String createStatistics() {
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<Statistics>");
        xmlBuilder.append(String.format("<Statistic name=\"ReceivedBytes\" value=\"%s\" />\n",channel.getReceivedBytes()));
        xmlBuilder.append(String.format("<Statistic name=\"ReceivedMessages\" value=\"%s\" />\n",channel.getReceivedMessages()));
        xmlBuilder.append(String.format("<Statistic name=\"SentBytes\" value=\"%s\" />\n",channel.getSentBytes()));
        xmlBuilder.append(String.format("<Statistic name=\"SentMessages\" value=\"%s\" />\n",channel.getSentMessages()));
        xmlBuilder.append("</Statistics>");
        return xmlBuilder.toString();
    }

}

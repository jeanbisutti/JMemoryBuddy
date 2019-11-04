package de.sandec.jmemorybuddy;

import com.sun.management.HotSpotDiagnosticMXBean;
import javax.management.MBeanServer;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

public class MemoryLeakUtils {

    static int steps = 10;
    static int overallTime = 5000;
    static int sleepTime = overallTime / steps;

    public static void createGarbage() {
        LinkedList list = new LinkedList<Integer>();
        int counter = 0;
        while(counter < 999999) {
            counter += 1;
            list.add(1);
        }
    }

    public static void assertCollectable(WeakReference weakReference) {
        int counter = 0;

        createGarbage();
        System.gc();

        while(counter < steps && weakReference.get() != null) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {}
            counter = counter + 1;
            createGarbage();
            System.gc();
        }

        if(weakReference.get() != null) {
            doHeapDump();
            throw new RuntimeException("Content of Weakreference was not collected. content: " + weakReference.get());
        } else {
            if(counter > steps / 3) {
                int percentageUsed = (int) (counter / steps * 100);
                System.out.println("Warning test seems to be unstable. time used: " + percentageUsed + "%");
            }
        }

    }

    public static void doMemTest(Consumer<Consumer<Object>> f) {
        LinkedList<WeakReference<Object>> toCheck = new LinkedList<WeakReference<Object>>();

        f.accept((Object elem) -> toCheck.add(new WeakReference<Object>(elem)));

        for(WeakReference<Object> wRef: toCheck) {
            assertCollectable(wRef);
        }

    }


    public static void doHeapDump() {
        try {
            getHotspotMBean().dumpHeap("./heapdump-"+new java.util.Date()+".hprof", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HotSpotDiagnosticMXBean getHotspotMBean() throws IOException {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        HotSpotDiagnosticMXBean bean =
                ManagementFactory.newPlatformMXBeanProxy(server,
                        "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
        return bean;
    }


}
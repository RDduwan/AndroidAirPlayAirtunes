package ss.serven.rduwan.airtunesandroid;

import android.util.Log;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 * Created by rduwan on 17/6/29.
 */

public class AirTunesRunnable implements Runnable {

    /**
     * The AirTunes/RAOP service type
     */
    static final String AIR_TUNES_SERVICE_TYPE = "_raop._tcp.local.";

    /**
     * The AirTunes/RAOP M-DNS service properties (TXT record)
     */
    static final Map<String, String> AIRTUNES_SERVICE_PROPERTIES = NetworkUtils.map(
            "txtvers", "1",
            "tp", "UDP",
            "ch", "2",
            "ss", "16",
            "sr", "44100",
            "pw", "false",
            "sm", "false",
            "sv", "false",
            "ek", "1",
            "et", "0,1",
            "cn", "0,1",
            "vn", "3");

    /**
     * The AirTunes/RAOP RTSP port
     */
    private int rtspPort = 5000; //default value

    protected List<JmDNS> jmDNSList;

    private static AirTunesRunnable instance = null;

    private final static String TAG = "AirTunesRunnable";

    private AirTunesRunnable() {
        jmDNSList = new java.util.LinkedList<JmDNS>();
    }

    public synchronized static AirTunesRunnable getInstance() {
        if (instance == null) {
            instance = new AirTunesRunnable();
        }
        return instance;
    }


    @Override
    public void run() {
        startAirTunesService();
    }

    private void startAirTunesService() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                onAppShutDown();
            }
        }));
        sendMulitCastToiOS();
    }

    /**
     * 通过DNS服务给局域网里面的iOS发送组播，使得iOS设备能发现你
     * java 使用JmDNS库
     */
    private void sendMulitCastToiOS() {
        //get Network details
        NetworkUtils networkUtils = NetworkUtils.getInstance();
        String hostName = "RDuwan-AirTunes";
        networkUtils.setHostName(hostName);
        String hardwareAddressString = networkUtils.getHardwareAddressString();
        try {
            synchronized (jmDNSList) {
                List<NetworkInterface> workInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (final NetworkInterface networkInterface : workInterfaces) {
                    //如果网络设备几口是 回送接口 & 点对点接口 & 没有运行 & 虚拟端口，则跳过执行
                    if (networkInterface.isLoopback() || networkInterface.isPointToPoint() || !networkInterface.isUp()
                            || networkInterface.isVirtual()) {
                        continue;
                    }
                    // 不支持组播  跳过
                    if (!networkInterface.supportsMulticast()) {
                        continue;
                    }

                    for (final InetAddress address : Collections.list(networkInterface.getInetAddresses())) {
                        //端口是是ipv4 或者  ipv6 端口
                        if (address instanceof Inet4Address || address instanceof Inet6Address) {
                            try {
                                final JmDNS jmDNS = JmDNS.create(address, hostName);
                                jmDNSList.add(jmDNS);

                                //构建AirTunes/RAOP （远程音频传输协议）服务
                                final ServiceInfo airTunesServiceInfo = ServiceInfo.create(
                                        AIR_TUNES_SERVICE_TYPE,
                                        hardwareAddressString + "@" + hostName,
                                        getRstpPort(),
                                        0,
                                        0,
                                        AIRTUNES_SERVICE_PROPERTIES
                                );
                                jmDNS.registerService(airTunesServiceInfo);
                                Log.d(TAG, "Success to publish service on " + address + ", port: " + getRstpPort());
                            } catch (final Throwable e) {
                                Log.e(TAG, "Failed to publish service on " + address, e);

                            }

                        }
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    public int getRstpPort() {
        return rtspPort;
    }

    private void onAppShutDown() {
        /* Stop all mDNS responders */
        synchronized(jmDNSList) {
            for(final JmDNS jmDNS: jmDNSList) {
                try {
                    jmDNS.unregisterAllServices();
                    Log.i(TAG, "Unregistered all services ");
                }
                catch (final Exception e) {
                    Log.i(TAG, "Failed to unregister some services");

                }
            }
        }
    }
}

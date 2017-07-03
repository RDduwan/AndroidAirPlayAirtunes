package ss.serven.rduwan.airtunesandroid;

import android.util.Log;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import ss.serven.rduwan.airtunesandroid.network.raop.RaopRtsPipelineFactory;

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
    private int rtspPort;

    private ExecutorService executorService;

    /**
     * All open RTSP channels. Used to close all open challens during shutdown.
     */
    protected ChannelGroup channelGroup;

    protected List<JmDNS> jmDNSList;

    private static AirTunesRunnable instance = null;

    private final static String TAG = "AirTunesRunnable";

    private AirTunesRunnable() {
        jmDNSList = new java.util.LinkedList<JmDNS>();
        executorService = Executors.newCachedThreadPool();
        channelGroup = new DefaultChannelGroup();
        initRtspPort();
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
        startServer();
        sendMulitCastToiOS();
    }

    private void startServer() {
        final ServerBootstrap airTunesBootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(executorService, executorService));
        airTunesBootstrap.setOption("reuseAddress", true); //端口重用
        airTunesBootstrap.setOption("child.tcpNoDelay", true);
        airTunesBootstrap.setOption("child.keepAlive", true); //保持连接
        airTunesBootstrap.setPipelineFactory(new RaopRtsPipelineFactory());

        try {channelGroup.add(airTunesBootstrap.bind(new InetSocketAddress(Inet4Address.getByName("0.0.0.0"), getRtspPort())));
        }
        catch (Exception e) {
            Log.e(TAG, "error",e);
            try {channelGroup.add(airTunesBootstrap.bind(new InetSocketAddress(Inet4Address.getByName("0.0.0.0"), getAnotherRtspPort())));
                Log.i(TAG,"start server:0.0.0.0: port:" + getRtspPort() + " bind address success");
            }
            catch (Exception e1) {
                Log.e(TAG, "error",e1);
            }
        }
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
                            String addressStr = address + "";
                            if (!addressStr.contains("192.")) {
                                continue;
                            }
                            try {
                                final JmDNS jmDNS = JmDNS.create(address, hostName);
                                jmDNSList.add(jmDNS);
                                //构建AirTunes/RAOP （远程音频传输协议）服务
                                final ServiceInfo airTunesServiceInfo = ServiceInfo.create(
                                        AIR_TUNES_SERVICE_TYPE,
                                        hardwareAddressString + "@" + hostName,
                                        getRtspPort(),
                                        0,
                                        0,
                                        AIRTUNES_SERVICE_PROPERTIES
                                );
                                jmDNS.registerService(airTunesServiceInfo);
                                Log.d(TAG, "Success to publish service on " + address + ", port: " + getRtspPort());
                                return;
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

    private void initRtspPort() {
        rtspPort = new Random().nextInt(60000) + 5000;
    }
    public int getRtspPort() {
        return rtspPort;
    }

    public int getAnotherRtspPort() {
        rtspPort = rtspPort + 88;
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

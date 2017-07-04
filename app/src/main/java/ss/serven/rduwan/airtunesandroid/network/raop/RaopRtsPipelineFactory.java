package ss.serven.rduwan.airtunesandroid.network.raop;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.rtsp.RtspRequestDecoder;
import org.jboss.netty.handler.codec.rtsp.RtspResponseEncoder;

import ss.serven.rduwan.airtunesandroid.AirTunesRunnable;
import ss.serven.rduwan.airtunesandroid.network.ExceptionLoggingHandler;
import ss.serven.rduwan.airtunesandroid.network.NetworkUtils;
import ss.serven.rduwan.airtunesandroid.network.raop.handlers.RaopAudioHandler;
import ss.serven.rduwan.airtunesandroid.network.raop.handlers.RaopRtspChallengeResponseHandler;
import ss.serven.rduwan.airtunesandroid.network.raop.handlers.RaopRtspHeaderHandler;
import ss.serven.rduwan.airtunesandroid.network.raop.handlers.RaopRtspOptionsHandler;
import ss.serven.rduwan.airtunesandroid.network.rtp.RtspErrorResponseHandler;
import ss.serven.rduwan.airtunesandroid.network.rtp.RtspLoggingHandler;
import ss.serven.rduwan.airtunesandroid.network.rtp.RtspUnsupportedResponseHandler;

/**
 * Created by rduwan on 17/7/3.
 */

public class RaopRtsPipelineFactory implements ChannelPipelineFactory {
    @Override
    public ChannelPipeline getPipeline() throws Exception {

        final ChannelPipeline pipeline = Channels.pipeline();
        //因为是管道 注意保持正确的顺序

        //构造executionHanlder 和关闭executionHanlder
        final AirTunesRunnable airTunesRunnable = AirTunesRunnable.getInstance();
        pipeline.addLast("exectionHandler", airTunesRunnable.getChannelExecutionHandler());
        pipeline.addLast("closeOnShutdownHandler", new SimpleChannelUpstreamHandler(){
            @Override
            public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
                airTunesRunnable.getChannelGroup().add(e.getChannel());
                super.channelOpen(ctx, e);
            }
        });

        //add exception logger
        pipeline.addLast("exceptionLogger", new ExceptionLoggingHandler());

        //rtsp decoder & encoder
        pipeline.addLast("decoder", new RtspRequestDecoder());
        pipeline.addLast("encoder", new RtspResponseEncoder());

        //rstp logger and errer response
        pipeline.addLast("logger", new RtspLoggingHandler());
        pipeline.addLast("errorResponse", new RtspErrorResponseHandler());

        //app airtunes need
        pipeline.addLast("challengeResponse", new RaopRtspChallengeResponseHandler(NetworkUtils.getInstance().getHardwareAddress()));
        pipeline.addLast("header", new RaopRtspHeaderHandler());
        //let iOS devices know server support methods
        pipeline.addLast("options", new RaopRtspOptionsHandler());

        //!!!Core handler audioHandler
        pipeline.addLast("audio", new RaopAudioHandler(airTunesRunnable.getExecutorService()));

        //unsupport Response
        pipeline.addLast("unsupportedResponse", new RtspUnsupportedResponseHandler());


        return pipeline;
    }
}

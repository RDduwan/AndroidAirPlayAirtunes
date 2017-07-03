package ss.serven.rduwan.airtunesandroid.network.raop;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.rtsp.RtspRequestDecoder;
import org.jboss.netty.handler.codec.rtsp.RtspResponseEncoder;

import ss.serven.rduwan.airtunesandroid.network.NetworkUtils;
import ss.serven.rduwan.airtunesandroid.network.raop.handlers.RaopRtspChallengeResponseHandler;
import ss.serven.rduwan.airtunesandroid.network.raop.handlers.RaopRtspHeaderHandler;
import ss.serven.rduwan.airtunesandroid.network.raop.handlers.RaopRtspOptionsHandler;

/**
 * Created by rduwan on 17/7/3.
 */

public class RaopRtsPipelineFactory implements ChannelPipelineFactory {
    @Override
    public ChannelPipeline getPipeline() throws Exception {

        final ChannelPipeline pipeline = Channels.pipeline();
        //因为是管道 注意保持正确的顺序
        pipeline.addLast("decoder", new RtspRequestDecoder());
        pipeline.addLast("encoder", new RtspResponseEncoder());
        pipeline.addLast("challengeResponse", new RaopRtspChallengeResponseHandler(NetworkUtils.getInstance().getHardwareAddress()));
        pipeline.addLast("header", new RaopRtspHeaderHandler());
        pipeline.addLast("options", new RaopRtspOptionsHandler());

        return pipeline;
    }
}

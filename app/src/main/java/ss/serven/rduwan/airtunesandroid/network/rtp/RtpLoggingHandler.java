/*
 * This file is part of AirReceiver.
 *
 * AirReceiver is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * AirReceiver is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with AirReceiver.  If not, see <http://www.gnu.org/licenses/>.
 */

package ss.serven.rduwan.airtunesandroid.network.rtp;


import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

import ss.serven.rduwan.airtunesandroid.network.raop.RaopRtpPacket;

/**
 * Logs incoming and outgoing RTP packets
 */
public class RtpLoggingHandler extends SimpleChannelHandler {
	
	private static final Logger LOG = Logger.getLogger(RtpLoggingHandler.class.getName());

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent evt) throws Exception {
		if (evt.getMessage() instanceof RtpPacket) {
			final RtpPacket packet = (RtpPacket)evt.getMessage();
			final Level level = getPacketLevel(packet);
			if (LOG.isLoggable(level)){
				LOG.log(level, evt.getRemoteAddress() + "> " + packet.toString());
			}
		}
		super.messageReceived(ctx, evt);
	}

	@Override
	public void writeRequested(final ChannelHandlerContext ctx, final MessageEvent evt) throws Exception {
		if (evt.getMessage() instanceof RtpPacket) {
			final RtpPacket packet = (RtpPacket)evt.getMessage();

			final Level level = getPacketLevel(packet);
			if (LOG.isLoggable(level)){
				LOG.log(level, evt.getRemoteAddress() + "< " + packet.toString());
			}
		}
		super.writeRequested(ctx, evt);
	}

	private Level getPacketLevel(final RtpPacket packet) {
		if (packet instanceof RaopRtpPacket.Audio){
			return Level.FINEST;
		}
		else if (packet instanceof RaopRtpPacket.RetransmitRequest){
			return Level.FINEST;
		}
		else if (packet instanceof RaopRtpPacket.Timing){
			return Level.INFO;
		}
		else{
			return Level.FINE;
		}
	}
}

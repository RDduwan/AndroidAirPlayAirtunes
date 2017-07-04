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

package ss.serven.rduwan.airtunesandroid.network.raop.handlers;


import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import java.util.logging.Logger;

import ss.serven.rduwan.airtunesandroid.network.InvalidPacketException;
import ss.serven.rduwan.airtunesandroid.network.raop.RaopRtpPacket;

/**
 * Decodes incoming packets, emitting instances of {@link RaopRtpPacket}
 */
public class RaopRtpDecodeHandler extends OneToOneDecoder {
	
	private static final Logger LOG = Logger.getLogger(RaopRtpDecodeHandler.class.getName());

	@Override
	protected Object decode(final ChannelHandlerContext ctx, final Channel channel, final Object msg) throws Exception {
		if (msg instanceof ChannelBuffer) {
			final ChannelBuffer buffer = (ChannelBuffer)msg;

			try {
				return RaopRtpPacket.decode(buffer);
			}
			catch (final InvalidPacketException e1) {
				LOG.warning(e1.getMessage());
				return buffer;
			}
		}
		else {
			return msg;
		}
	}
}

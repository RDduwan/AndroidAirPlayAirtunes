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


import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import ss.serven.rduwan.airtunesandroid.network.ProtocolException;

/**
 * Adds a few default headers to every RTSP response
 */
public class RaopRtspHeaderHandler extends SimpleChannelHandler
{
	private static final String HeaderCSeq = "CSeq";

	private static final String HeaderAudioJackStatus = "Audio-Jack-Status";
	private static final String HeaderAudioJackStatusDefault = "connected; type=analog";

	/*
	private static final String HeaderAudioLatency = "Audio-Latency";
	private static final long   HeaderAudioLatencyFrames = 88400;
	*/

	private String m_cseq;

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent evt)
		throws Exception
	{
		final HttpRequest req = (HttpRequest)evt.getMessage();

		synchronized(this) {
			if (req.containsHeader(HeaderCSeq)) {
				m_cseq = req.getHeader(HeaderCSeq);
			}
			else {
				throw new ProtocolException("No CSeq header");
			}
		}

		super.messageReceived(ctx, evt);
	}

	@Override
	public void writeRequested(final ChannelHandlerContext ctx, final MessageEvent evt)
		throws Exception
	{
		final HttpResponse resp = (HttpResponse)evt.getMessage();

		synchronized(this) {
			if (m_cseq != null)
				resp.setHeader(HeaderCSeq, m_cseq);

			resp.setHeader(HeaderAudioJackStatus, HeaderAudioJackStatusDefault);
			//resp.setHeader(HeaderAudioLatency, Long.toString(HeaderAudioLatencyFrames));
		}

		super.writeRequested(ctx, evt);
	}
}

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

package ss.serven.rduwan.airtunesandroid.network;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logs exceptions thrown by other channel handlers
 */
public class ExceptionLoggingHandler extends SimpleChannelHandler {
	private static Logger LOG = Logger.getLogger(ExceptionLoggingHandler.class.getName());

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent evt) throws Exception {
		super.exceptionCaught(ctx, evt);
		LOG.log(Level.WARNING, "Handler raised exception", evt.getCause());
	}
}

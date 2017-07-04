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

package ss.serven.rduwan.airtunesandroid.audio;

public interface SampleIndexedAccessor {
	SampleDimensions getDimensions();
	
	SampleIndexedAccessor slice(SampleOffset offset, SampleDimensions dimensions);

	SampleIndexedAccessor slice(SampleRange range);

	float getSample(int channel, int sample);
	void setSample(int channel, int index, float sample);
}

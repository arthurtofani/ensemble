/******************************************************************************

Copyright 2011 Leandro Ferrari Thomaz

This file is part of Ensemble.

Ensemble is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Ensemble is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Ensemble.  If not, see <http://www.gnu.org/licenses/>.

******************************************************************************/

package ensemble.audio.ambisonics;


import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Hashtable;

import jjack.JackCallback;
import jjack.JackPortFlags;
import jjack.SWIGTYPE_p_jack_client_t;
import jjack.SWIGTYPE_p_jack_port_t;
import jjack.jjack;
import jjack.jjackConstants;

import ensemble.Constants;
import ensemble.EventHandler;
import ensemble.Reasoning;
import ensemble.Sensor;
import ensemble.clock.TimeUnit;
import ensemble.memory.Memory;


public class AmbisonicsAudioOutputReasoning extends Reasoning {

	enum SPEAKERS_LAYOUT {
		MONO (1, gm_mono), 
		STEREO (2, gm_stereo), 
		SQUARE (4, gm_square), 
		PENTAGON (5, gm_pentagon), 
		HEXAGON (6, gm_hexagon), 
		OCTAGON1 (8, gm_octagon1), 
		OCTAGON2 (8, gm_octagon2), 
		SURROUND1 (5, gm_surround), 
		CUBE (8, gm_cube), 
		DODECAHEDRON1 (12, gm_dodecahedron1), 
		DODECAHEDRON2 (12, gm_dodecahedron2);
		
		public final int num_speakers;
		public final double[][] gain_matrix;

		SPEAKERS_LAYOUT(int num_speakers, double[][] gain_matrix) {
			this.num_speakers = num_speakers;
			this.gain_matrix = gain_matrix;
		}
		
		public static SPEAKERS_LAYOUT parseString(String sl) {
	 		if (sl.equals("MONO")) {
	 			return SPEAKERS_LAYOUT.MONO;
	 		}
	 		else if (sl.equals("STEREO")) {
	 			return SPEAKERS_LAYOUT.STEREO;
	 		}
	 		else if (sl.equals("SQUARE")) {
	 			return SPEAKERS_LAYOUT.SQUARE;
	 		}
	 		else if (sl.equals("PENTAGON")) {
	 			return SPEAKERS_LAYOUT.PENTAGON;
	 		}
	 		else if (sl.equals("HEXAGON")) {
	 			return SPEAKERS_LAYOUT.HEXAGON;
	 		}
	 		else if (sl.equals("OCTAGON1")) {
	 			return SPEAKERS_LAYOUT.OCTAGON1;
	 		}
	 		else if (sl.equals("OCTAGON2")) {
	 			return SPEAKERS_LAYOUT.OCTAGON2;
	 		}
	 		else if (sl.equals("SURROUND1")) {
	 			return SPEAKERS_LAYOUT.SURROUND1;
	 		}
	 		else if (sl.equals("CUBE")) {
	 			return SPEAKERS_LAYOUT.CUBE;
	 		}
	 		else if (sl.equals("DODECAHEDRON1")) {
	 			return SPEAKERS_LAYOUT.DODECAHEDRON1;
	 		}
	 		else if (sl.equals("DODECAHEDRON2")) {
	 			return SPEAKERS_LAYOUT.DODECAHEDRON2;
	 		}
	 		return null;
		}
		
	}

	// Ambisonics - matrix_gain[speaker][amb_channel]
	private static double[][] gm_mono = {
		{+1.4142, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000}
	};

	private static double[][] gm_stereo = {
		{+0.7071, +0.0000, +0.5000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.7071, +0.0000, -0.5000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000}
	};

	private static double[][] gm_square = {
		{+0.3536, +0.3536, +0.3536, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.2500, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.3536, +0.3536, -0.3536, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, -0.2500, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.3536, -0.3536, -0.3536, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.2500, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.3536, -0.3536, +0.3536, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, -0.2500, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000}
	};

	private static double[][] gm_pentagon = {
		{+0.2828,  0.3236,  0.2351, +0.0000, +0.0000, +0.0000, +0.0000, +0.1236, +0.3804, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.2828, -0.1236,  0.3804, +0.0000, +0.0000, +0.0000, +0.0000, -0.3236, -0.2351, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.2828, -0.4000,  0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.4000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.2828, -0.1236, -0.3804, +0.0000, +0.0000, +0.0000, +0.0000, -0.3236, +0.2351, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.2828, +0.3236, -0.2351, +0.0000, +0.0000, +0.0000, +0.0000, +0.1236, -0.3804, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000}
	};

	private static double[][] gm_hexagon = {
		{+0.2357,  0.2887,  0.1667, +0.0000, +0.0000, +0.0000, +0.0000, +0.1667, +0.2887, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.2357,  0.0000,  0.3333, +0.0000, +0.0000, +0.0000, +0.0000, -0.3333, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.2357, -0.2887,  0.1667, +0.0000, +0.0000, +0.0000, +0.0000, +0.1667, -0.2887, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.2357, -0.2887, -0.1667, +0.0000, +0.0000, +0.0000, +0.0000, +0.1667, +0.2887, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.2357,  0.0000, -0.3333, +0.0000, +0.0000, +0.0000, +0.0000, -0.3333, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.2357,  0.2887, -0.1667, +0.0000, +0.0000, +0.0000, +0.0000, +0.1667, -0.2887, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000}
	};

	private static double[][] gm_octagon1 = {
		{+0.1768,  0.2310,  0.0957, +0.0000, +0.0000, +0.0000, +0.0000, +0.1768, +0.1768, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1768,  0.0957,  0.2310, +0.0000, +0.0000, +0.0000, +0.0000, -0.1768, +0.1768, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1768, -0.0957,  0.2310, +0.0000, +0.0000, +0.0000, +0.0000, -0.1768, -0.1768, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1768, -0.2310,  0.0957, +0.0000, +0.0000, +0.0000, +0.0000, +0.1768, -0.1768, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1768, -0.2310, -0.0957, +0.0000, +0.0000, +0.0000, +0.0000, +0.1768, +0.1768, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1768, -0.0957, -0.2310, +0.0000, +0.0000, +0.0000, +0.0000, -0.1768, +0.1768, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1768,  0.0957, -0.2310, +0.0000, +0.0000, +0.0000, +0.0000, -0.1768, -0.1768, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1768,  0.2310, -0.0957, +0.0000, +0.0000, +0.0000, +0.0000, +0.1768, -0.1768, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000}
	};

	private static double[][] gm_octagon2 = {
		{+0.1768,  0.2500,  0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.2500, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1768,  0.1768,  0.1768, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.2500, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1768, -0.0000,  0.2500, +0.0000, +0.0000, +0.0000, +0.0000, -0.2500, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1768, -0.1768,  0.1768, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, -0.2500, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1768, -0.2500, -0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.2500, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1768, -0.1768, -0.1768, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.2500, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1768,  0.0000, -0.2500, +0.0000, +0.0000, +0.0000, +0.0000, -0.2500, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1768,  0.1768, -0.1768, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, -0.2500, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000}
	};

	private static double[][] gm_surround = {
		{+0.0000,  1.3660,  0.3660, +0.0000, +0.0000, +0.0000, +0.0000, -1.3660, +0.3660, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.4714, -1.8214,  0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +2.4880, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.0000,  1.3660, -0.3660, +0.0000, +0.0000, +0.0000, +0.0000, -1.3660, -0.3660, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.4714, -0.4553,  0.3660, +0.0000, +0.0000, +0.0000, +0.0000, +0.1220, -0.2113, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.4714, -0.4553, -0.3660, +0.0000, +0.0000, +0.0000, +0.0000, +0.1220, +0.2113, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000}
	};

	private static double[][] gm_cube = {
		{+0.1768,  0.2165,  0.2165, -0.2165, +0.0000, -0.1875, -0.1875, +0.0000, +0.1875, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1768,  0.2165, -0.2165, -0.2165, +0.0000, -0.1875, +0.1875, +0.0000, -0.1875, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1768, -0.2165, -0.2165, -0.2165, +0.0000, +0.1875, +0.1875, +0.0000, +0.1875, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1768, -0.2165,  0.2165, -0.2165, +0.0000, +0.1875, -0.1875, +0.0000, -0.1875, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1768,  0.2165,  0.2165,  0.2165, +0.0000, +0.1875, +0.1875, +0.0000, +0.1875, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1768,  0.2165, -0.2165,  0.2165, +0.0000, +0.1875, -0.1875, +0.0000, -0.1875, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1768, -0.2165, -0.2165,  0.2165, +0.0000, -0.1875, -0.1875, +0.0000, +0.1875, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1768, -0.2165,  0.2165,  0.2165, +0.0000, -0.1875, +0.1875, +0.0000, -0.1875, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000}
	};

	private static double[][] gm_dodecahedron1 = {
		{+0.1179,  0.0000,  0.0000,  0.2500, +0.4167, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179,  0.0000,  0.0000, -0.2500, +0.4167, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179,  0.1809,  0.1314,  0.1118, -0.0833, +0.2023, +0.1469, +0.0773, +0.2378, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179, -0.1809, -0.1314, -0.1118, -0.0833, +0.2023, +0.1469, +0.0773, +0.2378, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179,  0.1809, -0.1314,  0.1118, -0.0833, +0.2023, -0.1469, +0.0773, -0.2378, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179, -0.1809,  0.1314, -0.1118, -0.0833, +0.2023, -0.1469, +0.0773, -0.2378, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179, -0.0691,  0.2127,  0.1118, -0.0833, -0.0773, +0.2378, -0.2023, -0.1469, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179,  0.0691, -0.2127, -0.1118, -0.0833, -0.0773, +0.2378, -0.2023, -0.1469, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179, -0.0691, -0.2127,  0.1118, -0.0833, -0.0773, -0.2378, -0.2023, +0.1469, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179,  0.0691,  0.2127, -0.1118, -0.0833, -0.0773, -0.2378, -0.2023, +0.1469, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179, -0.2236,  0.0000,  0.1118, -0.0833, -0.2500, +0.0000, +0.2500, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179,  0.2236,  0.0000, -0.1118, -0.0833, -0.2500, +0.0000, +0.2500, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000}
	};

	private static double[][] gm_dodecahedron2 = {
		{+0.1179,  0.2500,  0.0000,  0.0000, -0.2083, +0.0000, +0.0000, +0.3125, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179, -0.2500,  0.0000,  0.0000, -0.2083, +0.0000, +0.0000, +0.3125, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179,  0.1118,  0.0000, -0.2236,  0.2917, -0.2500, +0.0000, +0.0625, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179, -0.1118,  0.0000, -0.2236,  0.2917, -0.2500, +0.0000, +0.0625, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179,  0.1118,  0.2127, -0.0691, -0.1606, -0.0773, -0.1469, -0.1636,  0.2378, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179, -0.1118, -0.2127,  0.0691, -0.1606, -0.0773, -0.1469, -0.1636,  0.2378, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179,  0.1118, -0.2127, -0.0691, -0.1606, -0.0773, +0.1469, -0.1636, -0.2378, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179, -0.1118,  0.2127,  0.0691, -0.1606, -0.0773, +0.1469, -0.1636, -0.2378, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179,  0.1118,  0.1314,  0.1809,  0.1189,  0.2023,  0.2378, -0.0239, +0.1469, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179, -0.1118, -0.1314, -0.1809,  0.1189,  0.2023,  0.2378, -0.0239, +0.1469, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179,  0.1118, -0.1314,  0.1809,  0.1189,  0.2023, -0.2378, -0.0239, -0.1469, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000},
		{+0.1179, -0.1118,  0.1314, -0.1809,  0.1189,  0.2023, -0.2378, -0.0239, -0.1469, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000, +0.0000}
	};

	private SPEAKERS_LAYOUT speakers_layout = SPEAKERS_LAYOUT.STEREO;
	private int num_channels;
	
	Hashtable<String,String> mapping = new Hashtable<String, String>();

	private Sensor sensor;
	private Memory sensorMemory;
	
	// JACK
	String 	client_name;
	long 	client;
	long[] 	ports;	
	double 	callbackStartTime, period;
	double 	step =	1/44100.0;

	@Override
	public boolean init() {
		
		// It must be in the format "channel-client:port,channel-client:port,..."
		String[] str = getParameter("MAPPING", "").split(",");
		if (str.length == 0) {
			System.out.println("[" + getComponentName() + "] No channels configured");
		} else {
			for (int i = 0; i < str.length; i++) {
				String[] str2 = str[i].split("-");
				mapping.put(str2[0], str2[1]);
			}
		}
		
		if (parameters.containsKey("SPEAKERS_LAYOUT")) {
			speakers_layout = SPEAKERS_LAYOUT.parseString(parameters.get("SPEAKERS_LAYOUT"));
		}
		else {
			speakers_layout = SPEAKERS_LAYOUT.STEREO;
		}
		
		// JACK
		client_name = Constants.FRAMEWORK_NAME+"_"+getAgent().getAgentName()+"_"+getComponentName();
		client = jjack.jack_client_open(client_name, new Process());
		if (client == 0) {
			System.err.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] JACK server not running... JACK will not be available!");
            return false;
		}
		
		return true;
	}
	
	@Override
	public boolean finit() {
		
		if (client != 0) {
			jjack.jack_client_close(client);
		}
		
		return true;
	}
	
	@Override
	protected void eventHandlerRegistered(EventHandler evtHdl) throws Exception {
		if (evtHdl instanceof Sensor) {
			if (evtHdl.getParameters().containsKey("AMBISONICS")) {
				// Gets the Sensor
				sensor = (Sensor)evtHdl;
				sensorMemory = getAgent().getKB().getMemory(sensor.getComponentName());
				// Ambisonics order
				num_channels = Integer.valueOf(sensor.getParameter("CHANNELS"));
				// Jack ports - speakers
				ports = new long[speakers_layout.num_speakers];
				for (int n = 0; n < ports.length; n++) {
					ports[n] = jjack.jack_port_register(client, 
									"speaker_"+(n+1),
									jjackConstants.JACK_DEFAULT_AUDIO_TYPE, 
									JackPortFlags.JackPortIsOutput);
				}
				// Activates the JACK client
				if (jjack.jack_activate(client) != 0) {
					System.err.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] Cannot activate JACK client... JACK will not be available!");
				}
				// Connects the port
				for (int n = 0; n < ports.length; n++) {
					if (mapping.containsKey("speaker_"+(n+1))) {
						if (jjack.jack_connect(client, client_name+":"+"speaker_"+(n+1), mapping.get("speaker_"+(n+1))) != 0) {
							System.err.println("[" + getAgent().getAgentName() + ":" + getComponentName() + "] Cannot connect playback ports");
							return;
						}
					}
				}
			}
		}
	}
	
	@Override
	protected void eventHandlerDeregistered(EventHandler evtHdl)
			throws Exception {
		if (evtHdl instanceof Sensor) {
			if (evtHdl.getParameters().containsKey("AMBISONICS")) {
				System.out.println("Entrei no deregistered!");
			}
		}

	}

	class Process implements JackCallback {

		boolean firstCall = true;
		double instant = 0;

		@Override
		public int process(int nframes, double time) {
			if (firstCall) {
				instant = getAgent().getClock().getCurrentTime(TimeUnit.SECONDS) - (nframes * step);
				firstCall = false;
			}
			double duration = (double)(nframes) * step;
			// Reads the memory
			double[][] d_buffer = (double[][])sensorMemory.readMemory(instant, duration, TimeUnit.SECONDS);
			// Gets the port buffers
			for (int n = 0; n < speakers_layout.num_speakers; n++) {
				FloatBuffer fOut = jjack.jack_port_get_buffer(ports[n], nframes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
				int ptr = 0;
				while (fOut.remaining() > 0) {
					double value = 0.0;
					for (int i = 0; i < num_channels; i++) {
						value += speakers_layout.gain_matrix[n][i] * d_buffer[i][ptr];
					}
					fOut.put((float)value);
					ptr++;
				}
			}
			instant = instant + duration;
			return 0;
			
		}
		
	}
		
}



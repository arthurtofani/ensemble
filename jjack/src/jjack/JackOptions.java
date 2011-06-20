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

package jjack;

public final class JackOptions {
  public final static int JackNullOption = jjackJNI.JackNullOption_get();
  public final static int JackNoStartServer = jjackJNI.JackNoStartServer_get();
  public final static int JackUseExactName = jjackJNI.JackUseExactName_get();
  public final static int JackServerName = jjackJNI.JackServerName_get();
  public final static int JackLoadName = jjackJNI.JackLoadName_get();
  public final static int JackLoadInit = jjackJNI.JackLoadInit_get();
}


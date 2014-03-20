/*
    Copyright(C) 2013 Ying-Chun Liu(PaulLiu). All rights reserved.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/

package org.debian.paulliu.xvsqexec;
import java.math.*;

public class Note {
    private String alias=null;
    private int pitch=-1;
    private int duration=0;  /* 480 is 1/4 note */
    private int tempo=0;

    public Note() {
    }

    public Note(Note a) {
	this.alias = a.alias;
	this.pitch = a.pitch;
	this.duration = a.duration;
	this.tempo = a.tempo;
    }

    public void setAlias(String alias) {
	this.alias = alias;
    }
    public String getAlias() {
	return alias;
    }

    public void setPitch(int pitch) {
	this.pitch = pitch;
    }
    public int getPitch() {
	return pitch;
    }
    public double getPitchInHZ() {
	return 440.0*( Math.pow ( Math.pow ( 2.0, 1.0/12.0 ) , (double)(getPitch()-9)));
    }
    public String getPitchInSci() {
	String[] ntable = {"C","C#","D","D#","E","F",
                           "F#","G","G#","A","A#","B"};
	String ret=null;
	int n = getPitch();
	int b;
	int o;
	if (n>=0) {
	    b = n%12;
	    o = (n/12)-1;
	    if (o<0) {
		return ret;
	    }
	} else {
	    return ret;
	}
	ret = String.format("%1$s%2$d",ntable[b],o);
	return ret;
    }

    public void setDuration(int duration) {
	this.duration = duration;
    }
    public int getDuration() {
	return duration;
    }

    public int getTempo() {
	return tempo;
    }
    public void setTempo(int tempo) {
	this.tempo = tempo;
    }

}
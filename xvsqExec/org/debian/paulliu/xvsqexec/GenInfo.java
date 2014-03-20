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

/**
 * This class stores or calculate-in-time the needed information for
 * generating the wav.
 */
public class GenInfo {
    private Note note=null;
    private OtoFileRecord otoFileRecord=null;
    private GenInfo prev=null;
    private GenInfo next=null;

    /**
     * Constructor
     * @note the Note class. can NOT be null.
     * @otoFileRecord the corresponding OtoFileRecord class. can be null.
     */
    public GenInfo(Note note, OtoFileRecord otoFileRecord) {
	this.note = note;
	this.otoFileRecord = otoFileRecord;
    }

    public Note getNote() {
	return note;
    }
    public void setNote(Note note) {
	this.note = note;
    }

    public OtoFileRecord getOtoFileRecord() {
	return otoFileRecord;
    }
    public void setOtoFileRecord(OtoFileRecord otoFileRecord) {
	this.otoFileRecord = otoFileRecord;
    }

    public void setPrev(GenInfo prev) {
	this.prev = prev;
    }
    public GenInfo getPrev() {
	return prev;
    }

    public void setNext(GenInfo next) {
	this.next = next;
    }
    public GenInfo getNext() {
	return next;
    }

    /**
     * Get the time (ms) of the note presented
     * @return time, unit: ms
     */
    public double getDurationInMS() {
	return (((double)(getNote().getDuration()))/1920.0*4.0*(60.0/((double)(getNote().getTempo()))))*1000.0;
    }

    /**
     * Get the prevoice time (ms) based on previous note 
     * @return time, unit: ms
     */
    public double getPrevoice() {
	double prevoice=0.0;
	double blank=0.0;
	if (prev == null) {
	    prevoice=0.0;
	    blank=0.0;
	} else {
	    if (getOtoFileRecord() != null) {
		prevoice = getOtoFileRecord().getPrevoice();
	    }
	    if (prevoice > prev.getDurationInMS()) {
		blank = prevoice - prev.getDurationInMS();
		prevoice = prev.getDurationInMS();
	    }
	}
	return prevoice;
    }

    /**
     * Get the overlap time (ms) based on previous note 
     * @return time, unit: ms
     */
    public double getOverlap() {
	double ret=0.0;
	if (getOtoFileRecord() != null) {
	    ret = getOtoFileRecord().getOverlap();
	}
	if (ret > getPrevoice()) {
	    ret = getPrevoice();
	}
	return ret;
    }

    /**
     * Get the blank time (ms) for wavtool
     * @return time, unit: ms
     */
    public double getWavtoolBlank() {
	double prevoice=0.0;
	double blank=0.0;
	if (prev == null) {
	    prevoice=0.0;
	    blank=0.0;
	} else {
	    if (getOtoFileRecord() != null) {
		prevoice = getOtoFileRecord().getPrevoice();
	    }
	    if (prevoice > prev.getDurationInMS()) {
		blank = prevoice - prev.getDurationInMS();
		prevoice = prev.getDurationInMS();
	    }
	}
	return blank;
    }


    /**
     * Get the suggested generation length (ms) for resampler.
     * @return time, unit: ms
     */
    public double getResamplerSuggestedLength() {
	double ret=0.0;
	ret += getDurationInMS();
	if (getOtoFileRecord() != null) {
	    ret += getOtoFileRecord().getPrevoice();
	}
	if (getNext() != null) {
	    if (getNext().getOtoFileRecord() != null) {
		ret += next.getOtoFileRecord().getOverlap();
	    }
	}
	return ret;
    }

    /**
     * Get Bend for resampler.
     * @return String for bend pitch
     */
    public String getResamplerBend() {
	double prevoiceMS = getPrevoice();
	String ret = "";
	if (getPrev()==null || prevoiceMS <= 0.0) {
	    return ret;
	}
	if (getNote().getPitch()==-1) {
	    return ret;
	}
	if (getPrev().getNote().getPitch()==-1) {
	    return ret;
	}
	String pitchC = GenInfo.pitchDiffBase64((int) java.lang.Math.round(getPrev().getNote().getPitchInHZ()), (int)(java.lang.Math.round(getNote().getPitchInHZ())));
	int bend_len = (int)(getPrevoice()*((double)getNote().getTempo())*96.0/60.0/1000.0);
	StringBuffer bend_buf = new StringBuffer();
	bend_buf.append(pitchC);
	if (bend_len > 1) {
	    bend_buf.append("#");
	    bend_buf.append(Integer.toString(bend_len));
	    bend_buf.append("#");
	}
	ret = bend_buf.toString();
	return ret;
    }

    /**
     * HZ to Note number
     * @hz the HZ
     * @return the note number
     */
    private static double HZ2NoteNum (double hz) {
	return (java.lang.Math.log(hz / 440.0) / java.lang.Math.log( java.lang.Math.pow (2.0, 1.0/12.0) )) + 9.0;
    }

    /**
     * calculate the pitch difference and encoded it by base64
     * @oldPitch the original pitch
     * @newPitch the new pitch
     */
    public static String pitchDiffBase64(int oldPitch,int newPitch) {
	String base64table = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	double dfactor=0.0;
	double i1,i2;
	int factor=0;
	String ret = "AA";
	if (oldPitch > newPitch) {
	    /* this factor is log based */
	    i1 = HZ2NoteNum(oldPitch);
	    i2 = HZ2NoteNum(newPitch);
	    dfactor = (i1-i2) * 100.0;
	    factor = ((int)dfactor);
	    if (factor > 2047) {
		factor = 2047;
	    }
	} else if (newPitch > oldPitch) {
	    i1 = HZ2NoteNum(oldPitch);
	    i2 = HZ2NoteNum(newPitch);
	    dfactor = (i2-i1) * 100.0;
	    factor = ((int)dfactor);
	    if (factor > 2047) {
		factor = 2047;
	    }
	    factor = 4096-factor;
	}
	ret = base64table.substring((factor/64)%64,(factor/64)%64+1) + base64table.substring(factor%64,factor%64+1);
	return ret;
    }


}

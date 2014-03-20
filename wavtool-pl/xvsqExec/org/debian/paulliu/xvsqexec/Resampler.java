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

import java.io.*;
import java.lang.*;

public class Resampler {
    private File resampler = null;
    private int counter=0;
    private File tempDir = null;

    public Resampler(File resampler) {
	counter=0;
	this.resampler = resampler;
	tempDir = createTempDir();
    }

    private File createTempDir() {
	File tempDir=null;
	File tempFile = null;
	String path = null;
	try {
	    tempFile = File.createTempFile("paulliuResampler",null);
	} catch (java.io.IOException e) {
	    return tempDir;
	}
	path = tempFile.getAbsolutePath();
	tempFile.delete();
	tempDir = new File(path);
	tempDir.mkdirs();
	return tempDir;
    }

    public File getWav(File in, String pitch, double offset, double duration,
		       double consonant, double blank, int volume, int tempo,
		       String pitchBend) {
	File tempFile = null;
	try {
	    tempFile = File.createTempFile(String.format("%1$05d_utau_",counter), ".wav", tempDir);
	    counter++;
	    tempFile.delete();
	} catch (java.io.IOException e) {
	    return tempFile;
	}

	ProcessBuilder pb = new ProcessBuilder(resampler.getAbsolutePath(),
					       in.getAbsolutePath(),
					       tempFile.getAbsolutePath(),
					       pitch,
					       "100",
					       "",
					       Double.toString(offset),
					       Double.toString(duration),
					       Double.toString(consonant),
					       Double.toString(blank),
					       Integer.toString(volume),
					       "0",
					       String.format("!%1$d",tempo),
					       pitchBend);
	
	Process p = null;
	try {
	    p =pb.start();
	    p.waitFor();
	} catch (Exception e) {
	}

	return tempFile;
    }
}

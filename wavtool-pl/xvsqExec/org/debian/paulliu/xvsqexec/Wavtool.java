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

/**
 * Wavtool class.
 * This class represent a wavtool.exe which concat wav files into one
 */
public class Wavtool {
    private java.util.logging.Logger logger = null;
    private static String logger_name = "xvsq";

    private File wavtool = null;
    private File outputFile = null;

    /**
     * constructor
     * @param wavtool the path of wavtool
     * @param outputFile the output wavfile
     */
    public Wavtool(File wavtool, File outputFile) {
	this.wavtool=wavtool;
	this.outputFile = outputFile;
	this.logger = java.util.logging.Logger.getLogger(Wavtool.logger_name);
    }

    /**
     * concat input wav to the output wavfile
     * It outputs the file to a temporarily file .wav.whd and .wav.dat.
     * To get the real .wav please call generateWav() after all the
     * input files are put.
     *
     * @param in the input wav file
     * @param offset the offset in ms. Skip ms from head.
     * @param length the total output length in ms.
     * @param overlap overlap ms with previous output
     * @param p should be double[5]. The time of control points. It should be begin -> p[0] -> p[1] -> p[4] ... p[2] <- p[3] <- end
     * @param v the corresponding volume to p. value: 0~100 to represent percent.
     *
     */
    public void putWav(File in, 
		       double offset, double length,
		       double overlap, double[] p, 
		       double[] v) {
	String filename_in = null;
	if (in != null) {
	    filename_in = in.getAbsolutePath();
	} else {
	    filename_in = new String("P");
	}
	String lengthString = String.format("0@60+%1$.3f", length);

	logger.info(String.format("%1$s %2$s %3$s %4$s %5$s %6$s %7$s %8$s %9$s %10$s %11$s %12$s %13$s %14$s %15$s %16$s",
				  wavtool.getAbsolutePath(),
				  outputFile.getAbsolutePath(),
				  filename_in,
				  Double.toString(offset),
				  lengthString,
				  Double.toString(p[0]),
				  Double.toString(p[1]),
				  Double.toString(p[2]),
				  Double.toString(v[0]),
				  Double.toString(v[1]),
				  Double.toString(v[2]),
				  Double.toString(v[3]),
				  Double.toString(overlap),
				  Double.toString(p[3]),
				  Double.toString(p[4]),
				  Double.toString(v[4])));

	ProcessBuilder pb = new ProcessBuilder(wavtool.getAbsolutePath(),
					       outputFile.getAbsolutePath(),
					       filename_in,
					       Double.toString(offset),
					       lengthString,
					       Double.toString(p[0]),
					       Double.toString(p[1]),
					       Double.toString(p[2]),
					       Double.toString(v[0]),
					       Double.toString(v[1]),
					       Double.toString(v[2]),
					       Double.toString(v[3]),
					       Double.toString(overlap),
					       Double.toString(p[3]),
					       Double.toString(p[4]),
					       Double.toString(v[4]) );
	
	Process process = null;
	try {
	    process = pb.start();
	    process.waitFor();
	} catch (Exception e) {
	}

    }

    /**
     * Generate the real wav file from temp .wav.whd and temp .wav.dat
     */
    public void generateWav() {
	File wavWHD = new File(outputFile.getAbsolutePath()+".whd");
	File wavDAT = new File(outputFile.getAbsolutePath()+".dat");

	FileOutputStream fout = null;

	try {
	    fout = new FileOutputStream(outputFile);
	} catch (java.io.IOException e) {
	    return;
	}

	if (wavWHD.exists()) {
	    FileInputStream fin1 = null;
	    try {
		fin1 = new FileInputStream(wavWHD);
	    } catch (java.io.IOException e) {
		return;
	    }
	    byte[] buf = new byte[1024];
	    while (true) {
		int r = 0;
		try {
		    r = fin1.read(buf);
		} catch (java.io.IOException e) {
		    break;
		}
		if (r<=0) {
		    break;
		}
		try {
		    fout.write(buf,0,r);
		} catch (java.io.IOException e) {
		    break;
		}
	    }
	}
	if (wavDAT.exists()) {
	    FileInputStream fin1 = null;
	    try {
		fin1 = new FileInputStream(wavDAT);
	    } catch (java.io.IOException e) {
		return;
	    }
	    byte[] buf = new byte[1024];
	    while (true) {
		int r = 0;
		try {
		    r = fin1.read(buf);
		} catch (java.io.IOException e) {
		    break;
		}
		if (r<=0) {
		    break;
		}
		try {
		    fout.write(buf,0,r);
		} catch (java.io.IOException e) {
		    break;
		}
	    }
	}
	try {
	    fout.close();
	} catch (java.io.IOException e) {
	}
    }
}

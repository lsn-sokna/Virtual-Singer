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
import java.util.*;

public class Main {

    private java.util.logging.Logger logger = null;
    private static String logger_name = "xvsq";
    private Resampler resampler = null;
    private Wavtool wavTool = null;

    public Main() {
	this.logger = java.util.logging.Logger.getLogger(Main.logger_name);
    }

    public void usage() {
	System.out.println("xvsqexec <lyrics.xvsq> <wavtool executable> <efb-gw executable> <out.wav> <oto.ini ...>");
    }

    private int[] KMPSetup(byte[] pat) {
	int length = pat.length;
	int[] fail = new int[length];
	int i,j;
	fail[0]=-1;
	for (i=1; i<length; i++) {
	    for (j=fail[i-1]; j>=0 && pat[j+1]!=pat[i]; j=fail[j]) ;
	    fail[i] = (j<0 && pat[j+1] != pat[i]) ? -1 : j+1;
	}
	return fail;
    }

    /**
     * KMP algorithm to search patttern inside a file
     *
     * @param text the file inputstream
     * @param pat the pattern.
     * @return position that finds the pattern. -1 means not found
     */
    private int KMP(InputStream text, byte[] pat)  {
	int[] fail = KMPSetup(pat);
	int p_length = pat.length;
	int p;
	int r=0;
	byte t;
	byte[] buf = new byte[1];
	int ret=0;

	try {
	    r = text.read(buf);
	    ret += r;
	} catch (java.io.IOException e) {
	    return -1;
	}
	if (r<=0) {
	    return -1;
	}
	t = buf[0];
	for (p=0; p<p_length; ) {
	    if (t != pat[p]) {
		if (p>0) {
		    p = fail[p-1]+1;
		} else {
		    try {
			r = text.read(buf);
			ret += r;
		    } catch (java.io.IOException e) {
			break;
		    }
		    if (r<=0) {
			break;
		    }
		    t = buf[0];
		}
	    } else {
		p++;
		try {
		    r = text.read(buf);
		    ret += r;
		} catch (java.io.IOException e) {
		    break;
		}
		if (r<=0) {
		    break;
		}
		t = buf[0];
	    }
	}
	return (p >= p_length) ? ret : -1;
    }

    private boolean isResampler(File resamplerFile) {
	FileInputStream fin = null;
	try {
	    fin = new FileInputStream(resamplerFile);
	} catch (java.io.IOException e) {
	    return false;
	}
	int r;
	r = KMP(fin, new String("WORLD").getBytes());
	try {
	    fin.close();
	} catch (java.io.IOException e) {
	}
	if (r>=0) {
	    return true;
	}
	return false;
    }

    private boolean isWavtool(File wavtoolFile) {
	FileInputStream fin = null;
	try {
	    fin = new FileInputStream(wavtoolFile);
	} catch (java.io.IOException e) {
	    return false;
	}
	int r;
	r = KMP(fin, new String("wavtool").getBytes());
	try {
	    fin.close();
	} catch (java.io.IOException e) {
	}
	if (r>=0) {
	    return true;
	}
	return false;
    }

    public void begin(String[] argv) {
	if (argv.length <= 0) {
	    usage();
	    return;
	}

	OtoIniManager oiManager = new OtoIniManager();
	XvsqReader xvsqReader = null;
	File resamplerFile = null;
	File outputWavFile = null;
	File wavtoolFile = null;
	for (int i=0; i<argv.length; i++) {
	    File file = new File(argv[i]);
	    if (argv[i].endsWith("oto.ini")) {
		oiManager.addOtoFile(file);
	    } else if (argv[i].endsWith(".xvsq")) {
		xvsqReader = new XvsqReader(file);
	    } else if (argv[i].endsWith(".wav")) {
		outputWavFile = file;
		File wavWHD = new File(argv[i]+".whd");
		File wavDAT = new File(argv[i]+".dat");

		if (wavWHD.exists()) {
		    wavWHD.delete();
		}
		if (wavDAT.exists()) {
		    wavDAT.delete();
		}
		if (outputWavFile.exists()) {
		    outputWavFile.delete();
		}
	    } else if (file.exists() && file.canExecute() && isWavtool(file)) {
		wavtoolFile = file;
	    } else if (file.exists() && file.canExecute() && isResampler(file)) {
		resamplerFile = file;
	    } else {
		logger.severe("Don't know how to open "+argv[i]);
	    }
	}

	if (wavtoolFile == null || resamplerFile == null
	    || outputWavFile==null || xvsqReader == null) {
	    usage();
	    return;
	}
	resampler = new Resampler(resamplerFile);
	wavTool = new Wavtool(wavtoolFile, outputWavFile);

	ArrayList<Note> notes;
	notes = xvsqReader.getNotes();
	for (int i=0; i<notes.size(); i++) {
	    Note note = notes.get(i);
	    logger.info(String.format("Note: %1$s %2$d %3$d",note.getAlias(), note.getPitch(), note.getDuration()));
	}

	HashMap<String, OtoFileRecord> otoFileRecords = oiManager.getAll();
	for (Iterator<String> i= otoFileRecords.keySet().iterator(); i.hasNext(); ) {
	    String alias1 = i.next();
	    OtoFileRecord r = otoFileRecords.get(alias1);
	    logger.info(String.format("Oto.ini: %1$s %2$s %3$s %4$f %5$f %6$f %7$f %8$f",
				      r.getFilename().getPath(), 
				      r.getAlias(),
				      r.getPath().getPath(),
				      r.getOffset(),
				      r.getConsonant(),
				      r.getBlank(),
				      r.getPrevoice(),
				      r.getOverlap()));
	}


	ArrayList<GenInfo> genInfoArray = new ArrayList<GenInfo>();
	/* find OtoFileRecord */
	for (int i=0; i<notes.size(); i++) {
	    Note note = notes.get(i);
	    OtoFileRecord oto = null;
	    oto = oiManager.get(note.getAlias());
	    GenInfo genInfo = new GenInfo(note, oto);
	    genInfoArray.add(genInfo);
	}

	for (int i=0; i<genInfoArray.size(); i++) {
	    GenInfo g = genInfoArray.get(i);
	    if (i+1 < genInfoArray.size()) {
		g.setNext(genInfoArray.get(i+1));
	    }
	    if (i>=1) {
		g.setPrev(genInfoArray.get(i-1));
	    }
	}

	for (int i=0; i<genInfoArray.size(); i++) {
	    Note note = genInfoArray.get(i).getNote();
	    double durationInMS = genInfoArray.get(i).getDurationInMS();

	    logger.info(String.format("Note: %1$s %2$d(%3$s) %4$d(%5$f)",note.getAlias(), note.getPitch(), note.getPitchInSci(), note.getDuration(), durationInMS));
	    OtoFileRecord oto=genInfoArray.get(i).getOtoFileRecord();
	    if (oto != null) {
		logger.info(String.format("Oto: %1$s %2$s %3$s %4$f %5$f %6$f %7$f %8$f",
					  oto.getFilename().getPath(), 
					  oto.getAlias(),
					  oto.getPath().getPath(),
					  oto.getOffset(),
					  oto.getConsonant(),
					  oto.getBlank(),
					  oto.getPrevoice(),
					  oto.getOverlap()));
	    }

	    /*	    double nextOverlap = 0.0;
	    double nextPrevoice = 0.0;
	    if ( i+1 < notes.size() ) {
		Note noteNext = notes.get(i+1);
		OtoFileRecord otoNext = oiManager.get(noteNext.getAlias());
		if (otoNext != null) {
		    nextOverlap = otoNext.getOverlap();
		    nextPrevoice = otoNext.getPrevoice();
		}
		}*/
	    File out=null;
	    if (oto != null) {
		File in = new File(oto.getPath(), oto.getFilename().getPath());
		out = resampler.getWav(in, note.getPitchInSci(), oto.getOffset(), genInfoArray.get(i).getResamplerSuggestedLength(), oto.getConsonant(), oto.getBlank(), 100, xvsqReader.getTempo(), genInfoArray.get(i).getResamplerBend());
	    }
	    if (out != null) {
		double p[] = new double[5];
		double v[] = new double[5];
		double nextOverlap=0.0;
		double nextPrevoice=0.0;
		//p[0] = genInfoArray.get(i).getOverlap();
		p[0] = 0.0;
		v[0] = 100.0;
		p[1] = 0.0;
		v[1] = v[0];
		p[4] = 0.0;
		v[4] = v[1];
		if (genInfoArray.get(i).getNext()!=null) {
		    nextOverlap = genInfoArray.get(i).getNext().getOverlap();
		    nextPrevoice = genInfoArray.get(i).getNext().getPrevoice();
		}
		//p[3] = nextOverlap;
		p[3] = 0.0;
		v[3] = 100.0;
		p[2] = 0.0;
		v[2] = v[3];
		logger.info(String.format("oto.getPrevoice() = %1$f, durationInMS = %2$f, nextOverlap = %3$f, nextPrevoice = %4$f", oto.getPrevoice(), durationInMS, nextOverlap, nextPrevoice));
		double length1 = genInfoArray.get(i).getPrevoice() + genInfoArray.get(i).getDurationInMS() + nextOverlap - nextPrevoice;
		if (length1 < 0.0) {
		    logger.warning(String.format("Warning: length1 = %1$f < 0", length1));
		    length1=0.0;
		}

		wavTool.putWav(out, genInfoArray.get(i).getWavtoolBlank(), length1, genInfoArray.get(i).getOverlap() , p, v);
	    } else {
		double p[] = new double[5];
		double v[] = new double[5];
		double nextOverlap=0.0;
		double nextPrevoice=0.0;

		if (genInfoArray.get(i).getNext()!=null) {
		    nextOverlap = genInfoArray.get(i).getNext().getOverlap();
		    nextPrevoice = genInfoArray.get(i).getNext().getPrevoice();
		}

		double length1 = durationInMS + nextOverlap - nextPrevoice;

		if (length1 < 0.0) {
		    logger.warning(String.format("Warning: length1 = %1$f < 0", length1));
		    length1=0.0;
		}

		wavTool.putWav(out, genInfoArray.get(i).getWavtoolBlank(), length1, 0.0, p, v);
	    }
	}
	wavTool.generateWav();
    }

    public static void main(String[] argv) {
	Main main = new Main();
	main.begin(argv);
    }
}

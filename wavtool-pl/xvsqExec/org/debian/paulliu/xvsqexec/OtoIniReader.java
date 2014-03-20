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

/**
 * Class to read an oto.ini
 *
 * The format of oto.ini:
 * Filename Alias Offset 子音部 Blank 先行發聲 Overlap
 * Unit: ms
 * Filename: the file name of wav file.
 * Alias: the alias used in editor.
 * Offset: 從 0 秒開始算. 數值 >=0 
 * 子音部: 從 offset 開始算, 數值 >=0
 * Blank: 從尾端開始倒算. 數值 >=0. 其中 Offset+Blank 不可能超過整個 wav 長度
 *        若數值 < 0. 則為從 Offset 起算
 * 先行發聲: 0 ~ wav 長度
 * Overlap: 任意值
 *
 */
public class OtoIniReader {
    private File otoFile;

    public OtoIniReader(File otoFile) {
	this.otoFile = otoFile;
    }

    /**
     * get OtoFileRecords from file
     * @return an ArrayList of OtoFileRecord.
     */
    public ArrayList<OtoFileRecord> getOtoFileRecords() {
	ArrayList<OtoFileRecord> ret = new ArrayList<OtoFileRecord>();

	boolean isUTF=false;
	isUTF = isUnicode();

	FileInputStream fin_is = null;

	try {
	    fin_is = new FileInputStream(otoFile);
	} catch (java.io.FileNotFoundException e) {
	    return ret;
	}

	BufferedReader fin = null;
	try {
	    InputStreamReader fin_sr=null;
	    if (isUTF) {
		fin_sr = new InputStreamReader(fin_is, "UTF-16");
	    } else {
		fin_sr = new InputStreamReader(fin_is, "SJIS");
	    }
	    fin = new BufferedReader(fin_sr);
	} catch (Exception e) {
	    return ret;
	}

	while (true) {
	    String s=null;
	    try {
		s = fin.readLine();
	    } catch (Exception e) {
		break;
	    }
	    if (s==null) {
		break;
	    }
	    String[] s_a=null;
	    s_a = s.split("[=]");
	    if (s_a == null || s_a.length != 2) {
		continue;
	    }
	    String filename = s_a[0];
	    String[] s_b = null;
	    s_b = s_a[1].split("[,]");
	    if (s_b == null || s_b.length != 6) {
		continue;
	    }
	    String alias = s_b[0];
	    String offset_s = s_b[1];
	    String consonant_s = s_b[2];
	    String blank_s = s_b[3];
	    String prevoice_s = s_b[4];
	    String overlap_s = s_b[5];

	    double offset=0.0;
	    double consonant=0.0;
	    double blank=0.0;
	    double prevoice=0.0;
	    double overlap=0.0;
	    try {
		offset = Double.parseDouble(offset_s);
		consonant = Double.parseDouble(consonant_s);
		blank = Double.parseDouble(blank_s);
		prevoice = Double.parseDouble(prevoice_s);
		overlap = Double.parseDouble(overlap_s);
	    } catch (Exception e) {
		continue;
	    }
	    OtoFileRecord rec = new OtoFileRecord();
	    rec.setFilename(filename);
	    rec.setAlias(alias);
	    File path = null;
	    try {
		path = otoFile.getAbsoluteFile().getParentFile();
	    } catch (Exception e) {
		continue;
	    }
	    if (path == null) {
		continue;
	    }
	    rec.setPath(path);
	    rec.setOffset(offset);
	    rec.setConsonant(consonant);
	    rec.setBlank(blank);
	    rec.setPrevoice(prevoice);
	    rec.setOverlap(overlap);
	    ret.add(rec);
	}

	return ret;
    }

    /**
     * return true if this file is a unicode file
     */
    private boolean isUnicode() {
	boolean ret=false;
	FileInputStream fin = null;

	try {
	    fin = new FileInputStream(otoFile);
	} catch (java.io.FileNotFoundException e) {
	    return ret;
	}

	try {
	    int c;
	    int c2;
	    c = fin.read();
	    if (c==0xff) {
		c2 = fin.read();
		if (c2==0xfe) {
		    ret=true;
		}
	    } else if (c==0xfe) {
		c2 = fin.read();
		if (c2==0xff) {
		    ret=true;
		}
	    }
	} catch (Exception e) {
	}

	try {
	    fin.close();
	} catch (Exception e) {
	}

	return ret;
    }

}

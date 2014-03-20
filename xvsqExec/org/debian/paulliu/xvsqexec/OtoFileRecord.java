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

public class OtoFileRecord {
    private File filename;
    private String alias;
    private File path;
    private double offset;
    private double consonant;
    private double blank;
    private double prevoice;
    private double overlap;

    public OtoFileRecord() {
    }

    public void setFilename(String filename) {
	this.filename = new File(filename);
    }
    public File getFilename() {
	return filename;
    }

    public void setAlias(String alias) {
	this.alias = alias;
    }
    public String getAlias() {
	return alias;
    }

    public void setPath(File path) {
	this.path = path;
    }
    public File getPath() {
	return path;
    }

    public void setOffset(double offset) {
	this.offset = offset;
    }
    public double getOffset() {
	return offset;
    }

    public void setConsonant(double consonant) {
	this.consonant = consonant;
    }
    public double getConsonant() {
	return consonant;
    }

    public void setBlank(double blank) {
	this.blank = blank;
    }
    public double getBlank() {
	return blank;
    }

    public void setPrevoice(double prevoice) {
	this.prevoice = prevoice;
    }
    public double getPrevoice() {
	return prevoice;
    }

    public void setOverlap(double overlap) {
	this.overlap = overlap;
    }
    public double getOverlap() {
	return overlap;
    }


}

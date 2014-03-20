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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XvsqReader {

    private java.util.logging.Logger logger = null;
    private File xvsqFile;
    private static String logger_name = "xvsq";
    private Document doc = null;

    public XvsqReader(File xvsqFile) {
	this.logger = java.util.logging.Logger.getLogger(XvsqReader.logger_name);
	this.xvsqFile = xvsqFile;

	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder db = null;
	try {
	    db = dbf.newDocumentBuilder();
	} catch (javax.xml.parsers.ParserConfigurationException e) {
	    logger.severe(e.toString());
	}
	try {
	    doc = db.parse(xvsqFile);
	} catch (org.xml.sax.SAXException e) {
	    logger.severe(e.toString());
	} catch (java.io.IOException e) {
	    logger.severe(e.toString());
	}

	doc.getDocumentElement().normalize();
    }

    private String getNodeText(Node node) {
	StringBuffer ret = new StringBuffer();

	NodeList childs = node.getChildNodes();
	for (int i=0; i<childs.getLength(); i++) {
	    Node child = childs.item(i);
	    if (child.getNodeType() != org.w3c.dom.Node.TEXT_NODE) {
		continue;
	    }
	    org.w3c.dom.Text nodeText = (org.w3c.dom.Text) child;
	    ret.append(nodeText.getWholeText());
	}
	return ret.toString();
    }

    public int getTempo() {
	int ret=120;

	Element root = doc.getDocumentElement();
	NodeList firstTempoTableList = root.getElementsByTagName("TempoTable");
	if (firstTempoTableList==null) {
	    return ret;
	}
	Node firstTempoTable_node = firstTempoTableList.item(0);
	if (firstTempoTable_node.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
	    return ret;
	}
	Element firstTempoTable = (Element)firstTempoTable_node;

	NodeList tempoTableEntryList = firstTempoTable.getElementsByTagName("TempoTableEntry");
	for (int i=0; i<tempoTableEntryList.getLength(); i++) {
	    Node tempoTableEntry_node = tempoTableEntryList.item(i);
	    if (tempoTableEntry_node.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
		continue;
	    }
	    Element tempoTableEntry = (Element)tempoTableEntry_node;
	    Node clock_node = tempoTableEntry.getElementsByTagName("Clock").item(0);
	    Node tempo_node = tempoTableEntry.getElementsByTagName("Tempo").item(0);
	    if (getNodeText(clock_node).compareTo("0")!=0) {
		continue;
	    }
	    ret = 60*1000000/Integer.parseInt(getNodeText(tempo_node));
	    break;
	}
	
	return ret;
    }

    public ArrayList<Note> getNotes() {
	ArrayList<Note> ret = new ArrayList<Note>();

	Element root = doc.getDocumentElement();

	NodeList rootChilds = root.getChildNodes();

	/* get first Track node */
	Node firstTrackNode = root.getElementsByTagName("Track").item(0);

	Element targetVsqTrack = null;
	NodeList childs = firstTrackNode.getChildNodes();
	for (int i=0; i<childs.getLength(); i++) {
	    Node child = childs.item(i);
	    if (child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
		continue;
	    }
	    if (child.getNodeName().compareTo("VsqTrack")!=0) {
		continue;
	    }
	    Node trackName = ((Element)(child)).getElementsByTagName("Name").item(0);
	    if (getNodeText(trackName).compareTo("Master Track")==0) {
		continue;
	    }
	    logger.info("Use track: "+getNodeText(trackName));
	    targetVsqTrack = (Element)child;
	}

	if (targetVsqTrack == null) {
	    return ret;
	}

	int previousClock=0;
	NodeList vsqEvents = targetVsqTrack.getElementsByTagName("VsqEvent");
	for (int i=0; i<vsqEvents.getLength(); i++) {
	    Node child1 = vsqEvents.item(i);
	    if (child1.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
		continue;
	    }
	    Element node_Child = (Element)child1;
	    /* padding P note if clock doesn't match */
	    Element node_Clock = (Element)node_Child.getElementsByTagName("Clock").item(0);
	    int clock = Integer.parseInt(getNodeText(node_Clock));
	    if (clock > previousClock) {
		Note notePadding = new Note();
		notePadding.setDuration(clock-previousClock);
		notePadding.setTempo(this.getTempo());
		ret.add(notePadding);
		previousClock = clock;
	    }
	    Element node_ID = (Element)node_Child.getElementsByTagName("ID").item(0);
	    Element node_LyricHandle = (Element)node_ID.getElementsByTagName("LyricHandle").item(0);
	    node_LyricHandle.normalize();
	    Note note1 = new Note();
	    if (node_LyricHandle.getChildNodes() == null || node_LyricHandle.getChildNodes().getLength()<=0) {
	    } else {
		Element node_Phrase = (Element)node_LyricHandle.getElementsByTagName("Phrase").item(0);
		node_Phrase.normalize();
		note1.setAlias(getNodeText(node_Phrase));
	    }

	    Element node_Note = (Element)node_ID.getElementsByTagName("Note").item(0);
	    if (node_Note == null) {
		note1.setPitch(-1);
	    } else {
		int pitch = -1;
		try {
		    pitch = Integer.parseInt(getNodeText(node_Note));
		} catch (Exception e) {
		    pitch = -1;
		    logger.warning(e.toString());
		}
		note1.setPitch(pitch);
	    }

	    Element node_Length = null;
	    NodeList node_ID_childs = node_ID.getChildNodes();
	    for (int i1=0; i1<node_ID_childs.getLength(); i1++) {
		Node node_ID_child = node_ID_childs.item(i1);
		if (node_ID_child.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
		    continue;
		}
		if (node_ID_child.getNodeName().compareTo("Length")!=0) {
		    continue;
		}
		node_Length = (Element)node_ID_child;
		break;
	    }
	    int duration=0;
	    try {
		duration = Integer.parseInt(getNodeText(node_Length));
	    } catch (Exception e) {
		duration = 0;
		logger.warning(e.toString());
	    }
	    note1.setDuration(duration);
	    note1.setTempo(this.getTempo());
	    if (note1.getDuration() > 0) {
		previousClock += note1.getDuration();
		ret.add(note1);
	    }

	}

	return ret;
    }
}
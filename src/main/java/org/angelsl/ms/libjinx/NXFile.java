/*
 * Copyright 2011-2012 angelsl.
 *
 * This file is part of libjinx.
 *
 * libjinx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * libjinx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with libjinx.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules,
 * and to copy and distribute the resulting executable under terms of your
 * choice, provided that you also meet, for each linked independent module,
 * the terms and conditions of the license of that module. An independent
 * module is a module which is not derived from or based on this library.
 * If you modify this library, you may extend this exception to your version
 * of the library, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version.
 */

package org.angelsl.ms.libjinx;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Pattern;

/**
 * An NX file.
 */
public class NXFile {

    private NXNode<?> _baseNode = null;
    private final RandomAccessFile _file;
    private final LittleEndianReader _ler;
    private String[] _strTbl = null;
    private long[] _bmpOffTbl = null;
    private long[] _mp3OffTbl = null;
    NXNode<?>[] _nodeTbl = null;

    private int _nodeId = 0;

    /**
     * Constructs an NX file from the given path and parses the file immediately.
     *
     * @param path The path the NX file is located at
     * @throws FileNotFoundException
     */
    public NXFile(String path) throws IOException, NXException {
        this(new RandomAccessFile(path, "r"));
    }

    /**
     * Constructs an NX file from a RandomAccessFile and parses the file immediately.
     *
     * @param file The RandomAccessFile representing the NX file
     */
    public NXFile(RandomAccessFile file) throws IOException, NXException {
        _file = file;
        _ler = new LittleEndianReader(_file);
        Parse();
    }

    /**
     * Returns the base node of the NX file, the node that is the eventual parent of all other nodes in the file.
     *
     * @return The base node
     */
    public NXNode<?> getBaseNode() {
        return _baseNode;
    }

    /**
     * Resolves a path in the form x/y/../y/./a
     *
     * @param s The path in the NX file
     * @return The node at the path, or null if no such node exists.
     */
    public NXNode<?> resolvePath(String s) {
        String[] e = (s.startsWith("/") ? s.substring(1) : s).split(Pattern.quote("/"));
        NXNode<?> r = _baseNode;
        for (String f : e) {
            if (f.equals(".")) continue;
            else if (f.equals("..")) r = _baseNode._parent;
            else r = _baseNode.getChild(f);
            if (r == null) return null;
        }
        return r;
    }

    // read header
    private void Parse() throws IOException, NXException {
        if (_ler.readUInt() != 0x32474B50) throw new NXException("Invalid NX file; magic not found");
        ParseStringTable();
        ParseBitmapTable();
        ParseMP3Table();

        // parse nodes
        _ler.seek(4);
        long nodeCount = _ler.readUInt();
        if (nodeCount > Integer.MAX_VALUE)
            throw new NXException("Unable to parse NX file; too many nodes (Java limitation)");
        if (nodeCount < 1) throw new NXException("Invalid NX file; zero nodes!");
        long baseNodeOffset = _ler.readLong();
        if (baseNodeOffset < 0)
            throw new NXException("Unable to parse NX file; node block out of range (Java limitation)");
        _ler.seek(baseNodeOffset);
        _nodeTbl = new NXNode<?>[(int) nodeCount];
        _baseNode = ParseNode(null);
        _strTbl = null; _bmpOffTbl = null; _mp3OffTbl = null;
    }

    private NXNode<?> ParseNode(NXNode<?> parent) throws IOException, NXException {
        String name = _strTbl[(int) _ler.readUInt()];
        short type = _ler.readUByte();
        NXNode<?> ret;
        switch (type & 0x7F) {
            case 0:
                ret = new NXNode<NXNode.Nothing>(name, NXNode._nothing, this, parent);
                break;
            case 1:
                ret = new NXNode<Integer>(name, _ler.readInt(), this, parent);
                break;
            case 2:
                ret = new NXNode<Double>(name, _ler.readDouble(), this, parent);
                break;
            case 3:
                ret = new NXNode<String>(name, _strTbl[(int) _ler.readUInt()], this, parent);
                break;
            case 4:
                ret = new NXNode<Point>(name, _ler.readPos(), this, parent);
                break;
            case 5:
                ret = new NXCanvasNode(name, this, parent, _ler, _bmpOffTbl.length > 0 ? _bmpOffTbl[(int) _ler.readUInt()] : (-1 + (0*_ler.readUInt())));
                break;
            case 6:
                ret = new NXMP3Node(name, this, parent, _ler, _mp3OffTbl.length > 0 ? _mp3OffTbl[(int) _ler.readUInt()] : (-1 + (0*_ler.readUInt())));
                break;
            case 7:
                ret = new NXLinkNode(name, (int) _ler.readUInt(), this, parent);
                break;
            default:
                throw new NXException("Unknown node type " + (type & 0x7F));
        }
        _nodeTbl[_nodeId++] = ret;
        if ((type & 0x80) != 0x80) return ret;
        int childCount = _ler.readUShort();
        for (; childCount > 0; --childCount) ret.addChild(ParseNode(ret));
        return ret;
    }

    private void ParseStringTable() throws IOException, NXException {
        _ler.seek(16);
        long strCount = _ler.readUInt();
        if (strCount > Integer.MAX_VALUE)
            throw new NXException("Unable to parse NX file; too many strings (Java limitation)");
        if (strCount < 1) throw new NXException("Invalid NX file; zero strings in string table");
        long strOffset = _ler.readLong(); // N.B. in NX spec, this is a uint64, but Java has no unsigned longs
        if (strOffset < 0)
            throw new NXException("Unable to parse NX file; string table out of range (Java limitation)");
        _ler.seek(strOffset);
        _strTbl = new String[(int) strCount];
        for (int i = 0; i < strCount; ++i)
            _strTbl[i] = _ler.readNXUTFString();
    }

    private void ParseBitmapTable() throws IOException, NXException {
        _ler.seek(28);
        long bmpCount = _ler.readUInt();
        if (bmpCount > Integer.MAX_VALUE)
            throw new NXException("Unable to parse NX file; too many bitmaps (Java limitation)");
        if (bmpCount == 0) {
            _bmpOffTbl = new long[0];
            return;
        }
        long bmpOffOffset = _ler.readLong();
        if (bmpOffOffset < 0)
            throw new NXException("Unable to parse NX file; bitmap offset table out of range (Java limitation)");
        _ler.seek(bmpOffOffset);
        _bmpOffTbl = new long[(int) bmpCount];
        for (int i = 0; i < bmpCount; ++i)
            _bmpOffTbl[i] = _ler.readLong();
    }

    private void ParseMP3Table() throws IOException, NXException {
        _ler.seek(40);
        long mp3Count = _ler.readUInt();
        if (mp3Count > Integer.MAX_VALUE)
            throw new NXException("Unable to parse NX file; too many MP3s (Java limitation)");
        if (mp3Count == 0) {
            _mp3OffTbl = new long[0];
            return;
        }
        long mp3OffOffset = _ler.readLong();
        if (mp3OffOffset < 0)
            throw new NXException("Unable to parse NX file; MP3 offset table out of range (Java limitation)");
        _ler.seek(mp3OffOffset);
        _mp3OffTbl = new long[(int) mp3Count];
        for (int i = 0; i < mp3Count; ++i)
            _mp3OffTbl[i] = _ler.readLong();
    }
}

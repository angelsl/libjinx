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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UTFDataFormatException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class LittleEndianReader {
    private final ByteBuffer bs;

    LittleEndianReader(ByteBuffer bs) {
        this.bs = bs.order(ByteOrder.LITTLE_ENDIAN);
    }

    private int intlReadByte() throws BufferUnderflowException {
        int ret = bs.get() & 0xFF;
        return ret;
    }

    public byte readByte() throws BufferUnderflowException {
        return bs.get();
    }

    public int readUByte() throws BufferUnderflowException {
        return intlReadByte();
    }

    public short readShort() throws BufferUnderflowException {
        return bs.getShort();
    }

    public int readUShort() throws BufferUnderflowException {
        return bs.getShort() & 0xFFFF;
    }

    public int readInt() throws BufferUnderflowException {
        return bs.getInt();
    }

    public long readUInt() throws BufferUnderflowException {
        return ((long)bs.getInt()) & 0xFFFFFFFFL;
    }

    public long readLong() throws BufferUnderflowException {
        return bs.getLong();
    }

    public float readFloat() throws BufferUnderflowException {
        return Float.intBitsToFloat(readInt());
    }

    public double readDouble() throws BufferUnderflowException {
        return Double.longBitsToDouble(readLong());
    }

    public final String readNXUTFString() throws UTFDataFormatException, BufferUnderflowException {
        return readUTF(this);
    }

    public byte[] read(int num) throws BufferUnderflowException {
        byte[] ret = new byte[num];
        for (int x = 0; x < num; x++) {
            ret[x] = readByte();
        }
        return ret;
    }

    public final Point readPos() throws BufferUnderflowException {
        final int x = readInt();
        final int y = readInt();
        return new Point(x, y);
    }

    public void skip(int num) throws IllegalArgumentException {
        bs.position(bs.position() + num);
    }

    public void seek(long offset) throws BufferUnderflowException {
        if(offset > Integer.MAX_VALUE) throw new IllegalArgumentException("Cannot seek to position more than 2147483647; Java limitation.");
        bs.position((int)offset);
    }

    @Override
    public String toString() {
        return bs.toString();
    }

    private final static String readUTF(LittleEndianReader in) throws BufferUnderflowException, UTFDataFormatException {
        int utflen = in.readUShort();
        byte[] bytearr = new byte[utflen];
        char[] chararr = new char[utflen];


        int c, char2, char3;
        int count = 0;
        int chararr_count = 0;

        in.bs.get(bytearr, 0, utflen);

        while (count < utflen) {
            c = (int) bytearr[count] & 0xff;
            if (c > 127) break;
            count++;
            chararr[chararr_count++] = (char) c;
        }

        while (count < utflen) {
            c = (int) bytearr[count] & 0xff;
            switch (c >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    /* 0xxxxxxx*/
                    count++;
                    chararr[chararr_count++] = (char) c;
                    break;
                case 12:
                case 13:
                    /* 110x xxxx   10xx xxxx*/
                    count += 2;
                    if (count > utflen)
                        throw new UTFDataFormatException(
                                "malformed input: partial character at end");
                    char2 = (int) bytearr[count - 1];
                    if ((char2 & 0xC0) != 0x80)
                        throw new UTFDataFormatException(
                                "malformed input around byte " + count);
                    chararr[chararr_count++] = (char) (((c & 0x1F) << 6) |
                            (char2 & 0x3F));
                    break;
                case 14:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    count += 3;
                    if (count > utflen)
                        throw new UTFDataFormatException(
                                "malformed input: partial character at end");
                    char2 = (int) bytearr[count - 2];
                    char3 = (int) bytearr[count - 1];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        throw new UTFDataFormatException(
                                "malformed input around byte " + (count - 1));
                    chararr[chararr_count++] = (char) (((c & 0x0F) << 12) |
                            ((char2 & 0x3F) << 6) |
                            ((char3 & 0x3F) << 0));
                    break;
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException(
                            "malformed input around byte " + count);
            }
        }
        // The number of chars produced may be less than utflen
        return new String(chararr, 0, chararr_count);
    }
}
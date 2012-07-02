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

// TODO: Canvas properties

import java.awt.image.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.github.decster.jnicompressions.Lz4Compression;
/**
 * A bitmap in an NX file.
 */
public class NXCanvasNode extends NXNode<BufferedImage> {
    private final LittleEndianReader _ler;
    private final long _bmOffset;

    NXCanvasNode(final String name, final NXFile file, final NXNode<?> parent, final LittleEndianReader ler, final long bmOffset) {
        super(name, null, file, parent);
        _ler = ler;
        _bmOffset = bmOffset;
    }

    @Override
    public BufferedImage getValue() {
        if(_bmOffset == -1) return null;
        try {
            if (_value == null) {
                _ler.seek(_bmOffset);
                int w = _ler.readUShort();
                int h = _ler.readUShort();
                long len = _ler.readUInt();
                Lz4Compression c = new Lz4Compression();
                ByteBuffer out = ByteBuffer.allocateDirect(w*h*4);
                c.DecompressDirect(_ler.getBuffer(), (int)_bmOffset+4, (int)len + 4, out, 0);
                out.rewind();
                out.order(ByteOrder.LITTLE_ENDIAN);
                BufferedImage bi = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
                for(int H = 0; H < h; H++)
                for(int W = 0; W < w; W++)
                {
                    int b = out.get() & 0xFF;
                    int g = out.get() & 0xFF;
                    int r = out.get() & 0xFF;
                    int a = out.get() & 0xFF;
                    bi.setRGB(W, H, (a << 24) | (r << 16) | (g << 8) | b);
                }
                _value = bi;
               return bi;
            }
            return _value;
        } catch (BufferUnderflowException i) {
        }
        return null;
    }
}

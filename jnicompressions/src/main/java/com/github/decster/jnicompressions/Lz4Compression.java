/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.decster.jnicompressions;

import java.nio.ByteBuffer;

public class Lz4Compression implements BlockCompression {
  public Lz4Compression() {
    NativeLoader.CheckNativeLibrary();
  } 

  @Override
  public int MaxCompressedSize(int uncompressedSize) {
    return Math.max((int)(uncompressedSize * 0.005), 8) + uncompressedSize;
  }

  @Override
  public native int CompressDirect(ByteBuffer src, int srcOffset, int srcLength,
      ByteBuffer dest, int destOffset);

  @Override
  public native int DecompressDirect(ByteBuffer src, int srcOffset, int srcLength,
      ByteBuffer dest, int destOffset);
}

#jnicompressions

This is a little stripped-down version of [jnicompressions](https://github.com/decster/jnicompressions) for libjinx.

 * Snappy support has been removed.
 * Only direct buffer (de)compression is supported.
 * Native library compiled for Windows, and 32-bit Linux and Mac.

So far this only targets x86 and x64 architectures.

Thanks to the original author, decster, for writing this.

##Compiling Native Libraries

###Windows (MSVC)

Please use the correct `cl` for your target architecture.

    cl src\main\native\src\compressions_Lz4Compression.cc
        src\main\native\lz4\lz4.c "%JAVA_HOME%\lib\jvm.lib"
        /DHAVE_CONFIG_H /DNDEBUG /LD /Ox /Ot /Oi
        /I"%JAVA_HOME%\include" /I"%JAVA_HOME%\include\win32"
        /Fecompressions.dll

###32-bit Linux (GCC)

    g++ src/main/native/src/compressions_Lz4Compression.cc \
        src/main/native/lz4/lz4.c \
        -DHAVE_CONFIG_H -I$JAVA_HOME/include \
        -I$JAVA_HOME/include/linux \
        -L$JAVA_HOME/jre/lib/i386/server \
        -L$JAVA_HOME/lib/i386/server -ljvm \
        -fPIC -O3 -DNDEBUG -shared -static-libgcc \
        -m32 -o libcompressions.so

###64-bit Linux (GCC)

    g++ src/main/native/src/compressions_Lz4Compression.cc \
        src/main/native/lz4/lz4.c \
        -DHAVE_CONFIG_H -I$JAVA_HOME/include \
        -I$JAVA_HOME/include/linux \
        -L$JAVA_HOME/jre/lib/amd64/server \
        -L$JAVA_HOME/lib/amd64/server -ljvm \
        -fPIC -O3 -DNDEBUG -shared -static-libgcc \
        -m64 -o libcompressions.so

###Mac OS X (LLVM-GCC)

    g++ src/main/native/src/compressions_Lz4Compression.cc \
        src/main/native/lz4/lz4.c -DHAVE_CONFIG_H \
        -I/System/Library/Frameworks/JavaVM.framework/Headers \
        -I/System/Library/Frameworks/JavaVM.framework/JavaVM \
        -fPIC -O3 -DNDEBUG -shared -static-libgcc \
        -o libcompressions.jnilib

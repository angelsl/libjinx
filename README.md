#libjinx
...is a simple Java library for reading NX files.

It is, however, very slow. Parsing the node tree of the GMS v40b Data.nx takes a full 4 seconds. Image support is also sketchy.

Direct all blame to Java.

##License
libjinx is licensed under the GNU GPL v3.0 with Classpath Exception.

##Acknowledgements
 * [retep998](https://github.com/retep998), the co-designer of the NX format
 * [aaronweiss74](https://github.com/aaronweiss74) for the idea of writing a Java library
 * [LZ4](http://code.google.com/p/lz4/), a fast and speedy compression algorithm used in NX to compress images
     * _though Java certainly doesn't do it justice_
 * [jnicompressions](https://github.com/decster/jnicompressions), the library used to perform LZ4 decompression in libjinx
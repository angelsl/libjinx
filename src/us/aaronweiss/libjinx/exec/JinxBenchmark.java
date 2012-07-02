package us.aaronweiss.libjinx.exec;

import java.io.IOException;
import org.angelsl.ms.libjinx.NXException;
import org.angelsl.ms.libjinx.NXFile;

/**
 * A simple tool for benchmarking angelsl's libjinx
 * @author Aaron Weiss
 * @version 1.0
 */
public class JinxBenchmark {
	/**
	 * Benchmarks NX
	 * @param args does nothing.
	 */
	public static void main(String[] args) {
		System.out.println("libjinx: bench: start");
		long startNano = System.nanoTime();
		long start = System.currentTimeMillis();
		try {
			new NXFile("Data.nx");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NXException e) {
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		long endNano = System.nanoTime();
		System.out.println("libjinx: bench: end");
		System.out.println("libjinx: bench: " + (end - start) + " ms " + (endNano - startNano) + " µs");
	}

}

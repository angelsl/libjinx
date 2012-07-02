package us.aaronweiss.libjinx.exec;

import java.io.IOException;
import org.angelsl.ms.libjinx.NXException;
import org.angelsl.ms.libjinx.NXFile;
import org.angelsl.ms.libjinx.NXNode;

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
			NXFile nx = new NXFile("Data.nx");
			for (NXNode<?> node : nx.getBaseNode()) {
				System.out.println("\t" + node.getName() + ": ");
				System.out.println("\t\tNode Children Count: " + node.childCount());
			}
			nx.close();
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

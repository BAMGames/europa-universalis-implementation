package com.mkl.eu.front.client.map.marker;

import de.fhpotsdam.unfolding.data.GeoJSONReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Code copied from PApplet or GeoJSONReader to be able to
 * build markers without having a PApplet.
 *
 * @author MKL.
 */
public class GeoJSONReaderUtil {
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GeoJSONReader.class);

    /**
     * Read a json file.
     *
     * @param filename path to the file.
     * @return the content of the file.
     */
    public static String readJson(String filename) {
        return join(loadStrings(filename), "");
    }

    /**
     * Creates a File inputStream from a path.
     *
     * @param filename path to the file.
     * @return an inputStream.
     */
    private static String[] loadStrings(String filename) {
        try (InputStream is = new FileInputStream(filename)) {
            return loadStrings(is);
        } catch (IOException e) {
            LOGGER.error("Impossible to load json.", e);
            return null;
        }
    }

    /**
     * Method copied from PApplet to put the content of an inputStream into
     * an array of String with UTF-8 encoding.
     *
     * @param input the inputStream.
     * @return an array of String.
     * @throws IOException if inputStream can't be read.
     */
    private static String[] loadStrings(InputStream input) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"))) {
            String[] string = new String[100];
            int lineCount = 0;

            String[] output;
            for (String line; (line = reader.readLine()) != null; string[lineCount++] = line) {
                if (lineCount == string.length) {
                    output = new String[lineCount << 1];
                    System.arraycopy(string, 0, output, 0, lineCount);
                    string = output;
                }
            }

            reader.close();
            if (lineCount == string.length) {
                return string;
            } else {
                output = new String[lineCount];
                System.arraycopy(string, 0, output, 0, lineCount);
                return output;
            }
        }
    }

    /**
     * Join an array of String into a single String with a given separator.
     *
     * @param list      array of String.
     * @param separator separator.
     * @return the joined String.
     */
    private static String join(String[] list, String separator) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < list.length; ++i) {
            if (i != 0) {
                sb.append(separator);
            }

            sb.append(list[i]);
        }

        return sb.toString();
    }
}

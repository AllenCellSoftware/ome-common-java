/*
 * #%L
 * Common package for I/O and related utilities
 * %%
 * Copyright (C) 2005 - 2016 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package loci.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple parser for INI configuration files.  Supports pound (#) as comments,
 * and backslash (\) to continue values across multiple lines.
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public class IniParser {

  // -- Fields --

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(IniParser.class);

  private String commentDelimiter = "#";

  private boolean slashContinues = true;

  // -- IniParser API methods --

  /**
   * Set the String that identifies a comment.  Defaults to "#".
   *
   * @param delimiter the String used to identify comments
   */
  public void setCommentDelimiter(String delimiter) {
    commentDelimiter = delimiter;
  }

  /**
   * Set whether or not a '\' at the end of a line signifies that the
   * line continues on the following line.
   *
   * By default, a '\' does continue the line.
   *
   * @param slashContinues true if a '\' at the end of a line signifies that
   *        the line continues on the following line
   */
  public void setBackslashContinuesLine(boolean slashContinues) {
    this.slashContinues = slashContinues;
  }

  /**
   * Parses the INI-style configuration data from the given resource.
   *
   * @param path the name of the resource to read
   * @return the IniList parsed from the named resource
   * @see #openTextResource(String)
   * @throws IOException if the resource cannot be read
   */
  public IniList parseINI(String path)
    throws IOException
  {
    return parseINI(openTextResource(path));
  }

  /**
   * Parses the INI-style configuration data from the given resource,
   * using the given class to find the resource.
   *
   * @param path the name of the resource to read
   * @param c the Class to use for finding the named resource
   * @return the IniList parsed from the named resource
   * @see #openTextResource(String, Class)
   * @throws IOException if the resource cannot be read
   */
  public IniList parseINI(String path, Class<?> c)
    throws IOException
  {
    return parseINI(openTextResource(path, c));
  }

  /**
   * Parses the INI-style wrapping the given file in a {@link BufferedReader}
   *
   * @param file the file on disk from which to read
   * @return the IniList parsed from the file
   * @throws IOException if the file cannot be read
   */
  public IniList parseINI(File file)
    throws IOException
  {
    FileInputStream fis = null;
    InputStreamReader isr = null;
    BufferedReader br = null;
    try {
      fis = new FileInputStream(file);
      isr = new InputStreamReader(fis, Constants.ENCODING);
      br = new BufferedReader(isr);
      return parseINI(br);
    } finally {
      if (br != null) {
        br.close();
      }
      if (isr != null) {
        isr.close();
      }
      if (fis != null) {
        fis.close();
      }
    }
  }

  /**
   * Parses the INI-style configuration data from the given input stream.
   *
   * @param in the BufferedReader stream from which to read
   * @return the IniList parsed from the reader
   * @throws IOException if the stream cannot be read
   */
  public IniList parseINI(BufferedReader in)
    throws IOException
  {
    IniList list = new IniList();
    IniTable attrs = null;
    String chapter = null;
    int no = 1;
    StringBuffer sb = new StringBuffer();
    while (true) {
      int num = readLine(in, sb);
      if (num == 0) break; // eof
      String line = sb.toString();
      LOGGER.debug("Line {}: {}", no, line);

      // ignore blank lines
      if (line.equals("")) {
        no += num;
        continue;
      }

      // check for chapter header
      if (isHeader(line, '{')) {
        chapter = parseHeader(line, '{', '}');
        continue;
      }

      // check for section header
      if (isHeader(line, '[')) {
        attrs = new IniTable();
        list.add(attrs);

        // strip brackets
        String header = parseHeader(line, '[', ']');
        if (chapter != null) header = chapter + ": " + header;

        attrs.put(IniTable.HEADER_KEY, header);
        no += num;
        continue;
      }

      // if we still haven't found a header, then this is the default
      // section (more similar to a properties file)
      if (attrs == null) {
          attrs = new IniTable();
          attrs.put(IniTable.HEADER_KEY, IniTable.DEFAULT_HEADER);
          list.add(attrs);
      }

      // parse key/value pair
      int equals = line.indexOf('=');
      if (equals < 0) {
        LOGGER.debug("Ignoring line {}", no);
        continue;
      }
      String key = line.substring(0, equals).trim();
      String value = line.substring(equals + 1).trim();
      attrs.put(key, value);
      no += num;
    }
    return list;
  }

  // -- Utility methods --

  /**
   * Opens a buffered reader for the given resource.
   *
   * @param path the name of the resource to read
   * @return the BufferedReader corresponding to the named resource
   * @see #openTextResource(String)
   */
  public static BufferedReader openTextResource(String path) {
    return openTextResource(path, IniParser.class);
  }

  /**
   * Opens a buffered reader for the given resource.
   *
   * @param path the name of the resource to read
   * @param c the Class to use for finding the named resource
   * @return the BufferedReader corresponding to the named resource
   * @see #openTextResource(String)
   */
  public static BufferedReader openTextResource(String path, Class<?> c) {
    try {
      return new BufferedReader(new InputStreamReader(
        c.getResourceAsStream(path), Constants.ENCODING));
    }
    catch (IOException e) {
      LOGGER.error("Could not open BufferedReader", e);
    }
    return null;
  }

  // -- Helper methods --

  /** Checks whether the input line is a INI header **/
  private boolean isHeader(String line, char start) {
    return (line != null && line.length() > 1 && line.charAt(0) == start);
  }

  /** Parse a header line given input delimiters **/
  private String parseHeader(String line, char start, char end) {
    if (line == null || line.length() <= 1) return null;
    if (line.charAt(0) != start) return null;
    if (line.charAt(line.length() - 1) == end) {
      return line.substring(1, line.length() - 1);
    }
    return line.substring(1);
  }

  /**
   * Reads (at least) one line from the given input stream
   * into the specified string buffer.
   *
   * @return number of lines read
   */
  private int readLine(BufferedReader in, StringBuffer sb) throws IOException {
    int no = 0;
    sb.setLength(0);
    boolean blockText = false;
    while (true) {
      String line = in.readLine();
      if (line == null) break;
      no++;

      // strip comments
      if (commentDelimiter != null) {
        int comment = line.indexOf(commentDelimiter);
        if (comment >= 0) line = line.substring(0, comment);
      }

      // kill whitespace
      if (!blockText) {
        line = line.trim();
      }

      // backslash signifies data continues to next line
      boolean slash = slashContinues && line.trim().endsWith("\\");
      blockText = slashContinues && line.trim().endsWith("\\n");

      if (blockText) {
        line = line.substring(0, line.length() - 2) + "\n";
      }
      else if (slash) {
        line = line.substring(0, line.length() - 1).trim() + " ";
      }
      sb.append(line);
      if (!slash && !blockText) break;
    }
    return no;
  }

}

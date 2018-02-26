/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyright [year] [name of copyright owner]".
 *
 * Copyright 2008 Sun Microsystems, Inc.
 * Portions Copyright 2015-2016 ForgeRock AS.
 */

package org.opends.guitools.controlpanel.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/** Class used to write the output and error of a given process in a printstream. */
public class ProcessReader
{
  private BufferedReader reader;
  private Thread readerThread;
  private Throwable lastException;
  private boolean interrupt;
  private boolean done;

  /**
   * The constructor.
   * @param process process whose output/error we want to write to the print
   * stream.
   * @param printStream the print stream.
   * @param isError whether we must write the error (or the output) must be
   * written to the stream.
   */
  public ProcessReader(Process process, final PrintStream printStream,
      boolean isError)
  {
    InputStream is;
    if (isError)
    {
      is = process.getErrorStream();
    }
    else
    {
      is = process.getInputStream();
    }
    reader = new BufferedReader(new InputStreamReader(is));

    readerThread = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        String line;
        try
        {
          while (!interrupt && (null != (line = reader.readLine())))
          {
            printStream.println(line);
          }
        }
        catch (Throwable t)
        {
          lastException = t;
        }
        done = true;
      }
    });
  }

  /** Starts reading the output (or error) of the process. */
  public void startReading()
  {
    readerThread.start();
  }

  /**
   * Interrupts the reading of the output (or error) of the process.  The method
   * does not return until the reading is over.
   */
  public void interrupt()
  {
    interrupt = true;
    while (!done)
    {
      try
      {
        readerThread.interrupt();
      }
      catch (Throwable t)
      {
      }
    }
  }

  /**
   * Returns the last exception that occurred reading the output (or the error)
   * of the process.
   * @return the last exception that occurred reading the output (or the error)
   * of the process.
   */
  public Throwable getLastException()
  {
    return lastException;
  }
}

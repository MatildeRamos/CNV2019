//package pt.ulisboa.tecnico.cnv;

/* ICount.java
 * Sample program using BIT -- counts the number of instructions executed.
 *
 * Copyright (c) 1997, The Regents of the University of Colorado. All
 * Rights Reserved.
 *
 * Permission to use and copy this software and its documentation for
 * NON-COMMERCIAL purposes and without fee is hereby granted provided
 * that this copyright notice appears in all copies. If you wish to use
 * or wish to have others use BIT for commercial purposes please contact,
 * Stephen V. O'Neil, Director, Office of Technology Transfer at the
 * University of Colorado at Boulder (303) 492-5647.
 */

import BIT.highBIT.*;
import pt.ulisboa.tecnico.cnv.a18.storage.db.AbstractStorage;

import java.io.*;
import java.util.*;
import java.io.BufferedWriter;
import java.io.FileWriter;


public class MyBITTool {
    private static PrintStream out = null;
    private static HashMap<Long, Integer> m_count = new HashMap<>();


    /* main reads in all the files class files present in the input directory,
     * instruments them, and outputs them to the specified output directory.
     */
    public static void main(String argv[]) {
        //File file_in = new File(argv[0]);
        //String infilenames[] = file_in.list();
        String outputFolder = argv[1];
        System.out.println("hello");
        //for (int i = 0; i < infilenames.length; i++) {
        //    String infilename = infilenames[i];
        if (argv[0].endsWith(".class")) {
            // create class info object
            ClassInfo ci = new ClassInfo("." + System.getProperty("file.separator") + argv[0]);

            // loop through all the routines
            for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                Routine routine = (Routine) e.nextElement();
                InstructionArray instructions = routine.getInstructionArray();
                routine.addBefore("MyBITTool", "mcount", new Integer(1));

                if (routine.getMethodName().contentEquals("solveImage") || routine.getMethodName().contentEquals("solve")) {
                    //method count
                    routine.addAfter("MyBITTool", "storeCount", ci.getClassName());
                }
                ci.write(outputFolder + System.getProperty("file.separator") + argv[0]);
            }
        }
    }

    public static synchronized void printCount(String foo) {
      Long id = Thread.currentThread().getId();
      try{
          BufferedWriter writer = new BufferedWriter(new FileWriter("/home/ec2-user/cnv-project/Metrics_T" + id + ".txt", true));
          writer.append(m_count.get(id) + " methods.\n");
          writer.append("\n\n");
          writer.close();
        } catch(Exception exception){
          System.out.println(exception);
        }
        m_count.put(id, null);
    }

    public static synchronized void storeCount(String foo){
        System.out.println("Tool done!");
        Long id = Thread.currentThread().getId();
        AbstractStorage.getStorage().storeNumberOfMethods(id, m_count.get(id));
        m_count.put(id, null);
    }

    public static synchronized void mcount(int incr) {
      Long id = Thread.currentThread().getId();
      Integer count = m_count.get(id);
      if (count == null) {
          m_count.put(id, 0);
      } else {
          m_count.put(id, count + 1);
      }
    }

}

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
import java.io.*;
import java.util.*;
import java.io.BufferedWriter;
import java.io.FileWriter;


public class MyBITTool {
    private static PrintStream out = null;
    private static HashMap<Long, Integer> m_count = new HashMap<>();
    private static HashMap<Long, Integer> i_count = new HashMap<>();
    private static HashMap<Long, Integer> b_count = new HashMap<>();

    private static HashMap<Long, Integer> new_count = new HashMap<>();
  	private static HashMap<Long, Integer> new_arraycount = new HashMap<>();
  	private static HashMap<Long, Integer> anew_arraycount = new HashMap<>();
  	private static HashMap<Long, Integer> multi_anewarraycount = new HashMap<>();

    private static HashMap<Long, Integer> loadcount = new HashMap<>();
    private static HashMap<Long, Integer> storecount = new HashMap<>();
    private static HashMap<Long, Integer> fieldloadcount = new HashMap<>();
    private static HashMap<Long, Integer> fieldstorecount = new HashMap<>();

    /* main reads in all the files class files present in the input directory,
     * instruments them, and outputs them to the specified output directory.
     */
    public static void main(String argv[]) {
        //File file_in = new File(argv[0]);
        //String infilenames[] = file_in.list();
        String outputFolder = argv[1];
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

                if (routine.getMethodName().contentEquals("solveImage")){
                    //method count
                    routine.addAfter("MyBITTool", "printCount", ci.getClassName());

                    //loads
                    for (Enumeration instrs = instructions.elements(); instrs.hasMoreElements(); ){
        							Instruction instr = (Instruction) instrs.nextElement();
        							int opcode=instr.getOpcode();
        							if (opcode == InstructionTable.getfield)
        								instr.addBefore("MyBITTool", "LSFieldCount", new Integer(0));
        							else if (opcode == InstructionTable.putfield)
        								instr.addBefore("MyBITTool", "LSFieldCount", new Integer(1));
        							else {
        								short instr_type = InstructionTable.InstructionTypeTable[opcode];
        								if (instr_type == InstructionTable.LOAD_INSTRUCTION) {
        									instr.addBefore("MyBITTool", "LSCount", new Integer(0));
        								}
        								else if (instr_type == InstructionTable.STORE_INSTRUCTION) {
        									instr.addBefore("MyBITTool", "LSCount", new Integer(1));
        								}
        							}
        						}

                    //instructions and bb
                    for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        bb.addBefore("MyBITTool", "count", new Integer(bb.size()));
                    }

                    //alloc
                    for (Enumeration instrs = instructions.elements(); instrs.hasMoreElements(); ) {
        							Instruction instr = (Instruction) instrs.nextElement();
        							int opcode=instr.getOpcode();
        							if ((opcode==InstructionTable.NEW) ||
        								(opcode==InstructionTable.newarray) ||
        								(opcode==InstructionTable.anewarray) ||
        								(opcode==InstructionTable.multianewarray)) {
        								instr.addBefore("MyBITTool", "allocCount", new Integer(opcode));
        							}
        						}
                }
            }
            ci.write(outputFolder + System.getProperty("file.separator") + argv[0]);
        }
    }

    public static synchronized void printCount(String foo) {
      Long id = Thread.currentThread().getId();
      try{
          BufferedWriter writer = new BufferedWriter(new FileWriter("/home/ec2-user/cnv-project/Metrics_T" + id + ".txt", true));
          writer.append(i_count.get(id) + " instructions in " + b_count.get(id) + " basic blocks were executed in " + m_count.get(id) + " methods.\n");
          writer.append("Allocations summary:\n");
    			writer.append("new:            " + new_count.get(id) + "\n");
    			writer.append("newarray:       " + new_arraycount.get(id) + "\n");
    			writer.append("anewarray:      " + anew_arraycount.get(id) + "\n");
    			writer.append("multianewarray: " + multi_anewarraycount.get(id) + "\n");
          writer.append("Load Store Summary:\n");
    			writer.append("Field load:    " + fieldloadcount.get(id) + "\n");
    			writer.append("Field store:   " + fieldstorecount.get(id) + "\n");
    			writer.append("Regular load:  " + loadcount.get(id) + "\n");
    			writer.append("Regular store: " + storecount.get(id) + "\n");
          writer.append("\n\n");
          writer.close();
        } catch(Exception exception){
          System.out.println(exception);
        }
        m_count.put(id, null);
        i_count.put(id, null);
        b_count.put(id, null);
        new_count.put(id, null);
        new_arraycount.put(id, null);
        anew_arraycount.put(id, null);
        multi_anewarraycount.put(id, null);
        loadcount.put(id, null);
        storecount.put(id, null);
        fieldloadcount.put(id, null);
        fieldstorecount.put(id, null);

    }

    public static synchronized void count(int incr) {
      Long id = Thread.currentThread().getId();
      Integer countb = b_count.get(id);
      Integer counti = i_count.get(id);
      if (countb == null){
        b_count.put(id, 0);
      } else {
        b_count.put(id, countb + 1);
      }

      if (counti == null){
        i_count.put(id, 0);
      } else {
        i_count.put(id, counti + incr);
      }
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

    public static synchronized void allocCount(int type) {
      Long id = Thread.currentThread().getId();
      Integer newcount = new_count.get(id);
      Integer newarraycount = new_arraycount.get(id);
      Integer anewarraycount = anew_arraycount.get(id);
      Integer multianewarraycount = multi_anewarraycount.get(id);
        switch(type) {
        case InstructionTable.NEW:
          if (newcount == null) {
              new_count.put(id, 0);
          } else {
              new_count.put(id, newcount + 1);
          }
          break;
        case InstructionTable.newarray:
          if (newarraycount == null) {
              new_arraycount.put(id, 0);
          } else {
              new_arraycount.put(id, newarraycount + 1);
          }
          break;
        case InstructionTable.anewarray:
          if (anewarraycount == null) {
              anew_arraycount.put(id, 0);
          } else {
              anew_arraycount.put(id, anewarraycount + 1);
          }
          break;
        case InstructionTable.multianewarray:
          if (multianewarraycount == null) {
              multi_anewarraycount.put(id, 0);
          } else {
              multi_anewarraycount.put(id, multianewarraycount + 1);
          }
          break;
        }
      }

      public static synchronized void LSFieldCount(int type) {
        Long id = Thread.currentThread().getId();
        Integer fieldload_count = fieldloadcount.get(id);
        Integer fieldstore_count = fieldstorecount.get(id);
    		if (type == 0){
            if (fieldload_count == null) {
                fieldloadcount.put(id, 0);
            } else {
                fieldloadcount.put(id, fieldload_count + 1);
            }
        } else{
            if (fieldstore_count == null) {
                fieldstorecount.put(id, 0);
            } else {
                fieldstorecount.put(id, fieldstore_count + 1);
            }
        }
    	}

    	public static synchronized void LSCount(int type) {
        Long id = Thread.currentThread().getId();
        Integer load_count = loadcount.get(id);
        Integer store_count = storecount.get(id);
    			if (type == 0){
            if (load_count == null) {
                loadcount.put(id, 0);
            } else {
                loadcount.put(id, load_count + 1);
            }
          }
    			else{
            if (store_count == null) {
                storecount.put(id, 0);
            } else {
                storecount.put(id, store_count + 1);
            }
          }
    		}
}

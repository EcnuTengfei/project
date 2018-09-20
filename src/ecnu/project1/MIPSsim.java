/**
 * 
 * @copyright: East China Normal University
 * @author tengfeili
 */

package ecnu.project1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MIPSsim {

	public MIPSsim() {
		// TODO Auto-generated constructor stub
	}

	public static int byte32ToInt(byte[] arr) {
        if (arr == null || arr.length != 32) {
            throw new IllegalArgumentException("byte array can't be empty, and the length is 32");
        }
        int sum = 0;
        for (int i = 0; i < 32; ++i) {
            sum |= (arr[i] << (31 - i));
        }
        return sum;
    }


	public static char[] shiftInstruction(char[] charArray, int number) {
		if (number > 0) {	//left shift
			for (int i = number; i < charArray.length; i++) {
				charArray[i-number] = charArray[i];
			}
			for (int i = charArray.length - number; i < charArray.length; i++) {
				charArray[i] = '0';
			}
		}else {			//right shift
			for (int i = charArray.length-number-1; i >= 0; i--) {
				charArray[i+number] = charArray[i];
			}
			for (int i = 0; i < number; i++) {
				charArray[i] = '0';
			}
		}
		return charArray;
	}
	
	public static String[] readInstruction(String filePath, int flag) throws IOException {
        File file = new File(filePath);
		BufferedReader bufReader = null;
		int n = (flag - 128)/4; 
		String[] strings = new String[30];
		String[] instructionArray = new String[30-n];
		try {
			bufReader = new BufferedReader(new FileReader(file));
			String instruction = "";
			for (int i = 0; (i<30)&&((instruction = bufReader.readLine())!=null); i++) {
				strings[i] = instruction;
			}
			for (int i = n,j=0; (i < strings.length)&&(j<instructionArray.length); i++,j++) {
				instructionArray[j] = strings[i];
				//System.out.println(instructionArray[j]);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return instructionArray;
	}
	
	/**
	 *  @author tengfeili
	 * @param filePath
	 * @throws IOException
	 */
	public static void simulation(String filePath) throws IOException {
        File file = new File(filePath);
        int[][] register = new int[4][8];
        int[][] data = new int[2][8];
        assembly(data,filePath);
        BufferedReader bufReader = null;
        BufferedWriter bufWriter = null;

        try {
			bufReader = new BufferedReader(new FileReader(file));
	        bufWriter = new BufferedWriter(new FileWriter("src/ecnu/project1/generated_simulation.txt"));

	        String temp = null;
	        String tempSimulation = "";
	        int cycle = 1, instructionID = 128;
	        String[] instructionArray = readInstruction(filePath, instructionID);
	        
	        for (int j = 0; (j < instructionArray.length)&&(instructionID <= 180); j += 1, cycle++, instructionID += 4) {
				temp = instructionArray[j];
	        	
				tempSimulation += "\n--------------------\n"; 
		        tempSimulation += "Cycle:"+ cycle +" "+ instructionID; 
				//Category 1
				if (temp.substring(0, 3).equals("000")) {
					if (temp.substring(3, 6).equals("000")) {
		            	tempSimulation += "\tJ ";
		            	char[] charArray = temp.substring(6, 32).toCharArray();
		            	MIPSsim.shiftInstruction(charArray, 2); //shift left
			        	String afterTransfer = String.valueOf(charArray);
			        	int target = Integer.parseInt(afterTransfer, 2);
			        	tempSimulation += "#" + target;
			        	instructionID = target - 4;
			        	j = (instructionID-128)/4;
		            }
		            else if(temp.substring(3, 6).equals("010")) {
	            		tempSimulation += "\tBEQ ";
	            		int rs = Integer.parseInt(temp.substring(6, 11), 2);
	            		int rt = Integer.parseInt(temp.substring(11, 16), 2);
	            		char[] charArray = temp.substring(16, 32).toCharArray();
	            		if (rs > 0) {
	            			MIPSsim.shiftInstruction(charArray, 2);
	            		}
	            		String afterTransition = String.valueOf(charArray);
	            		int offset = Integer.parseInt(afterTransition, 2);
	            		tempSimulation += "R"+rs+ ", R"+rt+", #"+offset;
	            		if (register[rs/8][rs%8]==register[rt/8][rt%8]) {
							instructionID += offset;
							j = (instructionID-128)/4;
						}
	            	}
	            	else if (temp.substring(3, 6).equals("100")) {
	            		tempSimulation += "\tBGTZ ";
	            		int rs = Integer.parseInt(temp.substring(6, 11), 2);
	            		char[] charArray = temp.substring(16, 32).toCharArray();
	            		if (rs > 0) {
	            			MIPSsim.shiftInstruction(charArray, 2);
	            		}
	            		String afterTransition = String.valueOf(charArray);
	            		int offset = Integer.parseInt(afterTransition, 2);
	            		tempSimulation += "R"+rs+", #"+offset;
	            		if (register[rs/8][rs%8] > 0) {
							instructionID += offset;
							j = (instructionID-128)/4;
						}
	            	}
	            	else if (temp.substring(3, 6).equals("101")) {
	            		tempSimulation += "\tBREAK";
	            	}
	            	else if (temp.substring(3, 6).equals("110")) {
	            		tempSimulation += "\tSW ";
	            		int base = Integer.parseInt(temp.substring(6, 11), 2);
	            		int rt = Integer.parseInt(temp.substring(11, 16), 2);
	            		int offset = Integer.parseInt(temp.substring(16, 32), 2);
	            		tempSimulation += "R"+rt+", "+offset+"(R"+base+")";
	            		int rtI = rt/8;
	            		int rtJ = rt%8;
	            		int baseI = base/8;
	            		int baseJ = base%8;

	            		int dataI = (((offset + register[baseI][baseJ])%184)/4)/8;
	            		int dataJ = (((offset + register[baseI][baseJ])%184)/4)%8;
	            		data[dataI][dataJ] = register[rtI][rtJ];
	            	}
	            	else if (temp.substring(3, 6).equals("111")) {
	            		tempSimulation += "\tLW ";
	            		int base = Integer.parseInt(temp.substring(6, 11), 2);
	            		int rt = Integer.parseInt(temp.substring(11, 16), 2);
	            		int offset = Integer.parseInt(temp.substring(16, 32), 2);
	            		tempSimulation += "R"+rt+", "+offset+"(R"+base+")";
	            		int baseI = base/8;
	            		int baseJ = base%8;
	            		int rtI = rt/8;
	            		int rtJ = rt%8;

	            		int dataI = (((offset + register[baseI][baseJ])%184)/4)/8;
	            		int dataJ = (((offset + register[baseI][baseJ])%184)/4)%8;
	            		register[rtI][rtJ] = data[dataI][dataJ];
	            	}
				}
	               //Category 2
	            else if (temp.substring(0, 3).equals("110")) {
	        		int rs = Integer.parseInt(temp.substring(3, 8), 2);
	        		int rt = Integer.parseInt(temp.substring(8, 13), 2);
	        		int rd = Integer.parseInt(temp.substring(16, 21), 2);
	        		if (temp.substring(13, 16).equals("000")) {
	            		tempSimulation += "\tADD ";
	            		tempSimulation += "R"+rd+", R"+rs+", R"+rt;
	            		register[rt/8][rt%8] = register[rd/8][rd%8] + register[rs/8][rs%8];
	            	}
	            	else if (temp.substring(13, 16).equals("001")) {
	            		tempSimulation += "\tSUB ";
	            		tempSimulation += "R"+rd+", R"+rs+", R"+rt;
	            	}
	            	else if (temp.substring(13, 16).equals("010")) {
	            		tempSimulation += "\tMUL ";
	            		tempSimulation += "R"+rd+", R"+rs+", R"+rt;
	            		register[rd/8][rd%8] = register[rt/8][rt%8] * register[rs/8][rs%8];
	            	}
	            	else if (temp.substring(13, 16).equals("011")) {
	            		tempSimulation += "\tAND ";
	            		tempSimulation += "R"+rd+", R"+rs+", R"+rt;
	            	}
	            	else if (temp.substring(13, 16).equals("100")) {
	            		tempSimulation += "\tOR ";
	            		tempSimulation += "R"+rd+", R"+rs+", R"+rt;
	            	}
	            	else if (temp.substring(13, 16).equals("101")) {
	            		tempSimulation += "\tXOR ";
	            		tempSimulation += "R"+rd+", R"+rs+", R"+rt;
	            	}
	            	else if (temp.substring(13, 16).equals("110")) {
	            		tempSimulation += "\tNOR ";
	            		tempSimulation += "R"+rd+", R"+rs+", R"+rt;
	            	}
	            }
	             //Category 3
	               else if (temp.substring(0, 3).equals("111")) {
	        		   int rs = Integer.parseInt(temp.substring(3, 8), 2);
	        		   int rt = Integer.parseInt(temp.substring(8, 13), 2);
	        		   int immediate = Integer.parseInt(temp.substring(16, 32), 2);
	            	   if (temp.substring(13, 16).equals("000")) {
	            		   tempSimulation += "\tADDI ";
	            		   tempSimulation += "R"+rt+", R"+rs+", #"+immediate;
	            		   register[rt/(register[0].length)][rt%(register[0].length)] = register[rs/(register[0].length)][rs%(register[0].length)]+immediate;
	            	   }
	            	   else if (temp.substring(13, 16).equals("001")) {
	            		   tempSimulation += "\tANDI ";
	            		   tempSimulation += "R"+rt+", R"+rs+", #"+immediate;
	            	   }
	            	   else if (temp.substring(13, 16).equals("010")) {
	            		   tempSimulation += "\tORI ";
	            		   tempSimulation += "R"+rt+", R"+rs+", #"+immediate;
	            	   }
	            	   else if (temp.substring(13, 16).equals("011")) {
	            		   tempSimulation += "\tXORI ";
	            		   tempSimulation += "R"+rt+", R"+rs+", #"+immediate;
	            	   }
	              }
		       
				 tempSimulation += "\n\nRegisters";
   				 int registerNumber = 0;
   				 for (int j2 = 0; j2 < register.length; j2++) {
   					tempSimulation += "\nR"+String.format("%02d", registerNumber)+":\t";
   					for (int k = 0; k < register[0].length; k++) {
   						tempSimulation += register[j2][k]+"\t";
   						registerNumber++;
   					}
   				 }
	   				
   				tempSimulation += "\n\nData";
   				int dataNumber = 184;
   				for (int k = 0; k < data.length; k++) {
   					tempSimulation += "\n"+dataNumber+":\t";
   					for (int k2 = 0; k2 < data[0].length; k2++) {
   						tempSimulation += data[k][k2]+"\t";
   						dataNumber += 4; 
   					}
   				}
   				if (temp.substring(0, 6).equals("000101")) {
   					bufWriter.write(tempSimulation);
   					break;
   				}
			}
	        System.out.println(tempSimulation);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            if (bufReader != null && bufWriter != null) {
                try {
                    bufReader.close();
                    bufWriter.close();
                } catch (IOException e) {
                    e.getStackTrace();
                }
            }
        }
	}
	
	/*
	 * assembly(String filePath)
	 */
	public static void assembly(int[][] data, String filePath) throws NumberFormatException, IOException {
        //data = new int[2][8];
        File file = new File(filePath);
       
        BufferedReader bufReader = null;
        BufferedWriter bufWriter = null;
        String temp = null; 
        int instructionId = 128;

        try {
            bufReader = new BufferedReader(new FileReader(file));
            bufWriter = new BufferedWriter(new FileWriter("src/ecnu/project1/generated_disassembly.txt"));
             
            while ((temp = bufReader.readLine()) != null) {
                //Category 1
               if (temp.substring(0, 3).equals("000")) {
            	   if (temp.substring(3, 6).equals("000")) {
            		   temp += "\t"+instructionId+"\t\tJ ";
            		   instructionId += 4;
            		   char[] charArray = temp.substring(6, 32).toCharArray();
            		   MIPSsim.shiftInstruction(charArray, 2); //shift left
        			   String afterTransfer = String.valueOf(charArray);
        			   int target = Integer.parseInt(afterTransfer, 2);
        			   temp += "#"+target;
            	   }
            	   else if(temp.substring(3, 6).equals("010")) {
            		   temp += "\t"+instructionId+"\t\tBEQ ";
            		   instructionId += 4;
            		   int rs = Integer.parseInt(temp.substring(6, 11), 2);
            		   int rt = Integer.parseInt(temp.substring(11, 16), 2);
            		   char[] charArray = temp.substring(16, 32).toCharArray();
            		   if (rs > 0) {
            			   MIPSsim.shiftInstruction(charArray, 2);
            		   }
            		   String afterTransition = String.valueOf(charArray);
            		   int offset = Integer.parseInt(afterTransition, 2);
        			   temp += "R"+rs+ ", R"+rt+", #"+offset;
            	   }
            	   else if (temp.substring(3, 6).equals("100")) {
            		   temp += "\t"+instructionId+"\t\tBGTZ ";
            		   instructionId += 4;
            		   int rs = Integer.parseInt(temp.substring(6, 11), 2);
            		   char[] charArray = temp.substring(16, 32).toCharArray();
            		   if (rs > 0) {
            			   MIPSsim.shiftInstruction(charArray, 2);
            		   }
            		   String afterTransition = String.valueOf(charArray);
            		   int offset = Integer.parseInt(afterTransition, 2);
        			   temp += "R"+rs+", #"+offset;
            	   }
            	   else if (temp.substring(3, 6).equals("101")) {
            		   temp += "\t"+ instructionId +"\t\tBREAK";
            		   instructionId += 4;
                       bufWriter.write("\n"+temp);	//Write data to generated_disassembly.txt.
            		   break;
            	   }
            	   else if (temp.substring(3, 6).equals("110")) {
            		   temp += "\t"+ instructionId +"\t\tSW ";
            		   instructionId += 4;
            		   int base = Integer.parseInt(temp.substring(6, 11), 2);
            		   int rs = Integer.parseInt(temp.substring(11, 16), 2);
            		   int offset = Integer.parseInt(temp.substring(16, 32), 2);
        			   temp += "R"+rs+", "+offset+"(R"+base+")";
            	   }
            	   else if (temp.substring(3, 6).equals("111")) {
            		   temp += "\t"+ instructionId +"\t\tLW ";
            		   instructionId += 4;
            		   int base = Integer.parseInt(temp.substring(6, 11), 2);
            		   int rs = Integer.parseInt(temp.substring(11, 16), 2);
            		   int offset = Integer.parseInt(temp.substring(16, 32), 2);
        			   temp += "R"+rs+", "+offset+"(R"+base+")";
            	   }
                   //bufWriter.write(temp+"\n");	//Write data to generated_disassembly.txt.
               }
               //Category 2
               else if (temp.substring(0, 3).equals("110")) {
        		   int rs = Integer.parseInt(temp.substring(3, 8), 2);
        		   int rt = Integer.parseInt(temp.substring(8, 13), 2);
        		   int rd = Integer.parseInt(temp.substring(16, 21), 2);
            	   if (temp.substring(13, 16).equals("000")) {
            		   temp += "\t"+instructionId+"\t\tADD ";
            		   instructionId += 4;
            		   temp += "R"+rd+", R"+rs+", R"+rt;
            	   }
            	   else if (temp.substring(13, 16).equals("001")) {
            		   temp += "\t"+instructionId+"\t\tSUB ";
            		   instructionId += 4;
            		   temp += "R"+rd+", R"+rs+", R"+rt;
            	   }
            	   else if (temp.substring(13, 16).equals("010")) {
            		   temp += "\t"+instructionId+"\t\tMUL ";
            		   instructionId += 4;
            		   temp += "R"+rd+", R"+rs+", R"+rt;
            	   }
            	   else if (temp.substring(13, 16).equals("011")) {
            		   temp += "\t"+instructionId+"\t\tAND ";
            		   instructionId += 4;
            		   temp += "R"+rd+", R"+rs+", R"+rt;
            	   }
            	   else if (temp.substring(13, 16).equals("100")) {
            		   temp += "\t"+instructionId+"\t\tOR ";
            		   instructionId += 4;
            		   temp += "R"+rd+", R"+rs+", R"+rt;
            	   }
            	   else if (temp.substring(13, 16).equals("101")) {
            		   temp += "\t"+instructionId+"\t\tXOR ";
            		   instructionId += 4;
            		   temp += "R"+rd+", R"+rs+", R"+rt;
            	   }
            	   else if (temp.substring(13, 16).equals("110")) {
            		   temp += "\t"+instructionId+"\t\tNOR ";
            		   instructionId += 4;
            		   temp += "R"+rd+", R"+rs+", R"+rt;
            	   }
                   //bufWriter.write(temp+"\n");	//Write data to generated_disassembly.txt.
               }
             //Category 3
               else if (temp.substring(0, 3).equals("111")) {
            	   int rs = Integer.parseInt(temp.substring(3, 8), 2);
        		   int rt = Integer.parseInt(temp.substring(8, 13), 2);
        		   int immediate = Integer.parseInt(temp.substring(16, 32), 2);
            	   if (temp.substring(13, 16).equals("000")) {
            		   temp += "\t"+instructionId+"\t\tADDI ";
            		   instructionId += 4;
            		   temp += "R"+rt+", R"+rs+", #"+immediate;
            	   }
            	   else if (temp.substring(13, 16).equals("001")) {
            		   temp += "\t"+instructionId+"\t\tANDI ";
            		   instructionId += 4;
            		   temp += "R"+rt+", R"+rs+", #"+immediate;
            	   }
            	   else if (temp.substring(13, 16).equals("010")) {
            		   temp += "\t"+instructionId+"\t\tORI ";
            		   instructionId += 4;
            		   temp += "R"+rt+", R"+rs+", #"+immediate;
            	   }
            	   else if (temp.substring(13, 16).equals("011")) {
            		   temp += "\t"+instructionId+"\t\tXORI ";
            		   instructionId += 4;
            		   temp += "R"+rt+", R"+rs+", #"+immediate;
            	   }
               }
               if (instructionId == 132) {
                   bufWriter.write(temp);	//Write data to generated_disassembly.txt.
               }else {
                   bufWriter.write("\n"+temp);	//Write data to generated_disassembly.txt.
               }
           }
            
            for (int i = 0; i < data.length; i++) {
				for (int j = 0; (j < data[0].length)&&((temp = bufReader.readLine()) != null); j++) {
					if (temp.substring(0, 1).equals("0")) {
	    				data[i][j] = Integer.parseInt(temp, 2);
	    				temp += "\t"+ instructionId +"\t\t"+ Integer.parseInt(temp, 2);
	    			}else {
	    				byte[] array = temp.getBytes();
	    				data[i][j] = MIPSsim.byte32ToInt(array);
	    				temp += "\t"+ instructionId +"\t\t"+ MIPSsim.byte32ToInt(array);
					}
	            	instructionId += 4;
	                bufWriter.write("\n"+temp);	//Write data to generated_disassembly.txt.
				}
			}
        } catch (Exception e) {
            e.getStackTrace();
        } finally {
            if (bufReader != null && bufWriter != null) {
                try {
                    bufReader.close();
                    bufWriter.close();
                } catch (IOException e) {
                    e.getStackTrace();
                }
            }
        }
    }

	
	public static void main(String[] args) throws IOException {
		MIPSsim.simulation("src/ecnu/project1/sample.txt");
	}

}

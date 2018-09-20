# project1

## Please compile to produce an executable named MIPSsim.

gcc MIPSsim.c –o MIPSsim or javac MIPSsim.java or g++ MIPSsim.cpp –o MIPSsim

## Please do not print anything on screen.
## Please do not hardcode input filename, accept it as command lines option.
## Execute to generate disassembly and simulation files and test with correct ones

diff –w –B generated_disassembly.txt correct_disassembly.txt  
diff –w –B generated_simulation.txt correct_simulation.txt

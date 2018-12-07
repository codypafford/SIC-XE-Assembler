package p;

import java.io.*;
import java.io.*;
import java.util.*;


public class Main {

    public static int EndAddress;
    public static ArrayList<Instruction> INSTRUCTIONLst = new ArrayList<>();
    public static int STARTADDRESS = 0;
    public static int staticTracker = 0;
    public static int LOCCTR = 0;
    public static String PROGRAMNAME = "";
    public static int BASEADDRESS;

    public static int size = 150;  //the size of the SICOPS text file
    public static int prime = findPrime(2 * size);
    static HashTable SICOPStable = new HashTable(prime);
    static HashTable LabelTable = new HashTable(prime);
    static HashTable SYMTAB = new HashTable(prime);

    static ArrayList<String> errorMessages = new ArrayList<>();
    static ArrayList<String> errors = new ArrayList<>();


    static HashMap<String, Integer> map = new HashMap<>();


    public static void main(String[] args) throws IOException {
        if (args.length == 0)  // checks for arguments

        {
            System.out.println("Missing File Name...");
            System.exit(0);
        }
        createSICOPS(args);
        passOne(args);
        writeToFile();
        createObjFile();

    }

    private static void createObjFile() throws FileNotFoundException {
        ArrayList al = new ArrayList();

        al.add(String.format("%06X", STARTADDRESS & 0xFFFFFF));
        al.add("000000");
        int i = 0;
        for(i = 0; i < INSTRUCTIONLst.size(); i++){
            if(INSTRUCTIONLst.get(i).getOPCODE().equals("END")){
                al.add("!");
            }
            if((INSTRUCTIONLst.get(i).getOPCODE().equals("RESW") || INSTRUCTIONLst.get(i).getOPCODE().equals("RESB")
            || INSTRUCTIONLst.get(i).getOPCODE().equals("BYTE")) && i < INSTRUCTIONLst.size()-1){
                 al.add("!");
                 al.add(String.format("%06X", INSTRUCTIONLst.get(i+1).getADDRESS() & 0xFFFFFF));
                 al.add("000000");
            }else{
                try{
                    al.add(INSTRUCTIONLst.get(i).getObjectCode());
                }catch (Exception e){

                }
            }
        }

        al.removeAll(Arrays.asList(null,""));
        int index = al.lastIndexOf("000000");
        al.set(index, String.format("%06X", STARTADDRESS & 0xFFFFFF));
        PrintStream fileStream = new PrintStream(PROGRAMNAME + ".obj");
        for (Object string: al) {
            fileStream.println(string);
        }
    }

    private static void writeToFile() {
        try (PrintStream fileStream = new PrintStream(PROGRAMNAME + ".lst")) {
            sortErrorMessagesandPrint(fileStream);
            fileStream.println(".");
            fileStream.println(".");
            fileStream.println(".");

            int lineNumber = 0;
            fileStream.println("Line    "  + "Location " + "Object Code       " + "Source Code");
            fileStream.println("----    "  + "--------" + " " + "------------"  + "    " + "-------------" );
            for (Instruction instruction: INSTRUCTIONLst){
                fileStream.print(String.format("%3s", lineNumber));
                fileStream.println(String.format("%10s\t%8s\t%7s\t%10s\t%10s\t%35s\t", String.format("%6X", instruction.getADDRESS() & 0xFFFFF), instruction.getObjectCode(), instruction.getLABEL(),
                        instruction.getOPCODE(), instruction.getOPERAND(), instruction.getCOMMENT()));
                lineNumber++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void createLTORG() {
        Stack<Instruction> LTORGS = new Stack();
        for (int i = 0; i < INSTRUCTIONLst.size(); i++) {
            if (INSTRUCTIONLst.get(i).getOPERAND().trim().contains("=")) {
                if (INSTRUCTIONLst.get(i).getOPERAND().length() >= 9) {
                    String s = INSTRUCTIONLst.get(i).getOPERAND().substring(0, 9).trim();         //manages the size of the LTORG (7 digits)
                    String line = s.trim() + " " + "BYTE" + " " + INSTRUCTIONLst.get(i).getOPERAND().substring(1).trim();
                    Instruction instruction = new Instruction(line);
                    LTORGS.push(instruction);

                } else {
                    String line = INSTRUCTIONLst.get(i).getOPERAND().trim() + " " + "BYTE" + " " + INSTRUCTIONLst.get(i).getOPERAND().substring(1).trim();
                   // System.out.println("this is the line" + line);
                    Instruction instruction = new Instruction(line);
                    LTORGS.push(instruction);
                }
            }
            if (INSTRUCTIONLst.get(i).getOPCODE().equals("LTORG")) {
                while (!LTORGS.isEmpty()) {
                    Instruction instruction = LTORGS.pop();
                    INSTRUCTIONLst.add(++i, instruction);
                }
            }else if(INSTRUCTIONLst.get(i).getOPCODE().equals("END")){
                while (!LTORGS.isEmpty()) {
                    Instruction instruction = LTORGS.pop();
                    INSTRUCTIONLst.add(++i, instruction);
                }
            }
        }
    }


    private static void sortErrorMessagesandPrint(PrintStream fileStream) {
        Set<String> set = new HashSet<String>(errorMessages);
        List<String> strings = new ArrayList<String>(set);
        Collections.sort(strings, new Comparator<String>() {
            public int compare(String o1, String o2) {            //COMPARES AND SORTS BASED OFF OF THE ADDRESS

                String o1StringPart = o1.replaceAll("\\d", "");
                String o2StringPart = o2.replaceAll("\\d", "");


                if (o1StringPart.equalsIgnoreCase(o2StringPart)) {
                    return extractInt(o1) - extractInt(o2);
                }
                return o1.compareTo(o2);
            }

            int extractInt(String s) {
                String num = s.replaceAll("\\D", "");
                // return 0 if no digits found
                return num.isEmpty() ? 0 : Integer.parseInt(num, 16);
            }
        });

        for (int i = 0; i < strings.size(); i++) {
            fileStream.println(strings.get(i));

        }


    }


    private static void getPCandE() {       //USE THE OPERAND TO FIND THE ADDRESS OF LABELS
        for (int i = 0; i < INSTRUCTIONLst.size(); i++) {
            try {
                if (INSTRUCTIONLst.get(i).getOPERAND().startsWith("#") || INSTRUCTIONLst.get(i).getOPERAND().startsWith("@")) {
                    String string = INSTRUCTIONLst.get(i).getOPERAND().substring(1);
                    int hv = findhashVal(string, prime);
                    int pc = LabelTable.SearchAddressofLabel(hv, string, prime);  //CORRECTLY FIND ADDRESSES
                    String PC = String.format("%02X", pc & 0xFFFFF);
                   // System.out.println("PC relative address of " + INSTRUCTIONLst.get(i).getOPCODE() + " is " + PC);
                    INSTRUCTIONLst.get(i).setPCRelative(pc);
                } else if (INSTRUCTIONLst.get(i).getOPERAND().contains(",X")) {
                    String[] str = INSTRUCTIONLst.get(i).getOPERAND().split(",");
                    String string = str[0];
                    int hv = findhashVal(string, prime);
                    int pc = LabelTable.SearchAddressofLabel(hv, string, prime);  //CORRECTLY FIND ADDRESSES
                    String PC = String.format("%02X", pc & 0xFFFFF);
                    INSTRUCTIONLst.get(i).setPCRelative(pc);
                  //  System.out.println("PC relative address of " + INSTRUCTIONLst.get(i).getOPCODE() + " is " + PC);

                } else {
                    int hv = findhashVal(INSTRUCTIONLst.get(i).getOPERAND(), prime);
                    int pc = LabelTable.SearchAddressofLabel(hv, INSTRUCTIONLst.get(i).getOPERAND(), prime);  //CORRECTLY FIND ADDRESSES
                    String PC = String.format("%03X", pc & 0xFFFFF);
                    if (INSTRUCTIONLst.get(i).getOPCODE().equals("BASE")) {
                        BASEADDRESS = pc;
                    }
                  //  System.out.println("PC relative address of " + INSTRUCTIONLst.get(i).getOPCODE() + " is " + PC);
                    INSTRUCTIONLst.get(i).setPCRelative(pc);
                }
            } catch (NullPointerException e) {
                //System.out.println("threw exception" + INSTRUCTIONLst.get(i).getOPERAND());
                try {
                    int check = Integer.parseInt(INSTRUCTIONLst.get(i).getOPERAND());
                } catch (Exception e1) {
                    if (INSTRUCTIONLst.get(i).getHasOperand() && !INSTRUCTIONLst.get(i).isInteger(INSTRUCTIONLst.get(i).getOPERAND().substring(1))
                            && !INSTRUCTIONLst.get(i).getOPERAND().contains(",X") && INSTRUCTIONLst.get(i).getADDRESS() != 0) {
                        //Main.errorMessages.add("ERROR: LABEL UNDEFINED AT ADDRESS " + String.format("%02X", INSTRUCTIONLst.get(i).getADDRESS()  & 0xFFFFF));
                    }
                }
            }
        }
    }



    private static void makeLableTable() {     // I SHOULD RENAME
        for (int i = 0; i < INSTRUCTIONLst.size(); i++) {
            if (INSTRUCTIONLst.get(i).getADDRESS() != 0) {
                if (INSTRUCTIONLst.get(i).getHasLabel()) {        //IF INSTRUCTION HAS A LABEL
                    int hv = findhashVal(INSTRUCTIONLst.get(i).getLABEL(), prime);
                    String lblAddress = INSTRUCTIONLst.get(i).getLABEL() + " " + INSTRUCTIONLst.get(i).getADDRESS();    //PUT INTO ITS OWN METHOD TO PRINT THE SYMBOLE-LABEL TABLE

                    LabelTable.searchDuplicates(INSTRUCTIONLst.get(i).getLABEL().trim());
                    createLabelTable(lblAddress, hv);

                }
            }
        }
        LabelTable.printArray();
    }


    private static void printFinalInstructions() {
        System.out.println("\n\n");

        for (Instruction instruction : INSTRUCTIONLst) {
            if (instruction.getADDRESS() == 0) {
                instruction.setADDRESS("----");
            }
            if (instruction.getOPCODE().equals("END") || instruction.getOPCODE().equals("LTORG") ||      //SET SOME INSTRUCTIONS TO NULL
                    instruction.getOPCODE().equals("USE") || instruction.getOPCODE().equals("BASE") ||
                    instruction.getOPCODE().equals("RESW") || instruction.getOPCODE().equals("RESB") || instruction.getOPCODE().equals("START")) {
                instruction.setObjectCode("");
            }


        }
    }

    private static void createFlags() {
        for (Instruction instruction : INSTRUCTIONLst) {
            if (instruction.getOPCODE().trim().contains("+")) {
                instruction.setE(true);
                instruction.setP(false);
                // System.out.println("JDJDJDJD");
            }
            if (instruction.getOPERAND().startsWith("#")) {
                instruction.setI(true);
                if (instruction.isInteger(instruction.getOPERAND().substring(1))) {
                    instruction.setP(false);           //SOMETIMES P CAN BE TRUE WHILE IT IS IMMEDIATE THOUGH?  #ZZZ
                }
            }
            if (instruction.getOPERAND().startsWith("@")) {
                instruction.setN(true);
            }
            if (instruction.getOPERAND().contains(",X")) {
                instruction.setX(true);
            }
            if (instruction.getOPERAND().equals("BASE")) {
                instruction.setB(true);       //NOT ACTUALLY THE RIGHT THING TO DO
            }
            if (!instruction.getI() && !instruction.getN()) {
                instruction.setI(true);
                instruction.setN(true);

            }
            if(instruction.getOPERAND().equals("")){
                instruction.setX(false);
                instruction.setB(false);
                instruction.setP(false);
                instruction.setE(false);
            }
        }

    }


    private static void createSICOPS(String[] args) {

        try {
            FileReader fileReader = new FileReader(args[1]);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {

                Pair pair = new Pair(line);
                int hashValue = findhashVal(pair.getMnemonic(), prime);
                SICOPStable.createHashArray(hashValue, line);
            }
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private static void passOne(String[] args) {

        try {
            FileReader fileReader = new FileReader(args[0]);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            String findComment[];

            while ((line = bufferedReader.readLine()) != null) {


                if (line.startsWith(".")) {           //IF INSTRUCTION ONLY HAS A COMMENT-----------------------
                    Instruction instruction = new Instruction();
                    instruction.setCOMMENT(line.trim());
                    INSTRUCTIONLst.add(instruction);
                  //  System.out.println(instruction);

                } else {
                    if (line.contains(".") && line.charAt(0) != '.') {          //IF INSTRUCTION HAS COMMENT----------------------
                        findComment = line.trim().split("\\.");
                        String[] str = findComment[0].trim().split("\\s+"); //retrieve the string from this part
                        if (str.length == 1) {
                            Instruction instruction = new Instruction();
                            instruction.setOPCODE(findComment[0].trim());
                            instruction.setCOMMENT("." + findComment[1]);
                            try {
                                int hashValue = findhashVal(instruction.getOPCODE(), prime);
                                int bz = SICOPStable.searchByteSize(hashValue, instruction, prime);
                                instruction.setBYTESIZE(bz);
                            } catch (NullPointerException e) {
                                checkValidOpcode(instruction);
                            }

                          //  System.out.println(instruction);
                            INSTRUCTIONLst.add(instruction);

                        } else {
                            Instruction instruction = new Instruction(str);
                            instruction.setCOMMENT("." + findComment[1]);

                            try {
                                int hashValue = findhashVal(instruction.getOPCODE(), prime);
                                int bz = SICOPStable.searchByteSize(hashValue, instruction, prime);
                                instruction.setBYTESIZE(bz);
                            } catch (NullPointerException e) {
                                checkValidOpcode(instruction);
                            }

                            INSTRUCTIONLst.add(instruction);
                        }

                    } else {                        //THIS IS IF INSTRUCTION HAS NO COMMENT--------------------
                        Instruction instruction = new Instruction(line);
                        try {
                            int hashValue = findhashVal(instruction.getOPCODE(), prime);
                            int bz = SICOPStable.searchByteSize(hashValue, instruction, prime);
                            instruction.setBYTESIZE(bz);
                        } catch (NullPointerException e) {
                            checkValidOpcode(instruction);

                        }
                        INSTRUCTIONLst.add(instruction);
                    }
                }
            }
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        createLTORG();
        calculateByteSize();
        calculateAddress();
        createFlags();
        getRelativeAddresses();     //Actual addresses if no "USE"
        makeLableTable();           //PRINTS OUT SYMBOL TABLE
        getPCandE();
        createObjectCode();
        printFinalInstructions();
        SYMTAB.printArray();
    }

    private static void createObjectCode() {
        Integer sOne = null;    //first part
        Integer sTwo = null;    //second part
        String TA = String.valueOf(0);      //third part
        String s;
        for (int i = 0; i < INSTRUCTIONLst.size(); i++) {
            try {
                if (INSTRUCTIONLst.get(i).getP() && ((INSTRUCTIONLst.get(i).getPCRelative() - INSTRUCTIONLst.get(i + 1).getADDRESS() >= 2048) || INSTRUCTIONLst.get(i).getPCRelative() - INSTRUCTIONLst.get(i + 1).getADDRESS() <= -2047)) {
                    INSTRUCTIONLst.get(i).setP(false);
                    INSTRUCTIONLst.get(i).setB(true);
                }
                if(INSTRUCTIONLst.get(i).getOPERAND().equals("")){
                    INSTRUCTIONLst.get(i).setX(false);
                    INSTRUCTIONLst.get(i).setB(false);
                    INSTRUCTIONLst.get(i).setP(false);
                    INSTRUCTIONLst.get(i).setE(false);
                    sTwo = 0;
                    TA = String.valueOf(0);
                }
            }catch(Exception e){

            }
            ///FIRST PART     FIRST PART     FIRST PART    FIRST PART    FIRST PART
            //   3 AND 4 BYTE INSTRUCTIONS      FOR 3 AND 4 BYTE INSTRUCTIONS
            if ((!INSTRUCTIONLst.get(i).getOPCODE().equals("WORD") &&
                    !INSTRUCTIONLst.get(i).getOPCODE().equals("RESW") && !INSTRUCTIONLst.get(i).getOPCODE().equals("BYTE") && !INSTRUCTIONLst.get(i).getOPCODE().equals("RESB"))) {


                if (INSTRUCTIONLst.get(i).getBYTESIZE() == 3 || INSTRUCTIONLst.get(i).getBYTESIZE() == 4) {
                    try {
                        if (INSTRUCTIONLst.get(i).getN() && INSTRUCTIONLst.get(i).getI()) {
                            int opcode = Integer.parseInt(INSTRUCTIONLst.get(i).getNumericOpcode(), 10);
                            sOne = opcode + 3;
                        } else if (INSTRUCTIONLst.get(i).getN() && !INSTRUCTIONLst.get(i).getI()) {
                            int opcode = Integer.parseInt(INSTRUCTIONLst.get(i).getNumericOpcode(), 10);
                            sOne = opcode + 2;

                        } else if (!INSTRUCTIONLst.get(i).getN() && INSTRUCTIONLst.get(i).getI()) {
                            int opcode = Integer.parseInt(INSTRUCTIONLst.get(i).getNumericOpcode(), 10);
                            sOne = opcode + 1;

                        } else if (!INSTRUCTIONLst.get(i).getN() && !INSTRUCTIONLst.get(i).getN()) {
                            int opcode = Integer.valueOf(INSTRUCTIONLst.get(i).getNumericOpcode());
                            sOne = opcode;

                        }
                    } catch (Exception e) {
                        //OKAY
                    }
                } else if (INSTRUCTIONLst.get(i).getBYTESIZE() == 2) {
                    try {
                        int opcode = Integer.valueOf(INSTRUCTIONLst.get(i).getNumericOpcode());
                        sOne = opcode;
                    } catch (Exception e) {

                    }
                }

                //-------------------------------------------------------------------------------------------------------------------
                //   FOR 3 AND 4 BYTE INSTRUCTIONS     FOR 3 AND 4 BYTE INSTRUCTIONS       FOR 3 AND 4 BYTE INSTRUCTIONS
                // SECOND AND THIRD PART     SECOND AND THIRD PART      SECOND AND THIRD PART       SECOND AND THIRD PART
                if (INSTRUCTIONLst.get(i).getBYTESIZE() == 3 || INSTRUCTIONLst.get(i).getBYTESIZE() == 4 && INSTRUCTIONLst.get(i).getBYTESIZE() != 2) {
                    String nixbpe = INSTRUCTIONLst.get(i).getX() + "-" + INSTRUCTIONLst.get(i).getB() + "-" + INSTRUCTIONLst.get(i).getP() + "-" + INSTRUCTIONLst.get(i).getE();
                    try {
                        switch (nixbpe) {
                            case "true-false-false-false":
                                s = String.valueOf(8);
                                sTwo = Integer.parseInt(s, 16);
                                String.format("%02X", sTwo & 0xFFF);
                                break;
                            case "true-true-false-false":              //GO TO CREATE FLAGS METHOD AND MAKE IT POSSIBLE TO USE BASE IF PC IS OUT OF RANGE-> CHECKRANGE()
                                s = String.valueOf('C');      // THIS
                                sTwo = Integer.parseInt(s, 16);
                                TA = String.valueOf(INSTRUCTIONLst.get(i).getPCRelative() - BASEADDRESS);
                                break;
                            case "true-false-true-false":
                                s = String.valueOf('A');      //THIS
                                sTwo = Integer.parseInt(s, 16);
                                if (INSTRUCTIONLst.get(i).isInteger(INSTRUCTIONLst.get(i).getOPERAND().substring(1))) {
                                    TA = String.valueOf(Integer.valueOf(INSTRUCTIONLst.get(i).getOPERAND()));
                                } else {
                                    TA = String.valueOf(INSTRUCTIONLst.get(i).getPCRelative() - INSTRUCTIONLst.get(i + 1).getADDRESS());
                                }
                                break;
                            case "true-false-false-true":
                                s = String.valueOf(9);
                                sTwo = Integer.parseInt(s, 16);
                                TA = String.valueOf(INSTRUCTIONLst.get(i).getPCRelative());
                                break;
                            case "false-true-false-false":
                                s = String.valueOf(4);     //THIS
                                sTwo = Integer.parseInt(s, 16);
                                TA = String.valueOf(INSTRUCTIONLst.get(i).getPCRelative() - BASEADDRESS);
                                break;
                            case "false-false-true-false":
                                s = String.valueOf(2);
                                sTwo = Integer.parseInt(s, 16);
                                TA = String.valueOf(INSTRUCTIONLst.get(i).getPCRelative() - INSTRUCTIONLst.get(i + 1).getADDRESS());
                                break;
                            case "false-false-false-true":
                                s = String.valueOf(1);
                                sTwo = Integer.parseInt(s, 16);
                                if (INSTRUCTIONLst.get(i).isInteger(INSTRUCTIONLst.get(i).getOPERAND().substring(1))) {
                                    TA = String.valueOf(Integer.valueOf(INSTRUCTIONLst.get(i).getOPERAND().substring(1)));
                                } else {
                                    TA = String.valueOf(INSTRUCTIONLst.get(i).getPCRelative());
                                }
                                break;
                            case "false-false-false-false":
                                s = String.valueOf(0);
                                sTwo = Integer.parseInt(s, 16);
                                TA = String.valueOf(Integer.valueOf(INSTRUCTIONLst.get(i).getOPERAND().substring(1)));
                                break;

                        }

                    } catch (Exception e) {

                    }
                    //-------------------------------------------------------------------------------------------------------------------
                    ////     SECOND PART FOR 2 BYTE INSTRUCTIONS
                } else if (INSTRUCTIONLst.get(i).getBYTESIZE() == 2) {
                    String[] strings = INSTRUCTIONLst.get(i).getOPERAND().split(",");
                    String register1 = strings[0].trim();
                    String register2 = strings[1].trim();
                    String r1 = "";
                    String r2 = "";
                    switch (register1) {
                        case "A":
                            r1 = String.valueOf(0);
                            break;
                        case "X":
                            r1 = String.valueOf(1);
                            break;
                        case "L":
                            r1 = String.valueOf(2);
                            break;
                        case "PC":
                            r1 = String.valueOf(8);
                            break;
                        case "SW":
                            r1 = String.valueOf(9);
                            break;
                        case "B":
                            r1 = String.valueOf(3);
                            break;
                        case "S":
                            r1 = String.valueOf(4);
                            break;
                        case "T":
                            r1 = String.valueOf(5);
                            break;
                        case "F":
                            r1 = String.valueOf(6);
                            break;
                    }
                    switch (register2) {
                        case "A":
                            r2 = String.valueOf(0);
                            break;
                        case "X":
                            r2 = String.valueOf(1);
                            break;
                        case "L":
                            r2 = String.valueOf(2);
                            break;
                        case "PC":
                            r2 = String.valueOf(8);
                            break;
                        case "SW":
                            r2 = String.valueOf(9);
                            break;
                        case "B":
                            r2 = String.valueOf(3);
                            break;
                        case "S":
                            r2 = String.valueOf(4);
                            break;
                        case "T":
                            r2 = String.valueOf(5);
                            break;
                        case "F":
                            r2 = String.valueOf(6);
                            break;
                    }

                    TA = String.valueOf(r1 + r2);

                }
                try {
                    StringBuilder sb = new StringBuilder();
                    String part1 = String.format("%02X", sOne & 0xFFF);
                    sb.append(part1);
                    if (INSTRUCTIONLst.get(i).getBYTESIZE() != 2) {
                        String part2 = String.format("%01X", sTwo & 0xFFF);
                        sb.append(part2);
                    }
                    if (INSTRUCTIONLst.get(i).getBYTESIZE() == 3 || INSTRUCTIONLst.get(i).getBYTESIZE() == 4) {
                        if (INSTRUCTIONLst.get(i).getBYTESIZE() == 4) {
                            String part3 = "00" + String.format("%03X", Integer.valueOf(TA) & 0xFFF);
                            sb.append(part3);
                        } else {
                            String part3 = String.format("%03X", Integer.valueOf(TA) & 0xFFF);
                            sb.append(part3);
                        }
                    } else
                        sb.append(TA);
                    INSTRUCTIONLst.get(i).setObjectCode(String.valueOf(sb));
                    // sOne = null;
                    //  sTwo = null;
                    //  TA = 0;

                } catch (Exception e) {

                }
                //-------------------------------------------------------------------------------------------------------------------
                //     IF OPCODE IS WORD/RESB/RESW/BYTE     IF OPCODE IS WORD/RESB/RESW/BYTE
                //CREATES OBJECT CODE FOR WORDS AND RESW'S
            } else if (INSTRUCTIONLst.get(i).getOPCODE().equals("WORD") ||
                    INSTRUCTIONLst.get(i).getOPCODE().equals("RESW") || INSTRUCTIONLst.get(i).getOPCODE().equals("BYTE")
                    || INSTRUCTIONLst.get(i).getOPCODE().equals("RESB")) {

                if (INSTRUCTIONLst.get(i).getOPCODE().trim().equals("RESW")) {                  //RESW
                    int n = 3 * Integer.parseInt(INSTRUCTIONLst.get(i).getOPERAND());
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int zz = 0; zz <= n; zz++) {
                        stringBuilder.append("F");
                    }
                    INSTRUCTIONLst.get(i).setObjectCode(String.valueOf(stringBuilder));
                } else if (INSTRUCTIONLst.get(i).getOPCODE().trim().equals("WORD")) {                  //WORD
                    String hex = String.valueOf((Integer.parseInt(INSTRUCTIONLst.get(i).getOPERAND(), 16)));
                    String objCode = String.format("%06X", Integer.parseInt(hex) & 0xFFFFF);
                    INSTRUCTIONLst.get(i).setObjectCode(objCode);
                } else if (INSTRUCTIONLst.get(i).getOPCODE().trim().equals("RESB")) {                   //RESB
                    StringBuilder stringBuilder = new StringBuilder();
                    int n = Integer.parseInt(INSTRUCTIONLst.get(i).getOPERAND());
                    for (int zz = 0; zz <= n; zz++) {
                        stringBuilder.append("F");
                    }
                    INSTRUCTIONLst.get(i).setObjectCode(String.valueOf(stringBuilder));              //BYTE
                } else if (INSTRUCTIONLst.get(i).getOPCODE().trim().equals("BYTE") && !INSTRUCTIONLst.get(i).getOPERAND().contains("=")) {
                    if (INSTRUCTIONLst.get(i).getOPERAND().contains("C'")) {
                        String str = INSTRUCTIONLst.get(i).getOPERAND().substring(2, INSTRUCTIONLst.get(i).getOPERAND().length() - 1);
                        char[] charArray = str.toCharArray();
                        StringBuilder builders = new StringBuilder();
                        for (char c : charArray) {
                            int ii = (int) c;
                            // Step-3 Convert integer value to hex using toHexString() method.
                            builders.append(Integer.toHexString(ii).toUpperCase());
                        }
                        INSTRUCTIONLst.get(i).setObjectCode(String.valueOf(builders));
                    } else if (INSTRUCTIONLst.get(i).getOPERAND().contains("X'")) {
                        INSTRUCTIONLst.get(i).setObjectCode(INSTRUCTIONLst.get(i).getOPERAND().substring(2, INSTRUCTIONLst.get(i).getOPERAND().length() - 1));
                    }
                } else if (INSTRUCTIONLst.get(i).getOPCODE().trim().equals("BYTE") && INSTRUCTIONLst.get(i).getOPERAND().contains("=")) {

                }
            }
        }
    }


    private static void getRelativeAddresses() {
        int locctr = Main.STARTADDRESS;

        String blockCurrent = "";
        Map<String, Integer> map = new HashMap<>();
        int blockNumber = 0;

        // foreach lst in INSTRUCTIONlst
        for (Instruction lst : INSTRUCTIONLst) {
            if (lst.getOPCODE().equals("USE")) {
                map.put(blockCurrent, locctr);
                blockCurrent = lst.getOPERAND();
                locctr = map.getOrDefault(lst.getOPERAND(), -1);
                if (locctr == -1) {
                    locctr = Main.STARTADDRESS;
                    lst.setRelativeAddress(STARTADDRESS);
                }
            } else {
                lst.setRelativeAddress(locctr);
                locctr += lst.getBYTESIZE();
            }
        }

        for (int i = 0; i < INSTRUCTIONLst.size(); i++) {
            if (INSTRUCTIONLst.get(i).getRelativeAddress() == 0) {
                INSTRUCTIONLst.get(i).setRelativeAddress(INSTRUCTIONLst.get(i + 1).getRelativeAddress());
            }
        }
    }

    private static void calculateByteSize() {
        for (Instruction instruction : INSTRUCTIONLst) {
            try {
                if (instruction.getOPCODE().equals("RESB") || instruction.getOPCODE().equals("RESW") || instruction.getOPCODE().equals("BYTE")
                        || instruction.getOPCODE().equals("WORD")) {

                    if (instruction.getOPCODE().equals("WORD")) {
                        instruction.setBYTESIZE(3);
                    } else if (instruction.getOPCODE().equals("RESB")) {
                        instruction.setBYTESIZE(Integer.parseInt(instruction.getOPERAND()));
                    } else if (instruction.getOPCODE().equals("RESW")) {
                        instruction.setBYTESIZE((3 * Integer.parseInt(instruction.getOPERAND())));
                    } else if (instruction.getOPCODE().equals("BYTE")) {
                        if (instruction.getOPERAND().contains("C'")) {
                            String str = instruction.getOPERAND().substring(1);
                            int size = str.length() - 2;    //TO GET RID OF " ' "
                            instruction.setBYTESIZE(size);
                            instruction.setCharacterConstant(true);
                        } else if (instruction.getOPERAND().contains("X'") && !instruction.getOPERAND().contains("=")) {
                            String str = instruction.getOPERAND().substring(1);
                            int size = str.length() - 1;
                            if (size % 2 != 0) {
                                Main.errorMessages.add("ERROR: ODD NUMBER OF HEX VALUES AT ADDRESS " + String.format("%02X", staticTracker & 0xFFFFF));
                            }
                            size = size / 2;
                            instruction.setBYTESIZE(size);
                            instruction.setHexConstant(true);
                        } else
                            Main.errorMessages.add("ERROR: NO QUOTES FOUND IN THE OPERAND FIELD AT ADDRESS " + String.format("%02X", staticTracker & 0xFFFFF));
                    }
                    if (!instruction.getHasLabel()) {
                        //Main.errorMessages.add("ERROR: MISSING LABEL AT ADDRESS " + String.format("%02X", staticTracker & 0xFFFFF));
                    }
                }
            } catch (Exception e) {

            }
        }

    }

    private static void createLabelTable(String lblAddress, int hv) {
        LabelTable.createHashArray(hv, lblAddress);

    }

    private static void calculateAddress() {
        for (Instruction instruction : INSTRUCTIONLst) {
            if (!instruction.getIgnore()) {
                instruction.setADDRESS(staticTracker);
                staticTracker += instruction.getBYTESIZE();
            }
            if (instruction.getOPCODE().equals("END")) {
                Main.EndAddress = instruction.getADDRESS();
            }
            if (!instruction.getHasLabel()) {
                Main.errorMessages.add("ERROR: NO LABEL FOUND AT ADDRESS " + String.format("%02X", instruction.getADDRESS() & 0xFFFFF));
            }
        }

    }

    private static void checkValidOpcode(Instruction instruction) {
        if (!instruction.getOPCODE().equals("START") && !instruction.getOPCODE().equals("END") && !instruction.getOPCODE().equals("RESW")
                && !instruction.getOPCODE().equals("RESB") && !instruction.getOPCODE().equals("BYTE") && !instruction.getOPCODE().equals("BASE") && !instruction.getOPCODE().equals("WORD")
                && !instruction.getOPCODE().equals("LTORG") && !instruction.getOPCODE().equals("") && !instruction.getOPCODE().equals("EQU")
                && !instruction.getOPCODE().equals("CSECT") && !instruction.getOPCODE().equals("USE")) {

            errorMessages.add("ERROR: INVALID MNEMONIC " + instruction.getOPCODE() + " WILL BE IGNORED ");

            instruction.setIgnore(true);     //WILL HIDE INVALID INSTRUCTIONS IF OPCODE IS INVALID
        }
    }

    private static int findPrime(int p) {
        if (p == 0)

            return 2;

        if (p % 2 == 0)

            p++;

        else

            p += 2;

        while (true)

        {

            if (isNoPrime(p) == 1)

            {

                return p;

            }

            p += 2;

        }
    }

    private static int isNoPrime(int p) {
        for (int aa = 3; aa <= Math.sqrt(p); aa += 2)

            if (p % aa == 0)

                return 0;

        return 1;
    }

    private static int getSize(String file) throws IOException {

        BufferedReader bb = null;
        try {
            bb = new BufferedReader(new FileReader(file));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String line;

        int n = 0;

        while ((line = bb.readLine()) != null)

        {

            n++;

        }

        bb.close();

        return n;

    }


    public static int findhashVal(String key, int p)   // Get Hash Value of each String

    {

        int hashVal = 0;

        for (int j = 0; j < key.length(); j++)

        {
            int Val = key.charAt(j);    //Using ASCII Values
            hashVal = (hashVal * 26 + Val) % p;

        }

        return hashVal;

    }


}


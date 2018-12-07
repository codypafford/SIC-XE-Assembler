package p;


import java.util.*;


public class HashTable {


    private String[] hashArray;

    public HashTable(int primeSize){

        hashArray = new String[primeSize];  // will store the Strings in the correct location after hashing

    }

    void createHashArray(int hashValue, String line) {  //The Insertion
        int probe;
        ArrayList<Integer> collisionTracker = new ArrayList<>();

        if (hashArray[hashValue] == null) { //if location is empty

            hashArray[hashValue] = line;


            probe = -1;

        }  else {

            if (hashValue == (hashArray.length - 1)){    // Checks if it is the end of the array

                probe = 0;     // moves the probing index to the beginning

            } else{
                probe = hashValue + 1;   // If not the end of array, add one because its Linear Probing!
            }

        }

        while ( (probe != -1) && (probe != hashValue) ) {    // probe cannot equal hashValue because of next else
            //statement
            if (hashArray[probe] == null) {

                hashArray[probe] = line;

                if(collisionTracker != null){
                    for(Integer number : collisionTracker){
                       // System.out.println("\tCollision at " + number);
                    }
                }
                probe = -1;


            } else {

                if (probe == (hashArray.length - 1)) {     // Checks if it is the end of the array

                    probe = 0;

                } else {
                    collisionTracker.add(probe);
                    probe++;    // increments index because Linear Probing!
                }

            }
        }
    }

    void SearchLinearProbe(int hashValue, truePair pair, int prime) {    //The Search
        // pair = The Pair made from each new Line
        while (hashArray[hashValue] != null)
        {
            truePair p = new truePair(hashArray[hashValue]);

            if (p.getWord().equals(pair.getWord()) && p.getNum() != 0 && p.getWord() != null && p.getNum() != null) {
                String address = String.format("%02X", p.getNum() & 0xFFFFF);
                System.out.println(String.format("%9s\t%12s\t%9s", hashValue,pair.getWord(),address));
                return;
            }
            hashValue = (hashValue + 1) % (prime);

        }

    }

    Boolean SearchSYMTAB(int hashValue, String string, int prime) {    //The Search
        // pair = The Pair made from each new Line
        while (hashArray[hashValue] != null)
        {
            String str [] = hashArray[hashValue].split("\\s");
            String  s = str[0];

            if (string.equals(s)) {
                return true;
            }
            hashValue = (hashValue + 1) % (prime);

        }

        return false;
    }

    Integer searchByteSize(int hashValue, Instruction instruction, int prime){
        while (hashArray[hashValue] != null)
        {
            Pair p = new Pair(hashArray[hashValue]);

            if (p.getMnemonic().equals(instruction.getOPCODE())) {

                instruction.setNumericOpcode(String.valueOf(p.getOpcode()));
                instruction.setHasnumericOpcode(true);
                return p.getByteSize();

            }

            hashValue = (hashValue + 1) % (prime);

        }

        return null;
    }

    void printArray(){
        for (int i = 0; i < hashArray.length; i++) {
            if(hashArray[i] != null) {
               // System.out.println(hashArray[i]);
            }
        }
    }


    public void searchDuplicates(String label) {

        for(int i = 0; i < hashArray.length; i++){
            if(hashArray[i] != null) {
                truePair p = new truePair(hashArray[i]);
                if(p.getWord().trim().equals(label)){
                    if(!Main.errors.contains("ERROR: DUPLICATE LABEL FOUND AT ADDRESS " + label)) {
                        Main.errors.add("ERROR: DUPLICATE LABEL FOUND AT ADDRESS " + String.format("%02X", p.getNum() & 0xFFFFF));
                    }
                }
            }
        }
    }


    Integer SearchAddressofLabel(int hashValue, String instruction, int prime) {    //The Search
        // pair = The Pair made from each new Line
        while (hashArray[hashValue] != null)
        {
            truePair p = new truePair(hashArray[hashValue]);

            if (p.getWord().equals(instruction) && p.getNum() != 0) {

                return p.getNum();
            }
            hashValue = (hashValue + 1) % (prime);
        }
        return null;
    }



    }


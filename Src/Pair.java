package p;

public class Pair {

    private String mnemonic;
    private Integer opcode;
    private Integer byteSize;
    public Boolean unrecognized = false;


    public Pair(String line) {
        String[] mnemonicNum = line.split("\\s+");
        mnemonic = mnemonicNum[0];

        if (mnemonicNum.length > 1 ) {
            try {
                opcode = Integer.parseInt(mnemonicNum[1], 16);
               // System.out.println(String.format("%x", opcode));    //THIS GIVES ME THE OPCODE I WANT TO DISPLAY(MAKE THEM 3 HEX DIGITS)
                if(mnemonicNum.length > 2){  //this should work. this wont assign a byte size to my sic file. only the sicops file
                     byteSize = Integer.parseInt(mnemonicNum[2], 16);

                }
            }
            catch (NumberFormatException e) {
                Main.errorMessages.add("ERROR: Invalid entry: " + line + " is an unrecognized input");
                unrecognized = true;
                return;
            }
        }

        else {
            opcode = null;
        }

    }

    public Integer getByteSize() {
        return byteSize;
    }

    @Override
    public String toString() {
        if(opcode == null){
            return mnemonic;
        }else
            return mnemonic + " " + opcode;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public Integer getOpcode() {
        return opcode;
    }
}

package p;


public class Instruction {

    private String LABEL = "";
    private String OPCODE= null;
    private String OPERAND = "";
    private String COMMENT = "";
    private int BYTESIZE = 0;
    private int ADDRESS;
    private Boolean ignore = false;
    private Boolean hasLabel = false;
    private Boolean hasOperand= false;
    private Boolean hasOpCode= false;
    private Boolean hasComment= false;

    private String numericOpcode = "    ";
    private Boolean N = false;
    private Boolean I = false;
    private Boolean X = false;
    private Boolean B = false;
    private Boolean P = true;
    private Boolean E = false;
    private Integer PCRelative;
    private Boolean hasnumericOpcode = false;
    private Boolean isInteger;
    private Boolean characterConstant;
    private Boolean hexConstant;
    private int RelativeAddress;
    private String objectCode = null;
   // private String codeBlockName = "DEFAULT";





    public Instruction(String line) {         //HANDLE ALL OTHER INSTRUCTIONS
        String[] operate = line.trim().split("\\s+");
        if(operate.length > 1) {
           // if(operate.)

            if (operate[1].equals("START")) {
                Main.PROGRAMNAME = operate[0];
                LABEL = operate[0];
                OPCODE = operate[1];
                OPERAND = operate[2];
                Main.STARTADDRESS = Integer.parseInt(operate[2],16);
                Main.staticTracker = Integer.parseInt(operate[2],16);
                Main.LOCCTR = Integer.parseInt(operate[2],16);
                hasLabel = true;
                hasOperand = true;
                hasOpCode = true;
            } else if (operate.length == 4) {
                LABEL = operate[0];    //label
                OPCODE = operate[1];    //opcode
                OPERAND = operate[2];    //operand
                COMMENT = operate[3];    //comment
                hasLabel = true;
                hasOpCode = true;
                hasOperand = true;
                hasComment = true;

            } else if (operate.length == 3 && !line.contains(".")) {
                LABEL = operate[0];
                OPCODE = operate[1];    //operate length changes based on instruction
                OPERAND = operate[2];
                hasLabel = true;
                hasOpCode = true;
                hasOperand = true;

            } else if (operate.length == 3 && line.contains(".")) {
                OPCODE = operate[0];
                OPERAND = operate[1];
                COMMENT = operate[2];
                hasOpCode = true;
                hasOperand = true;
                hasComment = true;


            }else if(operate.length == 2 && !line.contains(".")){
                OPCODE = operate[0];
                OPERAND = operate[1];
                hasOpCode = true;
                hasOperand = true;
                if(operate[0].equals("START")){
                    Main.STARTADDRESS = Integer.parseInt((operate[1]), 16);
                    Main.staticTracker = Integer.parseInt(operate[1],16);
                }
            }
        }else {
            OPCODE = operate[0];

        }

    }

    public Instruction(String[] str) {    //HANDLE COMMENTS
        //turn str to string first



        if(str[1].equals("START")){
            Main.PROGRAMNAME = str[0];
            LABEL = str[0];
            OPCODE = str[1];
            OPERAND = str[2];
            Main.STARTADDRESS = Integer.parseInt(str[2],16);
            Main.staticTracker = Integer.parseInt(str[2],16);
            Main.LOCCTR = Integer.parseInt(str[2],16);
            hasLabel = true;
            hasOperand = true;

        }else if(str.length == 3){
            LABEL = str[0];    //label
            OPCODE = str[1];    //opcode
            OPERAND = str[2];    //operand
            hasLabel = true;
            hasOperand = true;
            hasOpCode = true;

        }else if(str.length == 2){
            OPCODE = str[0];    //operate length changes based on instruction
            OPERAND = str[1];
            hasOperand = true;
            hasOpCode = true;


        }else if(str.length == 1){   //FIND WAY TO MAKE THIS NOT ALWAYS FALSE
            OPCODE = str[0];
        }else if(str == null){

        }
    }

    public Instruction() {
        LABEL = "";
        OPCODE = "";
        OPERAND = "";
    }

    @Override
    public String toString() {
       // return LABEL + " " + OPCODE + " " + OPERAND + " " + COMMENT;
        String strOpCode = hasnumericOpcode ?  String.format("%02X", Integer.parseInt(numericOpcode) & 0xFFFFF) : numericOpcode;
        return (String.format("Label =%s Opcode =%s   Operand =%s Comment =%s BYTESIZE = %s ADDRESS =%s NumOpCode =%s N=%s I=%s X=%s B=%s P=%s E=%s PC=%s ObjectCode=%s",
                LABEL, OPCODE, OPERAND, COMMENT, BYTESIZE, ADDRESS, strOpCode, N,I,X,B,P,E, PCRelative, objectCode));
    }

    public String getCOMMENT() {
        return COMMENT;
    }

    public void setCOMMENT(String COMMENT) {
        this.COMMENT = COMMENT;
    }

    public String getOPERAND() {

        return OPERAND;
    }

    public String getOPCODE() {

        return OPCODE;
    }

    public void setBYTESIZE(int BYTESIZE) {
        this.BYTESIZE = BYTESIZE;
    }

    public void setOPCODE(String OPCODE) {
        this.OPCODE = OPCODE;
    }

    public String getLABEL() {

        return LABEL;
    }

    public int getBYTESIZE() {
        return BYTESIZE;
    }

    public Boolean getIgnore() {
        return ignore;
    }

    public void setIgnore(Boolean ignore) {
        this.ignore = ignore;
    }

    public Boolean getHasLabel() {
        return hasLabel;
    }

    public int getADDRESS() {


        return ADDRESS;
    }

    public void setADDRESS(int ADDRESS) {

        this.ADDRESS = ADDRESS;
    }

    public String getNumericOpcode() {
        return numericOpcode;
    }

    public void setNumericOpcode(String numericOpcode) {
        this.numericOpcode = numericOpcode;
    }

    public Boolean getE() {
        return E;
    }

    public void setE(Boolean e) {
        E = e;
    }

    public Boolean getP() {

        return P;
    }

    public void setP(Boolean p) {
        P = p;
    }

    public Boolean getB() {

        return B;
    }

    public void setB(Boolean b) {
        B = b;
    }

    public Boolean getX() {

        return X;
    }

    public void setX(Boolean x) {
        X = x;
    }

    public Boolean getI() {

        return I;
    }

    public void setI(Boolean i) {
        I = i;
    }

    public Boolean getN() {

        return N;
    }

    public void setN(Boolean n) {
        N = n;
    }

    public Integer getPCRelative() {
        return PCRelative;
    }

    public void setPCRelative(Integer PCRelative) {
        this.PCRelative = PCRelative;
    }

    public Boolean getHasOperand() {
        return hasOperand;
    }

    public void setHasnumericOpcode(Boolean hasnumericOpcode) {
        this.hasnumericOpcode = hasnumericOpcode;
    }

    public  boolean isInteger(Object object) {
        if(object instanceof Integer) {
            return true;
        } else {
            String string = object.toString();

            try {
                Integer.parseInt(string);
            } catch(Exception e) {
                return false;
            }
        }

        return true;
    }

    public void setHexConstant(Boolean hexConstant) {
        this.hexConstant = hexConstant;
    }

    public void setCharacterConstant(Boolean characterConstant) {

        this.characterConstant = characterConstant;
    }

    public int getRelativeAddress() {
        return RelativeAddress;
    }

    public void setRelativeAddress(int RelativeAddress) {
        this.RelativeAddress = RelativeAddress;
    }

    public void setADDRESS(String s) {

    }

    public String getObjectCode() {
        return objectCode;
    }

    public void setObjectCode(String objectCode) {
        this.objectCode = objectCode;
    }
}

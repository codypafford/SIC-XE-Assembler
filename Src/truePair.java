package p;

public class truePair {

    private String word;
    private Integer num;
    public Boolean unrecognized = false;


    public truePair(String line) {
        String[] wordNum = line.split("\\s+");
        word = wordNum[0];

        if (wordNum.length > 1) {
            try {
                num = Integer.parseInt(wordNum[1]);
            }
            catch (NumberFormatException e) {
                Main.errorMessages.add("ERROR: Invalid entry: " + line + " is an unrecognized input");
                unrecognized = true;
                return;
            }
        }

        else {
            num = null;
        }

    }

    @Override
    public String toString() {
        if(num == null){
            return word;
        }else
            return word + " " + num;
    }


    public String getWord() {
        return word;
    }

    public Integer getNum() {
        return num;
    }
}


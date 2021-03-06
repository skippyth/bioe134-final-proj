import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Final version of this file should be translatable to an actual BUA.
 *
 * Created by Katherine on 11/10/2017.
 */
public class BUA {
    private String filename;
    private String piName;
    private String piTitle;
    private Date date;
    private String phone;
    private String dept;
    private String address;
    private String email;
    private String fax;
    private int mailCode;
    private String funding_sources;
    private String grant_nos;
    private Date startDate;
    private Date endDate;
    private String objective;
    private ArrayList<Integer>checkboxes;
    private boolean[]needAttachment;
    private boolean[]attached;
    private int bsl;
    private boolean invalid;
    private String[]roomsUsed;

    public BUA(String filename){
        this.invalid = false;
        this.funding_sources = "";
        this.grant_nos = "";
        this.filename = filename;
    }

    public void setDependent(ArrayList<ArrayList<Integer>>atts){
        this.needAttachment = new boolean[atts.size()];
        this.attached = new boolean[atts.size()];

        for(int i = 0; i<atts.size(); i++){
            int vvvv = countSame(atts.get(i), this.checkboxes);
            if(vvvv>3){
                this.needAttachment[i] = true;
            }
        }
    }

    int countSame(ArrayList<Integer>first, ArrayList<Integer>second){
        int count = 0;
        int num = 0;
        for(int i = 0; i<first.size()&&i<second.size(); i++){
            if(first.get(i)==1&&first.get(i).equals(second.get(i))){
                count++;
            }
        }
        System.out.println();
        return count;
    }


    public void setCheckboxes(ArrayList<Integer> checkboxes) {
        this.checkboxes = checkboxes;
    }

    public void detectAttachments(ArrayList<ArrayList<String>> strs){
        StringBuilder contentBuilder = new StringBuilder();
        for (String temp:strs.get(1)){
            contentBuilder.append(temp);
        }
        String content = contentBuilder.toString();
        String[] names = {"Attachment II-Section A: Recombinant DNA"
                ,"Attachment II-Section B: Biohazardous Agents & Toxins"
                ,"Attachment II-Section G: Plants"
                ,"RECOMBINANT OR SYNTHETIC NUCLEIC ACID MOLECULE EXPERIMENT QUESTIONNAIRE"
                ,"ANIMAL EXPERIMENT QUESTIONNAIRE"
                ,"List of California Airborne Transmissible Disease Pathogens"};
        for(int i = 0 ; i<names.length; i++){
            if(content.contains(names[i])){
                this.attached[i]=true;
            }
        }
        //different name for worksheet 1 in old forms:
        if(content.contains("RECOMBINANT DNA EXPERIMENTS QUESTIONNAIRE")){
            this.attached[3]=true;
        }

    }

    public boolean isInvalid(){
        if(!allAttached()){
            this.invalid = true;
        }
        return this.invalid;
    }

    public void setBSL(String header){
        header = header.replaceAll("\\p{C}", " ");
        header = header.replaceAll("Yes", "");
        header = header.replaceAll("FORMTEXT", "");

        String[]temp = header.split("\\s*FORMCHECKBOX\\s*");
        ArrayList<String>rU = new ArrayList<String>();
        int maxBSL = -1;
        for(String str:temp){
            String input = str;
            if(str.length()>2)
                input = str.substring(0, str.length()-2);
            if(!input.replaceAll("[^a-zA-Z\\d:]", "").isEmpty()) {
                rU.add(input);
            }

            String nums = str.replaceAll("\\D", "");
            if(!nums.isEmpty()) {
                char end = nums.charAt(nums.length() - 1);
                int last = 0;
                if (Character.isDigit(end)) {
                    last = Character.getNumericValue(end);
                }
                maxBSL = Math.max(maxBSL, last);
            }
        }
        this.bsl = maxBSL;
        this.roomsUsed = new String[rU.size()];
        this.roomsUsed = rU.toArray(this.roomsUsed);
    }

    public boolean allAttached(){
        for(int i = 0; i<needAttachment.length; i++){
            if(needAttachment[i]&&!attached[i]){
                return false;
            }
        }
        return true;
    }

    int min(int j, int k){
        if(j == -1) return k;
        if(k == -1) return j;
        return Math.min(j, k);
    }

    public void setHeader(String header){
        int index = 0;
        String currword = "Name of Principal Investigator";
        int i;
        int j;
        int k;
        i = header.indexOf(currword);
        if (i == -1) {
            setInvalid(true);
        } else{
            index = i + currword.length()+1;
            j = header.indexOf(":", index);
            k = header.indexOf("\n", i);
            this.piName = header.substring(index, min(j-4, k));
        }

        currword = "Date";
        i = header.indexOf(currword);
        if (i == -1) {
            setInvalid(true);
        } else{
            index = i + currword.length()+1;
            j = header.indexOf(":", index);
            k = header.indexOf("\n", i);
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            try{
                this.date = df.parse(header.substring(index, min(j, k)));
            } catch(Exception e){
                this.invalid = true;
            }
        }

        currword = "Title";
        i = header.indexOf(currword);
        if (i == -1) {
            setInvalid(true);
        } else{
            index = i + currword.length()+1;
            j = header.indexOf(":", index);
            k = header.indexOf("\n", i);
            this.piTitle = header.substring(index, min(j-12, k));
        }

        currword = "Phone Number";
        i = header.indexOf(currword);
        if (i == -1) {
            setInvalid(true);
        } else{
            index = i + currword.length()+1;
            k = header.indexOf("\n", i);
            String num = header.substring(index, k);
            num = num.replaceAll("\\D", "");
            if(num.length()>10)
                num = num.substring(0, 10);
            this.phone = num;
        }

        currword = "Department";
        i = header.indexOf(currword);
        if (i == -1) {
            setInvalid(true);
        } else{
            index = i + currword.length()+1;
            j = header.indexOf(":", index);
            k = header.indexOf("\n", i);
            this.dept = header.substring(index, min(j-8, k));
        }

        currword = "Address";
        i = header.indexOf(currword);
        if (i == -1) {
            setInvalid(true);
        } else{
            index = i + currword.length()+1;
            j = header.indexOf(":", index);
            k = header.indexOf("\n", i);
            this.address = header.substring(index, min(j-8, k)).trim();
        }

        currword = "E-mail Address";
        i = header.indexOf(currword);
        if (i == -1) {
            setInvalid(true);
        } else{
            index = i + currword.length()+1;
            j = header.indexOf(":", index);
            k = header.indexOf("\n", i);
            this.email = header.substring(index, min(j-11, k)).trim();

            //online regex matcher for valid email
            String tempPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
            Pattern p = Pattern.compile(tempPattern);
            Matcher m = p.matcher(this.email);
            if(!m.matches()){
                setInvalid(true);
            }

        }

        currword = "FAX Number";
        i = header.indexOf(currword);
        if (i == -1) {
            setInvalid(true);
        } else{
            index = i + currword.length()+1;
            k = header.indexOf("\n", i);
            String num = header.substring(index, k);
            num = num.replaceAll("\\D", "");
            if(num.length()>10)
                num = num.substring(0, 10);
            this.fax = num;
        }

        currword = "Mail Code";
        i = header.indexOf(currword);
        if (i != -1) {
            //optional field
            index = i + currword.length()+1;
            j = header.indexOf(":", index);
            k = header.indexOf("\n", i);
            try {
                this.mailCode = Integer.parseInt(header.substring(index, min(j, k)).trim());
            } catch(Exception e){
                // do nothing
            }
        }

        currword = "Funding Source(s)";
        i = header.indexOf(currword);
        if (i == -1) {
            setInvalid(true);
        } else{
            while(i!=-1) {
                index = i + currword.length() + 1;
                j = header.indexOf(":", index);
                k = header.indexOf("\n", i);
                this.funding_sources += " " + header.substring(index, min(j, k)).trim();
                i = header.indexOf(currword, index);
            }
        }

        String[]dontUse = new String[]{"Name of Principal Investigator", "Date", "Title", "Phone Number", "Department",
            "Address", "E-mail Address", "FAX Number", "Funding Source\\(s\\)", "Grant Number\\(s\\)",
            "Expected Duration of Experiment", "From", "To", "Research Objective"};
        for(String du:dontUse){
            this.funding_sources = this.funding_sources.replaceAll(du, "");
        }

        currword = "Grant Number(s)";
        i = header.indexOf(currword);
        if (i == -1) {
            setInvalid(true);
        } else{
            while(i!=-1) {
                index = i + currword.length() + 1;
                j = header.indexOf(":", index);
                k = header.indexOf("\n", i);
                this.grant_nos += " " + header.substring(index, min(j, k)).trim();
                this.grant_nos = this.grant_nos.trim();
                i = header.indexOf(currword, index);
            }
        }

        currword = "From";
        i = header.indexOf(currword);
        if (i == -1) {
            setInvalid(true);
        } else{
            index = i + currword.length()+1;
            j = header.indexOf(":", index);
            k = header.indexOf("\n", i);
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            try{
                this.startDate = df.parse(header.substring(index, index + 11));
            } catch(Exception e){
                this.invalid = true;
            }
        }

        currword = "To";
        i = header.indexOf(currword);
        if (i == -1) {
            setInvalid(true);
        } else{
            index = i + currword.length()+1;
            j = header.indexOf(":", index);
            k = header.indexOf("\n", i);
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            try{
                this.endDate = df.parse(header.substring(index, index + 11));
            } catch(Exception e){
                this.invalid = true;
            }
        }

        currword = "Research Objective";
        i = header.indexOf(currword);
        if (i == -1) {
            setInvalid(true);
        } else{
            index = i + currword.length()+1;
            this.objective = header.substring(index).trim();
        }

    }


    /**
     * Source; https://stackoverflow.com/questions/1526826/printing-all-variables-value-from-a-class
     * @return this object as a string: a serialization?
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        result.append(this.getClass().getName());
        result.append(" Object {");
        result.append(newLine);

        //determine fields declared in this class only (no fields of superclass)
        Field[] fields = this.getClass().getDeclaredFields();

        //print field names paired with their values
        for (Field field : fields) {
            result.append("  ");
            try {
                result.append(field.getName());
                result.append(": ");
                //requires access to private field:
                Object temp = field.get(this);
                if(temp instanceof String[]){
                    String arr = "[";
                    for(String str:(String[])temp){
                        arr += str + "; ";
                    }
                    if(arr.length()>2)
                        arr = arr.substring(0, arr.length()-2);
                    arr += "]";
                    result.append(arr);
                }
                else if(temp instanceof boolean[]){
                    String arr = "[";
                    for(boolean str:(boolean[])temp){
                        arr += str + "; ";
                    }
                    arr = arr.substring(0, arr.length()-2);
                    arr += "]";
                    result.append(arr);
                } else {
                    result.append(field.get(this));
                }
            } catch (IllegalAccessException ex) {
                System.out.println(ex);
            }
            result.append(newLine);
        }
        result.append("}");

        return result.toString();
    }

    public void setInvalid(boolean invalid){this.invalid = invalid; }

    //personnel
    //location (boxes)
    //bio materials storage (boxes)

    //safety eval: more boxes

}

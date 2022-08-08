package main.java.newTest;

import static main.java.perfecto.CSVHandler.COMMA_DELIMITER;

public class ResourceTypeStats{
    private String type;
    private int items;
    private Long size;
    private Double duration;
    private String comparisonResult;
    static final String PAGE_TYPE_CSV_FILE_HEADER = "type, items, size, duration, comparison result";


    // ************* Getters and Setters
    public String getType() {
        return type;
    }

    public int getItems() {
        return items;
    }

    public void setItems(int items) {
        this.items = items;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }

    public String getComparisonResult() {
        return comparisonResult;
    }

    public void setComparisonResult(String comparisonResult) {
        this.comparisonResult = comparisonResult;
    }

    //  ************* Constructors

    ResourceTypeStats(String type, Long size, Double duration){
        super();
        this.type = type;
        this.items = 1;
        this.size = size;
        this.duration = duration;
        this.comparisonResult = "";
    }
    private ResourceTypeStats(String type, int items, Long size, Double duration){
        this(type, size, duration);
        this.items = items;
    }
    ResourceTypeStats(String type, int items, Long size, Double duration, String comparisonResult){
        this(type, items, size, duration);
        this.comparisonResult = comparisonResult;
    }

    // ************* Print out data
    @Override
    public String toString(){
        return System.lineSeparator()+"***********"+
                System.lineSeparator()+ "Type= "+type+
                System.lineSeparator()+ "Items= "+items+
                System.lineSeparator()+ "Size= "+size+
                System.lineSeparator()+ "Duration= "+duration;
    }
    private String toCSVString() {
        String send = type+ COMMA_DELIMITER;
        send = send + items+ COMMA_DELIMITER;
        send = send + size+ COMMA_DELIMITER;
        send = send + duration;
        return send;
    }
    public String toCSVStringWithDiff() {
        return toCSVString() + COMMA_DELIMITER + comparisonResult;
    }

}

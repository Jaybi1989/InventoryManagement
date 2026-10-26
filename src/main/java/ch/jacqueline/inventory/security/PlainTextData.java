package ch.jacqueline.inventory.security;

public class PlainTextData {
    private String message;

    public PlainTextData() {
    }
    public PlainTextData(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    @Override
    public String toString() {
        return "PlainTextData{" +
                "message='" + message.substring(0, Math.min(message.length(), 20)) + "..." + '\'' +
                '}';
    }
}

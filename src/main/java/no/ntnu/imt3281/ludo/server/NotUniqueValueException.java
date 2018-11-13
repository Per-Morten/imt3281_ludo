package no.ntnu.imt3281.ludo.server;

@SuppressWarnings("serial")
public class NotUniqueValueException extends RuntimeException {
    String mValueName;

    public NotUniqueValueException(String valueName) {
        mValueName = valueName;
    }

    public String getValueName() {
        return mValueName;
    }
}

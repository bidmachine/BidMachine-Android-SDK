package io.bidmachine.utils;

/**
 * Enum contains of supported user genders options
 */
public enum Gender {
    Female("F", 1), Male("M", 2), Omitted("O", 0);

    private String ortbValue;
    private final int serverValue;

    Gender(String ortbValue, int serverValue) {
        this.ortbValue = ortbValue;
        this.serverValue = serverValue;
    }

    public String getOrtbValue() {
        return ortbValue;
    }

    public int getServerValue() {
        return serverValue;
    }

    public static Gender fromInt(Integer genderInt) {
        if (genderInt == null) {
            return null;
        }
        switch (genderInt) {
            case 0:
                return Omitted;
            case 1:
                return Female;
            case 2:
                return Male;
        }
        return null;
    }

}

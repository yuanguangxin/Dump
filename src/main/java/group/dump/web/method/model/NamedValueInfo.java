package group.dump.web.method.model;

/**
 * @author yuanguangxin
 */
public class NamedValueInfo {

    private String name;

    private boolean required;

    private String defaultValue;

    public NamedValueInfo(String name, boolean required, String defaultValue) {
        this.name = name;
        this.required = required;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}

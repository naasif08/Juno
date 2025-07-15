package juno.builder;

public interface JunoBuilder {

    void buildJuno();

    void flashFirmware();

    void clean();

    void setOption(String key, String value);
}

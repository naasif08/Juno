package juno.builder;

public interface JunoBuilder {

    void buildJuno();

    void flashFirmware(OperationMode operationMode);

    void clean();

    void setOption(String key, String value);
}

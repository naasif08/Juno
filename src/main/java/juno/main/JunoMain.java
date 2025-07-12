package juno.main;

import juno.builder.JunoBuilder;
import juno.builder.OperationMode;
import juno.builder.impl.LocalBuilder;


public class JunoMain {

    public static void main(String[] args) {

        JunoBuilder builder = new LocalBuilder();
            builder.buildJuno();
            builder.flashFirmware(OperationMode.LOCAL);

    }
}

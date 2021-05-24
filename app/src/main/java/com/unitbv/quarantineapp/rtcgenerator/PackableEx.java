package com.unitbv.quarantineapp.rtcgenerator;

public interface PackableEx extends Packable {
    void unmarshal(ByteBuf in);
}

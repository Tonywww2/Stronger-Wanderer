package com.tonywww.strongerwanderer.combat;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
/** One hit per victim per explicit strike; independent projections have independent ledgers. */
public final class HitLedger {
    private final Set<UUID> victims = new HashSet<>();
    private int strike = -1;
    public boolean claim(int segment, UUID victim) {
        if (segment != strike) { strike = segment; victims.clear(); }
        return victims.add(victim);
    }
}

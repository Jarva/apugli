package net.merchantpug.apugli.component;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import io.github.apace100.apoli.power.Active;

import java.util.Set;

public interface KeyPressComponent extends AutoSyncedComponent {
    Set<Active.Key> getCurrentlyUsedKeys();
    Set<Active.Key> getPreviouslyUsedKeys();
    void setPreviouslyUsedKeys();

    Set<Active.Key> getKeysToCheck();
    Set<Active.Key> getPreviousKeysToCheck();
    void setPreviousKeysToCheck();

    void addKeyToCheck(Active.Key key);
    void changePreviousKeysToCheckToCurrent();
    void addKey(Active.Key key);
    void removeKey(Active.Key key);
}

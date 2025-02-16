package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.util.IndirectPlayer;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface RoleFactory {
    Role makeRole(@Nullable Shadow shadow, @Nullable IndirectPlayer player);
}

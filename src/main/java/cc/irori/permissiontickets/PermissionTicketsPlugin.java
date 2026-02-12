package cc.irori.permissiontickets;

import cc.irori.permissiontickets.system.EarnPermissionTicketSystem;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class PermissionTicketsPlugin extends JavaPlugin {

    public PermissionTicketsPlugin(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        getEntityStoreRegistry().registerSystem(new EarnPermissionTicketSystem());
    }

    @Override
    protected void start() {}
}

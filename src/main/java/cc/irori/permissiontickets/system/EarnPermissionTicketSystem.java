package cc.irori.permissiontickets.system;

import cc.irori.permissiontickets.util.Logs;
import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Map;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class EarnPermissionTicketSystem extends EntityTickingSystem<EntityStore> {

    private static final String TAG_GRANTS = "PermissionTicket.Grants";
    private static final String TAG_MESSAGES = "PermissionTicket.Messages";

    private static final HytaleLogger LOGGER = Logs.logger();

    @Override
    public void tick(
            float dt,
            int index,
            @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {

        Player player = archetypeChunk.getComponent(index, Player.getComponentType());
        assert player != null;

        PlayerRef playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        Ref<EntityStore> ref = player.getReference();
        assert playerRef != null;
        assert ref != null;

        if (player.getGameMode() == GameMode.Creative) {
            return;
        }

        Inventory inventory = player.getInventory();
        ItemContainer container = inventory.getCombinedHotbarFirst();

        container.forEach((slot, itemStack) -> {
            Item item = itemStack.getItem();
            AssetExtraInfo.Data data = item.getData();

            Map<String, String[]> rawTags = data.getRawTags();
            String[] grants = rawTags.get(TAG_GRANTS);
            String[] messages = rawTags.get(TAG_MESSAGES);

            if (grants != null || messages != null) {
                SoundUtil.playSoundEvent2dToPlayer(
                        playerRef,
                        SoundEvent.getAssetMap().getIndex("SFX_Creative_Play_Brush_Shape"),
                        SoundCategory.UI);
                SoundUtil.playSoundEvent2dToPlayer(
                        playerRef,
                        SoundEvent.getAssetMap().getIndex("SFX_Creative_Play_Selection_Drag"),
                        SoundCategory.UI);

                if (grants != null) {
                    LuckPerms luckPerms = LuckPermsProvider.get();
                    luckPerms.getUserManager().modifyUser(playerRef.getUuid(), user -> {
                        for (String permission : grants) {
                            user.data()
                                    .add(luckPerms
                                            .getNodeBuilderRegistry()
                                            .forPermission()
                                            .permission(permission)
                                            .build());
                            LOGGER.atInfo().log(
                                    "Granted permission '%s' to player %s", permission, playerRef.getUsername());
                        }
                    });
                }

                if (messages != null) {
                    for (String message : messages) {
                        player.sendMessage(Message.translation(message));
                    }
                }

                container.removeItemStackFromSlot(slot);
            }
        });
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType());
    }
}

package ru.nezxenka.holycustomtitul.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.nezxenka.holycustomtitul.HolyCustomTitul;
import ru.nezxenka.holycustomtitul.utils.TextFormatter;
import java.util.Arrays;

public class TitleConfirmationMenu extends AbstractMenu {
    private final HolyCustomTitul plugin;
    private final Player player;
    private final String title;
    private boolean confirmed;
    private int pendingCount;

    public TitleConfirmationMenu(HolyCustomTitul plugin, Player player) {
        this(plugin, player, "");
    }

    public TitleConfirmationMenu(HolyCustomTitul plugin, Player player, String title) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.confirmed = false;
        initializeMenu();
        loadPendingCount();
    }

    private void initializeMenu() {
        createInventory(5, TextFormatter.color("&0Подтверждение титула"));
        setupItems();
    }

    private void setupItems() {
        ItemStack cancel = createItem(
                Material.RED_CONCRETE,
                " ",
                Arrays.asList("&c[ЛКМ] - отменить отправку")
        );
        inventory.setItem(0, cancel);

        ItemStack titleInfo = createItem(
                Material.ITEM_FRAME,
                " ",
                Arrays.asList(
                        "&bТитул: " + title,
                        "&7Нажмите кнопки слева или справа",
                        "&7для подтверждения или отмены"
                )
        );
        inventory.setItem(2, titleInfo);

        ItemStack confirm = createItem(
                Material.GREEN_CONCRETE,
                " ",
                Arrays.asList("&a[ЛКМ] - подтвердить отправку")
        );
        inventory.setItem(4, confirm);
    }

    private void loadPendingCount() {
        plugin.getDatabaseManager().getPlayerPendingTitles(player.getName()).thenAccept(titles -> {
            this.pendingCount = titles.size();
        });
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getTitle() {
        return title;
    }

    public int getPendingCount() {
        return pendingCount;
    }

    public void open() {
        player.openInventory(inventory);
    }
}
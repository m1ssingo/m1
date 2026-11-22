package dev.m1ssing0.packclient.mixin;

import dev.ftb.mods.ftblibrary.ui.input.Key;
import dev.ftb.mods.ftbquests.client.FTBQuestsKeyMappings;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestPanel;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestPositionableButton;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Chapter;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = QuestScreen.class, remap = false)
public abstract class QuestViewportMixin {
    @Unique private final Map<Long, Integer> m1packclient$chapterZooms = new HashMap<>();
    @Unique private long m1packclient$displayedChapter;

    @Inject(method = "selectChapter", at = @At("HEAD"))
    private void m1packclient$rememberZoom(Chapter next, CallbackInfo callback) {
        QuestScreen screen = (QuestScreen) (Object) this;
        Chapter current = screen.getSelectedChapter().orElse(null);
        if (current != null && current != next && current.id == m1packclient$displayedChapter) {
            m1packclient$chapterZooms.put(current.id, screen.getZoom());
            m1packclient$displayedChapter = 0;
        }
    }

    @Inject(method = "drawBackground", at = @At("HEAD"))
    private void m1packclient$prepareOverview(CallbackInfo callback) {
        QuestScreen screen = (QuestScreen) (Object) this;
        Chapter chapter = screen.getSelectedChapter().orElse(null);
        if (screen.isViewingQuest() || !m1packclient$isPackChapter(chapter)
                || chapter.id == m1packclient$displayedChapter) return;

        Integer zoom = m1packclient$chapterZooms.get(chapter.id);
        if (zoom == null) {
            if (!m1packclient$fitChapter(screen)) return;
        } else {
            // FTB restores the chapter's scroll with the previous chapter's zoom.
            QuestPanel panel = screen.questPanel;
            double halfWidth = panel.getWidth() / 2.0;
            double halfHeight = panel.getHeight() / 2.0;
            double ratio = (double) zoom / screen.getZoom();
            double scrollX = (panel.getScrollX() + halfWidth) * ratio - halfWidth;
            double scrollY = (panel.getScrollY() + halfHeight) * ratio - halfHeight;
            m1packclient$setZoom(screen, zoom);
            panel.setScrollX(scrollX);
            panel.setScrollY(scrollY);
        }
        m1packclient$displayedChapter = chapter.id;
    }

    @Inject(method = "restorePersistedScreenData", at = @At("RETURN"))
    private void m1packclient$refreshAfterRestore(CallbackInfo callback) {
        m1packclient$displayedChapter = 0;
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void m1packclient$restoreOverview(Key key, CallbackInfoReturnable<Boolean> callback) {
        QuestScreen screen = (QuestScreen) (Object) this;
        Chapter chapter = screen.getSelectedChapter().orElse(null);
        if (key.matches(FTBQuestsKeyMappings.KEY_GUI_ZOOM_RESET)
                && !screen.isViewingQuest() && m1packclient$isPackChapter(chapter)
                && m1packclient$fitChapter(screen)) {
            m1packclient$displayedChapter = chapter.id;
            callback.setReturnValue(true);
        }
    }

    @Unique
    private static boolean m1packclient$isPackChapter(Chapter chapter) {
        return chapter != null
                && (chapter.id & 0xFF0FFFFFFFFFFFFFL) == 0x4A00000000000001L
                && ((chapter.id >>> 52) & 0xF) <= 10;
    }

    @Unique
    private static boolean m1packclient$fitChapter(QuestScreen screen) {
        QuestPanel panel = screen.questPanel;
        int left = screen.getChapterPanelRight();
        // FTB's action buttons occupy the last 20 GUI pixels.
        int right = screen.getWidth() - 20;
        int innerWidth = right - left - 20;
        int innerHeight = screen.getHeight() - 20;
        ScreenRectangle bounds = m1packclient$measure(panel);
        if (bounds == null || innerWidth <= 0 || innerHeight <= 0) return false;

        double scale = Math.min((double) innerWidth / bounds.width(), (double) innerHeight / bounds.height());
        int zoom = Math.clamp((int) Math.floor(screen.getZoom() * scale), 4, 16);
        m1packclient$setZoom(screen, zoom);
        bounds = m1packclient$measure(panel);
        panel.setScrollX(bounds.left() + bounds.width() / 2.0 - (left + right) / 2.0);
        panel.setScrollY(bounds.top() + bounds.height() / 2.0 - screen.getHeight() / 2.0);
        return true;
    }

    @Unique
    private static ScreenRectangle m1packclient$measure(QuestPanel panel) {
        int left = Integer.MAX_VALUE;
        int top = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        int bottom = Integer.MIN_VALUE;
        for (var widget : panel.getWidgets()) {
            if (!(widget instanceof QuestPositionableButton) || widget.getWidth() <= 0) continue;
            left = Math.min(left, widget.getX());
            top = Math.min(top, widget.getY());
            right = Math.max(right, widget.getX() + widget.getWidth());
            bottom = Math.max(bottom, widget.getY() + widget.getHeight());
        }
        return right > left && bottom > top ? new ScreenRectangle(left, top, right - left, bottom - top) : null;
    }

    @Unique
    private static void m1packclient$setZoom(QuestScreen screen, int zoom) {
        screen.addZoom((zoom - screen.getZoom()) / 4.0);
    }
}

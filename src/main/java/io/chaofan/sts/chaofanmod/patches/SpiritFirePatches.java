package io.chaofan.sts.chaofanmod.patches;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.CampfireUI;
import com.megacrit.cardcrawl.rooms.RestRoom;
import com.megacrit.cardcrawl.scenes.AbstractScene;
import com.megacrit.cardcrawl.scenes.TheBeyondScene;
import com.megacrit.cardcrawl.scenes.TheBottomScene;
import com.megacrit.cardcrawl.scenes.TheCityScene;
import io.chaofan.sts.chaofanmod.relics.SpiritFire;
import io.chaofan.sts.chaofanmod.utils.ChaofanModEnums;
import javassist.CtBehavior;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class SpiritFirePatches {
    @SpirePatch(clz = AbstractRoom.class, method = "render")
    public static class AbstractRoomRenderPatch {
        @SpirePrefixPatch
        public static void Prefix(AbstractRoom __instance, SpriteBatch sb) {
            if (AbstractDungeon.getCurrRoom() instanceof RestRoom && SpiritFire.isInCampfireCombat) {
                if (__instance.monsters != null && AbstractDungeon.screen != AbstractDungeon.CurrentScreen.DEATH) {
                    __instance.monsters.render(sb);
                }
            }
        }

        @SpireInsertPatch(locator = Locator.class)
        public static void Insert(AbstractRoom __instance, SpriteBatch sb) {
            if (AbstractDungeon.getCurrRoom() instanceof RestRoom && SpiritFire.isInCampfireCombat) {
                if (__instance.phase == AbstractRoom.RoomPhase.COMBAT) {
                    AbstractDungeon.player.renderPlayerBattleUi(sb);
                }

                for (AbstractPotion i : __instance.potions) {
                    if (!i.isObtained) {
                        i.render(sb);
                    }
                }
            }
        }

        public static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher.InstanceOfMatcher matcher = new Matcher.InstanceOfMatcher(RestRoom.class);
                return LineFinder.findInOrder(ctBehavior, matcher);
            }
        }
    }

    @SpirePatch(clz = TheBottomScene.class, method = "renderCampfireRoom")
    @SpirePatch(clz = TheCityScene.class, method = "renderCampfireRoom")
    @SpirePatch(clz = TheBeyondScene.class, method = "renderCampfireRoom")
    public static class AbstractSceneRenderCampfireRoomPatch {

        @SpireInsertPatch(locator = Locator.class)
        public static void Insert(AbstractScene __instance, SpriteBatch sb) {
            Method m = ReflectionHacks.getCachedMethod(AbstractScene.class, "renderQuadrupleSize", SpriteBatch.class, TextureAtlas.AtlasRegion.class, boolean.class);
            try {
                m.invoke(__instance, sb, ReflectionHacks.getPrivate(__instance, AbstractScene.class, "campfireGlow"), CampfireUI.hidden && SpiritFire.isInCampfireCombat);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        public static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher.MethodCallMatcher matcher = new Matcher.MethodCallMatcher(ctBehavior.getDeclaringClass().getName(), "renderQuadrupleSize");
                return LineFinder.findInOrder(ctBehavior, matcher);
            }
        }
    }

    @SpirePatch(clz = RewardItem.class, method = "render")
    public static class RewardItemRenderPatch {

        @SpireInsertPatch(locator = Locator.class, localvars = { "tips" })
        public static void Insert(RewardItem __instance, SpriteBatch sb, ArrayList<PowerTip> tips) {
            if (__instance.relicLink.type == ChaofanModEnums.CHAOFAN_MOD_RUBY_KEY) {
                tips.add(new PowerTip(RewardItem.TEXT[7], RewardItem.TEXT[8] + FontHelper.colorString(CardCrawlGame.languagePack.getRelicStrings(SpiritFire.ID).DESCRIPTIONS[3] + RewardItem.TEXT[9], "y")));
            }
        }

        public static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctBehavior) throws Exception {
                Matcher.MethodCallMatcher matcher = new Matcher.MethodCallMatcher(TipHelper.class, "queuePowerTips");
                return LineFinder.findInOrder(ctBehavior, matcher);
            }
        }
    }
}

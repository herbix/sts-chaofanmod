package io.chaofan.sts.chaofanmod;

import basemod.AutoAdd;
import basemod.BaseMod;
import basemod.ModPanel;
import basemod.ReflectionHacks;
import basemod.abstracts.CustomRelic;
import basemod.helpers.RelicType;
import basemod.helpers.ScreenPostProcessorManager;
import basemod.interfaces.*;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.daily.mods.AbstractDailyMod;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.localization.*;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import io.chaofan.sts.CommonModUtils;
import io.chaofan.sts.chaofanmod.cards.AhhMyEyes;
import io.chaofan.sts.chaofanmod.events.Gremlin2048;
import io.chaofan.sts.chaofanmod.mods.Lonely;
import io.chaofan.sts.chaofanmod.monsters.SpiritFireMonster;
import io.chaofan.sts.chaofanmod.monsters.SpiritFireMonsterAct2;
import io.chaofan.sts.chaofanmod.monsters.SpiritFireMonsterAct3;
import io.chaofan.sts.chaofanmod.patches.ThirdPerspectiveViewPatches;
import io.chaofan.sts.chaofanmod.powers.AddFuelPower;
import io.chaofan.sts.chaofanmod.powers.HeavyHandPower;
import io.chaofan.sts.chaofanmod.relics.OldPhone;
import io.chaofan.sts.chaofanmod.relics.SpotLight;
import io.chaofan.sts.chaofanmod.relics.Stool;
import io.chaofan.sts.chaofanmod.rewards.HealReward;
import io.chaofan.sts.chaofanmod.rewards.RubyKeyReward;
import io.chaofan.sts.chaofanmod.utils.ChaofanModEnums;
import io.chaofan.sts.chaofanmod.variables.ShootCountVariable;
import io.chaofan.sts.enhancedsteamstatus.EnhancedSteamStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

@SpireInitializer
public class ChaofanMod implements
        EditStringsSubscriber,
        EditRelicsSubscriber,
        EditCardsSubscriber,
        EditKeywordsSubscriber,
        PostInitializeSubscriber,
        PostExhaustSubscriber,
        StartGameSubscriber,
        PostUpdateSubscriber {

    public static final String MOD_ID = "chaofanmod";
    public static final Logger logger = LogManager.getLogger(ChaofanMod.class.getName());
    public static Map<String, CommonModUtils.Keyword> keywords;

    public static String getImagePath(String file) {
        return MOD_ID + "/images/" + file;
    }

    public static String getShaderPath(String file) {
        return MOD_ID + "/shaders/" + file;
    }

    public static String makeId(String id) {
        return MOD_ID + ":" + id;
    }

    public static String invertId(String id) {
        return id.substring((MOD_ID + ":").length());
    }

    private static final List<ScreenPostProcessor> postProcessors = new ArrayList<>();

    @SuppressWarnings("unused")
    public static void initialize() {
        logger.info("Initializing ChaofanMod");

        ChaofanMod bladeGunnerMod = new ChaofanMod();
        BaseMod.subscribe(bladeGunnerMod);
        EnhancedSteamStatus.initialize();
    }

    @Override
    public void receivePostInitialize() {
        ModPanel settingsPanel = initSettings();

        Texture badgeTexture = ImageMaster.loadImage(MOD_ID + "/images/badge.png");
        BaseMod.registerModBadge(badgeTexture, "Chaofan Mod", "Chaofan", "", settingsPanel);

        BaseMod.addMonster(SpiritFireMonster.ID, () -> {
            if (AbstractDungeon.actNum == 3) {
                return new MonsterGroup(new SpiritFireMonsterAct3());
            } else if (AbstractDungeon.actNum == 2) {
                return new MonsterGroup(new SpiritFireMonsterAct2());
            } else {
                return new MonsterGroup(new SpiritFireMonster());
            }
        });

        BaseMod.registerCustomReward(ChaofanModEnums.CHAOFAN_MOD_HEAL, HealReward::load, HealReward::save);
        BaseMod.registerCustomReward(ChaofanModEnums.CHAOFAN_MOD_RUBY_KEY, RubyKeyReward::load, RubyKeyReward::save);

        HashMap<String, AbstractDailyMod> difficultyMods = ReflectionHacks.getPrivate(null, ModHelper.class, "difficultyMods");
        difficultyMods.put(Lonely.ID, new Lonely());

        BaseMod.addEvent(Gremlin2048.ID, Gremlin2048.class);

        BaseMod.addPower(HeavyHandPower.class, HeavyHandPower.POWER_ID);
    }

    private ModPanel initSettings() {
        return new ModPanel();
    }

    @Override
    public void receiveEditCards() {
        BaseMod.addDynamicVariable(new ShootCountVariable());
        new AutoAdd(MOD_ID)
                .packageFilter(AhhMyEyes.class)
                .cards();
    }

    @Override
    public void receiveEditRelics() {
        new AutoAdd(MOD_ID)
                .packageFilter(Stool.class)
                .any(CustomRelic.class, (info, relic) -> BaseMod.addRelic(relic, RelicType.SHARED));
    }

    @Override
    public void receiveEditStrings() {
        CommonModUtils.loadCustomStringsFile(MOD_ID, RelicStrings.class, "relics.json");
        CommonModUtils.loadCustomStringsFile(MOD_ID, CardStrings.class, "cards.json");
        CommonModUtils.loadCustomStringsFile(MOD_ID, MonsterStrings.class, "monsters.json");
        CommonModUtils.loadCustomStringsFile(MOD_ID, PowerStrings.class, "powers.json");
        CommonModUtils.loadCustomStringsFile(MOD_ID, OrbStrings.class, "orbs.json");
        CommonModUtils.loadCustomStringsFile(MOD_ID, RunModStrings.class, "run_mods.json");
        CommonModUtils.loadCustomStringsFile(MOD_ID, EventStrings.class, "events.json");
    }

    @Override
    public void receiveEditKeywords() {
        ChaofanMod.keywords = new HashMap<>();
        CommonModUtils.loadKeywordsFile(ChaofanMod.keywords);
    }

    @Override
    public void receivePostExhaust(AbstractCard abstractCard) {
        AddFuelPower.triggerExhaust(abstractCard);
    }

    @Override
    public void receiveStartGame() {
        clearPostProcessors();

        for (AbstractRelic relic : AbstractDungeon.player.relics) {
            if (relic.relicId.equals(OldPhone.ID)) {
                registerPostProcessor(new OldPhone.OldPhonePostProcessor());
            }
            if (relic.relicId.equals(SpotLight.ID)) {
                registerPostProcessor(new SpotLight.SpotLightPostProcessor());
            }
        }

        ThirdPerspectiveViewPatches.setEnable(false);
    }

    @Override
    public void receivePostUpdate() {
        if (CardCrawlGame.mode != CardCrawlGame.GameMode.GAMEPLAY && !postProcessors.isEmpty()) {
            clearPostProcessors();
        }
    }

    public static void registerPostProcessor(ScreenPostProcessor postProcessor) {
        postProcessors.add(postProcessor);
        ScreenPostProcessorManager.addPostProcessor(postProcessor);
    }

    public static void removePostProcessor(Class<? extends ScreenPostProcessor> processorClass) {
        for (Iterator<ScreenPostProcessor> iterator = postProcessors.iterator(); iterator.hasNext(); ) {
            ScreenPostProcessor postProcessor = iterator.next();
            if (processorClass.isAssignableFrom(postProcessor.getClass())) {
                ScreenPostProcessorManager.removePostProcessor(postProcessor);
                iterator.remove();
                break;
            }
        }
    }

    private void clearPostProcessors() {
        for (ScreenPostProcessor postProcessor : postProcessors) {
            ScreenPostProcessorManager.removePostProcessor(postProcessor);
        }
        postProcessors.clear();
    }
}

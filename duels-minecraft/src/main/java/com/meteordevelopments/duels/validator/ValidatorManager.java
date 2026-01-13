package com.meteordevelopments.duels.validator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import com.meteordevelopments.duels.DuelsPlugin;
import com.meteordevelopments.duels.party.Party;
import com.meteordevelopments.duels.setting.Settings;
import com.meteordevelopments.duels.util.Loadable;
import com.meteordevelopments.duels.util.function.Pair;
import com.meteordevelopments.duels.util.validator.BiValidator;
import com.meteordevelopments.duels.util.validator.TriValidator;
import com.meteordevelopments.duels.util.validator.ValidatorUtil;
import com.meteordevelopments.duels.validator.validators.match.*;
import com.meteordevelopments.duels.validator.validators.request.self.*;
import com.meteordevelopments.duels.validator.validators.request.target.*;
import org.bukkit.entity.Player;

import lombok.Getter;

public class ValidatorManager implements Loadable {

    private final Map<Class<?>, TriValidator<Player, Party, Collection<Player>>> selfValidators = new HashMap<>();
    private final Map<Class<?>, TriValidator<Pair<Player, Player>, Party, Collection<Player>>> targetValidators = new HashMap<>();

    private final DuelsPlugin plugin;

    @Getter
    private ImmutableList<TriValidator<Player, Party, Collection<Player>>> duelSelfValidators;
    @Getter
    private ImmutableList<TriValidator<Pair<Player, Player>, Party, Collection<Player>>> duelTargetValidators;

    @Getter
    private ImmutableList<TriValidator<Player, Party, Collection<Player>>> duelAcceptSelfValidators;
    @Getter
    private ImmutableList<TriValidator<Pair<Player, Player>, Party, Collection<Player>>> duelAcceptTargetValidators;

    @Getter
    private ImmutableList<TriValidator<Pair<Player, Player>, Party, Collection<Player>>> duelDenyTargetValidators;

    @Getter
    private ImmutableList<BiValidator<Collection<Player>, Settings>> matchValidators;

    public ValidatorManager(final DuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handleLoad() {
        selfValidators.put(SelfEmptyInventoryValidator.class, new SelfEmptyInventoryValidator(plugin));
        selfValidators.put(SelfPreventCreativeValidator.class, new SelfPreventCreativeValidator(plugin));
        selfValidators.put(SelfBlacklistedWorldValidator.class, new SelfEmptyInventoryValidator(plugin));
        selfValidators.put(SelfCombatTagValidator.class, new SelfPreventCreativeValidator(plugin));
        selfValidators.put(SelfDuelZoneValidator.class, new SelfEmptyInventoryValidator(plugin));
        selfValidators.put(SelfCheckMatchValidator.class, new SelfPreventCreativeValidator(plugin));
        selfValidators.put(SelfCheckSpectateValidator.class, new SelfEmptyInventoryValidator(plugin));

        targetValidators.put(TargetCheckSelfValidator.class, new TargetCheckSelfValidator(plugin));
        targetValidators.put(TargetCanRequestValidator.class, new TargetCanRequestValidator(plugin));
        targetValidators.put(TargetPartyValidator.class, new TargetPartyValidator(plugin));
        targetValidators.put(TargetCheckMatchValidator.class, new TargetCheckMatchValidator(plugin));
        targetValidators.put(TargetCheckSelfMatchValidator.class, new TargetCheckSelfMatchValidator(plugin));
        targetValidators.put(TargetCheckSpectateValidator.class, new TargetCheckSpectateValidator(plugin));
        targetValidators.put(TargetNoRequestValidator.class, new TargetNoRequestValidator(plugin));
        targetValidators.put(TargetHasRequestValidator.class, new TargetHasRequestValidator(plugin));
        targetValidators.put(TargetPartyOwnerValidator.class, new TargetPartyOwnerValidator(plugin));

        duelSelfValidators = ValidatorUtil.buildList(
                self(SelfEmptyInventoryValidator.class),
                self(SelfPreventCreativeValidator.class),
                self(SelfBlacklistedWorldValidator.class),
                self(SelfCombatTagValidator.class),
                self(SelfDuelZoneValidator.class),
                self(SelfCheckMatchValidator.class),
                self(SelfCheckSpectateValidator.class)
        );
        duelTargetValidators = ValidatorUtil.buildList(
                target(TargetCheckSelfValidator.class),
                target(TargetCanRequestValidator.class),
                target(TargetPartyValidator.class),
                target(TargetCheckMatchValidator.class),
                target(TargetCheckSpectateValidator.class),
                target(TargetHasRequestValidator.class),
                target(TargetPartyOwnerValidator.class)
        );

        duelAcceptSelfValidators = duelSelfValidators;
        duelAcceptTargetValidators = ValidatorUtil.buildList(
                target(TargetCheckSelfValidator.class),
                target(TargetPartyValidator.class),
                target(TargetCheckMatchValidator.class),
                target(TargetCheckSelfMatchValidator.class),
                target(TargetCheckSpectateValidator.class),
                target(TargetNoRequestValidator.class),
                target(TargetPartyOwnerValidator.class)
        );

        duelDenyTargetValidators = ValidatorUtil.buildList(
                target(TargetNoRequestValidator.class)
        );

        matchValidators = ValidatorUtil.buildList(
                new ModeValidator(plugin),
                new CheckAliveValidator(plugin),
                new PreventCreativeValidator(plugin),
                new BlacklistedWorldValidator(plugin),
                new CheckMoveValidator(plugin),
                new CombatTagValidator(plugin),
                new DuelZoneValidator(plugin),
                new PartyValidator(plugin)
        );
    }

    @Override
    public void handleUnload() {
        selfValidators.clear();
        targetValidators.clear();
    }

    private TriValidator<Player, Party, Collection<Player>> self(Class<? extends TriValidator<Player, Party, Collection<Player>>> clazz) {
        return selfValidators.get(clazz);
    }

    private TriValidator<Pair<Player, Player>, Party, Collection<Player>> target(Class<? extends TriValidator<Pair<Player, Player>, Party, Collection<Player>>> clazz) {
        return targetValidators.get(clazz);
    }
}
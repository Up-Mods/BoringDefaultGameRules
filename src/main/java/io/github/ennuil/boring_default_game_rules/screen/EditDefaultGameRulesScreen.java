package io.github.ennuil.boring_default_game_rules.screen;

import io.github.ennuil.boring_default_game_rules.config.ModConfigManager;
import io.github.ennuil.boring_default_game_rules.mixin.client.accessors.EditGameRulesScreenAccessor;
import io.github.ennuil.boring_default_game_rules.mixin.client.accessors.ScreenAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.GameRules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class EditDefaultGameRulesScreen extends EditGameRulesScreen {
	public EditDefaultGameRulesScreen(GameRules gameRules, Consumer<Optional<GameRules>> consumer) {
		super(gameRules, consumer);
		// You can't stop destiny, `final` keyword!
		((ScreenAccessor) this).setTitle(Component.translatable("boring_default_game_rules.edit_default_game_rules.title"));
	}

	@Override
	protected void init() {
		super.init();
		var button = new ResetButtonWidget();
		((EditGameRulesScreenAccessor) (EditDefaultGameRulesScreen.this)).getRuleList().children().add(button);
	}

	public class ResetButtonWidget extends EditGameRulesScreen.RuleEntry {
		private final Button resetButton;
		private final List<AbstractButton> widgets = new ArrayList<>();

		public ResetButtonWidget() {
			super(List.of(Component.translatable("boring_default_game_rules.edit_default_game_rules.reset_defaults.tooltip").getVisualOrderText()));
			this.resetButton = Button.builder(
				Component.translatable("boring_default_game_rules.edit_default_game_rules.reset_defaults"),
				button -> {
					double scrollAmount = ((EditGameRulesScreenAccessor) (EditDefaultGameRulesScreen.this)).getRuleList().scrollAmount();
					ModConfigManager.resetDefaults();
					((EditGameRulesScreenAccessor) (EditDefaultGameRulesScreen.this)).setGameRules(new GameRules(FeatureFlags.REGISTRY.allFlags()));
					EditDefaultGameRulesScreen.this.repositionElements();
					((EditGameRulesScreenAccessor) (EditDefaultGameRulesScreen.this)).getRuleList().setScrollAmount(scrollAmount);
				}
			).pos(10, 5)
			.size(150, 20)
			.build();
			this.widgets.add(this.resetButton);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return this.widgets;
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return this.widgets;
		}

		@Override
		public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			this.resetButton.setX(x + 33);
			this.resetButton.setY(y);
			this.resetButton.render(graphics, mouseX, mouseY, tickDelta);
		}
	}
}

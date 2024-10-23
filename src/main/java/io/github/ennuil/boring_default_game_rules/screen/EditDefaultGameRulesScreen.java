package io.github.ennuil.boring_default_game_rules.screen;

import io.github.ennuil.boring_default_game_rules.config.ModConfigManager;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.world.EditGameRulesScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class EditDefaultGameRulesScreen extends EditGameRulesScreen {
	public EditDefaultGameRulesScreen(GameRules gameRules, Consumer<Optional<GameRules>> consumer) {
		super(gameRules, consumer);
		// You can't stop destiny, `final` keyword!
		this.title = Text.translatable("boring_default_game_rules.edit_default_game_rules.title");
	}

	@Override
	protected void init() {
		super.init();
		var button = new ResetButtonWidget();
		EditDefaultGameRulesScreen.this.rulesList.children().add(button);
	}

	public class ResetButtonWidget extends EditGameRulesScreen.AbstractEntry {
		private final ButtonWidget resetButton;
		private final List<ClickableWidget> widgets = new ArrayList<>();

		public ResetButtonWidget() {
			super(List.of(Text.translatable("boring_default_game_rules.edit_default_game_rules.reset_defaults.tooltip").asOrderedText()));
			this.resetButton = ButtonWidget.builder(
				Text.translatable("boring_default_game_rules.edit_default_game_rules.reset_defaults"),
				button -> {
					double scrollAmount = EditDefaultGameRulesScreen.this.rulesList.getScrollAmount();
					ModConfigManager.resetDefaults();
					EditDefaultGameRulesScreen.this.gameRules = new GameRules();
					EditDefaultGameRulesScreen.this.repositionElements();
					EditDefaultGameRulesScreen.this.rulesList.setScrollAmount(scrollAmount);
				}
			).position(10, 5)
			.size(150, 20)
			.build();
			this.widgets.add(this.resetButton);
		}

		@Override
		public List<? extends Element> children() {
			return this.widgets;
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
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

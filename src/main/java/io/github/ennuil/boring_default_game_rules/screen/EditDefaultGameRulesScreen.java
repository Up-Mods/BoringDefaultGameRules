package io.github.ennuil.boring_default_game_rules.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import io.github.ennuil.boring_default_game_rules.config.ModConfigManager;
import io.github.ennuil.boring_default_game_rules.mixin.client.EditGameRulesScreenAccessor;
import io.github.ennuil.boring_default_game_rules.mixin.client.ScreenAccessor;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.world.EditGameRulesScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;

public class EditDefaultGameRulesScreen extends EditGameRulesScreen {
	public EditDefaultGameRulesScreen(GameRules gameRules, Consumer<Optional<GameRules>> consumer) {
		super(gameRules, consumer);
		// You can't stop destiny, `final` keyword!
		((ScreenAccessor) this).setTitle(Text.translatable("boring_default_game_rules.edit_default_game_rules.title"));
	}

	@Override
	protected void init() {
		super.init();
		var button = new ResetButtonWidget();
		((EditGameRulesScreenAccessor) this).getRuleListWidget().children().add(button);
	}

	public class ResetButtonWidget extends EditGameRulesScreen.AbstractRuleWidget {
		private final ButtonWidget resetButton;
		private final List<ClickableWidget> widgets = new ArrayList<>();

		public ResetButtonWidget() {
			super(List.of(Text.translatable("boring_default_game_rules.edit_default_game_rules.reset_defaults.tooltip").asOrderedText()));
			this.resetButton = new ButtonWidget(
				10, 5,
				150, 20,
				Text.translatable("boring_default_game_rules.edit_default_game_rules.reset_defaults"),
				button -> {
					double scrollAmount = ((EditGameRulesScreenAccessor) EditDefaultGameRulesScreen.this).getRuleListWidget().getScrollAmount();
					ModConfigManager.resetDefaults();
					((EditGameRulesScreenAccessor) EditDefaultGameRulesScreen.this).setGameRules(new GameRules());
					EditDefaultGameRulesScreen.this.clearAndInit();
					((EditGameRulesScreenAccessor) EditDefaultGameRulesScreen.this).getRuleListWidget().setScrollAmount(scrollAmount);
				}
			);
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
		public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			this.resetButton.x = x + 33;
			this.resetButton.y = y;
			this.resetButton.render(matrices, mouseX, mouseY, tickDelta);
		}
	}
}

package io.github.ennuil.boring_default_game_rules.mixin.client;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.ennuil.boring_default_game_rules.config.ModConfigManager;
import io.github.ennuil.boring_default_game_rules.screen.EditDefaultGameRulesScreen;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.EditGameRulesScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;

@Mixin(EditGameRulesScreen.class)
public abstract class EditGameRulesScreenMixin extends Screen {
	private EditGameRulesScreenMixin(Text text) {
		super(text);
	}

	@Shadow
	private EditGameRulesScreen.RuleListWidget ruleListWidget;

	@Shadow
	@Final
	@Mutable
	private GameRules gameRules;

	@Inject(method = "init()V", at = @At("TAIL"))
	private void addEditDefaultsButton(CallbackInfo ci) {
		// Don't let the button appear on screens that extends this screen
		if (((EditGameRulesScreen) (Object) this).getClass() == EditGameRulesScreen.class) {
			this.ruleListWidget.children().add(new EditDefaultsButtonWidget());
		}
	}

	@SuppressWarnings("all")
	@Unique
	public class EditDefaultsButtonWidget extends EditGameRulesScreen.AbstractRuleWidget {
		private final ButtonWidget editButton;
		private final List<ClickableWidget> widgets = new ArrayList<>();

		public EditDefaultsButtonWidget() {
			super(List.of(
				Text.translatable("boring_default_game_rules.game_rules.edit_default_game_rules.tooltip.1").asOrderedText(),
				Text.translatable("boring_default_game_rules.game_rules.edit_default_game_rules.tooltip.2").asOrderedText()
			));
			this.editButton = ButtonWidget.builder(Text.translatable("boring_default_game_rules.game_rules.edit_default_game_rules"), button -> {
				EditGameRulesScreenMixin.this.client.setScreen(new EditDefaultGameRulesScreen(new GameRules(), gameRulesWrapper -> {
					EditGameRulesScreenMixin.this.client.setScreen(EditGameRulesScreenMixin.this);
					gameRulesWrapper.ifPresent(gameRules -> ModConfigManager.updateConfig(gameRules));
					EditGameRulesScreenMixin.this.gameRules = new GameRules();
					EditGameRulesScreenMixin.this.clearChildren();
					EditGameRulesScreenMixin.this.init();
				}));
			})
				.position(10, 5)
				.size(150, 20)
				.build();
			this.widgets.add(this.editButton);
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
			this.editButton.setX(x + 33);
			this.editButton.setY(y);
			this.editButton.render(matrices, mouseX, mouseY, tickDelta);
		}
	}
}

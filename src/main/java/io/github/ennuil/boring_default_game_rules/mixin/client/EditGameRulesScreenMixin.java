package io.github.ennuil.boring_default_game_rules.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.ennuil.boring_default_game_rules.config.ModConfigManager;
import io.github.ennuil.boring_default_game_rules.screen.EditDefaultGameRulesScreen;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.EditGameRulesScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(EditGameRulesScreen.class)
public abstract class EditGameRulesScreenMixin extends Screen {
	private EditGameRulesScreenMixin(Text text) {
		super(text);
	}

	@Shadow
	public EditGameRulesScreen.GameRuleElementListWidget rulesList;

	@Shadow
	@Final
	private Consumer<Optional<GameRules>> ruleSaver;

	@Inject(method = "init()V", at = @At("TAIL"))
	private void addEditDefaultsButton(CallbackInfo ci) {
		// Don't let the button appear on screens that extends this screen
		if (((EditGameRulesScreen) (Object) this).getClass() == EditGameRulesScreen.class) {
			this.rulesList.children().add(new EditDefaultsButtonWidget());
		}
	}

	@ModifyExpressionValue(method = "init()V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/world/EditGameRulesScreen;TITLE:Lnet/minecraft/text/Text;"))
	private Text modifyTitle(Text original) {
		return this.title;
	}

	@SuppressWarnings("all")
	@Unique
	public class EditDefaultsButtonWidget extends EditGameRulesScreen.AbstractEntry {
		private final ButtonWidget editButton;
		private final List<ClickableWidget> widgets = new ArrayList<>();

		public EditDefaultsButtonWidget() {
			super(List.of(
				Text.translatable("boring_default_game_rules.game_rules.edit_default_game_rules.tooltip.1").asOrderedText(),
				Text.translatable("boring_default_game_rules.game_rules.edit_default_game_rules.tooltip.2").asOrderedText()
			));
			this.editButton = ButtonWidget.builder(Text.translatable("boring_default_game_rules.game_rules.edit_default_game_rules"), button -> {
				EditGameRulesScreenMixin.this.client.setScreen(new EditDefaultGameRulesScreen(new GameRules(), gameRulesWrapper -> {
					gameRulesWrapper.ifPresentOrElse(gameRules -> {
						ModConfigManager.updateConfig(gameRules);
						EditGameRulesScreenMixin.this.ruleSaver.accept(Optional.of(gameRules));
						EditGameRulesScreenMixin.this.client.setScreen(new EditGameRulesScreen(gameRules, EditGameRulesScreenMixin.this.ruleSaver));
					}, () -> EditGameRulesScreenMixin.this.client.setScreen(EditGameRulesScreenMixin.this));
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
		public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			this.editButton.setX(x + 33);
			this.editButton.setY(y);
			this.editButton.render(graphics, mouseX, mouseY, tickDelta);
		}
	}
}

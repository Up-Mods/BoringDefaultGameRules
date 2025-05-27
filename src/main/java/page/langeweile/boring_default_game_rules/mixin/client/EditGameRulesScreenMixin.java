package page.langeweile.boring_default_game_rules.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import page.langeweile.boring_default_game_rules.config.ModConfigManager;
import page.langeweile.boring_default_game_rules.screen.EditDefaultGameRulesScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(EditGameRulesScreen.class)
public abstract class EditGameRulesScreenMixin extends Screen {
	private EditGameRulesScreenMixin(Component component) {
		super(component);
	}

	@Shadow
	public EditGameRulesScreen.RuleList ruleList;

	@Shadow
	@Final
	private Consumer<Optional<GameRules>> exitCallback;

	@Inject(method = "init()V", at = @At("TAIL"))
	private void addEditDefaultsButton(CallbackInfo ci) {
		// Don't let the button appear on screens that extends this screen
		if (((EditGameRulesScreen) (Object) this).getClass() == EditGameRulesScreen.class) {
			this.ruleList.children().add(new EditDefaultsButtonWidget());
		}
	}

	@ModifyExpressionValue(method = "init()V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/worldselection/EditGameRulesScreen;TITLE:Lnet/minecraft/network/chat/Component;"))
	private Component modifyTitle(Component original) {
		return this.title;
	}

	@SuppressWarnings("all")
	@Unique
	public class EditDefaultsButtonWidget extends EditGameRulesScreen.RuleEntry {
		private final Button editButton;
		private final List<AbstractButton> widgets = new ArrayList<>();

		public EditDefaultsButtonWidget() {
			super(List.of(
				Component.translatable("boring_default_game_rules.game_rules.edit_default_game_rules.tooltip.1").getVisualOrderText(),
				Component.translatable("boring_default_game_rules.game_rules.edit_default_game_rules.tooltip.2").getVisualOrderText()
			));
			this.editButton = Button.builder(Component.translatable("boring_default_game_rules.game_rules.edit_default_game_rules"), button -> {
				EditGameRulesScreenMixin.this.minecraft.setScreen(new EditDefaultGameRulesScreen(new GameRules(FeatureFlags.REGISTRY.allFlags()), gameRulesWrapper -> {
					gameRulesWrapper.ifPresentOrElse(gameRules -> {
						ModConfigManager.updateConfig(gameRules);
						EditGameRulesScreenMixin.this.exitCallback.accept(Optional.of(gameRules));
						EditGameRulesScreenMixin.this.minecraft.setScreen(new EditGameRulesScreen(gameRules, EditGameRulesScreenMixin.this.exitCallback));
					}, () -> EditGameRulesScreenMixin.this.minecraft.setScreen(EditGameRulesScreenMixin.this));
				}));
			})
				.pos(10, 5)
				.size(150, 20)
				.build();
			this.widgets.add(this.editButton);
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
			this.editButton.setX(x + 33);
			this.editButton.setY(y);
			this.editButton.render(graphics, mouseX, mouseY, tickDelta);
		}
	}
}

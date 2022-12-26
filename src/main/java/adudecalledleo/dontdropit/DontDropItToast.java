package adudecalledleo.dontdropit;

import java.util.List;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

// copy of SystemToast lmao
public class DontDropItToast implements Toast {
	public enum Type {
		DROP_DELAY_DISABLED_TOGGLED;

		private final long displayDuration;

		Type(long displayDuration) {
			this.displayDuration = displayDuration;
		}

		Type() {
			this(5000L);
		}

		public long getDisplayDuration() {
			return this.displayDuration;
		}
	}

	private final Type type;
	private final int width;

	private Text title;
	private List<OrderedText> lines;
	private long startTime;
	private boolean justUpdated;


	public DontDropItToast(Type type, Text title, @Nullable Text description) {
		this(type, title, getTextAsList(description), Math.max(160,
				30 + Math.max(MinecraftClient.getInstance().textRenderer.getWidth(title),
						description == null ? 0 : MinecraftClient.getInstance().textRenderer.getWidth(description))));
	}

	private DontDropItToast(Type type, Text title, List<OrderedText> lines, int width) {
		this.type = type;
		this.title = title;
		this.lines = lines;
		this.width = width;
	}

	private static ImmutableList<OrderedText> getTextAsList(@Nullable Text text) {
		return text == null ? ImmutableList.of() : ImmutableList.of(text.asOrderedText());
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return 20 + this.lines.size() * 12;
	}

	@Override
	public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
		if (this.justUpdated) {
			this.startTime = startTime;
			this.justUpdated = false;
		}

		RenderSystem.setShaderTexture(0, TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		int width = this.getWidth();
		int height;
		if (width == 160 && this.lines.size() <= 1) {
			manager.drawTexture(matrices, 0, 0, 0, 64, width, this.getHeight());
		} else {
			height = this.getHeight();
			int l = Math.min(4, height - 28);
			this.drawPart(matrices, manager, width, 0, 0, 28);

			for (int m = 28; m < height - l; m += 10) {
				this.drawPart(matrices, manager, width, 16, m, Math.min(16, height - m - l));
			}

			this.drawPart(matrices, manager, width, 32 - l, height - l, l);
		}

		if (this.lines == null) {
			manager.getClient().textRenderer.draw(matrices, this.title, 18.0F, 12.0F, -256);
		} else {
			manager.getClient().textRenderer.draw(matrices, this.title, 18.0F, 7.0F, -256);

			for(height = 0; height < this.lines.size(); ++height) {
				manager.getClient().textRenderer.draw(matrices, this.lines.get(height), 18.0F, (float)(18 + height * 12), 0xFFFFFFFF);
			}
		}

		return startTime - this.startTime < this.type.getDisplayDuration() ? Visibility.SHOW : Visibility.HIDE;
	}

	private void drawPart(MatrixStack matrices, ToastManager manager, int width, int textureV, int y, int height) {
		int i = textureV == 0 ? 20 : 5;
		int j = Math.min(60, width - i);
		manager.drawTexture(matrices, 0, y, 0, 64 + textureV, i, height);

		for(int k = i; k < width - j; k += 64) {
			manager.drawTexture(matrices, k, y, 32, 64 + textureV, Math.min(64, width - k - j), height);
		}

		manager.drawTexture(matrices, width - j, y, 160 - j, 64 + textureV, j, height);
	}

	public void setContent(Text title, @Nullable Text description) {
		this.title = title;
		this.lines = getTextAsList(description);
		this.justUpdated = true;
	}

	public static void add(ToastManager manager, Type type, Text title, @Nullable Text description) {
		manager.add(new DontDropItToast(type, title, description));
	}

	public static void show(ToastManager manager, Type type, Text title, @Nullable Text description) {
		var toast = (DontDropItToast) manager.getToast(DontDropItToast.class, type);
		if (toast == null) {
			add(manager, type, title, description);
		} else {
			toast.setContent(title, description);
		}
	}

	public static void showDropDelayDisabledToggled(ToastManager manager, boolean newValue) {
		show(manager, Type.DROP_DELAY_DISABLED_TOGGLED, Text.translatable("key.dontdropit.toggleDropDelay"),
				newValue
						? Text.translatable("dontdropit.toast.toggleDropDelay.disabled")
						: Text.translatable("dontdropit.toast.toggleDropDelay.enabled"));
	}

	@Override
	public Type getType() {
		return type;
	}
}

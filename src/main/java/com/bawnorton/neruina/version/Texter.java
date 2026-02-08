package com.bawnorton.neruina.version;

import com.bawnorton.neruina.Neruina;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

//? if >=1.21.11 {
import net.minecraft.util.Util;
//?} else {
/*import net.minecraft.Util;
 *///?}

public interface Texter {
	Component LINE_BREAK = literal("\n");
	Component SPACE = literal(" ");
	Component NERUINA_HEADER = withStyle(literal("[Neruina]: "), style -> style.withColor(ChatFormatting.AQUA));

	Map<String, String> FALLBACK_LANG = Util.make(new HashMap<>(), map -> {
		try (InputStream langStream = Texter.class.getClassLoader().getResourceAsStream("assets/neruina/lang/en_us.json")) {
			if (langStream == null) return;

			Language.loadFromJson(langStream, map::put);
		} catch (IOException e) {
			Neruina.LOGGER.error("Failed to load default lang from en_us.json", e);
		}
	});

	static Component literal(String component) {
		return Component.literal(component);
	}

	static Component translatable(String key, Object... args) {
		return Component.translatableWithFallback(key, FALLBACK_LANG.getOrDefault(key, key), args);
	}

	static Component empty() {
		return Component.empty();
	}

	static Component withStyle(Component component, UnaryOperator<Style> style) {
		if (component instanceof MutableComponent mutableComponent) {
			mutableComponent.withStyle(style);
		}
		return component;
	}

	static Component concat(Component... texts) {
		MutableComponent component = Component.empty();
		for (Component t : texts) {
			if (t.getString().isEmpty()) continue;

			component.append(t);
		}
		return component;
	}

	static Component concatDelimited(Component delimiter, Component... texts) {
		MutableComponent component = Component.empty();
		for (int i = 0; i < texts.length; i++) {
			component.append(texts[i]);
			if (texts[i].getString().isEmpty()) {
				List<Component> siblings = component.getSiblings();
				if (!siblings.isEmpty()) {
					//? if >1.20.1 {
					siblings.removeLast();
					//?} else {
					/*siblings.remove(siblings.size() - 1);
					*///?}
				}
				continue;
			}
			if (i != texts.length - 1) {
				component.append(delimiter);
			}
		}
		return component;
	}

	static Component pad(Component component) {
		if (component.getString().isEmpty()) return component;

		MutableComponent padded = Component.empty();
		padded.append(LINE_BREAK);
		padded.append(component);
		padded.append(LINE_BREAK);
		return padded;
	}

	static Component format(Component component) {
		if (component.getString().isEmpty()) return component;

		return concat(NERUINA_HEADER, withStyle(component, style -> style.withColor(ChatFormatting.RED)));
	}

	static ClickEvent clickEvent(ClickEvent.Action action, String value) {
		//? if <=1.21.1 {
		/*return new ClickEvent(action, value);
		 *///?} else {
		return switch (action) {
			case OPEN_URL -> new ClickEvent.OpenUrl(URI.create(value));
			case OPEN_FILE -> new ClickEvent.OpenFile(value);
			case RUN_COMMAND -> new ClickEvent.RunCommand(value);
			case SUGGEST_COMMAND -> new ClickEvent.SuggestCommand(value);
			case COPY_TO_CLIPBOARD -> new ClickEvent.CopyToClipboard(value);
			case CHANGE_PAGE -> new ClickEvent.ChangePage(Integer.parseInt(value));
			default -> null;
		};
		//?}
	}

	//? if <=1.21.1 {
    /*static <T> HoverEvent hoverEvent(HoverEvent.Action<T> action, T value) {
        return new HoverEvent(action, value);
    }
    *///?} else {
	static HoverEvent hoverEvent(HoverEvent.Action action, Object value) {
		return switch (action) {
			case SHOW_TEXT -> new HoverEvent.ShowText((Component) value);
			case SHOW_ITEM -> new HoverEvent.ShowItem((ItemStack) value);
			case SHOW_ENTITY -> new HoverEvent.ShowEntity((HoverEvent.EntityTooltipInfo) value);
		};
	}
	//?}
}

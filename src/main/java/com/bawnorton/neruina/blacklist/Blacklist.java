package com.bawnorton.neruina.blacklist;

import com.bawnorton.neruina.util.ErroredType;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.List;

public record Blacklist(List<String> namespaces, List<ResourceLocation> entities, List<ResourceLocation> blocks,
                        List<ResourceLocation> blockEntities, List<ResourceLocation> items) {
	private static final Gson GSON = new GsonBuilder()
			.setPrettyPrinting()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.registerTypeAdapter(
					ResourceLocation.class, new TypeAdapter<>() {
						@Override
						public void write(JsonWriter out, Object value) throws IOException {
							if (value instanceof ResourceLocation id) {
								out.value(id.toString());
							} else if (value == null) {
								out.nullValue();
							}
						}

						@Override
						public Object read(JsonReader in) throws IOException {
							String value = in.nextString();
							ResourceLocation identifier = ResourceLocation.tryParse(value);
							if (identifier == null) {
								throw new IOException("Invalid ResourceLocation: " + value);
							}
							return identifier;
						}
					}
			)
			.create();

	public static Blacklist fromJson(JsonReader reader) {
		return GSON.fromJson(reader, Blacklist.class);
	}

	public boolean isBlacklisted(ErroredType type, ResourceLocation id) {
		if (id == null) {
			return false;
		}
		if (namespaces != null) {
			for (String namespace : namespaces) {
				if (id.getNamespace().equals(namespace)) {
					return true;
				}
			}
		}
		return switch (type) {
			case ENTITY -> entities != null && entities.contains(id);
			case BLOCK_STATE -> blocks != null && blocks.contains(id);
			case BLOCK_ENTITY -> blockEntities != null && blockEntities.contains(id);
			case ITEM_STACK -> items != null && items.contains(id);
			default -> false;
		};
	}
}

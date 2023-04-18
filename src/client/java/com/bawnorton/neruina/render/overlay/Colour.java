package com.bawnorton.neruina.render.overlay;

public record Colour(int red, int green, int blue) {

    public static Colour fromHex(int hex) {
        return new Colour((hex >> 16) & 0xFF, (hex >> 8) & 0xFF, hex & 0xFF);
    }

    public float getFRed() {
        return this.red() / 255.0F;
    }

    public float getFGreen() {
        return this.green() / 255.0F;
    }

    public float getFBlue() {
        return this.blue() / 255.0F;
    }
}
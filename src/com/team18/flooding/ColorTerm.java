package com.team18.flooding;

public class ColorTerm {

    private static final String ANSI_RESET = "\u001B[0m";

    public static void println(Color color, String text) {
        System.out.println(color.getAnsiCode() + text + ANSI_RESET);
    }

    public enum Color {
        BLACK("\u001B[30m"), RED("\u001B[31m"),
        GREEN("\u001B[32m"), YELLOW("\u001B[33m"),
        BLUE("\u001B[34m"), PURPLE("\u001B[35m"),
        CYAN("\u001B[36m"), WHITE("\u001B[37m");

        private final String ansiCode;

        private Color(String code) {
            this.ansiCode = code;
        }

        public String getAnsiCode() {
            return ansiCode;
        }
    }
}

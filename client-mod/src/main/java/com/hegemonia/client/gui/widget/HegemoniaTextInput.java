package com.hegemonia.client.gui.widget;

import com.hegemonia.client.gui.theme.HegemoniaColors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Custom styled text input field for Hegemonia GUIs
 */
public class HegemoniaTextInput extends AbstractWidget {

    private String text = "";
    private String placeholder = "";
    private int maxLength = 32;
    private int cursorPosition = 0;
    private int selectionStart = 0;
    private int scrollOffset = 0;

    private Consumer<String> onTextChanged;
    private Consumer<String> onEnterPressed;
    private Predicate<String> validator;

    // Animation
    private int cursorBlink = 0;
    private float focusProgress = 0f;

    public HegemoniaTextInput(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public HegemoniaTextInput(int x, int y, int width, int height, String placeholder) {
        super(x, y, width, height);
        this.placeholder = placeholder;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Update animations
        updateAnimations();

        // Background
        context.fill(x, y, x + width, y + height, HegemoniaColors.INPUT_BACKGROUND);

        // Border with focus effect
        int borderColor = focused ?
                HegemoniaColors.lerp(HegemoniaColors.INPUT_BORDER, HegemoniaColors.INPUT_BORDER_FOCUS, focusProgress) :
                HegemoniaColors.INPUT_BORDER;
        drawBorder(context, borderColor);

        // Calculate text area
        int textX = x + 6;
        int textY = y + (height - 8) / 2;
        int textAreaWidth = width - 12;

        // Enable scissor for text clipping
        context.enableScissor(x + 4, y, x + width - 4, y + height);

        // Draw text or placeholder
        if (text.isEmpty() && !focused) {
            context.drawText(textRenderer, placeholder, textX, textY, HegemoniaColors.INPUT_PLACEHOLDER, false);
        } else {
            String displayText = text;
            int displayOffset = textX - scrollOffset;

            // Draw selection highlight
            if (focused && selectionStart != cursorPosition) {
                int selStart = Math.min(selectionStart, cursorPosition);
                int selEnd = Math.max(selectionStart, cursorPosition);
                int selStartX = textX + textRenderer.getWidth(text.substring(0, selStart)) - scrollOffset;
                int selEndX = textX + textRenderer.getWidth(text.substring(0, selEnd)) - scrollOffset;
                context.fill(selStartX, y + 2, selEndX, y + height - 2, HegemoniaColors.withAlpha(HegemoniaColors.ACCENT_GOLD, 100));
            }

            // Draw text
            context.drawText(textRenderer, displayText, displayOffset, textY, HegemoniaColors.INPUT_TEXT, false);

            // Draw cursor
            if (focused && (cursorBlink / 10) % 2 == 0) {
                int cursorX = textX + textRenderer.getWidth(text.substring(0, cursorPosition)) - scrollOffset;
                context.fill(cursorX, y + 3, cursorX + 1, y + height - 3, HegemoniaColors.ACCENT_GOLD);
            }
        }

        context.disableScissor();
    }

    private void drawBorder(DrawContext context, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }

    private void updateAnimations() {
        cursorBlink++;

        float speed = 0.2f;
        if (focused) {
            focusProgress = Math.min(1f, focusProgress + speed);
        } else {
            focusProgress = Math.max(0f, focusProgress - speed);
        }
    }

    private void updateScrollOffset() {
        int cursorX = textRenderer.getWidth(text.substring(0, cursorPosition));
        int textAreaWidth = width - 12;

        if (cursorX - scrollOffset > textAreaWidth) {
            scrollOffset = cursorX - textAreaWidth + 5;
        } else if (cursorX < scrollOffset) {
            scrollOffset = Math.max(0, cursorX - 5);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !enabled) return false;

        if (isMouseOver(mouseX, mouseY)) {
            focused = true;
            cursorBlink = 0;

            // Calculate cursor position from click
            int relativeX = (int) mouseX - x - 6 + scrollOffset;
            cursorPosition = getCharacterIndexFromX(relativeX);
            selectionStart = cursorPosition;

            return true;
        } else {
            focused = false;
        }
        return false;
    }

    private int getCharacterIndexFromX(int x) {
        if (text.isEmpty()) return 0;

        for (int i = 0; i <= text.length(); i++) {
            int charX = textRenderer.getWidth(text.substring(0, i));
            if (charX > x) {
                return Math.max(0, i - 1);
            }
        }
        return text.length();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focused || !enabled) return false;

        boolean ctrl = Screen.hasControlDown();
        boolean shift = Screen.hasShiftDown();

        switch (keyCode) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (!text.isEmpty()) {
                    if (selectionStart != cursorPosition) {
                        deleteSelection();
                    } else if (cursorPosition > 0) {
                        if (ctrl) {
                            // Delete word
                            int wordStart = findWordStart(cursorPosition);
                            text = text.substring(0, wordStart) + text.substring(cursorPosition);
                            cursorPosition = wordStart;
                        } else {
                            text = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition);
                            cursorPosition--;
                        }
                    }
                    selectionStart = cursorPosition;
                    onTextChange();
                }
                return true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (!text.isEmpty()) {
                    if (selectionStart != cursorPosition) {
                        deleteSelection();
                    } else if (cursorPosition < text.length()) {
                        text = text.substring(0, cursorPosition) + text.substring(cursorPosition + 1);
                    }
                    selectionStart = cursorPosition;
                    onTextChange();
                }
                return true;
            }
            case GLFW.GLFW_KEY_LEFT -> {
                if (cursorPosition > 0) {
                    if (ctrl) {
                        cursorPosition = findWordStart(cursorPosition);
                    } else {
                        cursorPosition--;
                    }
                    if (!shift) selectionStart = cursorPosition;
                    updateScrollOffset();
                }
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                if (cursorPosition < text.length()) {
                    if (ctrl) {
                        cursorPosition = findWordEnd(cursorPosition);
                    } else {
                        cursorPosition++;
                    }
                    if (!shift) selectionStart = cursorPosition;
                    updateScrollOffset();
                }
                return true;
            }
            case GLFW.GLFW_KEY_HOME -> {
                cursorPosition = 0;
                if (!shift) selectionStart = cursorPosition;
                updateScrollOffset();
                return true;
            }
            case GLFW.GLFW_KEY_END -> {
                cursorPosition = text.length();
                if (!shift) selectionStart = cursorPosition;
                updateScrollOffset();
                return true;
            }
            case GLFW.GLFW_KEY_ENTER -> {
                if (onEnterPressed != null) {
                    onEnterPressed.accept(text);
                }
                return true;
            }
            case GLFW.GLFW_KEY_A -> {
                if (ctrl) {
                    selectionStart = 0;
                    cursorPosition = text.length();
                    return true;
                }
            }
            case GLFW.GLFW_KEY_C -> {
                if (ctrl && selectionStart != cursorPosition) {
                    String selected = getSelectedText();
                    client.keyboard.setClipboard(selected);
                    return true;
                }
            }
            case GLFW.GLFW_KEY_V -> {
                if (ctrl) {
                    String clipboard = client.keyboard.getClipboard();
                    insertText(clipboard);
                    return true;
                }
            }
            case GLFW.GLFW_KEY_X -> {
                if (ctrl && selectionStart != cursorPosition) {
                    String selected = getSelectedText();
                    client.keyboard.setClipboard(selected);
                    deleteSelection();
                    onTextChange();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!focused || !enabled) return false;

        if (Character.isISOControl(chr)) return false;

        insertText(String.valueOf(chr));
        return true;
    }

    private void insertText(String insert) {
        if (insert == null || insert.isEmpty()) return;

        // Delete selection first if any
        if (selectionStart != cursorPosition) {
            deleteSelection();
        }

        // Filter and validate
        StringBuilder sb = new StringBuilder();
        for (char c : insert.toCharArray()) {
            if (!Character.isISOControl(c)) {
                sb.append(c);
            }
        }
        String filtered = sb.toString();

        // Check length
        int available = maxLength - text.length();
        if (available <= 0) return;

        if (filtered.length() > available) {
            filtered = filtered.substring(0, available);
        }

        // Validate if validator is set
        String newText = text.substring(0, cursorPosition) + filtered + text.substring(cursorPosition);
        if (validator != null && !validator.test(newText)) {
            return;
        }

        text = newText;
        cursorPosition += filtered.length();
        selectionStart = cursorPosition;
        updateScrollOffset();
        onTextChange();
    }

    private void deleteSelection() {
        int start = Math.min(selectionStart, cursorPosition);
        int end = Math.max(selectionStart, cursorPosition);
        text = text.substring(0, start) + text.substring(end);
        cursorPosition = start;
        selectionStart = start;
        updateScrollOffset();
    }

    private String getSelectedText() {
        int start = Math.min(selectionStart, cursorPosition);
        int end = Math.max(selectionStart, cursorPosition);
        return text.substring(start, end);
    }

    private int findWordStart(int pos) {
        if (pos <= 0) return 0;
        pos--;
        while (pos > 0 && Character.isWhitespace(text.charAt(pos))) pos--;
        while (pos > 0 && !Character.isWhitespace(text.charAt(pos - 1))) pos--;
        return pos;
    }

    private int findWordEnd(int pos) {
        if (pos >= text.length()) return text.length();
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) pos++;
        while (pos < text.length() && !Character.isWhitespace(text.charAt(pos))) pos++;
        return pos;
    }

    private void onTextChange() {
        if (onTextChanged != null) {
            onTextChanged.accept(text);
        }
    }

    // ==================== Getters/Setters ====================

    public String getText() {
        return text;
    }

    public HegemoniaTextInput setText(String text) {
        this.text = text != null ? text : "";
        this.cursorPosition = this.text.length();
        this.selectionStart = cursorPosition;
        updateScrollOffset();
        return this;
    }

    public HegemoniaTextInput setPlaceholder(String placeholder) {
        this.placeholder = placeholder != null ? placeholder : "";
        return this;
    }

    public HegemoniaTextInput setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    public HegemoniaTextInput setOnTextChanged(Consumer<String> onTextChanged) {
        this.onTextChanged = onTextChanged;
        return this;
    }

    public HegemoniaTextInput setOnEnterPressed(Consumer<String> onEnterPressed) {
        this.onEnterPressed = onEnterPressed;
        return this;
    }

    public HegemoniaTextInput setValidator(Predicate<String> validator) {
        this.validator = validator;
        return this;
    }

    /**
     * Set to only accept numeric input (digits and decimal point)
     */
    public HegemoniaTextInput setNumericOnly(boolean numericOnly) {
        if (numericOnly) {
            this.validator = input -> {
                if (input.isEmpty()) return true;
                try {
                    Double.parseDouble(input.replace(",", "."));
                    return true;
                } catch (NumberFormatException e) {
                    // Allow partial input like "." or "-"
                    return input.matches("^-?[0-9]*\\.?[0-9]*$");
                }
            };
        } else {
            this.validator = null;
        }
        return this;
    }
}
